package headfront.jetfuel.execute.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    public void generateFunctionHash() throws Exception {
        List<Class> parameters = Arrays.asList(Long.class, Double.class, String.class);
        String functionHashName = FunctionUtils.getFunctionHashName("changeBankStatus", parameters);
        String expectedMessage = "changeBankStatus" + FunctionUtils.FUNCTION_SEPARATOR + "4" + FunctionUtils.FUNCTION_SEPARATOR + "3" + FunctionUtils.FUNCTION_SEPARATOR + "5";
        assertEquals(expectedMessage, functionHashName);
    }


    @Test
    public void testGetFunctionSignature() throws Exception {
        String functionSignature = FunctionUtils.getFunctionSignature("changeBankStatus", "Deepak", 0.4, true, 7, 77l);
        String expectedMessage = "changeBankStatus(String, Double, Boolean, Integer, Long)";
        assertEquals(expectedMessage, functionSignature);
    }

    @Test
    public void createCorrectTypeOfParam() throws Exception {
        Class[] parameters = {Long.class, Double.class, String.class, Boolean.class, Integer.class, Integer.class};
        Object[] values = {45l, "45.4", "ringclass", true, 8, 5};
        for (int i = 0; i < parameters.length; i++) {
            Object correctTypeOfParam = FunctionUtils.createCorrectTypeOfParam(parameters[i], values[i]);
            assertEquals("Testing " + values[i] + " for type " + parameters[i], parameters[i], correctTypeOfParam.getClass());
        }

    }

}