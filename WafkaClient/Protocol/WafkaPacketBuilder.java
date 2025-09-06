package WafkaClient.Protocol;

// Java packages
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// Project packages
import WafkaClient.Protocol.DataUtils.TopicData;

// Client facing request builder
// Program flow **
    // Client sets specific packet details in WafkaPacketBuilder
    // Client calls .buildPacket
    // buildPacket calls WafkaRequestRegistry
    // WafkaRequestRegistry matches version and API key to a concrete builder
    // WafkaRequestRegistry returns the concrete builder to WafkaPacketBuilder
    // inside .buildPacket, WafkaPacketBuilder calls concreteBuilder.build()
    // it then returns a packet object, that is then returned to the client by WafkaPacketBuilder
    
// Todo: Support both one shot and fluent build patterns
public class WafkaPacketBuilder {
    
    // Headers related
    short requestApiKey;
    short requestApiVersion;
    int correlationId;
    String clientId = null;

    // Body related
    String transactionalId = null;
    short acks;
    int timeoutMs;
    List<TopicData> topicData = null;

    public WafkaPacketBuilder(){}

    public byte[] buildPacket(){
        // Call registry to match with a compatible concrete builder
        WafkaRequestRegistry registry = new WafkaRequestRegistry();
        WafkaBaseBuilder concreteBuilder = registry.match(this.requestApiKey, this.requestApiVersion);
        
        // Refactor: use some form of context to convert current attributes to object that gets passed
        // to the concreteBuilder build parameters
        // for now this IS NOT type safe, just strings
        Map<String, Object> packetData = new HashMap<>();
        packetData.put("header", new HashMap<String, Object>());
        packetData.put("body", new HashMap<String, Object>());

        @SuppressWarnings("unchecked")
        HashMap<String, Object> header = (HashMap<String, Object>) packetData.get("header");
        header.put("requestApiKey", this.requestApiKey);
        header.put("requestApiVersion", this.requestApiVersion);
        header.put("correlationId", this.correlationId);
        header.put("clientId", this.clientId);

        @SuppressWarnings("unchecked")
        HashMap<String, Object> body = (HashMap<String, Object>) packetData.get("body");
        body.put("transactionalId", this.transactionalId);
        body.put("acks", this.acks);
        body.put("timeoutMs", this.timeoutMs);
        body.put("topicData", this.topicData);

        
        return concreteBuilder.build(packetData);
    }



    /*
     * This ones for the users that want the fluent control experience
     * in building their request header and bodies.
    */
    // Header related setters
    public void setRequestApiKey(short requestApiKey){this.requestApiKey = requestApiKey;}
    public void setRequestApiVersion(short requestApiVersion){this.requestApiVersion = requestApiVersion;}
    public void setCorrelationId(int correlationId){this.correlationId = correlationId;}
    public void setClientId(String clientId){this.clientId = clientId;}

    // Body related setters
    public void setTransactionalId(String transactionalId){this.transactionalId = transactionalId;}
    public void setAcks(short acks){this.acks = acks;}
    public void setTimeoutMs(int timeoutMs){this.timeoutMs = timeoutMs;}
    public void setTopicData(List<TopicData> topicData){this.topicData = topicData;}

    /*
     * Todo: add a oneshot builder for the users that dont give a damn
     * about setters
     * dont know how to do this effectively yet but, will come back to it
    */
    
    
}
