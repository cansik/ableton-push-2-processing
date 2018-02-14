package ch.bildspur.push;

import org.usb4java.Device;

public interface PushEventListener {
    void onPushConnected(Device device);

    void onPushDisconnected(Device device);
}
