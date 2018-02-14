package ch.bildspur.push;

import org.usb4java.Device;
import processing.core.PApplet;
import processing.core.PConstants;

import java.awt.image.BufferedImage;
import java.util.Objects;

public class PushDevice implements PConstants {

    private PApplet parent;
    private BufferedImage screenBuffer;

    private boolean isOpen = false;

    private Device device;

    public PushDevice(PApplet parent, Device device)
    {
        this.parent = parent;
        this.device = device;
    }

    public boolean open()
    {
        return true;
    }

    public void close()
    {

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
}
