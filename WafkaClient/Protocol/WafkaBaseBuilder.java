package WafkaClient.Protocol;

import java.util.Map;

public interface WafkaBaseBuilder {
    public byte[] build(Map<String, Object> packetData);
}
