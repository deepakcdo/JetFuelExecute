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
@ContextConfiguration(locations = {"/client/junitClientTest.xml"})
public class JetFuelExecuteClientTest extends JetFuelBaseClientTest {
    private static Logger LOG = LoggerFactory.getLogger(JetFuelExecuteClientTest.class);

    public JetFuelExecuteClientTest() {
        setRunningBothClientAndSerer(false);
    }

    @Test
    public void forceTest() throws Exception {
        callGetTradingDateWithNoParamsAndGetPositiveResponse();
    }
}
