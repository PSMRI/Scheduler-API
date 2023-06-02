# Scheduler-API
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)  ![branch parameter](https://github.com/PSMRI/HWC-API/actions/workflows/sast-and-package.yml/badge.svg)

It acts as an interface between client and the scheduling services provided, allowing users to interact for consultation with specialists. It also provides the info of availability and unavailability of specialists, retrieving available slots for specialists, booking and cancelling slots, and fetching day views of specialists for a particular specialization.

### Features
* Handles various requests for scheduling/booking/cancelling slots
* Provides slots availability
* provides specialists availability of any day

## Building From Source
This microservice is built on Java, Spring boot framework and MySQL DB.

### Prerequisites 
* JDK 1.8
* Maven 

$ ./mvn clean install

## Installation
This service has been tested on Wildfly as the application server.

### Prerequisites 
* Wildfly (or any compatible app server)
* Redis
* MySQL Database

## Usage
All features have been exposed as REST endpoints. Refer to the SWAGGER API specification for details.
