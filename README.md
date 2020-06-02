# Netty Load Balancer

## 1. Description

Initial implementation of tcp load balancer using Netty.
This repository contains the initial working version of a project under development.
This repository is just for my memo, in a production environment one should obviously use existing solutions (e.g.: HAProxy), .


## 2. Build

The gradle [`shadowJar`](https://imperceptiblethoughts.com/shadow/) plugin is used for building an executable "fat-jar" .

Run the following command in the project root directory.
````bash
./gradlew clean build
````

The "fat-jar" will be created as `/build/libs/nettyLoadBalancer-<version>-all.jar`.


## 3. Starting the Load Balancer

Copy and update the `config.yml` from the project `src/main/resources` folder.

The general format of the config file is:
```yaml
localPort: 8080
backendServers:
  - hostname: localhost
    port: 4040
  - hostname: localhost
    port: 4041
```

The confiuration file should include one backend parameter entry for each backend server.

To start the load balancer run the below command.
````bash
java -jar nettyLoadBalancer-<version>-all.jar -c <config file>
````
Where the [config file] is the full path or a relative path (to where you execute the jar) of the configuration file.  
e.g.:
````bash
java -jar nettyLoadBalancer-<version>-all.jar -c config.yml
````


## 4. Quick Test

The load balancer can be tested with the following steps:
1. Start multiple multi connection backend servers.
2. Start the load balancer as described in Section  [3.](README.md#3.Starting-the-Load-Balancer) .
3. Start and send messages from multiple clients to the backend servers.

If the load balancer is working as expected, the client will receive responses from a backend server.
The tools used during development tests are described in the below sub-sections.

### 4.1 Backend Servers

The backend servers should be able to accept simultaneous connections (so, for example, netcat can't be used).
Multiple [simple echo server](https://github.com/wdmssk/netty-echo) were used during development tests.
To start a backend server (with host names and ports listed in config.yml) run the below command.
````bash
java -jar netty-echo-<version>-all.jar -p  <port>
````

### 4.2 Clients

Development tests were conducted running multiple instances of netcat started from multiple terminals.
To start a netcat client, run:

````bash
nc <load-balancer-ip-address> <load-balancer-port>
````

After connecting to the load balancer, enter messages and press ENTER.
If the load balancer is working as expected, the client terminal will display the message echoes.


### 5. Architecture

The load balancer uses the round-robin algorithm to distribute incoming channels to backend servers.
Currently, this is implemented "using" the Netty EventLoopGroup logic that allocates EventLoop's to newly created channels.

In the current implementation, one EventLoop is created associated with each backend server.
One Bootstrap instance is also created for each EventLoop and parameters of the associated backend server.
When a new inbound channel is created, the correspondent outbound channel is instantiated with the Bootstrap associated with the inbound EventLoop.
Netty allocates new inbound channels to EventLoop's in a round-robin way, so this results in round-robin distribution of inbound channels to the backend servers.

In this approach, the outbound and inboud channels for a client/backend connection use the same EventLoop (thread).
This means that all the events and tasks related to a backend are handled by the same EventLoop (thread).

The achitectue of this initial development is simplistic, and needs to be improved.
One clear flaw is that it doesn't scale to handle a large number of backend servers.
Another major flaw is that it doesn't allow the use of different types of load balancing algorithms.