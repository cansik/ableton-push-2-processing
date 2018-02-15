package ch.bildspur.push.buffer;

import ch.bildspur.push.PushConstants;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

import java.util.Arrays;

public class PGraphicsBuffer implements PushImageBuffer, PushConstants {
    private static int CHUNK_SIZE = LINES_PER_TRANSFER * BYTES_PER_LINE;

    private static int RED_MASK_16BIT = 0x001f;
    private static int GREEN_MASK_16BIT = 0x07e0;
    private static int BLUE_MASK_16BIT = 0xf800;

    private PGraphics graphics;
    private PApplet parent;
    private byte[] maskedChunk = new byte[LINES_PER_TRANSFER * BYTES_PER_LINE];

    public PGraphicsBuffer(PApplet parent) {
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

                // convert from 32 bit RGB to 16 bit BGR
                short firstPixel = convertRGB32toBGR16(pixels[pixelOffset]);
                short secondPixel = convertRGB32toBGR16(pixels[pixelOffset + 1]);

                destination[destinationOffset] = (byte) ((firstPixel & 0xff) ^ 0xe7);
                destination[destinationOffset + 1] = (byte) ((firstPixel >>> 8) ^ 0xf3);
                destination[destinationOffset + 2] = (byte) ((secondPixel & 0xff) ^ 0xe7);
                destination[destinationOffset + 3] = (byte) ((secondPixel >>> 8) ^ 0xff);
            }
        }
    }

    /**
     * Converts a 32 bit RGB pixel into a 16 bit BGR pixel which is used by the ableton push display.
     *
     * @param pixel 32 bit RGB pixel data.
     * @return 16 bit BGR pixel.
     */
    private short convertRGB32toBGR16(int pixel) {
        // extract and map color components
        short red = mapTo5Bit((pixel & PImage.RED_MASK) >> 16);
        short green = mapTo5Bit((pixel & PImage.GREEN_MASK) >> 8);
        short blue = mapTo5Bit(pixel & PImage.BLUE_MASK);

        // create 16 bit BGR
        return (short) ((blue << 11) | (green << 6) | red);
    }

    private short mapTo5Bit(int number) {
        return (short) Math.round(PApplet.map(number, 0x0, 0xFE, 0x0, 0x1F));
    }
}
