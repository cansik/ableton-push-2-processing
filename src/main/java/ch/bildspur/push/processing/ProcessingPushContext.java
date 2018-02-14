package ch.bildspur.push.processing;

import ch.bildspur.push.PushContext;
import ch.bildspur.push.PushDevice;
import ch.bildspur.push.PushEventListener;
import org.usb4java.Device;
import processing.core.PApplet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessingPushContext implements PushEventListener {

    private PApplet parent;

    private Method onConnectedMethod;

    private Method onDisconnectedMethod;

    private PushContext context;

    public ProcessingPushContext(PApplet parent) {
        super();
        this.parent = parent;

        this.context = new PushContext();
        context.addPushEventListener(this);

        // register processing events
        parent.registerMethod("dispose", this);

        try {
            onConnectedMethod = parent.getClass().getMethod("onPushConnected", PushDevice.class);
            onDisconnectedMethod = parent.getClass().getMethod("onPushDisconnected", PushDevice.class);
        } catch (Exception e) {
        }
    }

    /**
     * Locate the Push 2 in the USB environment.
     *
     * @return the device object representing it, or null if it could not be found.
     */
    public List<ProcessingPushDevice> listDevices() {
        return context.listDevices().stream().map((e) -> new ProcessingPushDevice(parent, e)).collect(Collectors.toList());
    }

    public ProcessingPushDevice getFirstDevice() {
        return new ProcessingPushDevice(parent, context.getFirstDevice());
    }

    /**
     * Checks if a push device is available.
     *
     * @return true if push is available.
     */
    public boolean isPushAvailable() {
        return context.isPushAvailable();
    }

    public void open() {
        context.open();
    }

    public void close() {
       context.close();
    }

    public void dispose() {
        close();
    }

    public void addPushEventListener(PushEventListener listener) {
        context.addPushEventListener(listener);
    }

    public boolean isOpen() {
        return context.isOpen();
    }

    @Override
    public void onPushConnected(Device device) {
        if (onConnectedMethod != null) {
            try {
                onConnectedMethod.invoke(parent, new PushDevice(device));
            } catch (Exception e) {
                onConnectedMethod = null;
            }
        }
    }

    @Override
    public void onPushDisconnected(Device device) {
        if (onDisconnectedMethod != null) {
            try {
                onDisconnectedMethod.invoke(parent, new PushDevice(device));
            } catch (Exception e) {
                onDisconnectedMethod = null;
            }
        }
    }
}
