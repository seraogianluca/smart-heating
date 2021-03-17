#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"

// Log configuration 
#include "sys/log.h"
#define LOG_MODULE "Temperature"
#define LOG_LEVEL LOG_LEVEL_DBG

#define MIN_TEMP 17
#define MAX_TEMP 19

static unsigned int accept = -1;
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

static void res_event_handler(void) {
    LOG_INFO("Measuring...\n");
    temp = (rand() % (MAX_TEMP - MIN_TEMP + 1)) + MIN_TEMP;
    LOG_DBG("Temperature: %d\n", temp);
    coap_notify_observers(&res_temp);
}

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    int length;
    char msg[COAP_MAX_CHUNK_SIZE];

    if(request != NULL) {
        LOG_DBG("Request received.\n");
    }
 
    coap_get_header_accept(request, &accept);

    if(accept == APPLICATION_JSON) {   
        LOG_INFO("Composing message...\n");
        length = snprintf(msg, COAP_MAX_CHUNK_SIZE, "{\"temp\": \"%d\"}", temp);
        LOG_INFO("Message: %s\n", msg);

        if(length <= 0) {
            LOG_DBG("Message not composed.\n");
        } else {
            memcpy(buffer, (uint8_t *)msg, length);
            coap_set_header_content_format(response, APPLICATION_JSON);
            coap_set_payload(response, buffer, length);
        }
    } else {
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
	    const char *msg = "Supported content-types:application/json";
	    coap_set_payload(response, msg, strlen(msg));
    }
}