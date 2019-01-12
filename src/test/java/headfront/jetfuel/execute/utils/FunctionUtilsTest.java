package headfront.jetfuel.execute.utils;

import headfront.jetfuel.execute.functions.FunctionParameter;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by Deepak on 09/04/2017.
 */
public class FunctionUtilsTest {
    @Test
    public void validateParameters_valid() throws Exception {
        List<Class> parameters = Arrays.asList(Integer.class, Integer.class, String.class);
        boolean b = FunctionUtils.validateParameters(parameters);
        assertTrue(b);
    }

    @Test
    public void validateParameters_invalid() throws Exception {
        try {
            List<Class> parameters = Arrays.asList(Long.class, Date.class, String.class);
            boolean b = FunctionUtils.validateParameters(parameters);
            fail("Test code should not come here as an excpetion should be thrown");
        } catch (IllegalArgumentException expectedException) {
            String expectedMessage = "These parameters types are not supported [class java.util.Date]";
            String messageReceived = expectedException.getMessage();
            assertEquals(expectedMessage, messageReceived);
        }
    }

    @Test
    public void validateParameters_null() throws Exception {
        try {
            List<Class> parameters = Arrays.asList(Long.class, null, String.class);
            boolean b = FunctionUtils.validateParameters(parameters);
            fail("Test code should not come here as an excpetion should be thrown");
        } catch (IllegalArgumentException expectedException) {
            String expectedMessage = "These parameters types are not supported [null]";
            String messageReceived = expectedException.getMessage();
            assertEquals(expectedMessage, messageReceived);
        }
    }


    @Test
    public void testGetFunctionSignature() throws Exception {
        String functionSignature = FunctionUtils.getFunctionSignature("changeBankStatus", "Deepak", 0.4, true, 7, 77l);
        String expectedMessage = "changeBankStatus(String, Double, Boolean, Integer, Long)";
        assertEquals(expectedMessage, functionSignature);
    }

    @Test
    public void createCorrectTypeOfParam() throws Exception {
        Class[] parameters = {Long.class, Double.class, String.class, Boolean.class, Integer.class, Integer.class, List.class, Map.class};
        Object[] values = {45l, "45.4", "ringclass", true, 8, 5, "[2,2.33]", "{age=23,name=deepak}"};
        for (int i = 0; i < parameters.length; i++) {
            Object correctTypeOfParam = FunctionUtils.createCorrectTypeOfParam(parameters[i], values[i]);
            if (parameters[i].isInterface()) {
                assertTrue("Testing " + values[i] + " for type " + parameters[i] + " and we got " + correctTypeOfParam.getClass()
                        , parameters[i].isAssignableFrom(correctTypeOfParam.getClass()));
            } else {
                assertEquals("Testing " + values[i] + " for type " + parameters[i], parameters[i], correctTypeOfParam.getClass());
            }

        }
    }

    @Test
    public void testValidateParameters() throws Exception {
        List<FunctionParameter> expectedParameters = Arrays.asList(
                new FunctionParameter("", Long.class, ""),
                new FunctionParameter("", Double.class, ""),
                new FunctionParameter("", String.class, ""),
                new FunctionParameter("", Boolean.class, ""),
                new FunctionParameter("", Integer.class, ""),
                new FunctionParameter("", Integer.class, ""),
                new FunctionParameter("", List.class, ""),
                new FunctionParameter("", Map.class, ""));
        Object[] rawValues = {45l, "45.4", "ringclass", true, 8, 5, "[2,2.33]", "{age=23,name=deepak}"};
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < expectedParameters.size(); i++) {
            Object correctValue = FunctionUtils.createCorrectTypeOfParam(expectedParameters.get(i).getParameterType(), rawValues[i]);
            values.add(correctValue);
        }
        String reason = FunctionUtils.validateParameters(values, expectedParameters);
        assertTrue("Parameters was not valid due to " + reason, reason == null);
    }

}