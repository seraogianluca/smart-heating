#include <stdio.h>
#include <stdlib.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "sys/etimer.h"
#include "os/dev/leds.h"

// Log configuration 
#include "coap-log.h"
#include "sys/log.h"
#define LOG_MODULE "Node"
#define LOG_LEVEL LOG_LEVEL_DBG

#define SERVER_EP "coap://[fd00::1]:5683"

extern process_event_t RANGE_CHANGED;
extern process_event_t STATUS_CHANGED;

// Resources
extern coap_resource_t res_temp, res_hum, res_radiator;

static struct etimer timer;
char *reg_service_url = "/register";
static bool registered = false;

bool auto_mode = false;
int temp_threshold = 15;
extern int temp;
extern int min_temp;
extern int max_temp;
extern int min_hum;
extern int max_hum;
extern char status[4];

PROCESS(node, "Node");
AUTOSTART_PROCESSES(&node);

void interval_update(bool increment) {
	int delta = rand() % 8 + 1;
	int hum_delta = rand() % 3 + 1;

	if(increment) {
		min_temp += delta;
		max_temp += delta;

		min_hum -= hum_delta;
		max_hum -= hum_delta;
	} else {
		min_temp -= delta;
		max_temp -= delta;

		min_hum += hum_delta;
		max_hum += hum_delta;
	}
}

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
    coap_activate_resource(&res_hum, "hum");
    coap_activate_resource(&res_radiator, "radiator");

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
    
    //set timer for sensors
    etimer_set(&timer, CLOCK_SECOND * 5);

    //set initial radiator status
    leds_set(LEDS_NUM_TO_MASK(LEDS_RED));


    while(1) {
        PROCESS_WAIT_EVENT_UNTIL(ev == PROCESS_EVENT_TIMER || ev == RANGE_CHANGED || ev == STATUS_CHANGED);
        if(auto_mode) {
        	//change status based on temp
        	if(temp_threshold > temp) {
        		if((temp_threshold - temp) > 5) {
        			strcpy(status, "max\0");
        			leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
        		} else {
        			strcpy(status, "on\0");
        			leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
       			}
        		interval_update(true);
        	} else if (temp_threshold <= temp) {
        		strcpy(status, "off\0");
        		leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
        		interval_update(false);
        	}
        }

        res_radiator.trigger();

        if(ev == PROCESS_EVENT_TIMER || ev == RANGE_CHANGED) {
        	res_temp.trigger();
        	res_hum.trigger();
        	etimer_reset(&timer);
        }
    }  

    PROCESS_END();
}
