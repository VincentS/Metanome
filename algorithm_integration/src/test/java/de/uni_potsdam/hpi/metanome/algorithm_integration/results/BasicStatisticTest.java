package de.uni_potsdam.hpi.metanome.algorithm_integration.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnCombination;
import de.uni_potsdam.hpi.metanome.algorithm_integration.ColumnIdentifier;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.result_receiver.OmniscientResultReceiver;
import de.uni_potsdam.hpi.metanome.test_helper.GwtSerializationTester;

/**
 * @author Jakob Zwiener
 * 
 * Test for {@link BasicStatistic}
 */
public class BasicStatisticTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link BasicStatistic#sendResultTo(OmniscientResultReceiver)}
	 * 
	 * The {@link BasicStatistic} should be sendable to the {@link OmniscientResultReceiver}.
	 * 
	 * @throws CouldNotReceiveResultException 
	 */
	@Test
	public void testSendResultTo() throws CouldNotReceiveResultException {
		// Setup
		OmniscientResultReceiver resultReceiver = mock(OmniscientResultReceiver.class);
		BasicStatistic statistic = new BasicStatistic("Min", mock(Object.class), mock(ColumnIdentifier.class));
		
		// Execute functionality 
		statistic.sendResultTo(resultReceiver);
		
		// Check result
		verify(resultReceiver).receiveResult(statistic);
	}

	/**
	 * Test method for {@link BasicStatistic#BasicStatistic(String, Object, ColumnIdentifier...)}
	 * 
	 * A {@link BasicStatistic} should store the statistic's name, the value and the associated {@link ColumnCombination}.
	 */
	@Test
	public void testConstructor() {
		// Setup
		// Expected values
		String expectedStatisticName = "Min";
		String expectedStatisticValue = "minValue";
		ColumnIdentifier expectedColumn = new ColumnIdentifier("table42", "column23");
		ColumnCombination expectedColumnCombination = new ColumnCombination(expectedColumn);
		// Execute functionality
		BasicStatistic statistic = new BasicStatistic(expectedStatisticName, expectedStatisticValue, expectedColumn);
		
		// Check result
		assertEquals(expectedStatisticName, statistic.getStatisticName());
		assertEquals(expectedStatisticValue, statistic.getStatisticValue());
		assertEquals(expectedColumnCombination, statistic.getColumnCombination());
	}
	
	/**
	 * Test method for {@link BasicStatistic#toString()}
	 * 
	 * A {@link BasicStatistic} should return a human readable string representation.
	 */
	@Test
	public void testToString() {
		// Setup
		String expectedStatisticName = "Min";
		String expectedStatisticValue = "minValue";
		ColumnIdentifier expectedColumn = new ColumnIdentifier("table42", "column23");
		ColumnCombination expectedColumnCombination = new ColumnCombination(expectedColumn);
		BasicStatistic statistic = new BasicStatistic(expectedStatisticName, expectedStatisticValue, expectedColumn);
		// Expected values
		String expectedStringRepresentation = expectedStatisticName + BasicStatistic.NAME_COLUMN_SEPARATOR + 
				expectedColumnCombination + BasicStatistic.COLUMN_VALUE_SEPARATOR + expectedStatisticValue;
		
		// Execute functionality
		// Check result
		assertEquals(expectedStringRepresentation, statistic.toString());
	}
	
	/**
	 * Test method for {@link BasicStatistic#equals(Object)} and {@link BasicStatistic#hashCode()}
	 * 
	 * TODO docs
	 */
	@Test
	public void testEqualsHashCode() {
		// Setup
		BasicStatistic expectedStatistic = new BasicStatistic(
				"Min",
				"MinValue",
				new ColumnIdentifier("table2", "column47"));
		BasicStatistic expectedEqualStatistic = new BasicStatistic(
				"Min",
				"MinValue",
				new ColumnIdentifier("table2", "column47")); 
		BasicStatistic expectedNotEqualNameStatistic = new BasicStatistic(
				"Max",
				"MinValue",
				new ColumnIdentifier("table2", "column47")); 
		BasicStatistic expectedNotEqualValueStatistic = new BasicStatistic(
				"Min",
				"MaxValue",
				new ColumnIdentifier("table2", "column47"));
		BasicStatistic expectedNotEqualColumnStatistic = new BasicStatistic(
				"Min",
				"MinValue",
				new ColumnIdentifier("table2", "column42"));
		
		// Execute functionality
		// Check result
		assertEquals(expectedStatistic, expectedStatistic);
		assertEquals(expectedStatistic.hashCode(), expectedStatistic.hashCode());
		assertNotSame(expectedStatistic, expectedEqualStatistic);
		assertEquals(expectedStatistic, expectedEqualStatistic);
		assertEquals(expectedStatistic.hashCode(), expectedEqualStatistic.hashCode());
		assertNotEquals(expectedStatistic, expectedNotEqualNameStatistic);
		assertNotEquals(expectedStatistic.hashCode(), expectedNotEqualNameStatistic.hashCode());
		assertNotEquals(expectedStatistic, expectedNotEqualValueStatistic);
		assertNotEquals(expectedStatistic.hashCode(), expectedNotEqualValueStatistic.hashCode());
		assertNotEquals(expectedStatistic, expectedNotEqualColumnStatistic);
		assertNotEquals(expectedStatistic.hashCode(), expectedNotEqualColumnStatistic.hashCode());
	}
	
	/**
	 * Tests that the instances of {@link BasicStatistic} are serializable in GWT.
	 */
	@Test
	public void testGwtSerialization() {
		GwtSerializationTester.checkGwtSerializability(new BasicStatistic("Min", "minValue", mock(ColumnIdentifier.class)));
	}

}