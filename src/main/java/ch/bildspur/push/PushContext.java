package ch.bildspur.push;

import org.usb4java.*;
import processing.core.PConstants;

import java.util.ArrayList;
import java.util.List;

public class PushContext implements PushConstants, PConstants {
    private final Context context = new Context();

    private EventHandlingThread eventThread = null;

    private boolean isLibUsbInitialised = false;

    protected final PushEvent pushEvent = new PushEvent();

    public PushContext() {
        // add event listeners
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    private void initLibUsb() {
        if (isLibUsbInitialised)
            return;

        // Try initializing libusb
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to initialize libusb", result);
        }

        // setup hotplug
        HotplugCallbackHandle callbackHandle = new HotplugCallbackHandle();
        result = LibUsb.hotplugRegisterCallback(null,
                LibUsb.HOTPLUG_EVENT_DEVICE_ARRIVED
                        | LibUsb.HOTPLUG_EVENT_DEVICE_LEFT,
                LibUsb.HOTPLUG_ENUMERATE,
                LibUsb.HOTPLUG_MATCH_ANY,
                LibUsb.HOTPLUG_MATCH_ANY,
                LibUsb.HOTPLUG_MATCH_ANY,
                pushEvent, null, callbackHandle);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to register hotplug callback",
                    result);
        }

        isLibUsbInitialised = true;
    }

    private void exitLibUsb() {
        checkUsbLibState();
        LibUsb.exit(context);
        isLibUsbInitialised = false;
    }

    /**
     * Start the thread to process asynchronous events from LibUsb if it is not already running.
     */
    private synchronized void startEventThread() {
        if (eventThread == null) {
            eventThread = new EventHandlingThread(context);
            eventThread.setDaemon(true);
            eventThread.start();
        }
    }

    private synchronized void stopEventThread() {
        // Shut down the asynchronous event processing thread if it was started.
        if (eventThread != null) {
            eventThread.abort();
            try {
                eventThread.join();
            } catch (InterruptedException e) {
                System.err.println("Interrupted waiting for event handling thread to abort.");
            }
            eventThread = null;
        }
    }

    private void checkUsbLibState() {
        if (!isLibUsbInitialised)
            throw new LibUsbException("LibUsb not initialised", 1);
    }

    /**
     * Locate the Push 2 in the USB environment.
     *
     * @return the device object representing it, or null if it could not be found.
     * @throws LibUsbException if there is a problem communicating with the USB environment.
     */
    public List<PushDevice> listDevices() {
        checkUsbLibState();

        List<PushDevice> devices = new ArrayList<>();

        // Read the USB device list
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(null, list);
        if (result < 0) {
            throw new LibUsbException("Unable to get device list", result);
        }

        try {
            // Iterate over all devices and scan for the right one
            for (Device device : list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) {
                    throw new LibUsbException("Unable to read device descriptor", result);
                }
                if (descriptor.bDeviceClass() == LibUsb.CLASS_PER_INTERFACE &&
                        descriptor.idVendor() == VENDOR_ID && descriptor.idProduct() == PRODUCT_ID) {

                    devices.add(new PushDevice(device));
                }
            }
        } finally {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }

        return devices;
    }

    public PushDevice getFirstDevice() {
        checkUsbLibState();
        List<PushDevice> devices = listDevices();

        return (devices.size() > 0) ? devices.get(0) : null;
    }

    /**
     * Checks if a push device is available.
     *
     * @return true if push is available.
     */
    public boolean isPushAvailable() {
        checkUsbLibState();
        return listDevices().size() > 0;
    }

    public void open() {
        if(isOpen())
            return;

        initLibUsb();
        startEventThread();
    }

    public void close() {
        if(!isOpen())
            return;

        stopEventThread();
        exitLibUsb();
    }

    public void dispose() {
        close();
    }

    public void addPushEventListener(PushEventListener listener) {
        pushEvent.addListener(listener);
    }

    public boolean isOpen() {
        return isLibUsbInitialised;
    }
}
