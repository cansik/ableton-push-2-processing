package ch.bildspur.push.processing;

import ch.bildspur.push.PushContext;
import ch.bildspur.push.PushDevice;
import ch.bildspur.push.PushEventListener;
import org.usb4java.Device;
import processing.core.PApplet;

import java.lang.reflect.Method;

public class PImagePushContext extends PushContext implements PushEventListener {

    private PApplet parent;

    private Method onConnectedMethod;

    private Method onDisconnectedMethod;

    public PImagePushContext(PApplet parent) {
        super();
        this.parent = parent;

        pushEvent.addListener(this);

        // register processing events
        parent.registerMethod("dispose", this);

        try {
            onConnectedMethod = parent.getClass().getMethod("onPushConnected", PushDevice.class);
            onDisconnectedMethod = parent.getClass().getMethod("onPushDisconnected", PushDevice.class);
        } catch (Exception e) {
        }
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
