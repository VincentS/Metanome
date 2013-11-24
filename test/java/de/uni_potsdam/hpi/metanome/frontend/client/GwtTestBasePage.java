package de.uni_potsdam.hpi.metanome.frontend.client;

import org.junit.Test;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.TabLayoutPanel;

/**
 * Tests related to the overall page.
 */
public class GwtTestBasePage extends GWTTestCase{
	
	/**
	 * Test BasePage constructor. 
	 */
	@Test
	public void testNewBasePage(){
		//Execute
		BasePage page = new BasePage();
		
		//Check
		// -- Algorithm page
		assertTrue(page.getWidget(0) instanceof TabLayoutPanel);
		// -- Results page
		assertTrue(page.getWidget(1) instanceof TabLayoutPanel);
	}

	@Override
	public String getModuleName() {
		return "de.uni_potsdam.hpi.metanome.frontend.Hello";	
	}

}
