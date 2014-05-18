/*
 * Copyright 2014 by the Metanome project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.uni_potsdam.hpi.metanome.frontend.client.parameter;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;

import de.uni_potsdam.hpi.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationSettingDataSource;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationSpecification;
import de.uni_potsdam.hpi.metanome.frontend.client.TabWrapper;
import de.uni_potsdam.hpi.metanome.frontend.client.helpers.InputValidationException;
import de.uni_potsdam.hpi.metanome.frontend.client.runs.RunConfigurationPage;

public class ParameterTable extends FlexTable {

    private List<InputParameterWidget> childWidgets = new LinkedList<InputParameterWidget>();
    private List<InputParameterDataSourceWidget> dataSourceWidgets = new LinkedList<InputParameterDataSourceWidget>();
    private Button executeButton;
	private TabWrapper errorReceiver;

    /**
     * Creates a ParameterTable for user input for the given parameters.
     * Prompt and type specific input field are created for each parameter,
     * and a button added at the bottom that triggers algorithm execution.
     *
     * @param paramList         the list of parameters asked for by the algorithm.
     * @param primaryDataSource
     * @param errorReceiver 
     */
    public ParameterTable(List<ConfigurationSpecification> paramList, ConfigurationSettingDataSource primaryDataSource, TabWrapper errorReceiver) {
        super();
        this.errorReceiver = errorReceiver;
        
        int i = 0;
        for (ConfigurationSpecification param : paramList) {
            this.setText(i, 0, param.getIdentifier());

            InputParameterWidget currentWidget = WidgetFactory.buildWidget(param);
            this.setWidget(i, 1, currentWidget);
            if (currentWidget.isDataSource()) {
                InputParameterDataSourceWidget dataSourceWidget = (InputParameterDataSourceWidget) currentWidget;
                if (dataSourceWidget.accepts(primaryDataSource))
                    try {
                        dataSourceWidget.setDataSource(primaryDataSource);
                    } catch (AlgorithmConfigurationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                this.dataSourceWidgets.add(dataSourceWidget);
            } else
                this.childWidgets.add(currentWidget);
            i++;
        }

        this.executeButton = new Button("Run");
        this.executeButton.addClickHandler(new ParameterTableSubmitHandler());

        this.setWidget(i, 0, executeButton);
    }

    /**
     * When parameter values are submitted, their values are set and used to call
     * the execution service corresponding to the current tab.
     */
    public void submit() {
		try {
			List<ConfigurationSpecification> parameters = getConfigurationSpecificationsWithValues();
			List<ConfigurationSpecification> dataSources = getConfigurationSpecificationDataSourcesWithValues();
			getAlgorithmTab().callExecutionService(parameters, dataSources);
		} catch (InputValidationException e) {
			this.errorReceiver.addError(e.getMessage());
		}
    }

    /**
     * Iterates over the child widgets that represent data sources and retrieves their user input.
     *
     * @return The list of {@link InputParameterDataSource}s of this ParameterTable with their user-set values.
     * @throws InputValidationException 
     */
    public List<ConfigurationSpecification> getConfigurationSpecificationDataSourcesWithValues() throws InputValidationException {
        LinkedList<ConfigurationSpecification> parameterList = new LinkedList<ConfigurationSpecification>();
        for (InputParameterDataSourceWidget childWidget : this.dataSourceWidgets) {
			parameterList.add(childWidget.getUpdatedSpecification());
        }
        return parameterList;
    }

    /**
     * Iterates over the child widgets and retrieves their user input.
     *
     * @return The list of ConfigurationSpecifications of this ParameterTable with their user-set values.
     * @throws InputValidationException 
     */
    public List<ConfigurationSpecification> getConfigurationSpecificationsWithValues() throws InputValidationException {
        LinkedList<ConfigurationSpecification> parameterList = new LinkedList<ConfigurationSpecification>();
        for (InputParameterWidget childWidget : this.childWidgets) {
        	parameterList.add(childWidget.getUpdatedSpecification());
        }
        return parameterList;
    }


    /**
     * The AlgorithmTabs implement algorithm type specific methods, which can
     * be called via the AlgorithmTab's interface.
     *
     * @return the parent AlgorithmTab
     */
    private RunConfigurationPage getAlgorithmTab() {
        return (RunConfigurationPage) this.getParent();
    }
}