#include "contiki.h"
#include "coap-engine.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define MIN_TEMP 0
#define MAX_TEMP 50

static char room[15];
static char temp_s[3];
static int temp = 0;

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

static void
update_temp() {
    strcpy(room, "Example room");
    temp = (rand() % (MAX_TEMP - MIN_TEMP + 1)) + MIN_TEMP;
    sprintf(temp_s, "%d", temp);
    printf("Temperature: %s", temp_s);
}

EVENT_RESOURCE(res_temp,
         "title=\"temp\";rt=\"temperature\";if=\"sensor\";obs",
         res_get_handler,
         NULL,
         NULL,
         NULL,
         res_event_handler);

static void
res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    int length;
    char msg[50];

    strcpy(msg, "{\"Room\": \"");
    strcat(msg, room);
    strcat(msg, "\", \"Temperature\": \"");
    strcat(msg, temp_s);
    strcat(msg, "\"}\n");

    length = sizeof(msg);
    memcpy(buffer, (uint8_t *)msg, length-1);

    coap_set_header_content_format(response, APPLICATION_JSON);
    coap_set_payload(response, (uint8_t *)buffer, length);
}

static void
res_event_handler(void) {
    update_temp();
    coap_notify_observers(&res_temp);
}
