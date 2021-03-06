CONTIKI_PROJECT = temperature_sensor
all: $(CONTIKI:PROJECT)

PLATFORMS_EXCLUDE = sky z1

MODULES_REL += ./resources

CONTIKI=../..

include $(CONTIKI)/Makefile.dir-variables
MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/coap

include $(CONTIKI)/Makefile.include
