# Wafka

The wannabe kafka.

## Key terms

Event: A message containing key value pairs with a timestamp and optional meta-data.

Topic: A named stream of related events or messages. Topics can be split into partitions.

Broker: The actual server program.

Producer: A client program that sends a event or message to the kafka server.

Consumer: The program that uses the data collected by the kafka server.

What I have to make this project

1. A laptop
2. A bag of potato chips
3. Hopes
4. ... and dreams

## Design flow

```JavaScript
// The event the producer will send in a stream
{
    event_id: 1,
    topic_name: deliveries,
    timestamp: "yyyy-mm-dd",
    message: "truck--ontime--australia-melbourne-1021" 
}
```

The default partitioning strategy is round robin, though further strategies can be implemented.

After the server recieves the data, it should then determine which broker to send it to.

Inside each broker thread will be an object shown below that is constructed using known the incoming message from the main thread.

```JavaScript
{
  "broker_id": 1,
  "partitions": {
    "deliveries": {
      "messages": [
        //... deliveries messages
      ],
      "next_offset": 54
    },
    "shipping": {
      "messages": [
        //... shipping messages
      ],
      "next_offset": 23
    }
  }
}
```

## Partitioning strategies

Partition key

Uses a specific field in the message as input into a hash function that outputs which partition to assign the message to.

Round-robin

Assigns message in cyclical manner so that message load is as evenly distributed between partitions.

## Data storage

For each message, I know it looks like Im storing an array of messages, in the in-memory object inside a broker, but what should actually happen is the object just shows the current message.

What then needs to be done is the message is mapped onto some partition file that corresponds to the broker. This begs the question, how do I know how many partitions per broker? Well, this should account for how much parallelism or throughput the consumers can have. I dont want this to get too complicated so Im capping it to one partition file per topic for one broker.

The file naming will be topic-broker_id.txt.

The content format for each file will be a one liner json string representing the message so I dont lose the metadata for the message.
