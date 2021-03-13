#include "contiki.h"
#include "coap-engine.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

// Log configuration 
#include "sys/log.h"
#define LOG_MODULE "Humidity"
#define LOG_LEVEL LOG_LEVEL_DBG

#define MIN_HUM 40
#define MAX_HUM 45

static unsigned int accept = -1;
static int hum = 0;

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

// Resource definition
EVENT_RESOURCE(res_hum,
         "title=\"Humidity sensor\";rt=\"hum\";if=\"sensor\";obs",
         res_get_handler,
         NULL,
         NULL,
         NULL,
         res_event_handler);

static void res_event_handler(void) {
    LOG_INFO("Measuring...\n");
    hum = (rand() % (MAX_HUM - MIN_HUM + 1)) + MIN_HUM;
    LOG_DBG("Humidity: %d\n", hum);
    coap_notify_observers(&res_hum);
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
        length = snprintf(msg, COAP_MAX_CHUNK_SIZE, "{\"hum\": \"%d\"}", hum);
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