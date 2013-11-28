package de.uni_potsdam.hpi.metanome.frontend.client.widgets;

import com.google.gwt.user.client.ui.IntegerBox;

import de.uni_potsdam.hpi.metanome.frontend.client.parameter.InputParameter;
import de.uni_potsdam.hpi.metanome.frontend.client.parameter.InputParameterInteger;

public class InputParameterIntegerWidget extends IntegerBox implements InputParameterWidget {
	
	InputParameterInteger inputParameter;

	public InputParameterIntegerWidget(
			InputParameterInteger inputParameter) {
		super();
		this.inputParameter = inputParameter;
	}

	@Override
	public InputParameter getInputParameter() {
		if (this.getValue() != null)
			this.inputParameter.setValue(this.getValue());
		else
			this.inputParameter.setValue(0);
		
		return inputParameter;
	}

}