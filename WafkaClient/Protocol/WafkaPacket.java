package WafkaClient.Protocol;

public class WafkaPacket {
    // Todo: add a 4 byte to store total length in the final packet being constructed
    // such that [total_length (int32)][header][body] format remains
    byte[] header;
    byte[] body;
}
