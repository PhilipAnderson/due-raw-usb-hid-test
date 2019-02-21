#include <Arduino.h>

#include "HID/HID_device.h"

HID_device hid("hid-echo-device");

void setup() {
    hid.begin();
}

void loop() {

    static uint8_t buffer[64] = {};

    if (hid.available()) {
        hid.read(buffer);
        // echo
        hid.write(buffer);
    }

    delay(1);
}
