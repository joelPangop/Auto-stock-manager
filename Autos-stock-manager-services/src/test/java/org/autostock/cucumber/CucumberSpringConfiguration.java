package org.autostock.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Point d'entrée obligatoire pour que cucumber-spring sache comment démarrer
 * le contexte Spring Boot (glue partagée par tous les fichiers de steps).
 *
 * Le contexte complet de l'application est chargé. On utilise
 * WebEnvironment.MOCK plutôt que NONE : les scénarios appellent les services
 * directement (pas la couche HTTP — cf. décision "tests de service"), mais
 * MOCK reste nécessaire pour que Spring Boot considère le contexte comme une
 * application servlet. Sans ça, SpringBootWebSecurityConfiguration ne se
 * déclenche pas et SecurityConfig.authenticationManager() ne trouve pas de
 * bean AuthenticationConfiguration (échec de démarrage du contexte). MOCK
 * n'ouvre aucun port réseau, donc l'impact sur la vitesse des tests est nul.
 *
 * La base H2 (src/test/resources/application.properties) est créée et
 * détruite pour chaque exécution de la JVM de test.
 *
 * @Transactional ici fait en sorte que chaque scénario Cucumber s'exécute dans
 * sa propre transaction, automatiquement annulée (rollback) à la fin du
 * scénario — comme le ferait @Transactional sur une méthode de test JUnit.
 * Cela garantit l'isolation des scénarios entre eux sans avoir à nettoyer les
 * repositories manuellement.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
public class CucumberSpringConfiguration {
}
