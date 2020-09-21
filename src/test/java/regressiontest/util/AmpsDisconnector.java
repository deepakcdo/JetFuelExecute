package regressiontest.util;

import com.crankuptheamps.client.HAClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class AmpsDisconnector {
    private static Logger LOG = LoggerFactory.getLogger(AmpsDisconnector.class);
    private HAClient amps;
    private String connectionTopic;
    private ObjectMapper objectMapper;
    private String adminUrl;
    private WebQuery webquery = new WebQuery();

    public AmpsDisconnector(HAClient amps, String connectionTopic,
                            ObjectMapper objectMapper, String server, String adminPort) {
        this.amps = amps;
        this.connectionTopic = connectionTopic;
        this.objectMapper = objectMapper;
        this.adminUrl = getAmpsUrl(server, adminPort, false);
        LOG.info("Going to use adminUrl " + adminUrl);
        LOG.info("Waiting Rest service to update");
        // wait as we are using  the rest service that updates every 5 sec
        try {
            Thread.sleep(14000);
        } catch (InterruptedException e) {
            //ignore
        }
    }


    public static String getAmpsUrl(String connectionsStr, String adminPortStr, boolean useSecureHttp) {
        String server = connectionsStr.replace("tcp://", "");
        server = server.replace("tcps://", "");
        server = server.substring(0, server.lastIndexOf(":"));
        String prefix = useSecureHttp ? "https://" : "http://";
        return prefix + server + ":" + adminPortStr;
    }

    public void disconnect(String connectionName) throws Exception {
        LOG.info("Trying to Disconnect user " + connectionName);
        String urlAllUsers = adminUrl + "/amps/instance/clients.json";
        String allUsers = webquery.doWebQuery(urlAllUsers);
        Map map = objectMapper.readValue(allUsers, Map.class);
        Map<Object, Object> amps = (Map<Object, Object>) map.get("amps");
        Map<Object, Object> instance = (Map<Object, Object>) amps.get("instance");
        List<Map<Object, Object>> clients = (List<Map<Object, Object>>) instance.get("clients");
        LOG.info("Got "  + clients.size() + " connections");
        for (Map<Object, Object> client : clients) {
            String connection_name = (String) client.get("client_name");
            LOG.info("Checking "  + connection_name);
            if (connectionName.equals(connection_name)) {
                Object id = client.get("id");
                String disconnectUrl = adminUrl + "/amps/administrator/clients/" + id + "/disconnect";
                webquery.doWebQuery(disconnectUrl);
                LOG.info("Disconnected user " + connectionName + " with url " + disconnectUrl);
            }
        }
    }
}
