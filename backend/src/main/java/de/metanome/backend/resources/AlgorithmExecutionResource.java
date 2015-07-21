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

package de.metanome.backend.resources;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationSetting;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingDatabaseConnection;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingFileInput;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingTableInput;
import de.metanome.algorithm_integration.configuration.ConfigurationValue;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.input.TableInputGenerator;
import de.metanome.algorithm_integration.results.JsonConverter;
import de.metanome.backend.algorithm_execution.AlgorithmExecution;
import de.metanome.backend.algorithm_execution.ProcessRegistry;
import de.metanome.backend.configuration.DefaultConfigurationFactory;
import de.metanome.backend.helper.FileInputGeneratorMixIn;
import de.metanome.backend.helper.RelationalInputGeneratorMixIn;
import de.metanome.backend.helper.TableInputGeneratorMixIn;
import de.metanome.backend.result_postprocessing.ResultPostProcessor;
import de.metanome.backend.results_db.Algorithm;
import de.metanome.backend.results_db.EntityStorageException;
import de.metanome.backend.results_db.Execution;
import de.metanome.backend.results_db.ExecutionSetting;
import de.metanome.backend.results_db.HibernateUtil;
import de.metanome.backend.results_db.Input;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("algorithm_execution")
public class AlgorithmExecutionResource {

  private static final Class algorithmExecutionClass = AlgorithmExecution.class;

  /**
   * Stops the algorithm with the given identifier.
   *
   * @param executionIdentifier the execution identifier.
   */
  @GET
  @Path("/stop/{identifier}")
  @Produces("application/json")
  public void stopExecution(@PathParam("identifier") String executionIdentifier) {
    Process process = ProcessRegistry.getInstance().get(executionIdentifier);
    ProcessRegistry.getInstance().remove(executionIdentifier);
    process.destroy();
  }

