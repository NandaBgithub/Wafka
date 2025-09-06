package WafkaClient;

import java.util.*;
import java.io.*;
import java.net.*;

import WafkaClient.Protocol.WafkaPacketBuilder;
import WafkaClient.Protocol.DataUtils.PartitionData;
import WafkaClient.Protocol.DataUtils.TopicData;

public class WafkaClient {
    
    public WafkaClient(){

    }

    public void sendMessage(){

    }

    public static void main(String[] args) throws IOException{
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
