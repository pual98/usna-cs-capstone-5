// Java implementation for multithreaded chat client
// Save file as Client.java
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.Scanner;
import javax.swing.*;
import java.awt.event.*;
import java.lang.*;
import javax.swing.event.*;

public class Client implements Runnable
{
    final static int ServerPort = 1234;
    static int ID = 0;
    static int NUM_CLUSTERS = 0;
    private boolean isCoordinator = false;
    boolean inGroup = false; //used to check if Client tries to join more than one CIDS
    String groupname = null;
    ObjectInputStream dis;
    ObjectOutputStream dos;
    Scanner scn;
    ArrayList<SharingEntity> receivedEntities = new ArrayList<SharingEntity>();
    ArrayList<EntityCluster> clusters = null;
    ArrayList<Integer> memIDs = null;

    public Client() {
        boolean haveID = false;
        try{
            File myFile = new File(".config");
            if (myFile.createNewFile()){
                System.out.println("File is created!");
            }
            else{
                System.out.println("File already exists.");
            }
        } catch (FileNotFoundException e) { } catch (IOException e) { }
        try{
            Scanner scanner = new Scanner(new File(".config"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                System.out.println(line);
                if (line.contains("id")){
                    String arr[] = line.split(":");
                    if (arr[0].equals("id")){ haveID = true; ID = Integer.parseInt(arr[1]); }
                }
            }
        } catch (IOException e) { } catch (NullPointerException e) { }

        if (haveID == false) {
            try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(".config"))) {
                Random rand = new Random();
                int random = rand.nextInt(10000);
                String fileContent = "id:"+random;
                bufferedWriter.write(fileContent);
            } catch (IOException e) { }
        }
        scn = new Scanner(System.in);

