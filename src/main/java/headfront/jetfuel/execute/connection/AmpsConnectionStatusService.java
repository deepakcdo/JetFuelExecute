package headfront.jetfuel.execute.connection;

import java.util.Set;

public interface AmpsConnectionStatusService {

    /**
     * Allows you to set up a listener for a particular amps connection
     * @param connectionName you want notification for
     * @param listener
     */
    void registerAmpsConnectionListener(String connectionName, AmpsConnectionListener listener);

    /**
     * Allows you to remove a listener for a particular amps connection
     * @param connectionName you want unregister for
     * @param listener
     */
    void deRegisterAmpsConnectionListener(String connectionName, AmpsConnectionListener listener);

    /**
     * Register for all connections and disconnections
     * @param listener
     */
    void registerAmpsConnectionListener(AmpsConnectionListener listener);

    /**
     * Deregister for all connections and disconnections
     * @param listener
     */
    void deRegisterAmpsConnectionListener(AmpsConnectionListener listener);

    /**
     * @return a List of all active connections
     */
    Set<String> getActiveConnections();
}
