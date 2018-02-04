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
        publishAndCheckFunction(updateBankStatusFunction);
        unPublishAndCheckFunction(updateBankStatusFunction);
    }

    @Test
    public void creatingBidOfferQuoteStatusFunctionsThenUnpublish() throws Exception {
        publishAndCheckFunction(updateBidOfferQuoteStatusFunction);
        unPublishAndCheckFunction(updateBidOfferQuoteStatusFunction);
    }

    @Test
    public void creatingUpdateQuoteStatusFunctionThenUnpublish() throws Exception {
        publishAndCheckFunction(updateQuoteStatusFunction);
        unPublishAndCheckFunction(updateQuoteStatusFunction);
    }

    @Test
    public void creatingGetLastQuoteFunctionsThenUnpublish() throws Exception {
        publishAndCheckFunction(getLastQuoteFunction);
        unPublishAndCheckFunction(getLastQuoteFunction);
    }

    @Test
    public void creatingGetTradingDateFunctionsThenUnpublish() throws Exception {
        publishAndCheckFunction(getTradingDateFunction);
        unPublishAndCheckFunction(getTradingDateFunction);
    }

    @Test
    public void creatingDuplicateBankUpdateFunctionsThenUnpublish() throws Exception {
        publishAndCheckFunction(updateBankStatusFunction);
        checkDuplicateFunctionsCantBePublished(updateBankStatusFunction);
        unPublishAndCheckFunction(updateBankStatusFunction);
    }

    @Test
    public void testCreatingASameMethodWithDifferentParameters() throws Exception {
        publishAndCheckFunction(updateBidOfferQuoteStatusFunction);
        checkDuplicateFunctionsCantBePublished(updateBidQuoteStatusFunction);
        unPublishAndCheckFunction(updateBankStatusFunction);
    }

}
