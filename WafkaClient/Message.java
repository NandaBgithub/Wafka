package WafkaClient;

public class Message {
    String topicName;
    String message;

    public Message(String topicName, String message){
        this.topicName = topicName;
        this.message = message;
    }

    public String getTopicName(){return this.topicName;}
    public String getMessage(){return this.message;}
}
