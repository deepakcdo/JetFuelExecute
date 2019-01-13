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


/**
 * Created by Deepak on 09/04/2017.
 */
public class FunctionUtils {

    private static final Map<Class, Integer> FunctionsTypes = Collections.unmodifiableMap(Stream.of(
            new SimpleEntry<>(Boolean.class, 1),
            new SimpleEntry<>(Integer.class, 2),
            new SimpleEntry<>(Double.class, 3),
            new SimpleEntry<>(Long.class, 4),
            new SimpleEntry<>(String.class, 5),
            new SimpleEntry<>(Map.class, 6),
            new SimpleEntry<>(List.class, 7))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));

    public static final String FUNCTION_SEPARATOR = "_";
    public static final String NAME_SEPARATOR = ".";
    public static final String ESCAPED_NAME_SEPARATOR = "\\.";
    private static LocalDateTime date = LocalDateTime.now();
    private static String DATE_TIME = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private static AtomicInteger counter = new AtomicInteger();


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

    public static String getNextSimpleID(String name) {
        return name + FUNCTION_SEPARATOR + counter.incrementAndGet();
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
                if (parameterType.equals(Map.class)) {
                    return getMap(value);
                }
                if (parameterType.equals(List.class)) {
                    return getList(value);
                }
            } else {
                return "<NULL>";
            }
        } else {
            throw new RuntimeException("Unsupported parameter class " + parameterType);
        }
        return null;
    }

    private static Map getMap(Object value) {
        String stringValue = value.toString();
        stringValue = removeBrackets(stringValue);
        String[] split = stringValue.split(",");
        Map map = new LinkedHashMap();
        for (String part : split) {
            String[] keyValue = part.split("=");
            map.put(keyValue[0], keyValue[1]);
        }
        return map;
    }

    private static List getList(Object value) {
        String stringValue = value.toString();
        stringValue = removeBrackets(stringValue);
        String[] split = stringValue.split(",");
        return Arrays.asList(split);
    }

    private static String removeBrackets(String stringValue) {
        //remove all brackets
        if (stringValue.startsWith("[")) {
            stringValue = stringValue.substring(1);
        }
        if (stringValue.startsWith("{")) {
            stringValue = stringValue.substring(1);
        }
        if (stringValue.endsWith("]")) {
            stringValue = stringValue.substring(0, stringValue.length() - 1);
        }
        if (stringValue.endsWith("}")) {
            stringValue = stringValue.substring(0, stringValue.length() - 1);
        }
        return stringValue;
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
        publishMap.put(JetFuelExecuteConstants.ALLOW_MULTI_EXECUTE, jetFuelFunction.isAllowMultiExecute());
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
        final Boolean allowMultiExecute = (Boolean) map.get(JetFuelExecuteConstants.ALLOW_MULTI_EXECUTE);
        if (allowMultiExecute != null) {
            jetFuelFunction.setAllowMultiExecute(allowMultiExecute);
        }

        return jetFuelFunction;
    }

    public static String getIsoDateTime() {
        LocalDateTime date = LocalDateTime.now();
        String dateTimeStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return dateTimeStr;
    }

    public static String getIsoDateTime(int adjustHours) {
        LocalDateTime date = LocalDateTime.now();
        date = date.plusHours(adjustHours);
        String dateTimeStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return dateTimeStr;
    }

    public static String validateParameters(List<Object> receivedParameters, List<FunctionParameter> configuredParameters) {
        if (configuredParameters == null) {
            throw new RuntimeException("The setFunctionParameters(List<FunctionParameter>) has not been called so we cant validateParameters this. Please set it.");
        }
        String gotAndExpectedMsg = "Got " + receivedParameters + " expected " + configuredParameters;
        if (receivedParameters.size() != configuredParameters.size()) {
            return "Got " + receivedParameters.size() + " parameters but expected " + configuredParameters.size() + " parameters. " + gotAndExpectedMsg;
        }
        for (int i = 0; i < receivedParameters.size(); i++) {
            final FunctionParameter configuredParameter = configuredParameters.get(i);
            Object receivedParameter = receivedParameters.get(i);
            if (receivedParameter == null) {
                return "Got a null value for " + configuredParameter.getParameterName() + " this is not allowed";
            }
            Class<?> receivedParameterClass = receivedParameter.getClass();
            Class configuredParameterType = configuredParameter.getParameterType();
            if (receivedParameter.getClass() == Integer.class && configuredParameterType == Long.class) {
                return null; //allow this as long are bigger than ints
            }
            if (configuredParameterType.isInterface()) {
                if (configuredParameterType.isAssignableFrom(receivedParameter.getClass())) {
                    return null;
                }
            }
            if (!receivedParameterClass.equals(configuredParameterType)) {
                return "Parameter at index " + (i + 1) + " was " + receivedParameter + " with type " + receivedParameterClass + " we expected " + configuredParameterType;
            }
        }
        return null;
    }
}
