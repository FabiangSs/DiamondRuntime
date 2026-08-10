package dev.lcehub.emerald.xenvironment.components;

import dev.lcehub.emerald.alsaserver.ALSAClientConnectionHandler;
import dev.lcehub.emerald.alsaserver.ALSARequestHandler;
import dev.lcehub.emerald.xconnector.UnixSocketConfig;
import dev.lcehub.emerald.xconnector.XConnectorEpoll;
import dev.lcehub.emerald.xenvironment.EnvironmentComponent;

public class ALSAServerComponent extends EnvironmentComponent {
    private XConnectorEpoll connector;
    private final UnixSocketConfig socketConfig;

    public ALSAServerComponent(UnixSocketConfig socketConfig) {
        this.socketConfig = socketConfig;
    }

    @Override
    public void start() {
        if (connector != null) return;
        connector = new XConnectorEpoll(socketConfig, new ALSAClientConnectionHandler(), new ALSARequestHandler());
        connector.setMultithreadedClients(true);
        connector.start();
    }

    @Override
    public void stop() {
        if (connector != null) {
            connector.stop();
            connector = null;
        }
    }
}
