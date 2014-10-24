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

import de.metanome.algorithm_integration.AlgorithmExecutionException;

public class InputIterationException extends AlgorithmExecutionException {

  private static final long serialVersionUID = -7979656967183896126L;

  public InputIterationException() {
    super();
  }

  public InputIterationException(String message) {
    super(message);
  }

  public InputIterationException(String message, Throwable cause) {
    super(message, cause);
  }

}