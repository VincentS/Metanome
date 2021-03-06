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

package de.metanome.algorithm_integration.input;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;

/**
 * Generates new copies of a {@link RelationalInput}.
 *
 * @author Jakob Zwiener
 */
public interface RelationalInputGenerator {

  /**
   * Generates a new copy of the relational input that can be iterated from the beginning.
   *
   * @return new copy of the relational input
   * @throws InputGenerationException if no new copy could be created
   * @throws AlgorithmConfigurationException if the configuration is not correct
   */
  public RelationalInput generateNewCopy() throws InputGenerationException, AlgorithmConfigurationException;
}
