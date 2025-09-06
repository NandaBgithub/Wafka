package WafkaClient.Protocol.DataUtils;

public class PartitionData {
    int index;
    String records;

    public PartitionData(int index, String records){
        this.index = index;
        this.records = records;
    }

    public int getIndex(){
        return this.index;
    }

    public String getRecords(){
        return this.records;
    }
}
