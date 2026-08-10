package dev.lcehub.emerald.xconnector;

public interface ConnectionHandler {
    void handleConnectionShutdown(Client client);

    void handleNewConnection(Client client);
}