package regressiontest;

import headfront.jetfuel.execute.functions.JetFuelFunction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

        publishAndCheckFunction(jetFuelExecute, updateBankStatusFunction);
        publishAndCheckFunction(jetFuelExecute, updateBidOfferQuoteStatusFunction);
        publishAndCheckFunction(jetFuelExecute, updateQuoteStatusFunction);
        publishAndCheckFunction(jetFuelExecute, getLastQuoteFunction);
        publishAndCheckFunction(jetFuelExecute, getTradingDateFunction);
        publishAndCheckFunction(jetFuelExecute, getNextThreePriceTicksFunction);
        publishAndCheckFunction(jetFuelExecute, getNextThreePriceTicksInvalidFunction);
        publishAndCheckFunction(jetFuelExecute, getMarketPriceFunction);
        publishAndCheckFunction(jetFuelExecute, updateTraderStatusFunction);

        publishAndCheckFunction(jetFuelExecute1, updateBankStatusFunction1);
        publishAndCheckFunction(jetFuelExecute2, updateBankStatusFunction2);
        publishAndCheckFunction(jetFuelExecute3, updateBankStatusFunction3);
        publishAndCheckFunction(jetFuelExecute4, updateBankStatusFunction4);
        publishAndCheckFunction(jetFuelExecute5, updateBankStatusFunction5);

        publishAndCheckFunction(jetFuelExecute, getListOfWeekendDaysFunction);
        publishAndCheckFunction(jetFuelExecute, getNoOfHolidaysPerMonthFunction);

        publishAndCheckFunction(jetFuelExecute, placeOrderWithMapFunction);
        publishAndCheckFunction(jetFuelExecute, placeOrderWithMapAndStringFunction);
        publishAndCheckFunction(jetFuelExecute, placeOrderWithListFunction);
        publishAndCheckFunction(jetFuelExecute, placeOrderWithListAndStringFunction);


        LOG.info("Waiting for 5 minutes");
        // run for 5 min
        boolean keepRunning = true;
        int count = 1;
        while (keepRunning) {
            Thread.sleep(1000 * 60);
            if (count == -5) {
                keepRunning = false;
            }
            LOG.info("Waited for " + count + " minute/s");
            count++;
        }
        LOG.info("Done Waiting for test, unpublishing functions");

        unPublishAndCheckFunction(jetFuelExecute, updateBankStatusFunction);
        unPublishAndCheckFunction(jetFuelExecute, updateBidOfferQuoteStatusFunction);
        unPublishAndCheckFunction(jetFuelExecute, updateQuoteStatusFunction);
        unPublishAndCheckFunction(jetFuelExecute, getLastQuoteFunction);
        unPublishAndCheckFunction(jetFuelExecute, getTradingDateFunction);
        unPublishAndCheckFunction(jetFuelExecute, getNextThreePriceTicksFunction);
        unPublishAndCheckFunction(jetFuelExecute, getNextThreePriceTicksInvalidFunction);
        unPublishAndCheckFunction(jetFuelExecute, getMarketPriceFunction);
        unPublishAndCheckFunction(jetFuelExecute, updateTraderStatusFunction);

        unPublishAndCheckFunction(jetFuelExecute1, updateBankStatusFunction1);
        unPublishAndCheckFunction(jetFuelExecute2, updateBankStatusFunction2);
        unPublishAndCheckFunction(jetFuelExecute3, updateBankStatusFunction3);
        unPublishAndCheckFunction(jetFuelExecute4, updateBankStatusFunction4);
        unPublishAndCheckFunction(jetFuelExecute5, updateBankStatusFunction5);

        unPublishAndCheckFunction(jetFuelExecute, getListOfWeekendDaysFunction);
        unPublishAndCheckFunction(jetFuelExecute, getNoOfHolidaysPerMonthFunction);

        unPublishAndCheckFunction(jetFuelExecute, placeOrderWithMapFunction);
        unPublishAndCheckFunction(jetFuelExecute, placeOrderWithMapAndStringFunction);
        unPublishAndCheckFunction(jetFuelExecute, placeOrderWithListFunction);
        unPublishAndCheckFunction(jetFuelExecute, placeOrderWithListAndStringFunction);

        jetFuelExecute.shutDown();
        LOG.info("All Tests completed");
    }

}
