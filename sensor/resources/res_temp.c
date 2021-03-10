#include "contiki.h"
#include "coap-engine.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

// Log configuration 
#include "sys/log.h"
#define LOG_MODULE "Temperature"
#define LOG_LEVEL LOG_LEVEL_DBG

#define MIN_TEMP 17
#define MAX_TEMP 19

static char room[15];
static char temp_s[3];
static int temp = 0;

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

// Resource definition
EVENT_RESOURCE(res_temp,
         "title=\"Temperature sensor\";rt=\"temp\";if=\"sensor\";obs",
         res_get_handler,
         NULL,
         NULL,
         NULL,
         res_event_handler);

static void update_temp() {
    strcpy(room, "Example room");
    temp = (rand() % (MAX_TEMP - MIN_TEMP + 1)) + MIN_TEMP;
    sprintf(temp_s, "%d", temp);
    LOG_DBG("Room: %s \t Temperature: %s\n", room, temp_s);
}

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    int length;
    char msg[COAP_MAX_CHUNK_SIZE];

    if(request != NULL) {
        LOG_DBG("GET received.\n");
    }

    unsigned int accept = APPLICATION_JSON;
    coap_get_header_accept(request, &accept);

    if(accept == APPLICATION_JSON) {
        coap_set_header_content_format(response, APPLICATION_JSON);

        LOG_INFO("Composing message...\n");
        strcpy(msg, "{\"room\": \"");
        strcat(msg, room);
        strcat(msg, "\", \"temp\": \"");
        strcat(msg, temp_s);
        strcat(msg, "\"}\n");

        LOG_INFO("Message: %s\n", msg);

        length = sizeof(msg);

        if(length > COAP_MAX_CHUNK_SIZE) {
            LOG_DBG("Error: Max size = %d, actual size = %d", COAP_MAX_CHUNK_SIZE, length);
        } else {
            memcpy(buffer, (uint8_t *)msg, length-1);
            coap_set_payload(response, (uint8_t *)buffer, length);
        }

    } else {
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
	    const char *msg = "Supporting content-types:application/json";
	    coap_set_payload(response, msg, strlen(msg));
    }
}

static void res_event_handler(void) {
    LOG_INFO("Measuring...\n");
    update_temp();
    coap_notify_observers(&res_temp);
}
