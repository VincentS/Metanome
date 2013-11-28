package de.uni_potsdam.hpi.metanome.frontend.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.uni_potsdam.hpi.metanome.algorithm_integration.results.Result;
import de.uni_potsdam.hpi.metanome.frontend.client.parameter.InputParameter;

public interface ExecutionServiceAsync {

	public void executeAlgorithm(String algorithmName, List<InputParameter> parameters, AsyncCallback<Long> callback);

	public void fetchNewResults(String algorithmName, AsyncCallback<List<Result>> asyncCallback);
}