package de.uni_potsdam.hpi.metanome.frontend.server;

import java.io.FileNotFoundException;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationValue;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationValueBoolean;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationValueRelationalInputGenerator;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationValueString;
import de.uni_potsdam.hpi.metanome.frontend.client.parameter.InputParameterBoolean;
import de.uni_potsdam.hpi.metanome.frontend.client.parameter.InputParameterCsvFile;
import de.uni_potsdam.hpi.metanome.frontend.client.parameter.InputParameterString;

public class ExecutionServiceTest extends TestCase {
	
	ExecutionServiceImpl executionService = new ExecutionServiceImpl();
	InputParameterString stringParam = new InputParameterString("test");
	InputParameterBoolean boolParam = new InputParameterBoolean("boolean");
	InputParameterCsvFile csvParam = new InputParameterCsvFile("inputFile");
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	/**
	 * TODO docs
	 * 
	 * @throws FileNotFoundException
	 * @throws AlgorithmConfigurationException 
	 */
	@Test
	public void testConvertToInputParameter() throws AlgorithmConfigurationException {
		//Setup
		csvParam.setFileNameValue(Thread.currentThread().getContextClassLoader().getResource("inputData").getPath() + "/inputA.csv");
		
		//Execute
		ConfigurationValue confString = executionService.convertToConfigurationValue(stringParam);
		ConfigurationValue confBool = executionService.convertToConfigurationValue(boolParam);
		ConfigurationValue confCsv = executionService.convertToConfigurationValue(csvParam);
		
		//Check
		assertTrue(confString instanceof ConfigurationValueString);
		assertTrue(confBool instanceof ConfigurationValueBoolean);
		assertTrue(confCsv instanceof ConfigurationValueRelationalInputGenerator);
	}	

	/**
	 * TODO docs
	 */
	@Test
	public void testBuildCsvFileGenerator() {		
		//Setup
		csvParam.setFileNameValue("some/file/path");
		
		//Execute
		try {
			executionService.buildCsvFileGenerator(csvParam);
			fail("Expected exception was not thrown.");
		} catch (AlgorithmConfigurationException e) {
			//Test succesful.
		}

	}
}