#ifndef HID_device_
#define HID_device_

#include "HID.h"

class HID_device : public HID_ {

public:

    HID_device(const char *name) : name(name) {
    }

    int endpoint() {
        return pluggedEndpoint;
    }

    int available() {
        return USBD_Available(pluggedEndpoint + 1);
    }

    int write(const void *data) {
        return USBD_Send(pluggedEndpoint, data, 64);
    }

    int read(void *data) {
        return USBD_Recv(pluggedEndpoint + 1, data, 64);
    }

protected:

    uint8_t getShortName(char* name) {
        int len = strlen(this->name);
        for (int i = 0; i < len; i++) {
            name[i] = this->name[i];
        }
        return len;
    }

private:
    const char *name;
};

#endif
