package regressiontest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by Deepak on 21/01/2018.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/basic/junitBasicTest.xml"})
public class BasicJetFuelExecuteTest extends JetFuelBaseClientTest {


    @Test
    public void creatingBankUpdateFunctionsThenUnpublish() throws Exception {
        publishAndCheckFunction(getJetFuelExecute(), updateBankStatusFunction);
        unPublishAndCheckFunction(getJetFuelExecute(), updateBankStatusFunction);
    }

    @Test
    public void creatingBidOfferQuoteStatusFunctionsThenUnpublish() throws Exception {
        publishAndCheckFunction(getJetFuelExecute(), updateBidOfferQuoteStatusFunction);
        unPublishAndCheckFunction(getJetFuelExecute(), updateBidOfferQuoteStatusFunction);
    }

    @Test
    public void creatingUpdateQuoteStatusFunctionThenUnpublish() throws Exception {
        publishAndCheckFunction(getJetFuelExecute(), updateQuoteStatusFunction);
        unPublishAndCheckFunction(getJetFuelExecute(), updateQuoteStatusFunction);
    }

    @Test
    public void creatingGetLastQuoteFunctionsThenUnpublish() throws Exception {
        publishAndCheckFunction(getJetFuelExecute(), getLastQuoteFunction);
        unPublishAndCheckFunction(getJetFuelExecute(), getLastQuoteFunction);
    }

    @Test
    public void creatingGetTradingDateFunctionsThenUnpublish() throws Exception {
        publishAndCheckFunction(getJetFuelExecute(), getTradingDateFunction);
        unPublishAndCheckFunction(getJetFuelExecute(), getTradingDateFunction);
    }

    @Test
    public void creatingDuplicateBankUpdateFunctionsThenUnpublish() throws Exception {
        publishAndCheckFunction(getJetFuelExecute(), updateBankStatusFunction);
        checkDuplicateFunctionsCantBePublished(getJetFuelExecute(), updateBankStatusFunction);
        unPublishAndCheckFunction(getJetFuelExecute(), updateBankStatusFunction);
    }

    @Test
    public void testCreatingASameMethodWithDifferentParameters() throws Exception {
        publishAndCheckFunction(getJetFuelExecute(), updateBidOfferQuoteStatusFunction);
        checkDuplicateFunctionsCantBePublished(getJetFuelExecute(), updateBidQuoteStatusFunction);
        unPublishAndCheckFunction(getJetFuelExecute(), updateBankStatusFunction);
    }

    @Test
    public void creatingGetNextThreePriceTicksThenUnpublish() throws Exception {
        publishAndCheckFunction(getJetFuelExecute(), getNextThreePriceTicksFunction);
        unPublishAndCheckFunction(getJetFuelExecute(), getNextThreePriceTicksFunction);
    }

    @Test
    public void creatingGetNextThreePriceTicksInvalidThenUnpublish() throws Exception {
        publishAndCheckFunction(getJetFuelExecute(), getNextThreePriceTicksInvalidFunction);
        unPublishAndCheckFunction(getJetFuelExecute(), getNextThreePriceTicksInvalidFunction);
    }

}
