package headfront.jetfuel.execute;

/**
 * Interface to listen for amps client connection and disconnections.
 */
public interface ClientDisconnectionListener {

    void connected(String ampsConnectionName);

    void disconnected(String ampsConnectionName);
}
