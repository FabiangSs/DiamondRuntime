package dev.lcehub.emerald.xenvironment.components;

import dev.lcehub.emerald.alsaserver.ALSAClient;
import dev.lcehub.emerald.alsaserver.ALSAClientConnectionHandler;
import dev.lcehub.emerald.alsaserver.ALSARequestHandler;
import dev.lcehub.emerald.xconnector.UnixSocketConfig;
import dev.lcehub.emerald.xconnector.XConnectorEpoll;
import dev.lcehub.emerald.xenvironment.EnvironmentComponent;

public class ALSAServerComponent extends EnvironmentComponent {
    private XConnectorEpoll connector;
    private final UnixSocketConfig socketConfig;
    private final ALSAClient.Options options;

    public ALSAServerComponent(UnixSocketConfig socketConfig, ALSAClient.Options options) {
        this.socketConfig = socketConfig;
        this.options = options;
    }

    @Override
    public void start() {
        if (connector != null) return;
        ALSAClient.assignFramesPerBuffer(environment.getContext());
        connector = new XConnectorEpoll(socketConfig, new ALSAClientConnectionHandler(options), new ALSARequestHandler());
        connector.setMultithreadedClients(true);
        connector.start();
    }

    @Override
    public void stop() {
        if (connector != null) {
            connector.destroy();
            connector = null;
        }
    }
}
