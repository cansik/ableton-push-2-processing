package ch.bildspur.push.buffer;

import ch.bildspur.push.PushConstants;

import java.awt.image.*;

public class BufferedImageBuffer implements PushImageBuffer, PushConstants {
    private BufferedImage screen;

    private short[] pixels = new short[LINES_PER_TRANSFER * DISPLAY_WIDTH];
    private byte[] maskedChunk = new byte[LINES_PER_TRANSFER * BYTES_PER_LINE];

    @Override
    public void init() {
        // Create the buffered image which we can draw to, and which will convert that into pixel data
        // in the arrangement the Push wants.
        ColorModel colorModel = new DirectColorModel(16, 0x001f, 0x07e0, 0xf800);
        int[] bandMasks = new int[]{0x001f, 0x07e0, 0xf800};
        WritableRaster raster = WritableRaster.createPackedRaster(DataBuffer.TYPE_USHORT,
                DISPLAY_WIDTH, DISPLAY_HEIGHT, bandMasks, null);

        screen = new BufferedImage(colorModel, raster, false, null);
    }

    @Override
    public void prepareChunk() {

    }

    @Override
    public byte[] getChunk(int index) {
        screen.getRaster().getDataElements(0, index * LINES_PER_TRANSFER, DISPLAY_WIDTH, LINES_PER_TRANSFER, pixels);
        maskPixels(pixels, maskedChunk);

        return maskedChunk;
    }

    public short[] getRaw() {
        screen.getRaster().getDataElements(0, 0, DISPLAY_WIDTH, LINES_PER_TRANSFER, pixels);
        return pixels;
    }

    @Override
    public void dispose() {
        screen = null;
        pixels = null;
        maskedChunk = null;
    }

    /**
     * Expand an array of shorts representing eight rows of individual pixel samples into an array of bytes
     * with padding at the end of each row so it takes an even 2,048 bytes, masking the pixel data with the
     * "signal shaping pattern" required by the Push 2 display.
     *
     * @param pixels      the unmasked, un-padded pixel data, with one pixel in each short
     * @param destination an array into which the split, padded, and masked pixel bytes should be stored
     */
    private void maskPixels(short[] pixels, byte[] destination) {
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

    public BufferedImage getScreen() {
        return screen;
    }
}
