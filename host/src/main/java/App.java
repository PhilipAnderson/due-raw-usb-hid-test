import java.io.IOException;


public class App {

    public static void main(String[] args) throws IOException {

        try (USBDevice device = USBDeviceFinder.bySerialNumberString("hid-echo-device")) {

            if (device == null) {
                System.out.println("device not found");
                return;
            }

            String data = "1234567812345678123456781234567812345678123456781234567812345678";
            System.out.println("writing: " + data);

            device.write(data);

            System.out.println("reading ...");
            String received = device.read();
            System.out.println("read: " + received);

        } catch (IOException e) {
            System.out.println(e);
            System.out.flush();
        }
    }
}
