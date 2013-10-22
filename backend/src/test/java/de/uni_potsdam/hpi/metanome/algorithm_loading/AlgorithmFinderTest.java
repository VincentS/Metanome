
package de.uni_potsdam.hpi.metanome.algorithm_loading;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_potsdam.hpi.metanome.algorithm_integration.UniqueColumnCombinationsAlgorithm;

/**
 *
 */
public class AlgorithmFinderTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		
	}

	/**
	 * A valid algorithm jar should be loadable and of correct class.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getAlgorithmType() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException, InvocationTargetException, NoSuchMethodException {
		// Setup
		String jarFilePath = ClassLoader.getSystemResource("algorithms/example_ucc_algorithm-0.0.1-SNAPSHOT.jar").getFile();
		File file = new File(URLDecoder.decode(jarFilePath, "utf-8"));
		
		// Execute functionality
		List<Class<?>> algorithmInterfaces = new AlgorithmFinder().getAlgorithmInterfaces(file);
		
		// Check result
		assertNotNull(algorithmInterfaces);
		assertNotEquals(0, algorithmInterfaces.size());
		assertTrue(algorithmInterfaces.contains(UniqueColumnCombinationsAlgorithm.class));	
	}
	
	@Test
	public void retrieveAllJarFiles() throws IOException, ClassNotFoundException {
		// Setup
		AlgorithmFinder algoFinder = new AlgorithmFinder();
		
		//Execute
		String[] algos = algoFinder.getAvailableAlgorithms(null);
		
		//Check
		assertTrue(algos.length > 0);
	}
	
	@Test
	public void retrieveUniqueColumnCombinationJarFiles() throws IOException, ClassNotFoundException {
		// Setup
		AlgorithmFinder algoFinder = new AlgorithmFinder();
		
		//Execute
		String[] algos = algoFinder.getAvailableAlgorithms(UniqueColumnCombinationsAlgorithm.class);
		
		//Check
		assertTrue(algos.length > 0);
		//TODO make sure no wrong algorithms are returned
	}
}