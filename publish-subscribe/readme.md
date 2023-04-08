# Publish-Subscribe Architecture

## Motivation
Apache Kafka is a very popular stream processing platform. It is built upon publisher-subscriber architecture and supports use-cases involving extremely fast event processing across different producers and consumers.

We'll try to build a publisher - subscriber platform similar to Apache Kafka.

## Background

We live in a world where a plethora of applications (service or group of services) work together to achieve a high level functionality (e.g., Paying money using credit card involves swipe machine to capture credit card details, processing of credit card information by owning authorities, validation by bank, etc.). A single service is usually built to provide a specific functionality. A service can be decomposed into packages, classes and functions. Thus the logic to perform a high-level function is spread across many individual units (or Components). These individual units handle smaller functionalities and communicate with each other to achieve the higher level functionality.

It should be understood that communication channels are not very robust and hence data exchange could fail at times. System should consider failure scenarios and be built with consideration on following:
- To retry communication
- To ensure data persistence until data is consumed by all required processes

There are many ways to exchange data across services (or application or components). However, there could be factors such as visibility of a method within a class, deployment across different physical system, frequency of data exchange or even amount of data to be exchanged playing an important role in deciding proper communication channel and method.

### Scenario

Let's evaluate some scenarios and communication approaches.

For sake of simplicity, let's assume that system can choose to either transfer data or metadata depending on required features (i.e., size of data, data persistence, etc.).

#### One to One communication
- Data needs to be transferred from a method to another method, which are part of same package and deployed on same server.
    - Data can be passed as paramter to new method
        - If system crashes during exchange, the data being transferred is lost.
    - Data is stored in a memory location and reference to this memory location is passed. It is useful when huge amount of data is to be transferred across methods e.g. Graph
        - If system crashes during exchange, the data stored at memory location is lost.
    - Data is stored in file or database and paramters to fetch details is passed to method
        - Data is persistent across system failures
- Data needs to be passed to a service, part of different deployment.
    - If both services exist on same physical host. An appropriate logic can be used to transfer data using RAM or disk exhibiting similar behaviour as in previous scenarios.
    - Data can be exchanged through network. There could be involvement of other hosts depending on location of deployment of both services.
        - Network calls can fail leading to failure in exchanging data
    - Data can be stored in database or some storage service and only metadata is communicated to service. This is helpful in scenarios where data requires persistence or data is processed by multiple services.

#### One to Many communication
- Data is produced by a single services and consumed by multiple services existing within same deployement on same host
    - Data producing class can store a reference to objects consuming this data (Observber design pattern). The producer object loops through consumers objects and invoke consume function and pass the reference data (or metadata in case data is stored at shared memory location or disk or datatabase)
- Data is produced by a single service and consumed by multiple services existing across different deployement on different hosts
    - Similar to previous approach, remote consumer class objects method invocation can be abstracted as Remote Procedure Call and achieve similar functionality. The actual data/metadata exchange would happen via network. It should be noted that data is pushed to consumers instead of pulling in this approach.
    - Each consumer can request data via API call to producer and get data/metadata in response. It should be noted that data is pulled from producer in this approach.

#### Many to many communication
- Data is produced by multiple services and consumed by multiple services existing within same deployement on same host
    - Similar to one-to-many communication, each producer class can act as Observable class and pass data to consumers.
- Data is produced by multiple services and consumed by multiple services existing across different deployement on different hosts
    - Similar to one-to-many communication, each service class can pass data/metadata to all consumers via push or pull mechanism.
    - Unlike memory reference, network calls are costly. Data through different producers could be aggregated on a common server (and replicated on bunch of servers) and server to consumer via pull or push mechanism. This method acts as if there is only a single producer and thus could drastically reduce network calls.


## Publisher - Subscriber architecture

In Publisher-Subscriber architecture, producers are referred to as publishers since they bring data to the system and consumers are referred to as subscribers as they've subscribed to consume data from the system. 

System would use an abstraction to refer to a collection of data published by all producers and refer to it as topic. So, we can say that different consumers would read data from topic and different publisher would publish data to topic.