        try {
            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");
            //InetAddress ip = InetAddress.getByName("midn.cs.usna.edu");

            // establish the connection
            Socket s = new Socket(ip, ServerPort);

            // obtaining input and out streams
            dos = new ObjectOutputStream(s.getOutputStream());
            dis = new ObjectInputStream(s.getInputStream());
        } catch(UnknownHostException e) {} catch (IOException e) {}
    }
    public int getID() {
        return ID;
    }
    public void sendMessage(Message m) {
        try {
            dos.writeObject(m);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void run() {
        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable()
                {
                    @Override
                    public void run() {
                        try {
                            System.out.println("About to request rename:");
                            System.out.flush();
                            // write on the output stream
                            Message m = new Message(1000, "new name id:"+ID, ID, 0);
                            dos.writeObject(m);
                        } catch (IOException e) { e.printStackTrace(); }

                        while (true) {
                            // read the message to deliver.
                            try {
                                String msg = scn.nextLine();
                                Message m = new Message(1000, msg, ID, 0);
                                dos.writeObject(msg);
                            } catch (IOException e) { e.printStackTrace(); } catch (NoSuchElementException e) { }
                        }
                    }
                });

        // readMessage thread
        Thread readMessage = new Thread(new Runnable()
                {
                    @Override
                    public void run() {

                        while (true) {
                            try {
                                // read the message sent to this client
                                Message msg = (Message)dis.readObject();
                                JFrame f = new JFrame();

                                System.out.println("Client "+ID+" log msg: "+msg);

                                //01: msg = GROUPNAME
                                //    01 message to server requesting to make GROUP, named GROUPNAME
                                if(msg.type == 01){
                                    continue;
                                //02: msg = GROUPNAME
                                //    02 request to join GROUPNAME - (server should forward to coordinator)
                                }else if(msg.type == 02){
                                    String gn = msg.msg;
                                    int requestingID = msg.source;
                                    int reply = JOptionPane.showConfirmDialog(f, "Do you want to allow ID: "+requestingID+" to join "+gn+"?\n", "Collaboration Request", JOptionPane.YES_NO_OPTION);
                                    if (reply == JOptionPane.YES_OPTION) {
                                        Message toSend = new Message(03, gn+":accept:"+NUM_CLUSTERS, ID, requestingID);
                                        sendMessage(toSend);
                                        toSend = new Message(04, gn+":"+requestingID, ID, 0);
                                        sendMessage(toSend);
//                                        groupname = gn;
                                    }else{
                                        Message toSend = new Message(03, gn+":deny", ID, requestingID);
                                        sendMessage(toSend);
                                    }
                                    continue;
                                //03: msg = MSG:"accept" || MSG:"deny"
                                //    03 response from coordinator to TO with "accept" or "deny"
                                }else if(msg.type == 03){
                                    String gn= msg.msg.split(":")[0];
                                    if(msg.msg.contains("accept")) {
                                        NUM_CLUSTERS = Integer.parseInt(msg.msg.split(":")[2]);
                                        JOptionPane.showMessageDialog(null, "You have been accepted into group "+gn+".\n          Number of clusters = "+NUM_CLUSTERS, "Confirmation", JOptionPane.INFORMATION_MESSAGE);
                                        setGroupStatus();
                                        groupname = gn;
                                    }
                                    else
                                        JOptionPane.showMessageDialog(null, "You have been denied from group "+groupname+".", "Denial", JOptionPane.INFORMATION_MESSAGE);
                                    //return;
                                    continue;
                                //04: msg = GROUPNAME:TOADD
                                //    04 message from coordinator to server with ID TOADD to join have join the GROUPNAME
                                }else if(msg.type == 04){
                                    continue;
                                //05: msg = GROUPNAME
                                //    message to server requesting list of IDs in GROUPNAME
                                }else if(msg.type == 05){
                                    continue;
                                //06: msg = MSG
                                //    response from server with comma seperated MSG as a list of people in
                                //    EXAMPLE: 12,13,1,2 where each is an ID
                                }else if(msg.type == 06){
                                    ArrayList<String> mems = msg.members;
                                    memIDs = new ArrayList<Integer>();
                                    for(int i = 0; i < mems.size(); i++) {
                                        memIDs.add(Integer.parseInt(mems.get(i)));
                                    }
                                    //call function to send all IDs
                                    continue;
                                //10: msg = MSG
                                //    10 Generic message. Send message to the TO
                                }else if(msg.type == 10){
                                    JOptionPane.showMessageDialog(f, msg.msg, "Message from "+msg.source, JOptionPane.INFORMATION_MESSAGE);
                                    continue;
                                //12: msg = MSG#TO
                                //    Send clusterData object (sharingEntity)
                                //    should set entity
                                }else if(msg.type == 12){
                                    receivedEntities.add(msg.en);
                                    continue;
                                //14: msg = Error Message to Client trying to Create/Join Group
                                //    Response from server to requesting client
                                }else if(msg.type == 14){
                                    JOptionPane.showMessageDialog(f, msg.msg, "Error!", JOptionPane.ERROR_MESSAGE);
                                    continue;
                                //15: msg = "Success, you have created group:GROUPNAME"
                                //    Response from server to client after creatign a group
                                }else if(msg.type == 15){
                                    setGroupStatus();
                                    setAsCoordinator();
                                    JOptionPane.showMessageDialog(f, msg.msg, "Group Created", JOptionPane.INFORMATION_MESSAGE);
                                    groupname = (msg.msg.split(":")[1]).split(" ")[1];

                                } else if(msg.type == 16){
                                    clusters = msg.clusters;
                                    continue;
                                } else if(msg.type == 18) {
                                    String group_name = msg.msg;
                                    msg.members.remove(Integer.toString(ID));
                                    Object[] partners = msg.members.toArray();
                                    String choice = (String) JOptionPane.showInputDialog(null, "Choose client in "+group_name, group_name+" clients", JOptionPane.INFORMATION_MESSAGE, null, partners, partners[0]);
                                    if(choice != null) {
                                      String message = null;
                                      message = JOptionPane.showInputDialog("Desired Message");
                                      if(!message.equals("") && message != null) {
                                        Message m = new Message(10, message, ID, Integer.parseInt(choice));
                                        sendMessage(m);
                                      }
                                    }
                                }
                                else if(msg.type == 19) {
                                  ArrayList<String> options = new ArrayList<String>();
                                  for(int i = 3; i < 11; i++)
                                    options.add(""+i);
                                  Object[] choices = options.toArray();
                                  String prompt = "         Enter the number \nof clusters for k-Prototypes\n                  (3-10)";
                                  String num = (String) JOptionPane.showInputDialog(null, prompt, "Select Cluster Number", JOptionPane.INFORMATION_MESSAGE, null, choices, choices[0]);
                                  if(num != null) {
                                    NUM_CLUSTERS = Integer.parseInt(num);
                                    Message m = new Message(20, msg.msg+":"+NUM_CLUSTERS, ID, 0);
                                    sendMessage(m);
                                  }
                                }
                            } catch (IOException e) { e.printStackTrace(); return;} catch (ClassNotFoundException e) { }
                        }
                    }
                });

        sendMessage.start();
        readMessage.start();

    }

    public void kPrototypes() {
        JFrame f = new JFrame();
        /* if coordinator then choose starting centroids, distribute starting cent, sigstart*/

        ArrayList<Entity> dataset = Dataset.build("threeClusters.csv");
        if (isCoordinator){
            //int NUM_CLUSTERS = 3;
            this.clusters = new ArrayList<EntityCluster>();
            if(this.isCoordinator) {
                for(int i = 0; i < NUM_CLUSTERS; i++) {
                    EntityCluster c = new EntityCluster(i);
                    Entity randomCentroid = Entity.createRandomEntity(3,4); //params for createRandomEntity function depend on the # of attributes
                    c.setCentroid(randomCentroid);
                    this.clusters.add(c);
                }
            }
            Message msg = new Message(16, groupname, ID, 0);
            msg.setClusters(this.clusters);
            sendMessage(msg);
        }
        /*
         * receive cents
         */
        while (this.clusters == null){
            try{
                Thread.sleep(500);
            }catch (InterruptedException e) {}
        }
        boolean converged = false;
        while(!converged)
        {
            converged = true;
            for(Entity en : dataset)
            {
                //assign to cluster
                boolean interimConverged = assignCluster(en, this.clusters);
                //assign to cluster
                if(!interimConverged)
                {
                    converged = false;
                }
            }
            if(converged)
            {
                break;
            }
            //for each cluster:
            for(EntityCluster c : this.clusters)
            {
                SharingEntity clusterData = new SharingEntity();
                // sumlocal
                for(Entity en : dataset)
                {
                    if(en.getAssignedCluster() == c.getId())
                    {
                        clusterData.addEntity(en);
                    }
                }

                Message msg = new Message(12, groupname, ID, 0);
                msg.setEntity(clusterData);
                sendMessage(msg);
                // wait on sums
                while (receivedEntities.size() < 2){
                    try{
                        Thread.sleep(500);
                    }catch (InterruptedException e) {}
                }
                for(SharingEntity se : receivedEntities)
                {
                    clusterData.addSharingEntity(se);
                }
                c = new EntityCluster(clusterData.toEntity(), c.getId());
            }
        }
            JOptionPane.showMessageDialog(null, "Client "+ID+" completed ID!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean assignCluster(Entity en, ArrayList<EntityCluster> clusters)
    {
        double max = Double.MAX_VALUE;
        double min = max;
        int cluster = 0;
        double distance = 0.0;
        boolean converged = true;

        min = max;
        for(int i = 0; i < clusters.size(); i++) {
            EntityCluster c = clusters.get(i);

            distance = Entity.distanceEuclidean(en, c.getCentroid());
            if(distance < min) {
                min = distance;
                cluster = i;
            }
        }

        if(cluster != en.getAssignedCluster()) {
            converged = false;
        }
        en.setCluster(cluster);
        //possible update to total number of entities in cluster
        return converged;
    }
    public boolean assignClusters(ArrayList<Entity> data, ArrayList<EntityCluster> clusters)
    {
        double max = Double.MAX_VALUE;
        double min = max;
        int cluster = 0;
        double distance = 0.0;
        boolean converged = true;
        for(Entity en : data) {
            min = max;
            for(int i = 0; i < clusters.size(); i++) {
                EntityCluster c = clusters.get(i);

                distance = Entity.distanceEuclidean(en, c.getCentroid());
                if(distance < min) {
                    min = distance;
                    cluster = i;
                }
            }

            if(cluster != en.getAssignedCluster()) {
                converged = false;
            }
            en.setCluster(cluster);
        }
        //possible update to total number of entities in cluster
        return converged;
    }

    public void setGroupStatus() { this.inGroup = true; }

    public boolean getGroupStatus() { return inGroup; }

    public void setAsCoordinator() { isCoordinator = true; }

    public boolean getCoordinatorStatus() { return isCoordinator; }

    public static void main(String args[]) throws UnknownHostException, IOException {
        Client client = new Client();
        Thread clientThread = new Thread(client);
        clientThread.start();
    }
}
