#include <stdio.h>
#include <stdlib.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "os/dev/leds.h"

// Log configuration 
#include "coap-log.h"
#include "sys/log.h"
#define LOG_MODULE "Actuator"
#define LOG_LEVEL LOG_LEVEL_DBG

#define SERVER_EP "coap://[fd00::1]:5683"

// Resources
#if RADIATOR
extern coap_resource_t res_radiator;
#else
extern coap_resource_t res_dehumidifier;
#endif

extern process_event_t STATUS_CHANGED;

char *reg_service_url = "/register";
bool registered = false;

PROCESS(node, "Node");
AUTOSTART_PROCESSES(&node);

void client_chunk_handler(coap_message_t *response) {
    const uint8_t *chunk;

    if(response == NULL) {
        LOG_INFO("Request timed out\n");
        return;
    }

    if(!registered) {
        registered = true;
    }      

    int len = coap_get_payload(response, &chunk);

    LOG_INFO("|%.*s", len, (char *)chunk);
}

PROCESS_THREAD(node, ev, data) {
    static coap_endpoint_t server_ep;
    static coap_message_t request[1];

    PROCESS_BEGIN();

    // Resources activation
    #if RADIATOR
    coap_activate_resource(&res_radiator, "radiator");
    #else
    coap_activate_resource(&res_dehumidifier, "dehumidifier");
    #endif

    // Registration
    LOG_INFO("Registering...\n");
    coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
    coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
    coap_set_header_uri_path(request, reg_service_url);

    while(!registered) {
        LOG_DBG("Registration retry...\n");
        COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
    }

    LOG_INFO("Registered.\n");
    
    // Set initial status to off
    leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
    
    while(1) {
        PROCESS_WAIT_EVENT_UNTIL(ev == STATUS_CHANGED);
        
        #if RADIATOR
        res_radiator.trigger();
        #else
        res_dehumidifier.trigger();
        #endif
    }  

    PROCESS_END();
}