package ch.bildspur.push;

import ch.bildspur.push.wayang.PushEventListener;
import ch.bildspur.push.wayang.Wayang;
import org.usb4java.Device;
import processing.core.PApplet;
import processing.core.PConstants;

import java.awt.image.BufferedImage;

public class PushDevice implements PConstants, PushEventListener {

    private PApplet parent;
    private BufferedImage screenBuffer;

    private boolean isOpen = false;

    public PushDevice(PApplet parent)
    {
        this.parent = parent;

        // add event listeners
        Wayang.pushEvents.addListener(this);
        parent.registerMethod("stop", this);

        Wayang.initLibUsb();
        Wayang.startEventThread();
    }

    public boolean open()
    {
        // check if device is already open
        if(isOpen)
            return false;

        // check if device is connected
        if(!Wayang.isPushAvailable())
            return false;

        // open device
        screenBuffer = Wayang.open();
        isOpen = true;

        return true;
    }

    public void close()
    {
        if(!isOpen)
            return;

        Wayang.close();
        System.out.println("closed push 2 device");
    }

    public void stop()
    {
        close();
        Wayang.stopEventThread();
        Wayang.exitLibUsb();
    }

    @Override
    public void onPushConnected(Device device) {
        System.out.println("push connected!");
    }

    @Override
    public void onPushDisconnected(Device device) {
        System.out.println("push disconnected!");
    }
}
