package WafkaClient;

import java.util.*;
import java.util.concurrent.*;

import java.io.*;
import java.net.*;

import WafkaClient.Protocol.WafkaPacketBuilder;
import WafkaClient.Protocol.DataUtils.PartitionData;
import WafkaClient.Protocol.DataUtils.TopicData;

public class WafkaClient {
    BlockingQueue<Message> pendingMessageQueue = new LinkedBlockingQueue<>();
    BlockingQueue<TopicData> readyQueue = new LinkedBlockingQueue<>();
    int partitionMethod;
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
            TopicData topicData = processMessage(messageToProcess);
            this.readyQueue.add(topicData);
        }
    }

    private static TopicData processMessage(Message message){
        // Todo: call partitioning algorithms based
        // on partitionMethod and wrapping the result into a topic data object
        return null;
    }

    // Should be ran by the sender thread
    public void sendBatchToServer() throws InterruptedException{
        final int BATCHSIZE = 10;
        // Todo: build the packet using the stuff in protocol folder
        // then write into the output stream and listen to server's response
        while (this.isRunning){
            // Todo: build the packet should be according to a config file, but for now, 
            // data in wafka client will act as the config settings
            WafkaPacketBuilder builder = new WafkaPacketBuilder();
            List<TopicData> batch = new ArrayList<>();

            TopicData first = readyQueue.take();
            if (first != null){
                readyQueue.drainTo(batch, BATCHSIZE);
            }

            if (!batch.isEmpty()){
                // Todo: write to output
                batch.clear();
            }
        }
    }


    public static void main(String[] args) throws IOException{
        // In future, this building logic for building headers should
        // be read from some config file
        // as for the body, these should be set by a sepparate sender thread
        WafkaPacketBuilder builder = new WafkaPacketBuilder();
        List<TopicData> t = new ArrayList<>();
        List<PartitionData> p = new ArrayList<>();
        p.add(new PartitionData(0, "test"));
        t.add(new TopicData("testTopic", p));
        builder.setRequestApiKey((short) 0);
        builder.setRequestApiVersion((short) 3);
        builder.setCorrelationId(0);
        builder.setClientId("1234");
        builder.setTransactionalId("100");
        builder.setAcks((short) 1);
        builder.setTimeoutMs(300);
        builder.setTopicData(t);
        byte[] packet = builder.buildPacket();
        System.out.println(Arrays.toString(packet));

        String host = "localhost";
        int port = 3000;
        String line;
        OutputStream out = null;
        BufferedReader in = null;
        
        try (
            Socket wafkaClient = new Socket(host, port);
        ){
            out = wafkaClient.getOutputStream();
            out.write(packet);
            out.flush();

            in = new BufferedReader(new InputStreamReader(wafkaClient.getInputStream()));
            
            while (true){
                while ((line = in.readLine()) != null){
                    System.out.println(line);
                }
            }
            
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Server disconnected");
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
}
