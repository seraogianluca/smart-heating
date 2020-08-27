#include "contiki.h"
#include "coap-engine.h"
#include "sys/etimer.h"
#include <stdio.h>
#include <stdlib.h>

PROCESS(temperature, "Temperature sensor");
AUTOSTART_PROCESSES(&temperature);

extern coap_resource_t res_temp;
static struct etimer e_timer;

PROCESS_THREAD(temperature, ev, data) {
    PROCESS_BEGIN();
    PROCESS_PAUSE();
    
    coap_activate_resource(&res_temp, "temp");
    etimer_set(&e_timer, CLOCK_SECOND * 4);
    
    while(1) {
        PROCESS_WAIT_EVENT();
        if(ev == PROCESS_EVENT_TIMER && data == &e_timer){
		printf("Trigger and event\n");	
		res_temp.trigger();
		etimer_set(&e_timer, CLOCK_SECOND * 4);
	    }
    }                             

    PROCESS_END();
}
