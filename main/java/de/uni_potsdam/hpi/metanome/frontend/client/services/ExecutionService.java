package de.uni_potsdam.hpi.metanome.frontend.client.services;

import java.io.IOException;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import de.uni_potsdam.hpi.metanome.frontend.client.parameter.InputParameter;

@RemoteServiceRelativePath("executionService")
public interface ExecutionService extends RemoteService {

	public void executeInclusionDependencyAlgorithm(String algorithmName, List<InputParameter> parameters) throws IOException;
	public void executeFunctionalDependencyAlgorithm(String algorithmName, List<InputParameter> parameters) throws IOException;
	public void executeUniqueColumnCombinationsAlgorithm(String algorithmName, List<InputParameter> parameters) throws IOException;
	public void executeBasicStatisticsAlgorithm(String algorithmName, List<InputParameter> parameters);
	
}