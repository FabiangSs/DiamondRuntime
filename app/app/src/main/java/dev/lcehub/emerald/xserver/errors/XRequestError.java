package dev.lcehub.emerald.xserver.errors;

import static dev.lcehub.emerald.xserver.XClientRequestHandler.RESPONSE_CODE_ERROR;

import dev.lcehub.emerald.xconnector.XOutputStream;
import dev.lcehub.emerald.xconnector.XStreamLock;
import dev.lcehub.emerald.xserver.XClient;

import java.io.IOException;

public class XRequestError extends Exception  {
    private final byte code;
    private final int data;

    public XRequestError(int code, int data) {
        this.code = (byte)code;
        this.data = data;
    }

    public byte getCode() {
        return code;
    }

    public int getData() {
        return data;
    }

    public void sendError(XClient client, byte opcode) throws IOException {
        XOutputStream outputStream = client.getOutputStream();
        try (XStreamLock lock = outputStream.lock()) {
            outputStream.writeByte(RESPONSE_CODE_ERROR);
            outputStream.writeByte(code);
            outputStream.writeShort(client.getSequenceNumber());
            outputStream.writeInt(data);
            outputStream.writeShort(client.getRequestData());
            outputStream.writeByte(opcode);
            outputStream.writePad(21);
        }
    }
}
