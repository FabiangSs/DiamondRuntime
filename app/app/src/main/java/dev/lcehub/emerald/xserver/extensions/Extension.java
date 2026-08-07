package dev.lcehub.emerald.xserver.extensions;

import dev.lcehub.emerald.xconnector.XInputStream;
import dev.lcehub.emerald.xconnector.XOutputStream;
import dev.lcehub.emerald.xserver.XClient;
import dev.lcehub.emerald.xserver.XServer;
import dev.lcehub.emerald.xserver.errors.XRequestError;

import java.io.IOException;

public abstract class Extension {
    public static final byte START_MAJOR_OPCODE = -100;
    private final byte majorOpcode;
    protected final XServer xServer;

    public Extension(XServer xServer, byte majorOpcode) {
        this.xServer = xServer;
        this.majorOpcode = majorOpcode;
    }

    public abstract String getName();

    public byte getMajorOpcode() {
        return majorOpcode;
    }

    public byte getFirstErrorId() {
        return 0;
    }

    public byte getFirstEventId() {
        return 0;
    }

    public abstract void handleRequest(XClient client, XInputStream inputStream, XOutputStream outputStream) throws IOException, XRequestError;
}
