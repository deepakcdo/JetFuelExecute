package headfront.jetfuel.execute.connection;

import java.util.ArrayList;
import java.util.List;

public class TestAmpsConnectionListener implements AmpsConnectionListener {


    private List<String> connections = new ArrayList<>();
    private List<String> disconnections = new ArrayList<>();
    private List<String> connectedConnections = new ArrayList<>();
    @Override
    public void connected(String ampsConnectionName) {
        connections.add(ampsConnectionName);
        connectedConnections.add(ampsConnectionName);
    }

    @Override
    public void disconnected(String ampsConnectionName) {
        disconnections.add(ampsConnectionName);
        connectedConnections.remove(ampsConnectionName);
    }

    public List<String> getConnections() {
        return connections;
    }

    public List<String> getDisconnections() {
        return disconnections;
    }
    public List<String> getConnectedConnections() {
        return connectedConnections;
    }

}
