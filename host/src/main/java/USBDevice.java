import java.util.Arrays;
import java.io.IOException;
import javax.usb.*;


public class USBDevice implements AutoCloseable {

    private static final byte INTERFACE_NUMBER      = (byte)0x02;
    private static final byte ENDPOINT_IN_ADDRESS   = (byte)0x84;
    private static final byte ENDPOINT_OUT_ADDRESS  = (byte)0x05;
    private static final int  PACKET_LENGTH         = 0x40;

    private static final UsbInterfacePolicy FORCE_CLAIM = new UsbInterfacePolicy() {
            public boolean forceClaim(UsbInterface usbInterface) { return true; }
        };


    private final UsbDevice device;
    private final UsbPipe pipeIn;
    private final UsbPipe pipeOut;

    private final UsbInterface iface;


    public void write(String data) throws IOException {
        byte[] bytes = data.getBytes();

        if (bytes.length > PACKET_LENGTH) {
            throw new IOException("data string too long.");
        }

        byte[] packet = Arrays.copyOf(bytes, PACKET_LENGTH);

        try {
            UsbIrp irp = pipeOut.createUsbIrp();
            irp.setData(packet);
            pipeOut.syncSubmit(irp);
        } catch (UsbException e) {
            throw new IOException(e);
        }
    }

    public String read() throws IOException {

        try {
            byte[] packet = new byte[64];
            UsbIrp irp = pipeIn.createUsbIrp();
            irp.setData(packet);
            pipeIn.syncSubmit(irp);
            return new String(packet);
        } catch (UsbException e) {
            throw new IOException(e);
        }
    }


    USBDevice(UsbDevice device) throws IOException {

        this.device = device;

        UsbConfiguration configuration = device.getActiveUsbConfiguration();
        iface = configuration.getUsbInterface(INTERFACE_NUMBER);

        try {
            iface.claim(FORCE_CLAIM);

            UsbEndpoint endpointIn = iface.getUsbEndpoint(ENDPOINT_IN_ADDRESS);
            UsbEndpoint endpointOut = iface.getUsbEndpoint(ENDPOINT_OUT_ADDRESS);

            pipeIn = endpointIn.getUsbPipe();
            pipeIn.open();

            pipeOut = endpointOut.getUsbPipe();
            pipeOut.open();

        } catch (UsbException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            iface.release();
        } catch (UsbException e) {
            throw new IOException(e);
        }
    }
}
