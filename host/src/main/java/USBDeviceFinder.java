import java.util.List;
import java.io.IOException;
import javax.usb.*;
import javax.usb.event.*;


public class USBDeviceFinder {

    private static void dumpDevice(final UsbDevice device) {
        // Dump information about the device itself
        System.out.println(device);
        final UsbPort port = device.getParentUsbPort();
        if (port != null) {
            System.out.println("Connected to port: " + port.getPortNumber());
            System.out.println("Parent: " + port.getUsbHub());
        }

        // Dump device descriptor
        System.out.println(device.getUsbDeviceDescriptor());

        // Process all configurations
        for (UsbConfiguration configuration: (List<UsbConfiguration>) device
                 .getUsbConfigurations()) {
            // Dump configuration descriptor
            System.out.println(configuration.getUsbConfigurationDescriptor());

            // Process all interfaces
            for (UsbInterface iface: (List<UsbInterface>) configuration
                     .getUsbInterfaces())
                {
                    // Dump the interface descriptor
                    System.out.println(iface.getUsbInterfaceDescriptor());

                    // Process all endpoints
                    for (UsbEndpoint endpoint: (List<UsbEndpoint>) iface
                             .getUsbEndpoints())
                        {
                            // Dump the endpoint descriptor
                            System.out.println(endpoint.getUsbEndpointDescriptor());
                        }
                }
        }

        System.out.println();

        // Dump child devices if device is a hub
        if (device.isUsbHub()) {
            final UsbHub hub = (UsbHub) device;
            for (UsbDevice child: (List<UsbDevice>) hub.getAttachedUsbDevices()) {
                dumpDevice(child);
            }
        }
    }

    @FunctionalInterface
    interface Predicate {
        boolean test(UsbDevice device) throws Exception;
    }

    public static USBDevice byVendorAndProductId(short vendorId, short productId) throws IOException {

        UsbDevice device = findImpl(getRootHub(), dev -> {
                    UsbDeviceDescriptor desc = dev.getUsbDeviceDescriptor();
                    return desc.idVendor() == vendorId && desc.idProduct() == productId;
                });

        return device == null ? null : new USBDevice(device);
    }

    public static USBDevice bySerialNumberString(String serial) throws IOException {

        UsbDevice device = findImpl(getRootHub(), dev -> {
                    return serial.equals(dev.getSerialNumberString());
                });

        return device == null ? null : new USBDevice(device);
    }

    private static UsbHub getRootHub() throws IOException {

        try {
            UsbServices services = UsbHostManager.getUsbServices();
            return services.getRootUsbHub();
        } catch (UsbException e) {
            throw new IOException(e);
        }
    }

    private static UsbDevice findImpl(UsbHub hub, Predicate predicate) throws IOException {

        try {

            @SuppressWarnings("unchecked")
                List<UsbDevice> devices = (List<UsbDevice>) hub.getAttachedUsbDevices();

            for (UsbDevice device : devices) {

                try {
                    if (predicate.test(device)) {
                        return device;
                    }
                } catch (UsbException e) {
                    // Unable to open/test device, probably permission issue.
                    // Continue searching.
                }

                if (device.isUsbHub() && (device = findImpl((UsbHub)device, predicate)) != null) {
                    return device;
                }
            }

            return null;

        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
