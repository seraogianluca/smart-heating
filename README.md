# Introduction
The project aims to develop a complete IoT system referring to the home automation use case. The system has sensors to collect data and actuators for remote control, and they expose their functionalities through the CoAP protocol. Moreover, the system has a cloud application that interacts with the sensors/actuators to offer certain functionalities. IoT devices register to the cloud application at bootstrap. The cloud application has a command-line interface to show the collected data and allows the user to change the status of actuators. The cloud application is developed using [Californium](https://github.com/eclipse/californium). The IoT devices are implemented exploiting the [Contiki-NG](https://github.com/contiki-ng/contiki-ng) operating system.

# Quickstart
It is preferable to clone this repository into the Contiki-NG installation folder. To run the project open the `smart-heating.csc` simulation into cooja, compile and create all the motes and start the simulation. Connect the device network with the external network by executing the following command inside the `rpl-border-router` folder:
```
make TARGET=cooja connect-router-cooja
```
Compile and run the java application as you prefer.

# Documentation
A detailed documentation of the project is available in the wiki.
