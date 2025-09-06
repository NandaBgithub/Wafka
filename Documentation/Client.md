# Wafka Client

In most backends, to create a kafka client, a client object or producer object is instantiated. The client object then has a connection method that establishes a persistent connection to a kafka broker, communicating via custom binary protocol over TCP. In this case, there are methods to invoke when new messages need to be sent, the client stores a queue of message objects that it processes first, then sends to the server. The message being processed is poped from the queue and when the queue is empty, the client waits for a new message.

Thus, Wafka will follow this concept. However, slightly adjusted. This time there is just a single WafkaClient responsible for producing user messages, connecting with broker, processing the message to a binary protocol suitable packet and sending this packet to the broker.
