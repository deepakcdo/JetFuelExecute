package headfront.jetfuel.execute.impl;

import com.crankuptheamps.client.HAClient;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.MessageHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import headfront.jetfuel.execute.ClientDisconnectionListener;
import headfront.jetfuel.execute.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AmpsDisconnectionService {

    private static Logger LOG = LoggerFactory.getLogger(AmpsDisconnectionService.class);
    private final Map<String, List<ClientDisconnectionListener>> activeDisconnectionListeners = new ConcurrentHashMap<>();

    private ExecutorService jetFuelDisconnectionProcessorThread;

    public AmpsDisconnectionService(HAClient amps, String connectionTopic, ObjectMapper objectMapper) {
        jetFuelDisconnectionProcessorThread = Executors.newSingleThreadExecutor(
                new NamedThreadFactory("JetFuelDisconnectionProcessorThread"));

        try {
            amps.subscribe(new MessageHandler() {
                @Override
                public void invoke(Message message) {
                    String data = message.getData();
                    if (data != null) {
                        try {
                            Map map = objectMapper.readValue(data, Map.class);
                            Object clientStatus = map.get("ClientStatus");
                            if (clientStatus != null) {
                                Map clientStatusDetails = (Map) clientStatus;
                                String event = clientStatusDetails.get("event").toString();
                                String clientName = clientStatusDetails.get("client_name").toString();
                                if (event.equalsIgnoreCase("disconnect")) {
                                    List<ClientDisconnectionListener> clientDisconnectionListeners = activeDisconnectionListeners.get(clientName);
                                    if (clientDisconnectionListeners != null) {
                                        jetFuelDisconnectionProcessorThread.submit(() -> {
                                            clientDisconnectionListeners.forEach(listener -> listener.disconnected(clientName));
                                        });
                                    }
                                }
                                if (event.equalsIgnoreCase("logon")) {
                                    List<ClientDisconnectionListener> clientDisconnectionListeners = activeDisconnectionListeners.get(clientName);
                                    if (clientDisconnectionListeners != null) {
                                        jetFuelDisconnectionProcessorThread.submit(() -> {
                                            clientDisconnectionListeners.forEach(listener -> listener.connected(clientName));
                                        });
                                    }
                                }
                            }
                        } catch (IOException e) {
                            LOG.error("Unable to decode " + data, e);
                        }
                    }

                }
            }, connectionTopic, " /ClientStatus/event in ('disconnect','logon')", 10000);
        } catch (Exception e) {
            LOG.error("Unable to subscribe to " + connectionTopic, e);
        }

    }

    public void registerForDisconnections(String connectionName, ClientDisconnectionListener listener) {
        List<ClientDisconnectionListener> listeners = activeDisconnectionListeners.get(connectionName);
        if (listeners == null) {
            listeners = new ArrayList<>();
            activeDisconnectionListeners.put(connectionName, listeners);
        }
        listeners.add(listener);
    }

    public void deRegisterForDisconnections(String connectionName) {
        activeDisconnectionListeners.remove(connectionName);

    }
}
