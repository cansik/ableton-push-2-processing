package ch.bildspur.push.buffer;

public interface PushImageBuffer  {
    void init();

    byte[] getChunk(int index);

    void dispose();
}
