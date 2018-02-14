package ch.bildspur.push.processing;

import ch.bildspur.push.PushDevice;
import org.usb4java.Device;
import processing.core.PApplet;

public class PImagePushDevice extends PushDevice {
    private PApplet parent;

    public PImagePushDevice(PApplet parent, Device device) {
        super(device);

        this.parent = parent;
    }
}
