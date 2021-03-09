#include <stdio.h>
#include <stdlib.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "sys/etimer.h"

// Log configuration 
#include "coap-log.h"
#include "sys/log.h"
#define LOG_MODULE "Sensor"
#define LOG_LEVEL LOG_LEVEL_DBG

#define SERVER_EP "coap://[fd00::1]:5683"

// Resources
extern coap_resource_t res_temp;

static struct etimer e_timer;
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
    coap_activate_resource(&res_temp, "temp");
    etimer_set(&e_timer, CLOCK_SECOND * 4);

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

    while(1) {
        PROCESS_WAIT_EVENT();

        if(ev == PROCESS_EVENT_TIMER && data == &e_timer) {
            LOG_INFO("Triggering an event.");
		    res_temp.trigger();
		    etimer_set(&e_timer, CLOCK_SECOND * 4);
	    }
    }  

    PROCESS_END();
}