  /**
   * Executes an algorithm.
   *
   * @param params all parameters to execute the algorithm
   * @return the resulting execution
   */
  @POST
  @Consumes("application/json")
  @Produces("application/json")
  public Execution executeAlgorithm(AlgorithmExecutionParams params) {
    String executionIdentifier = params.getExecutionIdentifier();

    // Build the execution setting and store it.
    ExecutionSetting executionSetting = buildExecutionSetting(params);
    try {
      HibernateUtil.store(executionSetting);
    } catch (EntityStorageException e) {
      throw new WebException(e, Response.Status.BAD_REQUEST);
    }

    try {
      // Start the process, which executes the algorithm
      Process process =
          executeAlgorithm(String.valueOf(params.getAlgorithmId()),
                           executionIdentifier,
                           params.getMemory());
      ProcessRegistry.getInstance().put(executionIdentifier, process);

      // Forward messages from the process to the console output
      InputStreamReader isr = new InputStreamReader(process.getInputStream());
      BufferedReader br = new BufferedReader(isr);
      String lineRead;
      while ((lineRead = br.readLine()) != null) {
        System.out.println(lineRead);
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }

    Execution execution;
    try {
      // The algorithm execution was successful
      // Get the execution from hibernate
      ArrayList<Criterion> criteria = new ArrayList<>();
      criteria.add(Restrictions.eq("identifier", executionIdentifier));
      execution = (Execution) HibernateUtil.queryCriteria(Execution.class,
                                                          criteria.toArray(
                                                              new Criterion[criteria.size()]))
          .get(0);
    } catch (EntityStorageException | IndexOutOfBoundsException e) {
      // The execution process got killed - execution object is created anyway (with abortion flag set)
      AlgorithmResource algorithmResource = new AlgorithmResource();
      Algorithm algorithm = algorithmResource.get(params.getAlgorithmId());

      execution = new Execution(algorithm)
          .setExecutionSetting(executionSetting)
          .setAborted(true)
          .setInputs(AlgorithmExecution.parseInputs(executionSetting.getInputsJson()));
      ExecutionResource executionResource = new ExecutionResource();
      executionResource.store(execution);
    }

    // Execute the result post processing
    if (!executionSetting.getCountResults()) {
      try {
        ResultPostProcessor.extractAndStoreResults(execution);
      } catch (Exception e) {
        throw new WebException("Could not execute result post processing: " + e.getMessage(),
                               Response.Status.BAD_REQUEST);
      }
    }
    return execution;
  }

  /**
   * builds ExecutionSetting Object to persist information in AlgorithmExecutionParams to Database
   */
  protected ExecutionSetting buildExecutionSetting(AlgorithmExecutionParams params) {
    ExecutionSetting executionSetting = null;

    try {
      DefaultConfigurationFactory configurationFactory = new DefaultConfigurationFactory();
      List<ConfigurationValue>
          parameterValues = new LinkedList<>();
      List<Input> inputs = new ArrayList<>();
      FileInputResource fileInputResource = new FileInputResource();
      TableInputResource tableInputResource = new TableInputResource();
      DatabaseConnectionResource databaseConnectionResource = new DatabaseConnectionResource();

      // build configuration values
      for (ConfigurationRequirement requirement : params.getRequirements()) {
        // no value was set in the frontend
        // do not create a configuration value, so that the value can not be set
        // on the algorithm
        if (requirement.getSettings().length == 0) {
          continue;
        }

        // convert the requirement and add it to the parameters
        parameterValues.add(requirement.build(configurationFactory));

        // add inputs
        for (ConfigurationSetting setting : requirement.getSettings()) {
          if (setting instanceof ConfigurationSettingFileInput) {
            inputs.add(fileInputResource.get(((ConfigurationSettingFileInput) setting).getId()));
          } else if (setting instanceof ConfigurationSettingDatabaseConnection) {
            inputs.add(databaseConnectionResource
                           .get(((ConfigurationSettingDatabaseConnection) setting).getId()));
          } else if (setting instanceof ConfigurationSettingTableInput) {
            inputs.add(tableInputResource.get(((ConfigurationSettingTableInput) setting).getId()));
          }
        }
      }

      // convert configuration values to json strings
      List<String> parameterValuesJson = configurationValuesToJson(parameterValues);

      // convert inputs to json strings
      List<String> inputsJson = inputsToJson(inputs);

      // create a new execution setting object
      executionSetting =
          new ExecutionSetting(parameterValuesJson, inputsJson, params.getExecutionIdentifier())
              .setCacheResults(params.getCacheResults())
              .setWriteResults(params.getWriteResults())
              .setCountResults(params.getCountResults());
    } catch (AlgorithmConfigurationException e) {
      e.printStackTrace();
    }

    return executionSetting;
  }

  /**
   * Converts a list of ConfigurationValues to their Json representations
   *
   * @param parameterValues all parameters to execute the algorithm
   * @return the resulting list of Json Strings
   */
  public List<String> configurationValuesToJson(List<ConfigurationValue> parameterValues) {
    JsonConverter<ConfigurationValue>
        jsonConverter = new JsonConverter<>();
    jsonConverter.addMixIn(FileInputGenerator.class, FileInputGeneratorMixIn.class);
    jsonConverter.addMixIn(TableInputGenerator.class, TableInputGeneratorMixIn.class);
    jsonConverter.addMixIn(RelationalInputGenerator.class, RelationalInputGeneratorMixIn.class);
    return jsonConverter.toJsonStrings(parameterValues);
  }

  public List<String> inputsToJson(List<Input> inputs) {
    JsonConverter<Input> jsonConverter = new JsonConverter<Input>();
    return jsonConverter.toJsonStrings(inputs);
  }

  /**
   * starts execution of Algorithm in separate Process
   *
   * @param algorithmId         id of algorithm to be executed
   * @param executionIdentifier identifier for the upcoming algorithm execution
   * @param memory              memory argument for the process running the algorithm execution
   * @return resulting process object for the algorithm execution
   */
  private Process executeAlgorithm(String algorithmId, String executionIdentifier,
                                         String memory) throws IOException,
                                                               InterruptedException {
    String javaHome = System.getProperty("java.home");
    String javaBin = javaHome +
                     File.separator + "bin" +
                     File.separator + "java";
    String myPath = System.getProperty("java.class.path");
    String className = algorithmExecutionClass.getCanonicalName();

    try {
      URL baseUrl = algorithmExecutionClass.getProtectionDomain().getCodeSource().getLocation();
      File file = new File(baseUrl.toURI());
      String parent = file.getAbsoluteFile().getParent();
      String classesFolder =
          file.getAbsoluteFile().getParentFile().getParent() + File.separator + "classes";
      String parentPathWildCard = parent + File.separator + "*";
      myPath += File.pathSeparator + parentPathWildCard + File.pathSeparator + classesFolder;
    } catch (URISyntaxException ex) {
      ex.printStackTrace();
    }

    ProcessBuilder builder;
    if (!memory.equals("")) {
      builder = new ProcessBuilder(
          javaBin, "-Xmx" + memory + "m", "-Xms" + memory + "m", "-classpath", myPath, className,
          algorithmId, executionIdentifier);
    } else {
      builder = new ProcessBuilder(
          javaBin, "-classpath", myPath, className, algorithmId, executionIdentifier);
    }
    builder.redirectErrorStream(true);

    return builder.start();
  }

}