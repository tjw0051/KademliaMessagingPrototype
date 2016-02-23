import kademlia.JKademliaNode;
import kademlia.dht.GetParameter;
import kademlia.dht.KademliaStorageEntry;
import kademlia.node.KademliaId;
import kademlia.simulations.DHTContentImpl;

import java.util.Scanner;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by t_j_w on 09/02/2016.
 */
public class BootstrapNode {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            JKademliaNode b = new JKademliaNode("bootstrap", new KademliaId("bootstrapserver00000"), 7555);
            System.out.println("Created Bootstrap Node: " + b.getNode().getNodeId());
            while(true)
            {
                String message = scanner.nextLine();
                switch (message)
                {
                    case "/route": {
                        System.out.println(b.getRoutingTable());
                        break;
                    }
                    case "/read": {
                        GetParameter gp = new GetParameter(new KademliaId("nodemessage000000000"), "DHTContentImpl");
                        KademliaStorageEntry conte = b.get(gp);
                        DHTContentImpl newCont = (new DHTContentImpl()).fromSerializedForm(conte.getContent());
                        System.out.println("Content: " + newCont);
                        break;
                    }
                    case "/dht": {
                        System.out.println(b.getDHT());
                        break;
                    }
                    case "/refresh": {
                        b.refresh();
                    }
                }
            }

            //while(true) {}

        } catch (Exception e) {
            System.out.println("Exception Caught");
        }
    }
}
