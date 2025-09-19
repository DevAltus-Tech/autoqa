# Overview

## Automated Evidence Collection + Testing

This java based project was built specifically to showcase automated testing & evidence collection. This non-intrusive method does not require extensive rewrites to any projects you have.
It is designed to work with existing projects, and can be adapted to a variety of scenarios.
The framework is designed to be flexible and extensible, allowing you to easily add new tests and validation rules as needed. The goal is to provide a robust and reliable testing framework that can be used to ensure the quality and reliability of your applications.

Evidence collection is a critical aspect of software testing, as it provides a way to verify that the application is functioning as expected. This framework collects evidence in the form of logs that can be used to validate the application's behavior. It then generates a report indicating the disposoition of each test which can be attached to relese notes. 

This is strong enough to satisfy **_regulatory requirements_**, and flexible enough to be used in agile environments. It works in CI/CD pipelines, and can be run locally by developers.

## The Project
Contains the multi-module Maven project configuration, docker-compose, and this is where reports are produced. Additionally, this folder contains the ActiveMQ broker and configuration
allowing you to divorce running that from the tests.


The purpose of the subprojects is to demonstrate a project of sufficient complexity to showcase the testing framework. Each subproject has a specific role, and they work together to form a complete application. They are *representative* of reality, and should not be considered actual project.
![Project Structure](docs/architecture.png)



# Tests
Example configuration:

```yaml
tests:
  termination:
    timeout: 15000
  jms-connect:
    - order-generator
    - order-client
    - heartbeat
  log-validation:
    heartbeat:
      - "HEARTBEAT .* Sent heartbeat message.*"
    orders:
      - ".*ORDERS .* Received event: CONSUMER_CREATED for name: heartbeat.*"
      - ".*ORDERS .* Heartbeat sequence.*"

```
## Explanation
### JMS Connect
This section specifies the services that need to establish JMS (Java Message Service) connections. In this case, the order-generator and heartbeat services are specified. This means that the testing framework will validate that these services connected by inspecting management messages from Apache-Artemis. This is not log validation, but completely transparent connection validation.

### Log Validation
This section contains patterns for validating log messages. The framework will inspect the logs of the specified services and look for messages that match the provided regex patterns. If a message matches a pattern, it is considered a successful validation. This allows for a fair amount of flexibility, as the exact content of the log messages may vary, but the overall structure and key information should remain consistent.



# Module Descriptions

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
