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

package de.metanome.backend.algorithm_execution;

import de.metanome.algorithm_integration.Algorithm;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_execution.FileGenerator;
import de.metanome.algorithm_integration.algorithm_types.BasicStatisticsAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.ConditionalUniqueColumnCombinationAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.FunctionalDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.InclusionDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.OrderDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.TempFileAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.UniqueColumnCombinationsAlgorithm;
import de.metanome.backend.algorithm_loading.AlgorithmAnalyzer;
import de.metanome.algorithm_integration.configuration.ConfigurationValue;
import de.metanome.backend.resources.ExecutionResource;
import de.metanome.backend.result_receiver.CloseableOmniscientResultReceiver;
import de.metanome.backend.results_db.AlgorithmType;
import de.metanome.backend.results_db.EntityStorageException;
import de.metanome.backend.results_db.Execution;
import de.metanome.backend.results_db.ExecutionSetting;
import de.metanome.backend.results_db.Input;
import de.metanome.backend.results_db.Result;
import de.metanome.backend.results_db.ResultType;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Executes given algorithms.
 */
public class AlgorithmExecutor implements Closeable {

  protected CloseableOmniscientResultReceiver resultReceiver;
  protected FileGenerator fileGenerator;
  protected String resultPathPrefix;

  /**
   * Constructs a new executor with new result receivers and generators.
   *
   * @param resultReceiver receives all of the algorithms results
   * @param fileGenerator  generates temp files
   */
  public AlgorithmExecutor(CloseableOmniscientResultReceiver resultReceiver,
                           FileGenerator fileGenerator) {
    this.resultReceiver = resultReceiver;
    this.fileGenerator = fileGenerator;
  }

  /**
   * Executes an algorithm. The algorithm is loaded from the jar, configured and all receivers and
   * generators are set before execution. The execution containing the elapsed time while executing
   * the algorithm in nano seconds is returned.
   *
   * @param storedAlgorithm     the algorithm
   * @param parameters          parameters for algorithm execution
   * @param inputs              inputs for algorithm execution
   * @param executionIdentifier identifier for execution
   * @param executionSetting    setting for the execution
   * @return the execution
   */

  public Execution executeAlgorithm(
      de.metanome.backend.results_db.Algorithm storedAlgorithm,
      List<ConfigurationValue> parameters,
      List<Input> inputs,
      String executionIdentifier,
      ExecutionSetting executionSetting)
      throws IllegalArgumentException, SecurityException, IOException, ClassNotFoundException,
             InstantiationException, IllegalAccessException, InvocationTargetException,
             NoSuchMethodException, AlgorithmExecutionException, EntityStorageException {

    AlgorithmAnalyzer analyzer = new AlgorithmAnalyzer(storedAlgorithm.getFileName());
    Algorithm algorithm = analyzer.getAlgorithm();

    Set<Result> results = new HashSet<>();

    for (ConfigurationValue configValue : parameters) {
      configValue.triggerSetValue(algorithm, analyzer.getInterfaces());
    }

    if (analyzer.hasType(AlgorithmType.FD)) {
      FunctionalDependencyAlgorithm fdAlgorithm = (FunctionalDependencyAlgorithm) algorithm;
      fdAlgorithm.setResultReceiver(resultReceiver);

      results.add(new Result(resultPathPrefix, ResultType.FD));
    }

    if (analyzer.hasType(AlgorithmType.IND)) {
      InclusionDependencyAlgorithm indAlgorithm = (InclusionDependencyAlgorithm) algorithm;
      indAlgorithm.setResultReceiver(resultReceiver);

      results.add(new Result(resultPathPrefix, ResultType.IND));
    }

    if (analyzer.hasType(AlgorithmType.UCC)) {
      UniqueColumnCombinationsAlgorithm
          uccAlgorithm =
          (UniqueColumnCombinationsAlgorithm) algorithm;
      uccAlgorithm.setResultReceiver(resultReceiver);

      results.add(new Result(resultPathPrefix, ResultType.UCC));
    }

    if (analyzer.hasType(AlgorithmType.CUCC)) {
      ConditionalUniqueColumnCombinationAlgorithm
          cuccAlgorithm =
          (ConditionalUniqueColumnCombinationAlgorithm) algorithm;
      cuccAlgorithm.setResultReceiver(resultReceiver);

      results.add(new Result(resultPathPrefix, ResultType.CUCC));
    }

    if (analyzer.hasType(AlgorithmType.OD)) {
      OrderDependencyAlgorithm odAlgorithm = (OrderDependencyAlgorithm) algorithm;
      odAlgorithm.setResultReceiver(resultReceiver);

      results.add(new Result(resultPathPrefix, ResultType.OD));
    }

    if (analyzer.hasType(AlgorithmType.BASIC_STAT)) {
      BasicStatisticsAlgorithm basicStatAlgorithm = (BasicStatisticsAlgorithm) algorithm;
      basicStatAlgorithm.setResultReceiver(resultReceiver);

      results.add(new Result(resultPathPrefix, ResultType.STAT));
    }

    if (analyzer.hasType(AlgorithmType.TEMP_FILE)) {
      TempFileAlgorithm tempFileAlgorithm = (TempFileAlgorithm) algorithm;
      tempFileAlgorithm.setTempFileGenerator(fileGenerator);
    }

    long beforeWallClockTime = new Date().getTime(); // milliseconds
    long before = System.nanoTime(); // nanoseconds
    algorithm.execute();
    long after = System.nanoTime(); // nanoseconds
    long executionTimeInNanos = after - before;
    long executionTimeInMs = executionTimeInNanos / 1000000; // milliseconds

    Execution execution = new Execution(storedAlgorithm, beforeWallClockTime)
        .setEnd(beforeWallClockTime + executionTimeInMs)
        .setInputs(inputs)
        .setIdentifier(executionIdentifier)
        .setResults(results)
        .setCountResult(executionSetting.getCountResults());

    for (Result result : results) {
      result.setExecution(execution);
    }

    // Set the settings to the execution and store it
    execution.setExecutionSetting(executionSetting);
    ExecutionResource executionResource = new ExecutionResource();
    executionResource.store(execution);

    return execution;
  }

  public void setResultPathPrefix(String prefix) {
    this.resultPathPrefix = prefix;
  }

  @Override
  public void close() throws IOException {
    resultReceiver.close();
  }

}