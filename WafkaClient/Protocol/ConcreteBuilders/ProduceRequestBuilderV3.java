package WafkaClient.Protocol.ConcreteBuilders;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import WafkaClient.Protocol.WafkaBaseBuilder;
import WafkaClient.Protocol.DataUtils.PartitionData;
import WafkaClient.Protocol.DataUtils.TopicData;

public class ProduceRequestBuilderV3 implements WafkaBaseBuilder{
    
    @SuppressWarnings("unchecked")
    @Override
    public byte[] build(Map<String, Object> packetData) {
        HashMap<String, Object> header = (HashMap<String, Object>) packetData.get("header");
        HashMap<String, Object> body = (HashMap<String, Object>) packetData.get("body");

        // header
        int headerByteLength =
                Short.BYTES + // requestApiKey
                Short.BYTES + // requestApiVersion
                Integer.BYTES + // correlationId
                sizeOfString((String) header.get("clientId"));

        // body
        int bodyByteLength =
                sizeOfString((String) body.get("transactionalId")) +
                Short.BYTES + // acks
                Integer.BYTES; // timeoutMs

        // Topic data
        int topicDataBytes = Integer.BYTES; // size of topicData list
        for (TopicData data : (List<TopicData>) body.get("topicData")) {
            topicDataBytes += sizeOfString(data.getName());
            topicDataBytes += Integer.BYTES; // size of partitionData list

            for (PartitionData part : data.getPartitionData()) {
                topicDataBytes += Integer.BYTES; // index
                topicDataBytes += sizeOfString(part.getRecords());
            }
        }

        int totalPacketSize = Integer.BYTES + headerByteLength + bodyByteLength + topicDataBytes;

        ByteBuffer buffer = ByteBuffer.allocate(totalPacketSize);
        buffer.putInt(totalPacketSize - Integer.BYTES); // total length doesn't include itself

        // header
        buffer.putShort(((Number) header.get("requestApiKey")).shortValue());
        buffer.putShort(((Number) header.get("requestApiVersion")).shortValue());
        buffer.putInt(((Number) header.get("correlationId")).intValue());
        writeString(buffer, (String) header.get("clientId"));

        // body
        writeString(buffer, (String) body.get("transactionalId"));
        buffer.putShort(((Number) body.get("acks")).shortValue());
        buffer.putInt(((Number) body.get("timeoutMs")).intValue());

        // TopicData
        List<TopicData> topics = (List<TopicData>) body.get("topicData");
        buffer.putInt(topics.size());

        for (TopicData topic : topics) {
            writeString(buffer, topic.getName());

            List<PartitionData> parts = topic.getPartitionData();
            buffer.putInt(parts.size());

            for (PartitionData part : parts) {
                buffer.putInt(part.getIndex());
                writeString(buffer, part.getRecords()); // getRecords() returns String
            }
        }

        return buffer.array();
    }

    private void writeString(ByteBuffer buffer, String str) {
        if (str == null) {
            buffer.putInt(-1);
        } else {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }
    }
    
    // Returns number of bytes for a string in the packet
    private int sizeOfString(String str) {
        // for -1
        if (str == null) return 4; 
        return 4 + str.getBytes(StandardCharsets.UTF_8).length;
    }
}
