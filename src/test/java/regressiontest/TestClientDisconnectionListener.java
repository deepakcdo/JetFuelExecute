package regressiontest;

import headfront.jetfuel.execute.ClientDisconnectionListener;

import java.util.ArrayList;
import java.util.List;

public class TestClientDisconnectionListener implements ClientDisconnectionListener {


    private List<String> connections = new ArrayList<>();
    private List<String> disconnections = new ArrayList<>();
    @Override
    public void connected(String ampsConnectionName) {
        connections.add(ampsConnectionName);
    }

    @Override
    public void disconnected(String ampsConnectionName) {
        disconnections.add(ampsConnectionName);
    }

    public List<String> getConnections() {
        return connections;
    }

    public List<String> getDisconnections() {
        return disconnections;
    }

}
