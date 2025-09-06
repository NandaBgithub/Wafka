package WafkaClient.Protocol.DataUtils;

// Java packages
import java.util.List;

public class TopicData {
    String name;
    List<PartitionData> partitionData;

    public TopicData(String name, List<PartitionData>  partitionData){
        this.name = name;
        this.partitionData = partitionData;
    }

    public String getName(){
        return this.name;
    }

    public List<PartitionData> getPartitionData(){
        return this.partitionData;
    }
    
}
