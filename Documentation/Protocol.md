# Wafka protocol

The custom binary protocol for a simple wafka server. This protocol will be custom binary implemented over TCP, since Java's socket library is over TCP.

This protocol has custom packet structure depending on the Wafka version being used by the client. The version determines how the packet is structured since different versions would activate different functionality on the server side, for example, the real kafka version one would support timestamped messages, version zero might not support timestamped messages.

The version, is also accompanied by an API key integer specifying what type of request is being sent to the server. The API key determines the structure of the request being sent. For example, a Produce request will have api key of 0, and with the request will be a body. Now, the server will handle a api key 0 (Produce) request differently based on the version. Version 1, might time stamp the message, whilst version 0 might leave the message unstamped.

Both the API key and the version for the Wafka protocol will be sent through the request header from the client to the server.

The real Kafka would have a huge number of different API keys each specifying the type of request eg. Produce, fetch, Offset, HeartBeat ect, and a wide variety of version numbers for requests. As for why, Im pretty sure its because instead of replacing an older version, they just add a new one so that projects operating on older version dont break, and hence, Kafka would be backwards compatible.

But for Wafka I'll support one, that is, one type of request to the server and one version and the packets structured as follows...

**Headers**

```Text
Request Header v1 => request_api_key request_api_version correlation_id client_id 
  request_api_key => INT16
  request_api_version => INT16
  correlation_id => INT32
  client_id => NULLABLE_STRING
```

correlation_id: The id that is used to match client to a broker.

**Request Body for a produce request**

```Text
Produce Request (Version: 3) => transactional_id acks timeout_ms [topic_data]

  transactional_id => NULLABLE_STRING
  acks => INT16
  timeout_ms => INT32
  topic_data => name [partition_data] 
    name => STRING
    partition_data => index records 
      index => INT32
      records => RECORDS
```

transactional_id: The id of the actual transation.

acks: Depending on the acks value will determine the number of acknowledgements needed by the client from the server.

timeout_ms: The maximum time in milliseconds that the client will wait for acknowledgements from the server before timing out the request.

topic_data is an array of topics and partitions the client is producing messages to.

Inside each topic_data element are the following.

name: The topic name that this element/record is being sent to.

partition_data: An array of partitions elements that specify which partition on the server that this record element will be written to. In each partitions element are the following...

index: the partition number  within the topic. This number is determined by the client through partition algorithms like round robin.

records: the actual message data encoded in some way. Inside it might contain key value pairs, timestamps and so on...
the real Kafka will have different records per version, but for Wafka, I am just going to use strigs.
