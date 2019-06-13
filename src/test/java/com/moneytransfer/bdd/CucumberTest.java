package com.moneytransfer.bdd;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

/**
 * This is the Test to run, to check BDD tests.
 * @author Prashant
 *
 */
@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources")
public class CucumberTest {
	
}