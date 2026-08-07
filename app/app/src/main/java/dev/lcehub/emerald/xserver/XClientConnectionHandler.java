package dev.lcehub.emerald.xserver;

import dev.lcehub.emerald.xconnector.ConnectedClient;
import dev.lcehub.emerald.xconnector.ConnectionHandler;

public class XClientConnectionHandler implements ConnectionHandler {
    private final XServer xServer;

    public XClientConnectionHandler(XServer xServer) {
        this.xServer = xServer;
    }

    @Override
    public ConnectedClient newConnectedClient(long clientPtr, int fd) {
        return new XClient(clientPtr, fd, xServer);
    }

    @Override
    public void handleNewConnection(ConnectedClient client) {}

    @Override
    public void handleConnectionShutdown(ConnectedClient client) {
        ((XClient)client).freeResources();
    }
}
