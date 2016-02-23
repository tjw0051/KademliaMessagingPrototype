/**
 * Created by t_j_w on 09/02/2016.
 */

import kademlia.JKademliaNode;
import kademlia.dht.GetParameter;
import kademlia.dht.KademliaStorageEntry;
import kademlia.node.KademliaId;
import kademlia.node.Node;
import kademlia.simulations.DHTContentImpl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Exchanger;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;


public class Messenger {

    JKademliaNode _node;
    int localMessageCount;
    ListenThread listenThread;
    GetParameter iterParam;

    public Messenger(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter name:");
        String name = in.nextLine();

        try {
            createNode(name, Integer.parseInt(args[0]));
            System.out.println("Connecting to bootstrap server...");
            bootstrapNode();
            System.out.println("Printing chat history...");
            localMessageCount = getMessageCount();
            getChatHistory();
            listenThread = new ListenThread();
            listenThread.start();

            while(true) {
                //System.out.print(name + ": ");
                String message = in.nextLine();
                if (message.startsWith("/")) {
                    switch (message) {
                        case "read": {
                            //GetParameter gp = new GetParameter(new KademliaId("nodemessage000000000"), "DHTContentImpl");
                            //KademliaStorageEntry conte = e.get(gp);
                            //DHTContentImpl newCont = (new DHTContentImpl()).fromSerializedForm(conte.getContent());
                            //System.out.println("Content: " + newCont);
                            break;
                        }
                        case "/dht": {
                            System.out.println(_node.getDHT());
                            break;
                        }
                        case "/history": {
                            getChatHistory();
                            break;
                        }
                        case "/leave": {
                            leaveNetwork();
                            break;
                        }
                        case "/refresh": {
                            _node.refresh();
                            break;
                        }
                        default: {
                            //DHTContentImpl c = new DHTContentImpl(new KademliaId("nodemessage000000000"),
                            //        e.getNode().getNodeId().toString());
                            //c.setData(message);
                            //e.put(c);

                            //System.out.println("Message stored locally: " + message);
                            break;
                        }
                    }
                }
                else {
                    //System.out.println("sending message: " + message);
                    System.out.println(name + ": " + message);
                    sendMessage(name + ": " + message);
                }
            }

            /*
            DHTContentImpl var8 = new DHTContentImpl(kad2.getOwnerId(), data);
            kad2.put(var8);
            System.out.println("Retrieving Content");
            GetParameter gp = new GetParameter(var8.getKey(), "DHTContentImpl");
            gp.setOwnerId(var8.getOwnerId());
            System.out.println("Get Parameter: " + gp);
            KademliaStorageEntry conte = kad2.get(gp);
            System.out.println("Content Found: " + (new DHTContentImpl()).fromSerializedForm(conte.getContent()));
            System.out.println("Content Metadata: " + conte.getContentMetadata());
            */
        } catch (Exception e) {
            System.out.println("Exception Caught");
            e.printStackTrace();

        }
    }

    public class ListenThread extends Thread {
        public void run() {
            long time = System.nanoTime();
            while (true) {
                if (System.nanoTime() - time > 200) {
                    int msgCount = getMessageCount();
                    if (msgCount > localMessageCount) {
                        //System.out.println("new message incoming. Message count: " + msgCount + " local: " + localMessageCount);
                        for (int i = localMessageCount; i < msgCount; i++) {
                            String messageId = "nodemessage" + String.format("%9s", Integer.toString(i))
                                    .replace(' ', '0');
                            //System.out.println("Recieved - Message ID : " + messageId);
                            String message = getMessage(messageId);
                            if (message != null) {
                                System.out.println(message);
                            }
                        }
                        localMessageCount = msgCount;
                    }
                    time = System.nanoTime();
                }
            }
        }
    }
    /*
        Create a new node.
     */
    public boolean createNode(String name, int port) {
        try {
            System.out.println("Creating Node...");
            _node = new JKademliaNode(name, new KademliaId(), port);
            System.out.println("Created Node: " + _node.getNode().getNodeId());
            return true;
        } catch(Exception e) {
            System.out.println("Error creating node");
            e.printStackTrace();
            return false;
        }
    }

    public void leaveNetwork() {
        try {
            _node.shutdown(false);
        } catch (Exception e) {
            System.out.println("Error shutting down");
        }
    }
    /*
        Connect the node to the network via the bootstrap server.
     */
    public boolean bootstrapNode() {
        try {
            Node bootstrapNode = new Node(new KademliaId("bootstrapserver00000"), java.net.InetAddress.getLoopbackAddress(), 7555);
            _node.bootstrap(bootstrapNode);
            return true;
        } catch(Exception e) {
            System.out.println("Error bootstrapping node");
            return false;
        }
    }

