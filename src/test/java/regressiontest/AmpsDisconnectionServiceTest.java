package regressiontest;

import headfront.jetfuel.execute.impl.AmpsDisconnectionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import regressiontest.util.AmpsDisconnector;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/disconnections/main.xml"})
public class AmpsDisconnectionServiceTest {

    @Autowired
    AmpsDisconnectionService ampsDisconnectionService;
    @Autowired
    AmpsDisconnector ampsDisconnector;

    // Note this will throw exceptions when amps disconnects. This is not meant to fail the test
    @Test
    public void notifyOfDisconnect() throws Exception {
        List<String> expectedDisconnection = new ArrayList<>();
        String connectionName = "client1";
        expectedDisconnection.add(connectionName);
        TestClientDisconnectionListener ampsDisconnectionListener = new TestClientDisconnectionListener();
        ampsDisconnectionService.registerForDisconnections(connectionName, ampsDisconnectionListener);
        ampsDisconnector.disconnect(connectionName);
        Thread.sleep(3000);
        // we expect one disconnection and a reconnection.
        assertEquals("Expected one Disconnection ",expectedDisconnection,ampsDisconnectionListener.getDisconnections());
        assertEquals("Expected no Connection ", expectedDisconnection, ampsDisconnectionListener.getConnections());
    }

    // Note this will throw exceptions when amps disconnects. This is not meant to fail the test
    @Test
    public void dontNotifyOfDisconnect() throws Exception {
        String connectionName = "client2";
        TestClientDisconnectionListener ampsDisconnectionListener = new TestClientDisconnectionListener();
        ampsDisconnectionService.registerForDisconnections(connectionName, ampsDisconnectionListener);
        ampsDisconnectionService.deRegisterForDisconnections(connectionName);
        ampsDisconnector.disconnect(connectionName);
        Thread.sleep(2000);
        assertEquals("Expected one Disconnection ",new ArrayList<String>(),ampsDisconnectionListener.getDisconnections());
        assertEquals("Expected no Connection ", new ArrayList<String>(), ampsDisconnectionListener.getConnections());
    }

}