package ch.bildspur.push.wayang;

import org.usb4java.*;

import java.util.ArrayList;
import java.util.List;

public class PushCallback implements HotplugCallback {

    private List<PushEventListener> pushEventListeners = new ArrayList<>();

    public void addListener(PushEventListener listener)
    {
        pushEventListeners.add(listener);
    }

    private void invokeConnected(Device device)
    {
        for(PushEventListener listener : pushEventListeners)
            listener.onPushConnected(device);
    }

    private void invokeDisconnected(Device device)
    {
        for(PushEventListener listener : pushEventListeners)
            listener.onPushDisconnected(device);
    }

    @Override
    public int processEvent(Context context, Device device, int event, Object userData) {
        DeviceDescriptor descriptor = new DeviceDescriptor();
        int result = LibUsb.getDeviceDescriptor(device, descriptor);
        if (result != LibUsb.SUCCESS)
            throw new LibUsbException("Unable to read device descriptor",
                    result);

        if (descriptor.bDeviceClass() == LibUsb.CLASS_PER_INTERFACE
                && descriptor.idVendor() == Wayang.VENDOR_ID
                && descriptor.idProduct() == Wayang.PRODUCT_ID) {

            if(event == LibUsb.HOTPLUG_EVENT_DEVICE_ARRIVED)
                invokeConnected(device);
            else
                invokeDisconnected(device);
        }

        return 0;
    }
}
