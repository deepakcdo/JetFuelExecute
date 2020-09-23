package headfront.jetfuel.execute.connection;

/**
 * Interface to listen for amps client connection and disconnections.
 */
public interface AmpsConnectionListener {

    void connected(String ampsConnectionName);

    void disconnected(String ampsConnectionName);
}
