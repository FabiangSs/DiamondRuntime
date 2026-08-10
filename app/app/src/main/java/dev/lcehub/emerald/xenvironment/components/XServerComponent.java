package dev.lcehub.emerald.xenvironment.components;

import dev.lcehub.emerald.xenvironment.EnvironmentComponent;
import dev.lcehub.emerald.xconnector.XConnectorEpoll;
import dev.lcehub.emerald.xconnector.UnixSocketConfig;
import dev.lcehub.emerald.xserver.XClientConnectionHandler;
import dev.lcehub.emerald.xserver.XClientRequestHandler;
import dev.lcehub.emerald.xserver.XServer;

public class XServerComponent extends EnvironmentComponent {
    private XConnectorEpoll connector;
    private final XServer xServer;
    private final UnixSocketConfig socketConfig;

    public XServerComponent(XServer xServer, UnixSocketConfig socketConfig) {
        this.xServer = xServer;
        this.socketConfig = socketConfig;
    }

    @Override
    public void start() {
        if (connector != null) return;
        connector = new XConnectorEpoll(socketConfig, new XClientConnectionHandler(xServer), new XClientRequestHandler());
        connector.setInitialInputBufferCapacity(262144);
        connector.setCanReceiveAncillaryMessages(true);
        connector.start();
    }

    @Override
    public void stop() {
        if (connector != null) {
            connector.stop();
            connector = null;
        }
    }

    public XServer getXServer() {
        return xServer;
    }
}
