package headfront.jetfuel.execute.utils;

import com.crankuptheamps.client.ConnectionInfo;
import com.crankuptheamps.client.DefaultServerChooser;
import com.crankuptheamps.client.HAClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Deepak on 09/04/2017.
 */
public class HaClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(HaClientFactory.class);

    public HAClient createHaClient(String connectionName, String connectionUrl, boolean addSuffixToConnName) throws Exception {
        DefaultServerChooser serverChooser = new ServerChooser();
        final String[] urls = connectionUrl.split(",");
        final List<String> listOfUrls = Arrays.asList(urls);
        Collections.shuffle(listOfUrls);
        for (String listOfUrl : listOfUrls) {
            serverChooser.add(listOfUrl);
        }
        if (addSuffixToConnName) {
            Random random = new Random(System.currentTimeMillis());
            int rand = random.nextInt(100000);
            connectionName = connectionName + "_" + rand;
        }

        HAClient haClient = new HAClient(connectionName);
        haClient.setServerChooser(serverChooser);
        haClient.setHeartbeat(5);
//        haClient.setFailedWriteHandler(message -> LOG.error("Message from LastChanceHandler " + message.toString()));
        haClient.setExceptionListener(e -> LOG.error("Exception thrown in the AMPS library.", e));
        haClient.setLastChanceMessageHandler(message -> LOG.error("Message from LastChanceHandler " + message.toString()));
        LOG.info("Connecting to amps");
        haClient.connectAndLogon();
        LOG.info("Connected to amps!!!!!!!!!!!!");
        return haClient;
    }

    static class ServerChooser extends DefaultServerChooser {

        @Override
        public void reportFailure(Exception exception, ConnectionInfo info) throws Exception {
            super.reportFailure(exception, info);
            LOG.error("Disconnected from amps  " + removePassword(info).toString(), exception);
        }

        @Override
        public void reportSuccess(ConnectionInfo info) {
            super.reportSuccess(info);
            LOG.info("Connected to amps " + removePassword(info).toString());
        }

        private ConnectionInfo removePassword(ConnectionInfo info){
            String key = "client.uri";
            String url = "" +info.get(key);
            String cleaned = url.replaceAll(":[^////].*@", ":*****@");
            info.put(key,cleaned);
            return info;
        }
    }

}
