import kademlia.DefaultConfiguration;
import kademlia.JKademliaNode;
import kademlia.dht.KadContent;
import kademlia.message.SimpleMessage;
import kademlia.message.SimpleReceiver;
import kademlia.node.KademliaId;
import kademlia.routing.KademliaRoutingTable;
import kademlia.simulations.DHTContentImpl;

import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by t_j_w on 09/02/2016.
 */
public class Test {
    JKademliaNode[] kads;
    public int numKads = 10;

    public Test() {
        try {
            this.kads = new JKademliaNode[this.numKads];
            this.kads[0] = new JKademliaNode("user0", new KademliaId("HRF456789SD584567460"), 1334);
            this.kads[1] = new JKademliaNode("user1", new KademliaId("ASF456789475DS567461"), 1209);
            this.kads[2] = new JKademliaNode("user2", new KademliaId("AFG45678947584567462"), 4585);
            this.kads[3] = new JKademliaNode("user3", new KademliaId("FSF45J38947584567463"), 8104);
            this.kads[4] = new JKademliaNode("user4", new KademliaId("ASF45678947584567464"), 8335);
            this.kads[5] = new JKademliaNode("user5", new KademliaId("GHF4567894DR84567465"), 13345);
            this.kads[6] = new JKademliaNode("user6", new KademliaId("ASF45678947584567466"), 12049);
            this.kads[7] = new JKademliaNode("user7", new KademliaId("AE345678947584567467"), 14585);
            this.kads[8] = new JKademliaNode("user8", new KademliaId("ASAA5678947584567468"), 18104);
            this.kads[9] = new JKademliaNode("user9", new KademliaId("ASF456789475845674U9"), 18335);

            for(int e = 1; e < this.numKads; ++e) {
                this.kads[e].bootstrap(this.kads[0].getNode());
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public KadContent putContent(String content, JKademliaNode owner) {
        DHTContentImpl c = null;

        try {
            c = new DHTContentImpl(owner.getOwnerId(), "Some Data");
            owner.put(c);
            return c;
        } catch (IOException var5) {
            System.err.println("Error whiles putting content " + content + " from owner: " + owner.getOwnerId());
            return c;
        }
    }

    public void shutdownKad(JKademliaNode kad) {
        try {
            kad.shutdown(false);
        } catch (IOException var3) {
            System.err.println("Error whiles shutting down node with owner: " + kad.getOwnerId());
        }

    }

    public void printRoutingTable(int kadId) {
        System.out.println(this.kads[kadId].getRoutingTable());
    }

    public void printRoutingTables() {
        for(int i = 0; i < this.numKads; ++i) {
            this.printRoutingTable(i);
        }

    }

    public void printStorage(int kadId) {
        System.out.println(this.kads[kadId].getDHT());
    }

    public void printStorage() {
        for(int i = 0; i < this.numKads; ++i) {
            this.printStorage(i);
        }

    }

    public static void main(String[] args) {
        Test rtss = new Test();

        try {
            rtss.printRoutingTables();
            rtss.shutdownKad(rtss.kads[3]);
            rtss.putContent("Content owned by kad0", rtss.kads[0]);
            rtss.printStorage();
            Thread.sleep(1000L);
            rtss.printRoutingTables();
        } catch (InterruptedException var5) {
            ;
        }

        Scanner sc = new Scanner(System.in);

        while(true) {
            while(true) {
                System.out.println("\n\n ************************* Options **************************** \n");
                System.out.println("1 i - Print routing table of node i");
                int val1 = sc.nextInt();
                int val2 = sc.nextInt();
                switch(val1) {
                    case 1:
                        rtss.printRoutingTable(val2);
                }
            }
        }
    }
}
