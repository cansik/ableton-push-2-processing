package ch.bildspur.push;

import ch.bildspur.push.buffer.PushImageBuffer;
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

    private boolean isOpen = false;

    private Device device;

    private PushImageBuffer screenBuffer;

    public PushDevice(Device device) {
        this.device = device;
    }

    public boolean open(PushImageBuffer screenBuffer) {
        if (isOpen)
            return true;

        this.screenBuffer = screenBuffer;

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
            screenBuffer.dispose();

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

        for (int i = 0; i < (DISPLAY_HEIGHT / LINES_PER_TRANSFER); i++) {
            transferBuffer.clear();
            transferBuffer.put(screenBuffer.getChunk(i));
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

        for (int i = 0; i < (DISPLAY_HEIGHT / LINES_PER_TRANSFER); i++) {
            transferBuffer.clear();
            transferBuffer.put(screenBuffer.getChunk(i));
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
        screenBuffer.init();
    }

    public boolean isOpen() {
        return isOpen;
    }
}
