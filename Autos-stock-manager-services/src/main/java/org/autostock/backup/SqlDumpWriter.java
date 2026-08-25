package org.autostock.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Genere un dump SQL complet a partir de la connexion JDBC de l'application.
 *
 * <p>Volontairement ecrit en Java plutot qu'en appelant {@code mysqldump} : le
 * conteneur applicatif ne contient pas le client MySQL, et l'y ajouter
 * imposerait de modifier le Dockerfile, de reconstruire l'image et de
 * transmettre le mot de passe root au backend. Ici, on reutilise la
 * {@link DataSource} deja configuree — aucun secret supplementaire, aucun
 * binaire supplementaire.
 *
 * <p>Le dump produit est rejouable tel quel par {@code mysql < dump.sql} :
 * structure des tables, donnees, vues et declencheurs, dans un ordre qui
 * neutralise les contraintes de cle etrangere pendant le chargement.
 */
class SqlDumpWriter {

    private static final Logger log = LoggerFactory.getLogger(SqlDumpWriter.class);

    /** Taille approximative au-dela de laquelle on ferme l'INSERT en cours. */
    private static final int MAX_STATEMENT_BYTES = 512 * 1024;

    private final DataSource dataSource;

    SqlDumpWriter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Ecrit le dump complet dans le flux fourni.
     *
     * @return le nom du schema exporte
     */
    String write(Writer out) throws SQLException, IOException {
        try (Connection conn = dataSource.getConnection()) {
            String schema = conn.getCatalog();
            DatabaseMetaData md = conn.getMetaData();

            writeHeader(out, schema, md);

            List<String> tables = new ArrayList<>();
            List<String> views = new ArrayList<>();
            listTables(conn, tables, views);

            for (String table : tables) {
                writeTableStructure(conn, out, table);
                writeTableData(conn, out, table);
            }
            for (String view : views) {
                writeViewStructure(conn, out, view);
            }
            writeTriggers(conn, out, tables);

            writeFooter(out);
            out.flush();

            log.info("Dump SQL généré : schéma={} tables={} vues={}", schema, tables.size(), views.size());
            return schema;
        }
    }

    // -----------------------------------------------------------------------
    // En-tete / pied
    // -----------------------------------------------------------------------

    private void writeHeader(Writer out, String schema, DatabaseMetaData md)
            throws SQLException, IOException {
        out.write("-- Auto-stock-manager — export SQL\n");
        out.write("-- Schéma  : " + schema + "\n");
        out.write("-- Serveur : " + md.getDatabaseProductName() + " " + md.getDatabaseProductVersion() + "\n");
        out.write("--\n\n");
        out.write("/*!40101 SET NAMES utf8mb4 */;\n");
        out.write("SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;\n");
        out.write("SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;\n");
        out.write("SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';\n");
        out.write("SET @OLD_TIME_ZONE=@@TIME_ZONE, TIME_ZONE='+00:00';\n\n");
    }

    private void writeFooter(Writer out) throws IOException {
        out.write("\nSET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;\n");
        out.write("SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;\n");
        out.write("SET SQL_MODE=@OLD_SQL_MODE;\n");
        out.write("SET TIME_ZONE=@OLD_TIME_ZONE;\n\n");
        // Marqueur de fin : une restauration doit refuser un dump qui ne le porte pas.
        out.write("-- Dump completed\n");
    }

    // -----------------------------------------------------------------------
    // Inventaire
    // -----------------------------------------------------------------------

    private void listTables(Connection conn, List<String> tables, List<String> views)
            throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SHOW FULL TABLES")) {
            while (rs.next()) {
                String name = rs.getString(1);
                String type = rs.getString(2);
                if ("VIEW".equalsIgnoreCase(type)) {
                    views.add(name);
                } else {
                    tables.add(name);
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Structure
    // -----------------------------------------------------------------------

    private void writeTableStructure(Connection conn, Writer out, String table)
            throws SQLException, IOException {
        out.write("\n--\n-- Structure de la table " + table + "\n--\n\n");
        out.write("DROP TABLE IF EXISTS " + ident(table) + ";\n");
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SHOW CREATE TABLE " + ident(table))) {
            if (rs.next()) {
                out.write(rs.getString(2) + ";\n");
            }
        }
    }

    private void writeViewStructure(Connection conn, Writer out, String view)
            throws SQLException, IOException {
        out.write("\n--\n-- Vue " + view + "\n--\n\n");
        out.write("DROP VIEW IF EXISTS " + ident(view) + ";\n");
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SHOW CREATE VIEW " + ident(view))) {
            if (rs.next()) {
                out.write(rs.getString(2) + ";\n");
            }
        } catch (SQLException e) {
            // Une vue dont la definition est illisible (privileges du DEFINER)
            // ne doit pas faire echouer tout l'export.
            log.warn("Vue ignorée : {} ({})", view, e.getMessage());
            out.write("-- vue non exportable : " + e.getMessage() + "\n");
        }
    }