In a lot of real-world scenarios, there exists multiple producers (different sources of data) and multiple consumers (different use-cases build on data). Moreover, publishers could be publishing data at different rate compared to rate at which data is consumed by consumers. e.g., Consider a stock-exchange. Many sellers could register their willingness to sell certain stock at preferred price. Multiple sellers would be interested in knowing the price at which stock is available for purchase before placing a buy order at certain target price. 

We'll try to build a system which would act as central platform to allow data sharing across multiple producers and multiple consumers. This system should allow admins to create a topic to which a producer can publish data and consumer can consume data.

Consumers don't share their state with other consumers. However, depending on task, it is common requirement that message is consumed by only one of consumers among set of consumers. Our system would allow formation of consumer group and ensure that new consumers can be added/removed from this group and guarantee that the message is read by only one consumer in a consumer group. (In cases of failure at consumer end, it is possible that messages might be consumed by multiple consumers. Our system should guarantee that only a single consumer can commit successful offset read. Consumers usually consume data and commit read after they've performed their task.)

Needless to say, system should be distributed to allow parallel read and write for higher throughput and availability as well as to store huge amount of data. Data read and write logic should allow for extremely fast read and write across multiple topics by different publisher and subscriber at any time. Also, the system can support managing state of consumer and consumer groups.

Note: Multiple producer and consumer instances can run on a system allowing it to act as publisher/consumer for multiple topics. It might be possible to add logic to support this feature within single instance but we'll keep design simple for mean-time. 


### Functional Requirement:
- Multiple producers should be able to populate data against a topic
- Multiple consumers should be able to produce data from a topic
- A new publisher should be able to publish data against existing topic
- A new subscriber should be able to subscribe and consume data from existing topic
- An existing publisher/subscriber can leave the system
- System should be able to process different rate of data production by producers and different rate of data consumption across consumers
- System should be able to manage multiple topics

### Non functional requirements:
- Data should be persisted for time defined in configuration (e.g., A topic could be configured to store data for 7 days)
- Subscribers should be able to get data via either pull or push mechanism
- System should be highly available
- System should be performant

### Good to have features:
- System should be able to manage state of subscriber (i.e, Store information of last read)
- System should allow to form a subscriber group so that messages across subscribers group is distributed(read by only one subscriber) among subscribers part of it
- System should allow creation of different partitions within a topic and allow publishing to desired partition. Partition could be used to balance incoming load from publisher. System would also maintain order of publishing messages within a partition (i.e., incoming messages within a partition could be accessed in sequential manner).

Note:
- System would not support idempotence for message delivery as it might happen that consumer received a message but crashed before processing it hence it'll be responsibility of consumer to ensure it processes a message only once even if same message is delivered multiple time.

### High level working
The process would be to onboard topics on the system. System would analyze nodes for details such as geography, free space, topics onboarded etc. and allocate partitions to various nodes and assign them as leader or follower.

Publishers would be able to connect to specific leader nodes for publishing data corresponding to specific topic and partition. System would update offset as well as update replicas.

Consumers would connect to system as a part of consumerGroup and subscribe to topics. System would distribute topics and partitions among consumers (This act is called rebalancing and would occur depending on configuration of consumer group). Consumers would connect to leaders or followers of topics and partitions assigned to them and poll to read some message from specified offset and update last read offset on server once their read is successful.

### APIs
POST /topic
- Create a topic by providing details such as name, number of partitions, replication
GET /topic
- Receive a list of all topics
PUT /topic/{topicId}
- Publish a message into a specific partition of a topic
POST /subscribe/{topicId}
- Subscribe to a topic as a member of specific consumer group and obtain list of topics and partition assigned to self within consumer group
GET /topicId/{partitionId}/{offsetId}/{count}
- Read messages from a parition of a topic starting from offsetId
POST /topicId/{partitionId}/{offsetId}
- Update last read offset on a partition i.e., commit offset value
GET /topicId/{partitionId}/{offsetId}
- Get last read offset on a partition

We'll not discuss APIs for security, heartbeat, node rebalance, synchronization and other management APIs for sake of simplicity.


### Database
Storage is required for:
- Metadata such as topics
- System data about state of various topics, consumers and server nodes
- Store data produced by producers

Each onboarded topic to the system would have details such as topicName, topicId (auto-generated), numberOfPartitions and details of leader (main node) and
follower (replica node) for each partition. These details change very rarely e.g., Leader and follower information would change when a node goes down.

