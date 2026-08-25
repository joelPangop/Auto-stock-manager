package org.autostock.cucumber;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Point d'entrée JUnit 5 (Suite) qui exécute tous les fichiers .feature situés
 * dans src/test/resources/features via le moteur JUnit Platform de Cucumber.
 *
 * Lancement :
 *   mvn test                              -> exécute cette suite + les tests unitaires
 *   mvn test -Dtest=RunCucumberTest        -> uniquement les scénarios Cucumber
 *   mvn test -Dcucumber.filter.tags="@vente"  -> uniquement les scénarios tagués @vente
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "org.autostock.cucumber")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, summary, html:target/cucumber-report/report.html")
public class RunCucumberTest {
}
