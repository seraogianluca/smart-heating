#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"

// Log configuration 
#include "sys/log.h"
#define LOG_MODULE "Humidity"
#define LOG_LEVEL LOG_LEVEL_DBG

process_event_t RANGE_CHANGED;
extern struct process node;

static unsigned int accept = -1;
static int min_hum = 27;
static int max_hum = 30;
static int hum = 0;

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

// Resource definition
EVENT_RESOURCE(res_hum,
         "title=\"Humidity sensor\";rt=\"hum\";if=\"sensor\";obs",
         res_get_handler,
         res_post_put_handler,
         res_post_put_handler,
         NULL,
         res_event_handler);

static void res_event_handler(void) {
    LOG_INFO("Measuring...\n");
    hum = (rand() % (max_hum - min_hum + 1)) + min_hum;
    LOG_DBG("Humidity: %d\n", hum);
    coap_notify_observers(&res_hum);
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

static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    int len = 0;

    if(request != NULL) {
        LOG_DBG("Post received.\n");
    }

    unsigned int post_accept = APPLICATION_JSON;
    coap_get_header_accept(request, &post_accept);

    if(post_accept == APPLICATION_JSON) {
        const uint8_t **msg;
        char *delta;
        int payload_len = request->payload_len;

        LOG_DBG("The payload len is: %d\n", payload_len);

        msg = malloc(request->payload_len);
        if(msg == NULL) {
            LOG_DBG("Error: no memory allocated for the payload.\n");
            return;
        }

        len = coap_get_payload(request, msg);
        LOG_DBG("Message received: %s.\n", (char *)*msg);

        if(len > 0) {
            // {"delta":"-2"}
            size_t size = 0;
            const char* start = (char *)*msg + 10;
            const char* end = (char *)*msg + payload_len - 2;
            size = end - start;

            if(size == 0) {
                LOG_DBG("Size equal to 0.\n");
                return;
            } else {
                delta = malloc(size);
                strncpy(delta, start, size);
                delta[size] = '\0';
            }

            min_hum += atoi(delta);
            max_hum += atoi(delta);

            LOG_DBG("Range changed: min %d, max %d.\n", min_hum, max_hum);

            coap_set_status_code(response, CHANGED_2_04);
            process_post(&node, RANGE_CHANGED, NULL);
            free(msg);
        } else {
            coap_set_status_code(response, BAD_REQUEST_4_00);
        }
    } else {
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
	    const char *msg = "Supported content-types:application/json";
	    coap_set_payload(response, msg, strlen(msg));
    }
}
