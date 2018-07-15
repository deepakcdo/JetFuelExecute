package regressiontest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

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

//    @Test
//    public void callUpdateBankStatusAndGetPositiveResponseALL() throws Exception {
//        runNamedTest("*.updateBankStatus");
//    }

    private void runNamedTest(String functionName) throws Exception {
        String expectedMsg = "Deepak is authorised, Bank status is ON";
        callJetFuelFunction(getJetFuelExecute(), functionName, null,
                new Object[]{"Deepak", true}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, true, null, false,
                new String[]{"Completed"}, true, false, 0);
    }

    private Optional<String> getFunction(String tag, String functionSuffix) {
        final Set<String> availableFunctions = jetFuelExecute.getAvailableFunctions();
        final Optional<String> foundFunction = availableFunctions.stream().filter(function -> function.contains(tag) && function.contains(functionSuffix)).findAny();
        assertTrue("Function with tag '" + tag + "' and functions Suffix '" + functionSuffix + "' not found", foundFunction.isPresent());
        return foundFunction;
    }

}
