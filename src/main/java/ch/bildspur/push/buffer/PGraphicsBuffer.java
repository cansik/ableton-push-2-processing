package ch.bildspur.push.buffer;

import ch.bildspur.push.PushConstants;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.Texture;

import java.util.Arrays;

public class PGraphicsBuffer implements PushImageBuffer, PushConstants {
    private static int CHUNK_SIZE = LINES_PER_TRANSFER * BYTES_PER_LINE;

    private PGraphics graphics;
    private PApplet parent;
    private byte[] maskedChunk = new byte[LINES_PER_TRANSFER * BYTES_PER_LINE];

    public PGraphicsBuffer(PApplet parent)
    {
        this.parent = parent;
    }

    @Override
    public void init() {
        graphics = parent.createGraphics(DISPLAY_WIDTH, DISPLAY_HEIGHT, PConstants.JAVA2D);
    }

    @Override
    public void prepareChunk() {
        graphics.loadPixels();
        maskPixels(graphics.pixels, maskedChunk);
    }

    @Override
    public byte[] getChunk(int index) {
        return Arrays.copyOfRange(maskedChunk,
                CHUNK_SIZE * index,
                CHUNK_SIZE * (index + 1));
    }

    @Override
    public void dispose() {
        graphics.dispose();
    }

    public PGraphics getGraphics() {
        return graphics;
    }

    /**
     * Expand an array of shorts representing eight rows of individual pixel samples into an array of bytes
     * with padding at the end of each row so it takes an even 2,048 bytes, masking the pixel data with the
     * "signal shaping pattern" required by the Push 2 display.
     *
     * @param pixels      the unmasked, un-padded pixel data, with one pixel in each short
     * @param destination an array into which the split, padded, and masked pixel bytes should be stored
     */
    private void maskPixels(int[] pixels, byte[] destination) {
        for (int y = 0; y < LINES_PER_TRANSFER; y++) {
            for (int x = 0; x < DISPLAY_WIDTH; x += 2) {
                int pixelOffset = (y * DISPLAY_WIDTH) + x;
                int destinationOffset = (y * BYTES_PER_LINE) + (x * 2);
                destination[destinationOffset] = (byte) ((pixels[pixelOffset] & 0xff) ^ 0xe7);
                destination[destinationOffset + 1] = (byte) ((pixels[pixelOffset] >>> 8) ^ 0xf3);
                destination[destinationOffset + 2] = (byte) ((pixels[pixelOffset + 1] & 0xff) ^ 0xe7);
                destination[destinationOffset + 3] = (byte) ((pixels[pixelOffset + 1] >>> 8) ^ 0xff);
            }
        }
    }
}
