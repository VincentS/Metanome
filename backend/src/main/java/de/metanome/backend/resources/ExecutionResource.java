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

import de.metanome.backend.result_receiver.ResultReader;
import de.metanome.backend.results_db.EntityStorageException;
import de.metanome.backend.results_db.Execution;
import de.metanome.backend.results_db.HibernateUtil;
import de.metanome.backend.results_db.Result;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("executions")
public class ExecutionResource {

  /**
   * Deletes the execution, which has the given id, from the database.
   *
   * @param id the id of the execution, which should be deleted
   */
  @DELETE
  @Path("/delete/{id}")
  public void delete(@PathParam("id") long id) {
    try {
      Execution execution = (Execution) HibernateUtil.retrieve(Execution.class, id);
      Set<Result> results = execution.getResults();
      HibernateUtil.delete(execution);
      // delete result files from disk
      for (Result result : results) {
        File file = new File(result.getFileName());
        if (file.exists()) {
          file.delete();
        }
      }
    } catch (EntityStorageException e) {
      throw new WebException(e, Response.Status.BAD_REQUEST);
    }
  }

  /**
   * Retrieves an execution from the database.
   *
   * @param id the execution's id
   * @return the execution
   */
  @GET
  @Path("/get/{id}")
  @Produces("application/json")
  public Execution get(@PathParam("id") long id) {
    try {
      return (Execution) HibernateUtil.retrieve(Execution.class, id);
    } catch (EntityStorageException e) {
      throw new WebException(e, Response.Status.BAD_REQUEST);
    }
  }

  /**
   * @return all executions in the database
   */
  @GET
  @Produces("application/json")
  @SuppressWarnings("unchecked")
  public List<Execution> getAll() {
    List<Execution> executions = null;
    try {
      executions = (List<Execution>) HibernateUtil.queryCriteria(Execution.class);
    } catch (EntityStorageException e) {
      // Algorithm should implement Entity, so the exception should not occur.
      e.printStackTrace();
    }

    return executions;
  }

  /**
   * Reads counter results from file.
   *
   * @param id the execution's id
   * @return the updated result
   */
  @GET
  @Path("/count-results/{executionId}")
  @Produces("application/json")
  public Map<String, Integer> readCounterResult(@PathParam("executionId") long id) {
    Map<String, Integer> results = new HashMap<>();
    try {
      Execution execution = (Execution) HibernateUtil.retrieve(Execution.class, id);

      for (Result result : execution.getResults()) {
        results.put(result.getType().getName(),
          ResultReader.readCounterResultFromFile(result.getFileName()));
      }
    } catch (Exception e) {
      throw new WebException(e, Response.Status.BAD_REQUEST);
    }
    return results;
  }

}