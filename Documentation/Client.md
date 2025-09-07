# Wafka Client

In most backends, to create a kafka client, a client object or producer object is instantiated. The client object then has a connection method that establishes a persistent connection to a kafka broker, communicating via custom binary protocol over TCP. In this case, there are methods to invoke when new messages need to be sent, the client stores a queue of message objects that it processes first, then sends to the server. The message being processed is poped from the queue and when the queue is empty, the client waits for a new message.

Thus, Wafka will follow this concept. However, slightly adjusted. This time there is just a single WafkaClient responsible for producing user messages, connecting with broker, processing the message to a binary protocol suitable packet and sending this packet to the broker.

In wafka, the client has two threads running. One for message processing and the other for message sending. The flow inside the main method will be as follows...

1. User's backend program invokes a ```.sendMessage(topicName, messageString)``` on their Wafka client instance.
2. The method then adds to a queue of pendig messaages internal to the WafkaClient instance.
3. The thread that waits for new pending messages will detect the new entry in the pending queue, and then invoke a handler to process the message. This handler is responsible for invoking a partition logic, and packing the message into a TopicData object. Once processing completes, it should send the TopicData object into a ready queue.
4. The second thread, the sender thread, should detect the new message sent into the readyQueue and then add it or drain it completely into a batch or list of TopicData. Once drained, immediately create a custom binary packet and send it to the server.

So I'll have it go User sends message -> pending message processor thread -> batch sender thread
