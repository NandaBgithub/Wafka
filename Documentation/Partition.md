# Partitions

Partitions are the txt files that Wafka will be writing in append mode according to the topic.
Every partition is for a topic.
Partition files are written to based on what broker is responsible for it.
The brokers in wafka are the threads that the WafkaServer will give write access to for a partition file.
One broker for every set of brokers is considered a leader broker. This is the only broker WafkaServer
will give write access to, the other brokers are replication brokers that just replicate data from the partition files of the leader.

For example.
A in this simulation, there is a single WafkaServer running on a port. This single server will
act as if it is a cluster of brokers. The brokers in this case are just threads, initially there will be three threads and each are assigned partition(s). Broker1 will be the leader, it manages partition1 and partition2. Partition1 is responsible for the first topic that the client should write, and partition2 for the second topic.
Broker2 would then follow Broker1 (the leader) such that anytime partition1 is written to by Broker1, a broadcast is sent to Broker2 to replicate it in its own partition. So broker2, is responsible for replicating partition 1 in a sense. Broker3 does the same thing, but this time for partition 2.

Normal kafka servers are way more sophisticated. They would have bootstrap servers that a client would connect to in order to determine what brokers are available, what clusters these brokers are on, what partitions these brokers have, how many brokers there are, how many partitions per topic and so on... Using this metadata, the client can easily determine which broker to actually send their topics to. And if one broker doesnt handle all the topics that clients have, then multiple broker connections can easily be opened by the client to however many brokers necessary in order to deliver all its topic data.