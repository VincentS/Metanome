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

import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationSettingDataSource;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationSettingSQLIterator;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationSpecification;
import de.uni_potsdam.hpi.metanome.algorithm_integration.configuration.ConfigurationSpecificationSQLIterator;

import java.util.List;

public class InputParameterSQLIteratorWidget extends InputParameterDataSourceWidget {

    /**
     * Corresponding inputParameter, where the value is going to be written
     */
    private ConfigurationSpecificationSQLIterator specification;
    private List<SQLIteratorInput> inputWidgets;

    public InputParameterSQLIteratorWidget(
            ConfigurationSpecificationSQLIterator config) {
        super(config);
    }

    @Override
    protected void addInputField(boolean optional) {
        SQLIteratorInput widget = new SQLIteratorInput(optional);
        this.inputWidgets.add(widget);
        this.add(widget);
    }

    @Override
    public ConfigurationSpecification getUpdatedSpecification() {
        // Build an array with the actual number of set values.
        ConfigurationSettingSQLIterator[] values = new ConfigurationSettingSQLIterator[inputWidgets.size()];

        for (int i = 0; i < inputWidgets.size(); i++) {
            values[i] = inputWidgets.get(i).getValue();
        }

        specification.setValues(values);

        return this.specification;
    }

    @Override
    public void setDataSource(ConfigurationSettingDataSource dataSource) {
        if (dataSource instanceof ConfigurationSettingSQLIterator)
            this.inputWidgets.get(0).setValues((ConfigurationSettingSQLIterator) dataSource);
        else
            ; //TODO throw some exception
    }

    @Override
    public boolean accepts(ConfigurationSettingDataSource setting) {
        return setting instanceof ConfigurationSettingSQLIterator;
    }

    @Override
    public List<SQLIteratorInput> getInputWidgets() {
        return this.inputWidgets;
    }

    @Override
    public void setInputWidgets(List<? extends InputField> inputWidgetsList) {
        this.inputWidgets = (List<SQLIteratorInput>) inputWidgetsList;
    }

    @Override
    public ConfigurationSpecification getSpecification() {
        return this.specification;
    }

    @Override
    public void setSpecification(ConfigurationSpecification config) {
        this.specification = (ConfigurationSpecificationSQLIterator) config;
    }


}
