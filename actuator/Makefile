CONTIKI_PROJECT = actuator
all: $(CONTIKI:PROJECT)

PLATFORMS_EXCLUDE = sky z1

MODULES_REL += ./resources

CONTIKI=../..

include $(CONTIKI)/Makefile.dir-variables
MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/coap

include $(CONTIKI)/Makefile.include
