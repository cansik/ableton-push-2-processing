package ch.bildspur.push.processing;

import ch.bildspur.push.PushDevice;
import ch.bildspur.push.buffer.PGraphicsBuffer;
import org.usb4java.Device;
import processing.core.PApplet;
import processing.core.PGraphics;

public class ProcessingPushDevice extends PushDevice {
    private PApplet parent;

    private PGraphicsBuffer graphicsBuffer;

    public ProcessingPushDevice(PApplet parent, Device device) {
        super(device);

        this.parent = parent;
    }

    public ProcessingPushDevice(PApplet parent, PushDevice device)
    {
        this(parent, device.getDevice());
    }

    public boolean open()
    {
        return super.open(graphicsBuffer);
    }

    public PGraphics getPGraphics()
    {
        return graphicsBuffer.getGraphics();
    }
}
