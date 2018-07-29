package regressiontest;

import headfront.jetfuel.execute.JetFuelExecute;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by Deepak on 21/01/2018.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/client/junitClientTest.xml"})
public class JetFuelExecuteClientTest extends JetFuelBaseClientTest {
    private static Logger LOG = LoggerFactory.getLogger(JetFuelExecuteClientTest.class);

    public JetFuelExecuteClientTest() {
        setRunningBothClientAndSerer(false);
    }

    @Test
    public void forceTest() throws Exception {
        callMarketPriceThenCancel();
    }

    @Test
    public void callUpdateBankStatusAndGetPositiveResponse1() throws Exception {
        Optional<String> foundFunction = getFunction("_5_", "updateBankStatus");
        runNamedTest(foundFunction.get());
    }

    @Test
    public void callUpdateBankStatusAndGetPositiveResponse2() throws Exception {
        final Optional<String> foundFunction = getFunction("_2_", "updateBankStatus");
        runNamedTest(foundFunction.get());
    }

    @Test
    public void callUpdateBankStatusAndGetPositiveResponse3() throws Exception {
        final Optional<String> foundFunction = getFunction("_3_", "updateBankStatus");
        runNamedTest(foundFunction.get());
    }

    @Test
    public void callUpdateBankStatusAndGetPositiveResponse4() throws Exception {
        final Optional<String> foundFunction = getFunction("_4_", "updateBankStatus");
        runNamedTest(foundFunction.get());
    }

    @Test
    public void callUpdateBankStatusAndGetPositiveResponse5() throws Exception {
        final Optional<String> foundFunction = getFunction("_5_", "updateBankStatus");
        runNamedTest(foundFunction.get());
    }

    @Test
    public void callUpdateBankStatusAndGetPositiveResponseALL() throws Exception {
        JetFuelExecute jetFuelExecuteToUse = getJetFuelExecute();
        final int uncompletedFunctionCountBeforeTest = jetFuelExecuteToUse.getUncompletedFunctionCount();
        String functionName = "*.updateBankStatus";
        String expectedMsg = "Deepak is authorised, Bank status is ON";
        callJetFuelFunction(jetFuelExecuteToUse, functionName, null,
                new Object[]{"Deepak", true}, sleepValueForTest,
                0, false,
                6, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, true, null, false,
                new String[]{"Completed", "Completed", "Completed", "Completed", "Completed", "Completed"},
                true, false, 0);
        assertEquals("UnComplete function count should match", uncompletedFunctionCountBeforeTest + 1,
                jetFuelExecuteToUse.getUncompletedFunctionCount());
    }

    @Test
    public void callUpdateBankStatusAndGetPositiveResponseFrom_1_And_3_only() throws Exception {
        JetFuelExecute jetFuelExecuteToUse = getJetFuelExecute();
        final int uncompletedFunctionCountBeforeTest = jetFuelExecuteToUse.getUncompletedFunctionCount();
        String functionName = "*.updateBankStatus";
        String traderName = "1_3_Only";
        String expectedMsg = traderName + " is authorised, Bank status is ON";
        callJetFuelFunction(jetFuelExecuteToUse, functionName, null,
                new Object[]{traderName, true}, sleepValueForTest,
                0, false,
                2, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, true, null, false,
                new String[]{"Completed", "Completed"},
                true, false, 0);
        assertEquals("UnComplete function count should match", uncompletedFunctionCountBeforeTest + 1,
                jetFuelExecuteToUse.getUncompletedFunctionCount());
    }

    private void runNamedTest(String functionName) throws Exception {
        JetFuelExecute jetFuelExecuteToUse = getJetFuelExecute();
        final int uncompletedFunctionCountBeforeTest = jetFuelExecuteToUse.getUncompletedFunctionCount();
        String expectedMsg = "Deepak is authorised, Bank status is ON";
        callJetFuelFunction(jetFuelExecuteToUse, functionName, null,
                new Object[]{"Deepak", true}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, true, null, false,
                new String[]{"Completed"}, true, false, 0);
        assertEquals("UnComplete function count should match", uncompletedFunctionCountBeforeTest,
                jetFuelExecuteToUse.getUncompletedFunctionCount());
    }


    private Optional<String> getFunction(String tag, String functionSuffix) {
        final Set<String> availableFunctions = jetFuelExecute.getAvailableFunctions();
        final Optional<String> foundFunction = availableFunctions.stream().filter(function -> function.contains(tag) && function.contains(functionSuffix)).findAny();
        assertTrue("Function with tag '" + tag + "' and functions Suffix '" + functionSuffix + "' not found", foundFunction.isPresent());
        return foundFunction;
    }

    @Test
    public void checkResponseTimeForFunctionsIsBelow1Sec() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        final JetFuelExecute jetFuelExecuteToUse = getJetFuelExecute();
        TestFunctionResponseListener response = new TestFunctionResponseListener(countDownLatch);
        String fullFunctionName = jetFuelExecuteToUse.findFunction(updateTraderStatusFunction.getFunctionName()).get(0);
        final long startTime = System.currentTimeMillis();
        String callID = jetFuelExecuteToUse.executeFunction(fullFunctionName, new Object[]{"Deepak", true, "safe"}, response);
        countDownLatch.await(sleepValueForTest, TimeUnit.MILLISECONDS);
        final long timeTaken = System.currentTimeMillis() - startTime;
        assertEquals("Call should complete successfully", response.getOnCompletedCount(), 1);
        responseStatsWriter.writeStats(callID, timeTaken);
        assertTrue("Test should take less than 1 sec and test " + callID + " took " + timeTaken + " millis.", timeTaken < 1000);
    }

}
