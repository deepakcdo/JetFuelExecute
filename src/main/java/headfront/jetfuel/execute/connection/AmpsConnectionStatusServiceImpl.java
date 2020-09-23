package headfront.jetfuel.execute.connection;

import com.crankuptheamps.client.HAClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import headfront.jetfuel.execute.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AmpsConnectionStatusServiceImpl implements AmpsConnectionStatusService {

    private static Logger LOG = LoggerFactory.getLogger(AmpsConnectionStatusServiceImpl.class);
    private final Map<String, Set<AmpsConnectionListener>> connectionListenersPerUser = new ConcurrentHashMap<>();
    private final Set<AmpsConnectionListener> connectionListeners = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> activeConnections = new ConcurrentSkipListSet<>();

    private ExecutorService jetFuelDisconnectionProcessorThread;

    /**
     * Setup an AmpsConnectionStatusService
     *
     * @param amps            Aconnected amps instance
     * @param connectionTopic - Statustopic for amps. use headfront.jetfuel.execute.JetFuelExecuteConstants#CLIENT_STATUS_TOPIC as default
     * @param objectMapper    - json decoder
     */
    public AmpsConnectionStatusServiceImpl(HAClient amps, String connectionTopic, ObjectMapper objectMapper) {
        jetFuelDisconnectionProcessorThread = Executors.newSingleThreadExecutor(
                new NamedThreadFactory("JetFuelDisconnectionProcessorThread"));
        try {
            amps.subscribe(message -> {
                String data = message.getData();
                if (data != null) {
                    processMessage(objectMapper, data);
                }

            }, connectionTopic, " /ClientStatus/event in ('disconnect','logon')", 10000);
            LOG.info("Subscribed to Connection event on topic " + connectionTopic);
        } catch (Exception e) {
            LOG.error("Unable to subscribe to " + connectionTopic, e);
        }

    }

    private void processMessage(ObjectMapper objectMapper, String data) {
        try {
            Map map = objectMapper.readValue(data, Map.class);
            Object clientStatus = map.get("ClientStatus");
            if (clientStatus != null) {
                Map clientStatusDetails = (Map) clientStatus;
                String event = clientStatusDetails.get("event").toString();
                String clientName = clientStatusDetails.get("client_name").toString();
                if (event.equalsIgnoreCase("disconnect")) {
                    handleDisconnect(clientName);
                }
                if (event.equalsIgnoreCase("logon")) {
                    handleConnect(clientName);
                }
            }
        } catch (IOException e) {
            LOG.error("Unable to decode " + data, e);
        }
    }

    private void handleConnect(String clientName) {
        activeConnections.add(clientName);
        // First notify any specific user listeners
        Set<AmpsConnectionListener> connectionListenerForUser = connectionListenersPerUser.get(clientName);
        if (connectionListenerForUser != null) {
            jetFuelDisconnectionProcessorThread.submit(() -> {
                connectionListenerForUser.forEach(listener -> listener.connected(clientName));
            });
        }
        // Then the general one
        jetFuelDisconnectionProcessorThread.submit(() -> {
            connectionListeners.forEach(listener -> listener.connected(clientName));
        });
    }

    private void handleDisconnect(String clientName) {
        activeConnections.remove(clientName);
        // First notify any specific user listeners
        Set<AmpsConnectionListener> connectionListenerForUser = connectionListenersPerUser.get(clientName);
        if (connectionListenerForUser != null) {
            jetFuelDisconnectionProcessorThread.submit(() -> {
                connectionListenerForUser.forEach(listener -> listener.disconnected(clientName));
            });
        }
        // Then the general one
        jetFuelDisconnectionProcessorThread.submit(() -> {
            connectionListeners.forEach(listener -> listener.disconnected(clientName));
        });
    }

    @Override
    public void registerAmpsConnectionListener(String connectionName, AmpsConnectionListener listener) {
        Set<AmpsConnectionListener> listeners = connectionListenersPerUser.get(connectionName);
        if (listeners == null) {
            listeners = Collections.synchronizedSet(new HashSet<>());
            connectionListenersPerUser.put(connectionName, listeners);
        }
        listeners.add(listener);
    }

    @Override
    public void deRegisterAmpsConnectionListener(String connectionName, AmpsConnectionListener listener) {
        connectionListenersPerUser.remove(connectionName);
    }

    @Override
    public void registerAmpsConnectionListener(AmpsConnectionListener listener) {
        connectionListeners.add(listener);
        // Let late joiners get all connected users
        jetFuelDisconnectionProcessorThread.submit(() -> {
            activeConnections.forEach(clientName -> {
                connectionListeners.forEach(connectionListener -> connectionListener.connected(clientName));
            });
        });
    }

    @Override
    public void deRegisterAmpsConnectionListener(AmpsConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    @Override
    public Set<String> getActiveConnections() {
        return Collections.unmodifiableSet(activeConnections);
    }
}
