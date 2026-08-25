package org.autostock.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

public class Hooks {

    @Autowired
    private TestAuthSupport authSupport;

    @Before
    public void avantChaqueScenario() {
        SecurityContextHolder.clearContext();
    }

    @After
    public void apresChaqueScenario() {
        authSupport.deconnecter();
    }
}