    private void writeTriggers(Connection conn, Writer out, List<String> tables)
            throws SQLException, IOException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SHOW TRIGGERS")) {
            boolean first = true;
            while (rs.next()) {
                if (first) {
                    out.write("\n--\n-- Déclencheurs\n--\n\n");
                    first = false;
                }
                String name = rs.getString("Trigger");
                out.write("DROP TRIGGER IF EXISTS " + ident(name) + ";\n");
                out.write("DELIMITER ;;\n");
                try (Statement st2 = conn.createStatement();
                     ResultSet rs2 = st2.executeQuery("SHOW CREATE TRIGGER " + ident(name))) {
                    if (rs2.next()) {
                        out.write(rs2.getString("SQL Original Statement") + ";;\n");
                    }
                }
                out.write("DELIMITER ;\n");
            }
        } catch (SQLException e) {
            log.warn("Déclencheurs non exportés : {}", e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Donnees
    // -----------------------------------------------------------------------

    private void writeTableData(Connection conn, Writer out, String table)
            throws SQLException, IOException {

        out.write("\n--\n-- Données de la table " + table + "\n--\n\n");
        out.write("LOCK TABLES " + ident(table) + " WRITE;\n");

        // Streaming ligne a ligne : une grosse table ne doit jamais etre
        // materialisee en memoire dans le conteneur.
        try (Statement st = conn.createStatement(
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            st.setFetchSize(Integer.MIN_VALUE);

            try (ResultSet rs = st.executeQuery("SELECT * FROM " + ident(table))) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();

                StringBuilder sb = new StringBuilder();
                long rows = 0;

                while (rs.next()) {
                    if (sb.length() == 0) {
                        sb.append("INSERT INTO ").append(ident(table)).append(" VALUES ");
                    } else {
                        sb.append(",");
                    }

                    sb.append("(");
                    for (int i = 1; i <= cols; i++) {
                        if (i > 1) {
                            sb.append(",");
                        }
                        sb.append(render(rs, i, meta.getColumnType(i)));
                    }
                    sb.append(")");
                    rows++;

                    if (sb.length() >= MAX_STATEMENT_BYTES) {
                        sb.append(";\n");
                        out.write(sb.toString());
                        sb.setLength(0);
                    }
                }

                if (sb.length() > 0) {
                    sb.append(";\n");
                    out.write(sb.toString());
                }
                if (rows == 0) {
                    out.write("-- table vide\n");
                }
            }
        }

        out.write("UNLOCK TABLES;\n");
    }

    // -----------------------------------------------------------------------
    // Rendu des valeurs
    // -----------------------------------------------------------------------

    private String render(ResultSet rs, int index, int sqlType) throws SQLException {
        switch (sqlType) {
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB: {
                byte[] bytes = rs.getBytes(index);
                if (rs.wasNull()) {
                    return "NULL";
                }
                if (bytes.length == 0) {
                    return "''";
                }
                // Notation hexadecimale : seule forme sure pour du binaire,
                // l'echappement textuel corromprait les octets non imprimables.
                StringBuilder hex = new StringBuilder(2 + bytes.length * 2);
                hex.append("0x");
                for (byte b : bytes) {
                    hex.append(String.format("%02X", b));
                }
                return hex.toString();
            }

            case Types.BIT:
            case Types.BOOLEAN: {
                boolean v = rs.getBoolean(index);
                return rs.wasNull() ? "NULL" : (v ? "1" : "0");
            }

            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.REAL:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL: {
                String v = rs.getString(index);
                return (v == null || rs.wasNull()) ? "NULL" : v;
            }

            default: {
                String v = rs.getString(index);
                return (v == null || rs.wasNull()) ? "NULL" : quote(v);
            }
        }
    }

    /** Echappe une valeur textuelle selon les regles de MySQL. */
    private static String quote(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
        sb.append('\'');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\0':   sb.append("\\0");   break;
                case '\n':   sb.append("\\n");   break;
                case '\r':   sb.append("\\r");   break;
                case '\\':   sb.append("\\\\");  break;
                case '\'':   sb.append("\\'");   break;
                case '"':    sb.append("\\\"");  break;
                case '\032': sb.append("\\Z");   break;
                default:     sb.append(c);
            }
        }
        sb.append('\'');
        return sb.toString();
    }

    /** Protege un identifiant par des accents graves. */
    private static String ident(String name) {
        return "`" + name.replace("`", "``") + "`";
    }
}
