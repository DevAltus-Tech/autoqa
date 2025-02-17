# Project Overview

## Project Description

This project is a Java-based application that uses Maven for build automation. It includes several modules, each with specific functionality. Below is an overview of the modules, how to build the
project, and what each module does.  
Modules


### 0. project

main module - contains the multi-module Maven project configuration, docker-compose, and this is where reports are produced. Additionally, this folder contains the ActiveMQ broker and configuration
allowing you to divorce running that from the tests.

#### jms-monitor
*Description:* This module listens to JMS management events, and provides a framework for working with them (via callbacks).


### 1. validator

*Description:* This module is responsible for validating video files. It includes functionality to read logs from Docker containers and validate the content.
*Key Classes:*

- `DockerLogsReader`: Reads logs from Docker containers.
- `DockerLogsReaderTest`: Unit tests for DockerLogsReader.

### 2. heartbeat

*Description:* This module handles the heartbeat functionality to ensure the application is running and responsive. It uses JMS (Java Message Service) to send and receive heartbeat messages.
*Key Classes:*

- `HearbeatConfig`: Configuration class for setting up JMS connections and heartbeat topics.
- `ArtemisNotificationsListener`: Listens for notifications from the Artemis message broker.


- ArtemisNotificationsListener: Listens for notifications from the Artemis message broker and processes them.


### 4. order-generator
*Description:* This module is responsible for generating orders and sending them to the appropriate JMS topics. It receives messages from the heartbeat modules and publishes messages to a order-queue. It also receives acks from the `order-client`

*Key Classes:*
- `OrderGenerator`: Main class responsible for generating and sending orders.
- `OrderGeneratorConfig`: Configuration class for setting up JMS connections and order topics.




## How to Build

To build the project, you need to have Maven installed. Follow the steps below to build the project:  

```bash 
cd your-repo/projectx1g
mvn clean install
```  

This command will compile the source code in each of the projects (including tests) and package them into JAR files. The JAR files will be placed in the `target` directory of each module.


### Running the Application
To run the application, you can use the following command (from the `project` directory):  
```bash
docker compose up --build
```


# Configuration
All projects have a `src/main/resources` directory that contains configuration files.
the `application.yaml` file contains the configuration for the application. You can modify this file to change the application's behavior.

## Validation
This section contains configuration options related to the testing framework.
x1
Example configuration:
```bash
tests:
  termination:
    timeout: 40000
    shutdown-on-success: true
  jms-connect:
    - order-generator
    - heartbeat
  log-validation:
    heartbeat:
      - "HEARTBEAT .* Sent heartbeat message.*"
    orders:
      - ".*ORDERS .* Received event: CONSUMER_CREATED for name: heartbeat.*"
      - ".*ORDERS .* Heartbeat sequence.*"

```
### Explanation

*termination*: Specifies the conditions under which the tests should be terminated.

- **timeout:** Specifies the maximum time (in milliseconds) to wait before forcibly terminating the tests. In this case, it is set to 40000 milliseconds (40 seconds).

- **shutdown-on-success:** If set to true, the application will shut down automatically after the tests complete successfully.


*jms-connect*: A list of services that need to establish JMS (Java Message Service) connections. In this case, the order-generator and heartbeat services are specified.

*log-validation*: This section contains patterns for validating log messages.  

- **heartbeat:** Contains a list of regex patterns to validate log messages related to the heartbeat service.  
- **orders:** Contains a list of regex patterns to validate log messages related to the orders service.


## Copyright (c) 2025 DevAltus, LLC

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are not permitted.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, and display the Software, provided that the following conditions are met:

1. The Software is used for personal and non-commercial purposes only.
2. Copies or substantial portions of the Software may not be modified or altered in any way.
3. This notice and the permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
