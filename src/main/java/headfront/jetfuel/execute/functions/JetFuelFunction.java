package headfront.jetfuel.execute.functions;


import headfront.jetfuel.execute.FunctionAccessType;
import headfront.jetfuel.execute.FunctionExecutionType;
import headfront.jetfuel.execute.utils.FunctionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static headfront.jetfuel.execute.utils.AssertChecks.notNull;


/**
 * Created by Deepak on 09/04/2017.
 */
public class JetFuelFunction {

    private String functionName;
    private List<FunctionParameter> functionParameters;
    private List<String> parametersNames;
    private List<Class> parametersTypes;
    private List<String> parametersDesc;
    private String fullFunctionName;
    private Class returnType;
    private FunctionProcessor executor;
    private Class<? extends Runnable> onClientDisconnect = null;
    private String functionDescription;
    private String functionPublisherName;
    private String publisherHostname;
    private String publisherTime;
    private String returnTypeDescription;
    private FunctionAccessType functionAccessType;
    private FunctionExecutionType executionType;

    public JetFuelFunction(String functionName, String functionDescription,
                           List<FunctionParameter> functionParameters,
                           Class returnType, String returnTypeDescription, FunctionProcessor executor,
                           FunctionAccessType functionAccessType,
                           FunctionExecutionType executionType) {
        notNull(functionName, "functionName cannot be null");
        notNull(functionDescription, "functionDescription cannot be null");
        this.executionType = executionType;
        this.returnTypeDescription = returnTypeDescription;
        this.functionDescription = functionDescription;
        this.executor = executor;
        this.returnType = returnType;
        this.functionName = functionName;
        this.functionParameters = functionParameters;
        this.functionAccessType = functionAccessType;
        parametersNames = functionParameters.stream().map(FunctionParameter::getParameterName).collect(Collectors.toList());
        parametersTypes = functionParameters.stream().map(FunctionParameter::getParameterType).collect(Collectors.toList());
        parametersDesc = functionParameters.stream().map(FunctionParameter::getDescription).collect(Collectors.toList());
    }

    // This is set at Publish time

    public void setTransientFunctionDetatils(String functionPublisherName, String publisherHostname,
                                             String publisherTime) {
        this.functionPublisherName = functionPublisherName;
        this.publisherHostname = publisherHostname;
        this.publisherTime = publisherTime;
        fullFunctionName = FunctionUtils.getFullFunctionName(functionPublisherName, functionName);
    }

    // These are getters for data that is set at publish time

    public String getFunctionPublisherName() {
        return functionPublisherName;
    }

    public String getPublisherHostname() {
        return publisherHostname;
    }

    public String getPublisherTime() {
        return publisherTime;
    }


    // Normal getters

    public String getFunctionName() {
        return functionName;
    }

    public String getFunctionDescription() {
        return functionDescription;
    }

    public Class getReturnType() {
        return returnType;
    }

    public String getReturnTypeDescription() {
        return returnTypeDescription;
    }

    public List<FunctionParameter> getFunctionParameters() {
        return Collections.unmodifiableList(functionParameters);
    }

    public List<String> getParametersNames() {
        return Collections.unmodifiableList(parametersNames);
    }

    public List<Class> getParametersTypes() {
        return Collections.unmodifiableList(parametersTypes);
    }

    public List<String> getParametersDesc() {
        return Collections.unmodifiableList(parametersDesc);
    }

    public String getFullFunctionName() {
        return fullFunctionName;
    }

    public FunctionProcessor getExecutor() {
        return executor;
    }

    public FunctionAccessType getFunctionAccessType() {
        return functionAccessType;
    }

    public FunctionExecutionType getExecutionType() {
        return executionType;
    }

    // not used for now
    public Class<? extends Runnable> getOnClientDisconnect() {
        return onClientDisconnect;
    }

    public void setOnClientDisconnect(Class<? extends Runnable> onClientDisconnect) {
        this.onClientDisconnect = onClientDisconnect;
    }

    public String isValidToPublish() {
        String[] names = {"functionPublisherName", "publisherHostname", "publisherTime"};
        String[] values = {functionPublisherName, publisherHostname, publisherTime};
        for (int i = 0; i < names.length; i++) {
            final String valid = checkIfSet(values[i], names[i]);
            if (valid != null) {
                return valid;
            }
        }
        return null;
    }

    private String checkIfSet(String value, String name) {
        if (value == null) {
            return name + " was null so Function cant be published";
        }
        return null;
    }
}
