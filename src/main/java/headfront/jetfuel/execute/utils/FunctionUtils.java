package headfront.jetfuel.execute.utils;

import headfront.jetfuel.execute.FunctionAccessType;
import headfront.jetfuel.execute.FunctionExecutionType;
import headfront.jetfuel.execute.JetFuelExecuteConstants;
import headfront.jetfuel.execute.functions.FunctionParameter;
import headfront.jetfuel.execute.functions.JetFuelFunction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.Assert.hasLength;
import static org.springframework.util.Assert.notNull;

/**
 * Created by Deepak on 09/04/2017.
 */
public class FunctionUtils {

    private static final Map<Class, Integer> FunctionsTypes = Collections.unmodifiableMap(Stream.of(
            new SimpleEntry<>(Boolean.class, 1),
            new SimpleEntry<>(Integer.class, 2),
            new SimpleEntry<>(Double.class, 3),
            new SimpleEntry<>(Long.class, 4),
            new SimpleEntry<>(String.class, 5))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));

    public static final String FUNCTION_SEPARATOR = "_";
    public static final String NAME_SEPARATOR = ".";
    public static final String ESCAPED_NAME_SEPARATOR = "\\.";
    private static LocalDateTime date = LocalDateTime.now();
    private static String DATE_TIME = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private static AtomicInteger counter = new AtomicInteger();


    //  Parameters list can be empty
    public static String getFunctionHashName(String functionName, List<Class> parameters) throws IllegalArgumentException {
        notNull(functionName, "FunctionName cannot be null");
        hasLength(functionName, "FunctionName cannot be empty String");
        validateParameters(parameters);
        List<String> functionHashParts = new ArrayList<>();
        functionHashParts.add(functionName);
        parameters.forEach(parameter -> {
            functionHashParts.add(FunctionsTypes.get(parameter).toString());
        });
        return String.join(FUNCTION_SEPARATOR, functionHashParts);
    }

    public static String getFunctionHashName(String functionName, Object... parameters) throws IllegalArgumentException {
        List<Class> parameterClass = new ArrayList<>();
        for (Object param : parameters) {
            parameterClass.add(param.getClass());
        }
        return getFunctionHashName(functionName, parameterClass);
    }

    public static String getFunctionSignature(String functionName, Object... parameters) throws IllegalArgumentException {
        List<String> functionSignature = new ArrayList<>();
        for (Object param : parameters) {
            functionSignature.add(param.getClass().getSimpleName());
        }
        final String params = String.join(", ", functionSignature);
        return functionName + "(" + params + ")";
    }

    public static boolean validateParameters(List<Class> parameters) throws IllegalArgumentException {
        List<Class> invalidParameter = parameters.stream().filter(param -> !FunctionsTypes.containsKey(param)).collect(Collectors.toList());
        if (invalidParameter.size() == 0) {
            return true;
        } else {
            throw new IllegalArgumentException("These parameters types are not supported " + invalidParameter);
        }
    }

    public static String getNextID(String name) {
        return name + NAME_SEPARATOR + DATE_TIME + FUNCTION_SEPARATOR + counter.incrementAndGet();
    }

    public static String getFullFunctionName(JetFuelFunction function) {
        return function.getFunctionPublisherName() + NAME_SEPARATOR + function.getFunctionName();
    }

    public static String getFullFunctionName(String pubName, String functionName) {
        return pubName + NAME_SEPARATOR + functionName;
    }

    public static Object createCorrectTypeOfParam(Class parameterType, Object value) {
        if (FunctionsTypes.containsKey(parameterType)) {
            if (value != null) {
                if (parameterType.equals(Boolean.class)) {
                    return getBoolean(value);
                }
                if (parameterType.equals(Integer.class)) {
                    return getInt(value);
                }
                if (parameterType.equals(Double.class)) {
                    return getDouble(value);
                }
                if (parameterType.equals(Long.class)) {
                    return getLong(value);
                }
                if (parameterType.equals(String.class)) {
                    return value.toString();
                }
            } else {
                return "<NULL>";
            }
        } else {
            throw new RuntimeException("Unsupported parameter class " + parameterType);
        }
        return null;
    }

    public static boolean getBoolean(Object s) {
        if (((s != null) && (s.toString().equalsIgnoreCase("true") || s.toString().equalsIgnoreCase("false")))) {
            return Boolean.parseBoolean(s.toString());
        } else {
            throw new RuntimeException("Invalid boolean value '" + s + "'");
        }
    }

    public static int getInt(Object s) {
        try {
            return Integer.parseInt(s.toString());
        } catch (Exception e) {
            throw new RuntimeException("Invalid integer value '" + s + "'");
        }
    }

    public static double getDouble(Object s) {
        try {
            return Double.parseDouble(s.toString());
        } catch (Exception e) {
            throw new RuntimeException("Invalid double value '" + s + "'");
        }
    }

    public static long getLong(Object s) {
        try {
            return Long.parseLong(s.toString());
        } catch (Exception e) {
            throw new RuntimeException("Invalid long value '" + s + "'");
        }
    }

    public static String getParameterHash(Object... functionParameters) {
        if (functionParameters.length == 0) {
            return "";
        }
        List<String> functionHashParts = new ArrayList<>();
        for (Object parameter : functionParameters) {
            Integer integer = FunctionsTypes.get(parameter.getClass());
            if (integer == null) {
                throw new RuntimeException("Unsupported parameter value = " + parameter + " type = " + parameter.getClass());
            } else {
                functionHashParts.add(integer.toString());
            }
        }
        return FUNCTION_SEPARATOR + String.join(FUNCTION_SEPARATOR, functionHashParts);
    }

    public static Map<String, Object> createMapFromJetFuelFunction(JetFuelFunction jetFuelFunction, String ampsConnectionName, String hostName) {
        Map<String, Object> publishMap = new HashMap<>();
        publishMap.put(JetFuelExecuteConstants.PUBLISH_FUNCTION_ID, jetFuelFunction.getFullFunctionName());
        publishMap.put(JetFuelExecuteConstants.FUNCTION_NAME, jetFuelFunction.getFunctionName());
        publishMap.put(JetFuelExecuteConstants.FUNCTION_DESCRIPTION, jetFuelFunction.getFunctionDescription());
        publishMap.put(JetFuelExecuteConstants.FUNCTION_PUBLISHER_NAME, ampsConnectionName);
        publishMap.put(JetFuelExecuteConstants.FUNCTION_PUBLISHER_HOSTNAME, hostName);
        LocalDateTime date = LocalDateTime.now();
        String dateTimeStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        publishMap.put(JetFuelExecuteConstants.FUNCTION_PUBLISH_TIME, dateTimeStr);
        publishMap.put(JetFuelExecuteConstants.PARAMETERS_NAMES, jetFuelFunction.getParametersNames());
        publishMap.put(JetFuelExecuteConstants.PARAMETERS_TYPES, jetFuelFunction.getParametersTypes());
        publishMap.put(JetFuelExecuteConstants.PARAMETERS_DESC, jetFuelFunction.getParametersDesc());
        publishMap.put(JetFuelExecuteConstants.RETURN_TYPE, jetFuelFunction.getReturnType());
        publishMap.put(JetFuelExecuteConstants.RETURN_DESCRIPTION, jetFuelFunction.getReturnTypeDescription());
        publishMap.put(JetFuelExecuteConstants.FUNCTION_ACCESS_TYPE, jetFuelFunction.getFunctionAccessType().name());
        publishMap.put(JetFuelExecuteConstants.FUNCTION_EXECUTION_TYPE, jetFuelFunction.getExecutionType().name());
        return publishMap;
    }

    public static JetFuelFunction createJetFuelFunctionFromMap(Map<String, Object> map) throws Exception {
        final List<String> paraNames = (List<String>) map.get(JetFuelExecuteConstants.PARAMETERS_NAMES);
        final List<String> paraTypes = (List<String>) map.get(JetFuelExecuteConstants.PARAMETERS_TYPES);
        final List<String> paraDesc = (List<String>) map.get(JetFuelExecuteConstants.PARAMETERS_DESC);
        List<FunctionParameter> functionParameters = new ArrayList<>();
        if (paraNames != null) {
            for (int i = 0; i < paraNames.size(); i++) {
                FunctionParameter parameter = new FunctionParameter(paraNames.get(i),
                        Class.forName(paraTypes.get(i)), paraDesc.get(i));
                functionParameters.add(parameter);
            }
        }

        JetFuelFunction jetFuelFunction = new JetFuelFunction((String) map.get(JetFuelExecuteConstants.FUNCTION_NAME),
                (String) map.get(JetFuelExecuteConstants.FUNCTION_DESCRIPTION),
                functionParameters,
                Class.forName((String) map.get(JetFuelExecuteConstants.RETURN_TYPE)),
                (String) map.get(JetFuelExecuteConstants.RETURN_DESCRIPTION), null,
                FunctionAccessType.valueOf((String) map.get(JetFuelExecuteConstants.FUNCTION_ACCESS_TYPE)),
                FunctionExecutionType.valueOf((String) map.get(JetFuelExecuteConstants.FUNCTION_EXECUTION_TYPE)));
        jetFuelFunction.setTransientFunctionDetatils((String) map.get(JetFuelExecuteConstants.FUNCTION_PUBLISHER_NAME),
                (String) map.get(JetFuelExecuteConstants.FUNCTION_PUBLISHER_HOSTNAME),
                (String) map.get(JetFuelExecuteConstants.FUNCTION_PUBLISH_TIME));

        return jetFuelFunction;
    }

    public static String getIsoDateTime() {
        LocalDateTime date = LocalDateTime.now();
        String dateTimeStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return dateTimeStr;
    }
}
