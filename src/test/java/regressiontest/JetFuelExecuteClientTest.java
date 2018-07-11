package regressiontest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;

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
        String expectedMsg = "Deepak is authorised, Bank status is ON";
        callJetFuelFunction(getJetFuelExecute(), "updateBankStatus", updateBankStatusFunction1,
                new Object[]{"Deepak", true}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, true, null, false,
                new String[]{"Completed"}, true, false, 0);
    }

    @Test
    public void callUpdateBankStatusAndGetPositiveResponse2() throws Exception {
        String expectedMsg = "Deepak is authorised, Bank status is ON";
        callJetFuelFunction(getJetFuelExecute(), "updateBankStatus", updateBankStatusFunction2,
                new Object[]{"Deepak", true}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, true, null, false,
                new String[]{"Completed"}, true, false, 0);
    }

    @Test
    public void callUpdateBankStatusAndGetPositiveResponse3() throws Exception {
        String expectedMsg = "Deepak is authorised, Bank status is ON";
        callJetFuelFunction(getJetFuelExecute(), "updateBankStatus", updateBankStatusFunction3,
                new Object[]{"Deepak", true}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, true, null, false,
                new String[]{"Completed"}, true, false, 0);
    }

    @Test
    public void callUpdateBankStatusAndGetPositiveResponse4() throws Exception {
        String expectedMsg = "Deepak is authorised, Bank status is ON";
        callJetFuelFunction(getJetFuelExecute(), "updateBankStatus", updateBankStatusFunction4,
                new Object[]{"Deepak", true}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, true, null, false,
                new String[]{"Completed"}, true, false, 0);
    }

    @Test
    public void callUpdateBankStatusAndGetPositiveResponse5() throws Exception {
        String expectedMsg = "Deepak is authorised, Bank status is ON";
        callJetFuelFunction(getJetFuelExecute(), "updateBankStatus", updateBankStatusFunction5,
                new Object[]{"Deepak", true}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, true, null, false,
                new String[]{"Completed"}, true, false, 0);
    }
}
