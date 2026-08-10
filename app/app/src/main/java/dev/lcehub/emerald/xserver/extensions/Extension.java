package dev.lcehub.emerald.xserver.extensions;

import dev.lcehub.emerald.xconnector.XInputStream;
import dev.lcehub.emerald.xconnector.XOutputStream;
import dev.lcehub.emerald.xserver.XClient;
import dev.lcehub.emerald.xserver.errors.XRequestError;

import java.io.IOException;

public interface Extension {
    String getName();

    byte getMajorOpcode();

    byte getFirstErrorId();

    byte getFirstEventId();

    void handleRequest(XClient client, XInputStream inputStream, XOutputStream outputStream) throws IOException, XRequestError;
}
