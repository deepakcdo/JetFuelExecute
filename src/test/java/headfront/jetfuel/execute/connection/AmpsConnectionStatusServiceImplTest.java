package headfront.jetfuel.execute.connection;

import com.crankuptheamps.client.HAClient;
import headfront.jetfuel.execute.utils.HaClientFactory;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/disconnections/main.xml"})
public class AmpsConnectionStatusServiceImplTest {

    @Autowired
    AmpsConnectionStatusServiceImpl ampsConnectionStatusServiceImpl;
    @Autowired
    AmpsDisconnector ampsDisconnector;
    @Autowired
    HaClientFactory haClientFactory;

    @Value("${appConnectionUrl}")
    private String ampsConnectionUrl;

    private String TEST_CONNECTION_NAME_1 = "client1";
    private String TEST_CONNECTION_NAME_2 = "client2";
    private String TEST_CONNECTION_NAME_3 = "client3";

    private static HAClient haClient1;
    private static HAClient haClient2;
    private static HAClient haClient3;

    @Before
    public void setup() throws Exception{
         haClient1 = createHaClient(TEST_CONNECTION_NAME_1);
         haClient2 = createHaClient(TEST_CONNECTION_NAME_2);
         haClient3 = createHaClient(TEST_CONNECTION_NAME_3);
        Thread.sleep(8000); // We use a disconnector that is based on the rest interface so we have ot wait
    }

    @After
    public  void clearUp()throws Exception{
        haClient1.close();
        haClient2.close();
        haClient3.close();
        Thread.sleep(3000);
    }

    // Note this will throw exceptions when amps disconnects. This is not meant to fail the test
    @Test
    public void notifyOfDisconnect() throws Exception {
        List<String> expectedDisconnection = new ArrayList<>();
        String connectionName = TEST_CONNECTION_NAME_1;
        expectedDisconnection.add(connectionName);
        TestAmpsConnectionListener ampsDisconnectionListener = new TestAmpsConnectionListener();
        ampsConnectionStatusServiceImpl.registerAmpsConnectionListener(connectionName, ampsDisconnectionListener);
        ampsDisconnector.disconnect(connectionName);
        Thread.sleep(3000);
        // we expect one disconnection and a reconnection.
        assertEquals("Expected one Disconnection ", expectedDisconnection, ampsDisconnectionListener.getDisconnections());
        assertEquals("Expected no Connection ", expectedDisconnection, ampsDisconnectionListener.getConnections());

    }

    // Note this will throw exceptions when amps disconnects. This is not meant to fail the test
    @Test
    public void dontNotifyOfDisconnect() throws Exception {
        List<String> expectedDisconnection = new ArrayList<>();
        String connectionName = TEST_CONNECTION_NAME_1;
        TestAmpsConnectionListener ampsDisconnectionListener = new TestAmpsConnectionListener();
        ampsConnectionStatusServiceImpl.registerAmpsConnectionListener(connectionName, ampsDisconnectionListener);
        ampsConnectionStatusServiceImpl.deRegisterAmpsConnectionListener(connectionName, ampsDisconnectionListener);
        ampsDisconnector.disconnect(connectionName);
        Thread.sleep(3000);
        // we expect one disconnection and a reconnection.
        assertEquals("Expected no Disconnection ", expectedDisconnection, ampsDisconnectionListener.getDisconnections());
        assertEquals("Expected no Connection ", expectedDisconnection, ampsDisconnectionListener.getConnections());
    }

    @Test
    public void testConnectedOnListener() throws Exception {
        List<String> connectedUsers = new ArrayList<>();
        connectedUsers.add(TEST_CONNECTION_NAME_1);
        connectedUsers.add(TEST_CONNECTION_NAME_2);
        connectedUsers.add(TEST_CONNECTION_NAME_3);
        Thread.sleep(3000);
        TestAmpsConnectionListener ampsDisconnectionListener = new TestAmpsConnectionListener();
        ampsConnectionStatusServiceImpl.registerAmpsConnectionListener(ampsDisconnectionListener);
        Thread.sleep(1500);
        assertEquals("Expected three Connections ", connectedUsers, ampsDisconnectionListener.getConnectedConnections());
        haClient1.close();
        connectedUsers.remove(TEST_CONNECTION_NAME_1);
        Thread.sleep(1500);
        assertEquals("Expected two Connections ", connectedUsers, ampsDisconnectionListener.getConnectedConnections());
        haClient2.close();
        connectedUsers.remove(TEST_CONNECTION_NAME_2);
        Thread.sleep(1500);
        assertEquals("Expected one Connections ", connectedUsers, ampsDisconnectionListener.getConnectedConnections());
        haClient3.close();
        connectedUsers.remove(TEST_CONNECTION_NAME_3);
        Thread.sleep(1500);
        assertEquals("Expected no Connection ", new ArrayList<String>(), ampsDisconnectionListener.getConnectedConnections());
    }

    @Test
    public void testDisconnectOnListener() throws Exception {
        String connectionName = TEST_CONNECTION_NAME_2;
        TestAmpsConnectionListener ampsDisconnectionListener = new TestAmpsConnectionListener();
        ampsConnectionStatusServiceImpl.registerAmpsConnectionListener(ampsDisconnectionListener);
        ampsConnectionStatusServiceImpl.deRegisterAmpsConnectionListener(ampsDisconnectionListener);
        ampsDisconnector.disconnect(connectionName);
        Thread.sleep(2000);
        assertEquals("Expected one Disconnection ", new ArrayList<String>(), ampsDisconnectionListener.getDisconnections());
        assertEquals("Expected no Connection ", new ArrayList<String>(), ampsDisconnectionListener.getConnections());
    }

    @Test
    public void testActiveConnections() throws Exception {
        Set<String> connectedUsers = new HashSet<>();
        connectedUsers.add(TEST_CONNECTION_NAME_1);
        connectedUsers.add(TEST_CONNECTION_NAME_2);
        connectedUsers.add(TEST_CONNECTION_NAME_3);
        Set<String> activeConnections = ampsConnectionStatusServiceImpl.getActiveConnections();
        assertEquals("Expected two active Connections", connectedUsers, activeConnections);
        haClient1.close();
        haClient2.close();
        haClient3.close();
        Thread.sleep(3000);
        assertEquals("Expected no active Connections", new HashSet<>(), activeConnections);
    }

    private HAClient createHaClient(String connectionName) throws Exception {
        return haClientFactory.createHaClient(connectionName, ampsConnectionUrl, false);
    }

}