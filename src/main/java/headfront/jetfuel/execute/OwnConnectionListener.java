package headfront.jetfuel.execute;

/**
 * Interface to listen for own connection and disconnections.
 */
public interface OwnConnectionListener {

    void connected();

    void disconnected();
}