Most of these details are generated at once during topic onboarding and changes occur very rarely. The data can be aligned in a hierarchical form as shown below and stored on
a read heavy system. The data looks like a JSON document and could be managed efficiently on a database such as MongoDb.

```JSON
{
    "topicId": int,
    "topicName": string,
    "partitions": 
    [
        {
            "partitionId": int,
            "leader": Node,
            "followers":[Node]
        }
    ]
}
```

During the process of onboarding a topic, system would require node statistics such as geography, free space, numberOfTopicsOnboarded, etc. These details would be added or updated during node addition and removal from system or addition of new topics. The details corresponding to each node on server is needed for partition allocation and hence would be read completely, which means that storing entire data as document would be a good choice, allowing to read/update data all at once. Updates would happen rarely or quite infrequent (topic addition is not a very frequent action) and even data read is quite infrequent in this case.

Node statistics information can be stored in following manner:
```JSON
{
    "nodeId": int,
    "geography": string,
    "availableSpace": int,
    "topics": 
    [
        {
            "topicId": int,
            "offset": int,
            "role": [follower/leader]
        }
    ]
}
```

In order to store state of consumer, system would store details corresponding to consumer group. Since the details of all consumer within a group is needed for allocation of topics and partitions, following details can be stored together.

```JSON
{
    "consumerGroupId": int,
    "topics": [],
    "consumers": [],
    "allocation":
    [
        {
            "topicId": int,
            "partitionId": int,
            "consumerNode": Node
        }
    ]
}
```

