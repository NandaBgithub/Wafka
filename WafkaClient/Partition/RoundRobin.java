package WafkaClient.Partition;

public class RoundRobin {
    
    // Wafka is so shit, its only supporting one topic per partition
    public static int assign(int curBroker, int[] brokers){
        int numBrokers = brokers.length;
        curBroker = (curBroker == numBrokers) ? 0 : curBroker + 1;
        return curBroker;
    }
}