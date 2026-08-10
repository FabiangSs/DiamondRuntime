package dev.lcehub.emerald.xenvironment.components;

import dev.lcehub.emerald.sysvshm.SysVSHMConnectionHandler;
import dev.lcehub.emerald.sysvshm.SysVSHMRequestHandler;
import dev.lcehub.emerald.sysvshm.SysVSharedMemory;
import dev.lcehub.emerald.xconnector.UnixSocketConfig;
import dev.lcehub.emerald.xconnector.XConnectorEpoll;
import dev.lcehub.emerald.xenvironment.EnvironmentComponent;
import dev.lcehub.emerald.xserver.SHMSegmentManager;
import dev.lcehub.emerald.xserver.XServer;

public class SysVSharedMemoryComponent extends EnvironmentComponent {
    private XConnectorEpoll connector;
    public final UnixSocketConfig socketConfig;
    private SysVSharedMemory sysVSharedMemory;
    private final XServer xServer;

    public SysVSharedMemoryComponent(XServer xServer, UnixSocketConfig socketConfig) {
        this.xServer = xServer;
        this.socketConfig = socketConfig;
    }

    @Override
    public void start() {
        if (connector != null) return;
        sysVSharedMemory = new SysVSharedMemory();
        connector = new XConnectorEpoll(socketConfig, new SysVSHMConnectionHandler(sysVSharedMemory), new SysVSHMRequestHandler());
        connector.start();

        xServer.setSHMSegmentManager(new SHMSegmentManager(sysVSharedMemory));
    }

    @Override
    public void stop() {
        if (connector != null) {
            connector.stop();
            connector = null;
        }

        sysVSharedMemory.deleteAll();
    }
}
