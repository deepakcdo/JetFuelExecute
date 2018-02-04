package headfront.jetfuel.execute.functions;

import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
 * Created by Deepak on 28/05/2017.
 */
public abstract class AbstractFunctionExecutor implements FunctionProcessor {
    private List<FunctionParameter> functionParameters;

    public void setFunctionParameters(List<FunctionParameter> functionParameters) {
        notNull(functionParameters);
        this.functionParameters = functionParameters;
    }

    public void validateAndExecuteFunction(String id, List<Object> parameters, FunctionResponse result) {
        if (functionParameters == null) {
            throw new RuntimeException("The setFunctionParameters(List<FunctionParameter>) has not been called so we cant validate this. Please set it.");
        }
        String validate = validate(parameters);
        if (validate == null) {
            try {
                executeFunction(id, parameters, result);
            } catch (Exception e) {
                result.onError(id, "Unable to process Function call", e.getMessage() + " " + e.toString());
            }
        } else {
            result.onError(id, "Validation failed.", validate);
        }
    }

    protected abstract void executeFunction(String id, List<Object> parameters, FunctionResponse result);


    private String validate(List<Object> parameters) {
        String gotAndExpectedMsg = "Got " + parameters + " expected " + functionParameters;
        if (parameters.size() != functionParameters.size()) {
            return "Got " + parameters.size() + " parameters but expected " + functionParameters.size() + " parameters. " + gotAndExpectedMsg;
        }
        for (int i = 0; i < parameters.size(); i++) {
            Object parameter = parameters.get(i);
            Class<?> parameterClass = parameter.getClass();
            Class parameterType = functionParameters.get(i).getParameterType();
            if (!parameterClass.equals(parameterType)) {
                return "Parameter at index " + (i + 1) + " was " + parameter + " with type " + parameterClass + " we expected " + parameterType;
            }
        }
        return null;
    }
}
