package ch.bildspur.push.buffer;

public interface PushImageBuffer {
    void init();

    void prepareChunk();

    byte[] getChunk(int index);

    void dispose();
}
