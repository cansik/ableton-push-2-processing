package ch.bildspur.push;

public interface PushConstants {
    /**
     * The header sent before each frame of display pixels.
     */
    byte[] FRAME_HEADER = new byte[]{
            (byte) 0xff, (byte) 0xcc, (byte) 0xaa, (byte) 0x88,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00
    };

    /**
     * The width of the display, in pixels.
     */
    int DISPLAY_WIDTH = 960;

    /**
     * The height of the display, in pixels.
     */
    int DISPLAY_HEIGHT = 160;

    /**
     * The number of display lines we send in each USB bulk transfer operation. As @cansik pointed out, we
     * get better frames per second if we send the whole display at once.
     */
    int LINES_PER_TRANSFER = DISPLAY_HEIGHT;

    /**
     * The number of bytes the Push expects to receive for each line of the display.
     */
    int BYTES_PER_LINE = 2048;

    int VENDOR_ID = 0x2982;
    int PRODUCT_ID = 0x1967;
}