Consumer nodes are less reliable and could be much more frequently updated. Hence, the above data could have decent number of updates (still it'd be a single digit updates in a matter of minute and not much). However, each consumer would update read offset very frequently. The data to manage read offset could be organized as follows:

|ConsumerGroupId|ConsumerNode|TopicId|PartitionId|Offset|
|---|---|---|---|---|
|c123|con1|top14|part1|12456|
|c123|con2|top14|part2|13245|
|c124|con3|top14|part1|11111|

Any database supporting frequent write would be a good choice. (We can use slow write databases as well. It'd involve use of cache, Write ahead log and batch update). 

Finally, we need to decide on storage of data written by publishers. The data to be written by various publishers could be paritioned and brought to considerable write threshold. We'll still try to maximize write throughput. Instead of using a database, we'll work with files directly on system and only append to a file. Each append operation would increase file size and OS would help in allocation and writing to file. Each time a file needs to be resize to support new data, increment to file size could be done in block of some size (e.g., 128MB), making it easier to store data corresponding to same file at a contiguous location, thus supporting fast read and write.

Instead of writing entire data (of specific topic and partition) to one file, it could be broken down into multiple files with a max size limit on file. A convention such as fileName.part00001 can be used to create file chunks.

Overall, system would try to write data to a file. If there is no empty space available within file, file would be increased by some size (e.g., 128MB) if new size fits within max size limit or system would create a new file part and write data to it. Some additional metadata or logic is needed to work with file parts, the simplest logic would be to store a list of all parts corresponding to file and a reference to last part. Details like minOffset, maxOffset could also be stored corresponding to file part.

### Back-of-the-envelope calculation

The approximate load on system would vary a lot depending on use-case. If a system is used for private purpose within an industry, it'll typically have lower loads compared to public shared service handling many clients. 

The following approximation should be good for a private industry and okayish for a shared public infrastructure.
- 1M topics
- 10 partitions in each topic
- 10M producers
- 0.1M consumer group
- 1M consumers

Using table details from database section, we can calculate approximate storage and throughtput values. 

#### Metadata storage analysis
- Topic Details
    - 1M (topics)* 700B (approx details for a topic) ~ 700 MB

- Node Details
    - Node details storage requirement would grow as more nodes are included in system. However, depending on various calculated values, we can decide number of nodes in the system. It's like a chicken and egg problem. 
    
    - In order to start, let's assume that system has approximately 100 nodes. Assuming equal load, each node would handle 0.01M topics as leader and 0.01M * (replicationFactor-1) topics as follower. Lets assume replicationFactor = 3.

    - Storage requirements: 0.01M (topics) * 3 (replicationFactor) * 50B (details corresponding to each topic) + 0.01M (topics) * 50B (node details) ~ 1.5MB

- Consumer Details
    - We've two tables. One to store details of consumer and allocation within consumer group and another to store offset details.
    - Consumer Group details: 0.1M (consumer group) * 50B (allocation detail) * 1 (topic subscribed by consumer group) * 10 (partitions) ~ 5MB
    - Offset Details: 1M (topics) * 10 (partition) * 40B (data in each row) ~ 400MB

#### Data storage analysis
Number of message produced by producers, message size, replication factor and data retention policy greatly impacts data storage requirements. Events such as "clicks" i.e., Reaction on a post/tweet, View product details, etc. have small message size but large number of events. On other hand, processing of data in ETL pipeline - such messages could be large but less frequent.

Generally, message size are of 1KB (but they could be as large as 1MB). Very frequent events could be as high as 1M/second (only lasts few seconds) hence we'd use average 1K events/second per topic in calculation.

1M (topics) * 1K (events/sec) * 86400 (data is available for 1 day) * 1KB ~ 86400TB/day

It's a lot of data. Since, very few topics produce 1K events/sec, let's further split topics and events frequency to get more accurate result.

1K (topics) * 1K (events/sec) * 86400 * 1KB + 10K (topics) * 0.1K (events/sec) * 86400 * 1KB + 100K (topics) * 0.01K (events/sec) * 86400 * 1KB + 899K (topics) * 1 (event/sec) * 86400 * 1KB ~ 86.4TB/day

Let's not forget that system has 100 nodes to handle this storage and also manage replicationFactor of 3. Roughly each node would store 86.4TB*3/100 ~ 2.5TB/day. Data retention is usually less for events producing data at faster rate. From above calculation, system is adding 2.5TB data each day when data retention period is 1 day. If data is needed to be stored for more days, the storage would also grow in its multiple.

#### Throughput analysis
- Producer throughput
    - As discussed earlier, the rates of producing data could vary a lot depending on event.
    - In case of live events such as cricket match, a lot of people from various device could be sharing their reaction on current status of match. 1M or even more people could be predicting the winner (and consumer processing these events need to update live win prediction.)

    - 1M (producers) * 1 (message per second) * 10KB (message size) = 10MB/s. Assuming this topic has 10 partition, data is spread over 10 nodes. Resulting throughput per server is 1MB/s.

    - Most use-case doesn't have a lot of producers producing data simultaneously. Let's consider an average scenario:
    - 1K (producers) * 10 (message per second) * 10KB (message size) = 100MB/s. Throughput across each node would be ~10MB/s.

- Consumer throughput
    - Events requiring heavy-processing usually have more consumers so that lots of messages can be processed in parallel for time-taking process. Whereas, quick to process events have less number of consumers. On average, a consumer would process : 1 (topic) * 5 (partitions) * 1K (messages/sec) * 10KB (message size) ~ 50 MB/s. It is very unlikely that consumer is reading all data from a single node. Average throughput on each server would be ~10MB/s due to one such consumer.

- Server throughput:
    - 1M (topics) * 1K (events/sec) * 10 KB(message size) / 100 (servers) ~ 0.1TB/s. Again, 1M topics wouldn't be simulataneously producing 1K events/s. Replacing 1M with 1K, gives a better approximation of 100MB/s.

    - In case of live events (i.e., heavy load scenario), producers and consumers would be producing and consuming data almost in real-time. So, input to server would be at 100MB/s and output from server would also be 100MB/s.

Note: We've considered that there are 100 servers in our system. In case, system scales to handles more than 1M topics/producers/consumers, number of server nodes would should also be scaled to keep throughput values in limit and deliver similar results as with 1M topics/producers/consumers.


### Architecture

#### Server
Server contains nodes with two kinds of responsibility.
- To process metadata from nodes, producers and consumers and redirect to correct leader node
- To process data from producer and send data to consumer

These functionalities can be present on same or different set of server. If there are too less co-ordinator nodes, it is possible that metadata information produced during addition or removal of node or creation of topic isn't processed (If all nodes are down!). If there are too many co-ordinator, time taken to achieve consensus would be high (It is possible to use RAFT algorithm for consensus and make all process run as co-ordinator). This feature can be configured by system adiminstrator depending on factors such as location, network, SLA etc. In HLD diagram, it is assumed that a set of servers from all servers act as co-ordinator and all nodes in server act as data nodes.

#### DNS
DNS contains IPs of co-ordinator nodes and acts as load-balancer.

#### Data Flow
Producer fetches IP from DNS and connects to a co-ordinator node. It fetches details of all nodes associated with a topic and catches it locally. Producer determines the partition of message and sends it to leader node for corresponding topic and partition.

The node receives the message and verifies if it intended receipient and if is leader for corresponding topic and partition. If it is intended recepient, it stores the message in the file on disk and sends data to follower nodes and responds back to producer depending on acknowledgement configuration.

A consumer node registers itself as a part of consumer group with co-ordinator node. Depending on configuration, partitions might be assigned to it immediately or with delay. Once a consumer node knows details of partition, it communicates with corresponding data node to fetch data from specific offset. After reading data, consumer node requests node to update offset. (Consumer node also stores offset value locally after fetching it initially so that subsequent request can be made without re-fetching offset value).

#### Topic Creation
A new topic creation request alongwith required details such as partition count is received at one of co-ordinator node. This node fetches node statistics and tries to allocate nodes for each partition as leader or follower and shares allocation details with other co-ordinator and updates database entries.

#### Cache
Metadata details such as leader/follower nodes for topic and partition combination is stored in local cache after fetching it for first time from database. Cache is updated using read-through mechanism and uses LRU eviction policy.

#### Recovery
In case of a consumer node failure, one of existing node can be promoted as co-ordinator node. This newly promoted node would sync with other co-ordinator to update itself with relevant details.

In case of data node failure, one of follower node will be promoted to leader and a new follower node would be assigned immediately or with delay (as per configuration). Once the new follower node is assigned, it'll update itself with messages from leader or other follower node.

#### Persistence
Data is stored on internal disk of leader node along with replication on follower node. All write requests are processed on number of nodes configured by producer to receive an acknowlegdement from. In order to guarantee a higher chance of persistence, data should be acknowledged from multiple nodes on each write. The chances of all these nodes failing together is extremely low and thus provides higher chance of persistence.

#### Data Purging and Archival
Messages are immutable and hence can be considered for compression. However, it should be noted that uncompression would be required later (either on server or consumer) to retrieve original data.

System stores data in a queue like structure. The offset value increases in each newly added file and within file, offset value increases sequentially. During purge, system can simply delete file if offset timestamp is beyond maintenance range or sequentially iterate through file and partition it and later delete chunk corresponding to old data. Purge operation can be time consuming involving multiple disk read and write. It should be performed as scheduled batch operation.

#### Diagram
![HLD](https://github.com/Prakash-Jha-Dev/SystemDesign/blob/PubSub/publish-subscribe/src/main/PubSub.png)

### Bottleneck

With increase in number of topics, throughput value would increase and after a certain value would reach unrealistic levels. System needs to add new nodes during scaling to distribute load across nodes.

Scaling down system is challenging as topics are distributed across nodes. In order to bring down a node, data from multiple topics needs to be synced to an existing node before node can be safely removed from system. A similar situation occurs when a node fails, while one of follower node becomes leader and ensures availability, a lot of data sync could be required to replicate data to an existent node to bring back system to configured leader/follower specification.

In case of uneven distribution of topics with heavier writes, disk of a node could get full quickly with no space left to write new data. In such cases, disk capacity of nodes needs to be increased after 80% of disk space is used to ensure smooth operations.

### Benchmarking and Monitoring
State of nodes in the system needs to be monitored in real-time. Different healthcheck end-point can be setup to either pull or push healthly node information to co-ordinator nodes in the system.

System also needs to keep a check on disk usage and perform disk increment or purge or archival to increase free space on disk. Old data should be purged in timely manner to avoid unnecessary disk increment operations.

### Security
Each producer and consumer needs to be authenticated and authorized for data access on a topic and assigned a sessionKey which can be verified by data node before sharing data with producer or consumer.

Access to data node must be authorized by a co-ordinator node. The co-ordinator node would have a full mechanism of authentication and authorization as it acts as public interface. Data nodes can be streamlined security setup and guranatee access to only nodes authorized by one of co-ordinator node.
 

