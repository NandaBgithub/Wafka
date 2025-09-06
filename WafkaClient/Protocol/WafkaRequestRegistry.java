package WafkaClient.Protocol;

import WafkaClient.Protocol.ConcreteBuilders.ProduceRequestBuilderV3;

public class WafkaRequestRegistry {
    private final ProduceRequestBuilderV3 produceRequestBuilderV3 = new ProduceRequestBuilderV3();
    public WafkaRequestRegistry(){}


    public WafkaBaseBuilder match(short apiKey, short version){
        if (apiKey == 0){ // Produce request
            if (version == 3){
                return this.produceRequestBuilderV3;
            }
        }
        
        // todo: replace this with some default builder
        return null;
    }
}
