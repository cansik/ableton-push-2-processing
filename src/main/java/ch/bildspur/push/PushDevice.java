package ch.bildspur.push;

import org.usb4java.*;
import processing.core.PApplet;
import processing.core.PConstants;

import java.awt.image.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

public class PushDevice implements PConstants, PushConstants {

    /**
     * When we have initialized the library, this will hold the long-lived direct buffer we use to
     * send pixel data to the display. If it is null, it means the library has not been initialized,
     * or it has been shut down.
     */
    private ByteBuffer transferBuffer = null;

    /**
     * A smaller buffer used to send the frame header.
     */
    private ByteBuffer headerBuffer = null;

    /**
     * When we have opened the Push display, this will hold the device handle we used to open it.
     */
    private DeviceHandle pushHandle = null;

    private PApplet parent;
    private BufferedImage screenBuffer;

    private boolean isOpen = false;

    private Device device;

    public PushDevice(PApplet parent, Device device) {
        this.parent = parent;
        this.device = device;
    }

    public boolean open() {
        if (isOpen)
            return true;

        // Things look promising so allocate our byte buffers
        transferBuffer = ByteBuffer.allocateDirect(BYTES_PER_LINE * LINES_PER_TRANSFER);
        headerBuffer = ByteBuffer.allocateDirect(FRAME_HEADER.length);
        headerBuffer.put(FRAME_HEADER);

        try {
            // open device
            DeviceHandle handle = new DeviceHandle();
            int result = LibUsb.open(device, handle);
            if (result == LibUsb.SUCCESS) {
                openPushDisplay(handle);
            } else {
                throw new IllegalStateException("Unable to find Ableton Push 2 display device");
            }
        } catch (RuntimeException e) {
            close();
            throw e;
        }

        isOpen = true;
        return true;
    }

    public void close() {
        if (!isOpen)
            return;

        if (pushHandle != null) {
            screenBuffer = null;

            LibUsb.close(pushHandle);
            pushHandle = null;
        }
        if (transferBuffer != null) {
            headerBuffer = null;
            transferBuffer = null;
        }

        isOpen = false;
    }

    /**
     * Send a frame of pixels, corresponding to whatever has been drawn in the image returned by open(),
     * to the display.
     *
     * @throws LibUsbException       if there is a problem communicating.
     * @throws IllegalStateException if the Push 2 has not been opened.
     */
    public synchronized void sendFrame() {
        if (transferBuffer == null) {
            throw new IllegalStateException("Push 2 device has not been opened");
        }
        IntBuffer transferred = IntBuffer.allocate(1);
        int result = LibUsb.bulkTransfer(pushHandle, (byte) 0x01, headerBuffer, transferred, 1000);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Transfer of frame header to Push 2 display failed", result);
        }

        // We send many lines at a time to the display; allocate buffers big enough to receive them,
        // expand with the row stride padding, and mask with the signal shaping pattern.
        short[] pixels = new short[LINES_PER_TRANSFER * DISPLAY_WIDTH];
        byte[] maskedChunk = new byte[LINES_PER_TRANSFER * BYTES_PER_LINE];
        for (int i = 0; i < (DISPLAY_HEIGHT / LINES_PER_TRANSFER); i++) {
            screenBuffer.getRaster().getDataElements(
                    0, i * LINES_PER_TRANSFER, DISPLAY_WIDTH, LINES_PER_TRANSFER, pixels);
            maskPixels(pixels, maskedChunk);
            transferBuffer.clear();
            transferBuffer.put(maskedChunk);
            transferred.clear();
            result = LibUsb.bulkTransfer(pushHandle, (byte) 0x01, transferBuffer, transferred, 1000);
            if (result != LibUsb.SUCCESS) {
                throw new LibUsbException("Transfer of frame image to Push 2 display failed", result);
            }
        }
    }

    /**
     * Send a frame of pixels asynchronously, corresponding to whatever has been drawn in the image returned by open(),
     * to the display.
     *
     * @throws LibUsbException       if there is a problem communicating.
     * @throws IllegalStateException if the Push 2 has not been opened.
     */
    public synchronized void sendFrameAsync() {
        if (transferBuffer == null) {
            throw new IllegalStateException("Push 2 device has not been opened.");
        }

        Transfer headerTransfer = LibUsb.allocTransfer();
        // Could look at transfer.status and transfer.actualLength here and report issues once we have
        // a logging framework or outer callback interface.
        LibUsb.fillBulkTransfer(headerTransfer, pushHandle, (byte) 0x01, headerBuffer, LibUsb::freeTransfer, null, 1000);

        int result = LibUsb.submitTransfer(headerTransfer);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Asynchronous transfer of frame header to Push 2 display failed", result);
        }

        // We send many lines at a time to the display; allocate buffers big enough to receive them,
        // expand with the row stride padding, and mask with the signal shaping pattern.
        short[] pixels = new short[LINES_PER_TRANSFER * DISPLAY_WIDTH];
        byte[] maskedChunk = new byte[LINES_PER_TRANSFER * BYTES_PER_LINE];
        for (int i = 0; i < (DISPLAY_HEIGHT / LINES_PER_TRANSFER); i++) {
            screenBuffer.getRaster().getDataElements(
                    0, i * LINES_PER_TRANSFER, DISPLAY_WIDTH, LINES_PER_TRANSFER, pixels);
            maskPixels(pixels, maskedChunk);
            transferBuffer.clear();
            transferBuffer.put(maskedChunk);
            Transfer frameTransfer = LibUsb.allocTransfer();
            // Again could report issues based on transfer.status and transfer.actualLength once there's a way.
            LibUsb.fillBulkTransfer(frameTransfer, pushHandle, (byte) 0x01, transferBuffer, LibUsb::freeTransfer, null, 1000);
            result = LibUsb.submitTransfer(frameTransfer);

            if (result != LibUsb.SUCCESS) {
                throw new LibUsbException("Asynchronous transfer of frame image to Push 2 display failed", result);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PushDevice that = (PushDevice) o;
        return Objects.equals(device, that.device);
    }

    @Override
    public int hashCode() {

        return Objects.hash(device);
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

    /**
     * Opens the Push 2 display interface when the device has been found and opened.
     *
     * @param handle the opened Push 2 device.
     * @throws LibUsbException if there is a problem communicating with the USB environment.
     */
    private void openPushDisplay(DeviceHandle handle) {
        int result = LibUsb.claimInterface(handle, 0);
        if (result < 0) {
            LibUsb.close(handle);
            throw new LibUsbException("Unable to claim interface 0 of Push 2 device", result);
        }
        pushHandle = handle;

        // Create the buffered image which we can draw to, and which will convert that into pixel data
        // in the arrangement the Push wants.
        ColorModel colorModel = new DirectColorModel(16, 0x001f, 0x07e0, 0xf800);
        int[] bandMasks = new int[]{0x001f, 0x07e0, 0xf800};
        WritableRaster raster = WritableRaster.createPackedRaster(DataBuffer.TYPE_USHORT,
                DISPLAY_WIDTH, DISPLAY_HEIGHT, bandMasks, null);
        screenBuffer = new BufferedImage(colorModel, raster, false, null);
    }

    public BufferedImage getScreenBuffer() {
        return screenBuffer;
    }

    public void setScreenBuffer(BufferedImage screenBuffer) {
        this.screenBuffer = screenBuffer;
    }

    public boolean isOpen() {
        return isOpen;
    }
}
