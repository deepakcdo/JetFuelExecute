package regressiontest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by Deepak on 21/01/2018.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/server/junitServerTest.xml"})
public class JetFuelExecuteServerTest extends JetFuelBaseTests {
    private static Logger LOG = LoggerFactory.getLogger(JetFuelExecuteServerTest.class);

    public JetFuelExecuteServerTest() {
        setRunningBothClientAndSerer(false);
    }

    @Test
    public void publishFunctionsAndWait5min() throws Exception {

        publishAndCheckFunction(updateBankStatusFunction);
        publishAndCheckFunction(updateBidOfferQuoteStatusFunction);
        publishAndCheckFunction(updateQuoteStatusFunction);
        publishAndCheckFunction(getLastQuoteFunction);
        publishAndCheckFunction(getTradingDateFunction);
        publishAndCheckFunction(getNextThreePriceTicks);

        LOG.info("Waiting for 5 minutes");
        // run for 5 min
        boolean keepRunning = true;
        int count = 1;
        while (keepRunning) {
            Thread.sleep(1000 * 60);
            if (count == 5) {
                keepRunning = false;
            }
            LOG.info("Waited for " + count + " minute/s");
            count++;
        }
        LOG.info("Done Waiting for test, unpublishing functions");

        unPublishAndCheckFunction(updateBankStatusFunction);
        unPublishAndCheckFunction(updateBidOfferQuoteStatusFunction);
        unPublishAndCheckFunction(updateQuoteStatusFunction);
        unPublishAndCheckFunction(getLastQuoteFunction);
        unPublishAndCheckFunction(getTradingDateFunction);
        unPublishAndCheckFunction(getNextThreePriceTicks);
        jetFuelExecute.shutDown();
        LOG.info("All Tests completed");
    }

}
