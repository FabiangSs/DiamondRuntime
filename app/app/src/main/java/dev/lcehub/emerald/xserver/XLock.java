package dev.lcehub.emerald.xserver;

public interface XLock extends AutoCloseable {
    @Override
    void close();
}