    public String getMessage(String id) {
        GetParameter gp = new GetParameter(new KademliaId(id), "MessageContent");
        try {
            KademliaStorageEntry entry = _node.get(gp);
            MessageContent newContent = (new MessageContent()).fromSerializedForm(entry.getContent());
            long ts = newContent.getCreatedTimestamp() * 1000L;
            //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            //sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            //String time = sdf.format(ts);
            /*
            String time = String.format("%02d:%02d:%02d",
                    (ts / (1000 * 60 * 60)) % 24,
                    (ts / (1000 * 60)) % 60,
                    (ts / 1000) % 60);
            */

            int hours = (int) ((ts / (1000*60*60)) % 24);
            int minutes = (int) ((ts / (1000*60)) % 60);
            String time = hours + ":" + minutes;

            return (time + " " + newContent.getData());
        } catch(kademlia.exceptions.ContentNotFoundException nc) {
            System.out.println("Content not found");
            return null;
        } catch (IOException e) {
            System.out.println("IO Exception: ");
            e.printStackTrace();
            return null;
        }
    }
/*
    public int getMessageCount() {
        //Check the amount of previous messages stored in the history.
        int messageCount = 0;
        GetParameter gp = new GetParameter(new KademliaId("nodemessageiter00000"), "MessageContent");
        try {
            KademliaStorageEntry entry = _node.get(gp);
            MessageContent newContent = (new MessageContent()).fromSerializedForm(entry.getContent());
            messageCount = Integer.parseInt(newContent.getData());
        } catch(kademlia.exceptions.ContentNotFoundException nc) {
            System.out.println("nodeMessageIter not found");
            setMessageCount(0);
            messageCount = 0;
        } catch (IOException e) {
            System.out.println("IO Exception: ");
            setMessageCount(0);
            messageCount = 0;
            e.printStackTrace();
            return -1;
        }
        return messageCount;
    }
*/
    public int getMessageCount() {
        int messageCount = 0;
        if(iterParam == null)
            iterParam = new GetParameter(new KademliaId("nodemessageiter00000"), "MessageContent");
        try {
            KademliaStorageEntry entry = _node.get(iterParam);
            MessageContent newContent = (new MessageContent()).fromSerializedForm(entry.getContent());
            messageCount = Integer.parseInt(newContent.getData());
        } catch (Exception e) {
            //System.out.println("Message Iter not found");
            setMessageCount(0);
            messageCount = 0;
        }
        return messageCount;
    }

    /*
        Set the iterator iterator for counting messages stored.
     */
    public void setMessageCount(int count) {
        try {
            MessageContent content = new MessageContent(_node.getNode().getNodeId().toString(),
                    new KademliaId("nodemessageiter00000"), (Integer.toString(count)));
            _node.put(content);
        } catch (IOException io) {
            //System.out.println("Error setting message count.");
            //io.printStackTrace();
        }
    }

    /*
        Print out the chat history.
     */
    public boolean getChatHistory() {

        //Iterate through existing messages and print to log, showing full chat history.
        for(int i = 0; i < localMessageCount; i++) {
            //Message id in format "nodemessage" + 0 padding + message number.
            //e.g. "nodemessage000000023"
            String messageId = "nodemessage" + String.format("%9s", Integer.toString(i)).replace(' ', '0');
            String message = getMessage(messageId);
            if (message != null)
                System.out.println(message);
        }
        return true;
    }

    /*
        Store a message in the DHT with the ID 'nodemessage000000000'
     */
    public void sendMessage(String message)
    {
        //Get the current message iterator
        int messageCount = getMessageCount();
        //Create Message ID appended with message iterator
        String messageId = "nodemessage"
            + String.format("%9s", Integer.toString(messageCount)).replace(' ', '0');
        ///System.out.println("sending - MessageID: " + messageId);
        //Create Message.
        MessageContent content = new MessageContent(_node.getNode().getNodeId().toString(),
                new KademliaId(messageId), message);
        //Increase local iterator to prevent listen thread reading its own message.
        localMessageCount++;
        try {
            _node.put(content);
            //Increate the message iterator on the DHT.
            setMessageCount(localMessageCount);
        } catch (IOException e) {
            System.out.println("Cannot store message.");
        }
    }

    /*
        Create new messenger instance.
     */
    public static void main(String[] args) {
        Messenger messenger = new Messenger(args);
    }
}
