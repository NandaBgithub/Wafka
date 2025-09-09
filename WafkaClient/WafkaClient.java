package WafkaClient;

import java.util.*;
import java.util.concurrent.*;

import java.io.*;
import java.net.*;

import WafkaClient.Partition.RoundRobin;
import WafkaClient.Protocol.WafkaPacketBuilder;
import WafkaClient.Protocol.DataUtils.PartitionData;
import WafkaClient.Protocol.DataUtils.TopicData;

public class WafkaClient {
    BlockingQueue<Message> pendingMessageQueue = new LinkedBlockingQueue<>();
    BlockingQueue<TopicData> readyQueue = new LinkedBlockingQueue<>();
    
    // Metadata needed for brokers and partitions
    int partitionMethod = 0;
    int curBroker = 0;
    int[] leaderBrokers = {1, 2, 3};

    Socket clientSocket = null;
    BufferedReader in = null;
    OutputStream out = null;
    volatile boolean isRunning = false;
    
    List<TopicData> topics = new ArrayList<>(); // Todo: change to batch
    
    public WafkaClient(int partitionMethod){
        this.partitionMethod = partitionMethod;
    }

    // users invoke this to send message to internal message buffer for processing
    public void sendMessage(String topicName, String message){
        Message messageObject = new Message(topicName, message);
        this.pendingMessageQueue.add(messageObject);
    }

    // initial tcp connection method
    public void connect(String host, int port) throws UnknownHostException, IOException{
        try {
            this.clientSocket = new Socket(host, port);
            out = this.clientSocket.getOutputStream();
            in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            this.isRunning = true;
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // Should be ran by the message processor thread
    public void loopMessageProcesses() throws InterruptedException{
        while (this.isRunning){
            Message messageToProcess = this.pendingMessageQueue.take();  
            TopicData topicData = this.processMessage(messageToProcess);
            this.readyQueue.add(topicData);
        }
    }

    private TopicData processMessage(Message message){
        // Partition
        int broker;
        if (this.partitionMethod == 0){
            broker = RoundRobin.assign(curBroker, leaderBrokers);
        } else {broker = 0;}

        // Wrap current message to a TopicData object and add to readyQueue
        PartitionData p = new PartitionData(broker, message.getMessage());
        List<PartitionData> l = new ArrayList<>();
        l.add(p);
        TopicData t = new TopicData(message.getTopicName(), l);
        return t;
    }

    // Should be ran by the sender thread
    public void sendBatchToServer() throws InterruptedException, IOException{
        final int BATCHSIZE = 10;
        
        while (this.isRunning){
            // Todo: build the packet should be according to a config file, but for now, 
            // data in wafka client will act as the config settings
            WafkaPacketBuilder builder = new WafkaPacketBuilder();
            List<TopicData> batch = new ArrayList<>();
            String serverResponse;

            TopicData first = readyQueue.take();
            if (first != null){
                readyQueue.drainTo(batch, BATCHSIZE);
            }

            if (!batch.isEmpty()){
                builder.setRequestApiKey((short) 0);
                builder.setRequestApiVersion((short) 3);
                builder.setCorrelationId(0);
                builder.setClientId("testCLient");
                builder.setTransactionalId("1");
                builder.setAcks((short) 1);
                builder.setTimeoutMs(300);
                builder.setTopicData(batch);
                byte[] packet = builder.buildPacket();
                this.out.write(packet);
                out.flush();
                batch.clear();

                while ((serverResponse = in.readLine()) != null){
                    System.out.println(serverResponse);
                }
            }
        }
    }


    public static void main(String[] args) throws IOException{
        // open connection to server
        WafkaClient wafkaClient = new WafkaClient(0);
        wafkaClient.connect("localhost", 3000);

        // open thread for processing messages
        Thread processor = new Thread(()-> {
            try {
                wafkaClient.loopMessageProcesses();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // open thread for sending messages
        Thread sender = new Thread(() -> {
            try {
                wafkaClient.sendBatchToServer();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });

        processor.start();
        sender.start();

        wafkaClient.sendMessage("delivery", "delivery-truck--ontime--australia-melbourne-1021");
        wafkaClient.sendMessage("delivery", "delivery-truck--ontime--australia-melbourne-1021");
        wafkaClient.sendMessage("delivery", "delivery-truck--ontime--australia-melbourne-1021");
        wafkaClient.sendMessage("delivery", "delivery-truck--ontime--australia-melbourne-1021");
        wafkaClient.sendMessage("delivery", "delivery-truck--ontime--australia-melbourne-1021");
    }
}
