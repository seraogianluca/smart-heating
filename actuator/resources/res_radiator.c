#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "os/dev/leds.h"

// Log configuration 
#include "sys/log.h"
#define LOG_MODULE "Radiator"
#define LOG_LEVEL LOG_LEVEL_DBG

process_event_t STATUS_CHANGED;
extern struct process node;

static unsigned int accept = -1;
static char status[4] = "off"; 

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

// Resource definition
EVENT_RESOURCE(res_radiator,
         "title=\"Radiator actuator\";rt=\"radiator\";if=\"actuator\";obs",
         res_get_handler,
         res_post_put_handler,
         res_post_put_handler,
         NULL,
         res_event_handler);

static void res_event_handler(void) {
    LOG_INFO("Get status...\n");
    coap_notify_observers(&res_radiator);
}

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    int length;
    char msg[COAP_MAX_CHUNK_SIZE];

    if(request != NULL) {
        LOG_DBG("Get received.\n");
    }
 
    coap_get_header_accept(request, &accept);

    if(accept == APPLICATION_JSON) {   
        LOG_INFO("Composing message...\n");
        length = snprintf(msg, COAP_MAX_CHUNK_SIZE, "{\"status\": \"%s\"}", status);
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

static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    size_t len = 0;
    const char *new_status = NULL;

    if(request != NULL) {
        LOG_DBG("Post received.\n");
    }

    len = coap_get_post_variable(request, "status", &new_status);

    if(len > 0) {
        LOG_DBG("Actual stauts: %s, New status: %s\n", status, new_status);
        strcpy(status, new_status);

        if(strncmp(status, "off", len) == 0) {  
            leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
        } else if(strncmp(status, "on", len) == 0) {
            leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
        } else if(strncmp(status, "max", len) == 0) {
            leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
        }

        coap_set_status_code(response, CHANGED_2_04);
        process_post(&node, STATUS_CHANGED, NULL);
    } else {
        coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}