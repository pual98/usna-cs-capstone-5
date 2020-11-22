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
    private boolean isCoordinator = false;
    boolean inGroup = false; //used to check if Client tries to join more than one CIDS
    String groupname = null;
    ObjectInputStream dis;
    ObjectOutputStream dos;
    Scanner scn;
    ArrayList<SharingEntity> receivedEntities = new ArrayList<SharingEntity>();

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
                            String msg = scn.nextLine();
                            Message m = new Message(1000, msg, ID, 0);
                            try {
                                // write on the output stream
                                dos.writeObject(msg);
                            } catch (IOException e) { e.printStackTrace(); }
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

                                //01:FROM:GROUPNAME - 01 message to server requesting to make GROUP, named
                                if(msg.type == 01){
                                    continue;
                                    //02:FROM:GROUPNAME - 02 request to join GROUPNAME - (server should forward to
                                }else if(msg.type == 02){
                                    String gn = msg.msg;
                                    int requestingID = msg.source;
                                    int reply = JOptionPane.showConfirmDialog(f, "Do you want to allow ID: "+requestingID+" to join "+gn+"?\n", "Collaboration Request", JOptionPane.YES_NO_OPTION);
                                    if (reply == JOptionPane.YES_OPTION) {
                                        Message toSend = new Message(03, gn+":accept", ID, requestingID);
                                        sendMessage(toSend);
                                        toSend = new Message(04, gn+":"+requestingID, ID, 0);
                                        sendMessage(toSend);
                                        groupname = gn;
                                    }else{
                                        Message toSend = new Message(03, gn+":deny", ID, requestingID);
                                        sendMessage(toSend);
                                    }
                                    continue;
                                    //03:FROM:MSG#TO - 03 response from coordinator to TO with "accept" or "deny"
                                }else if(msg.type == 03){
                                    String groupname = msg.msg.split(":")[0];
                                    if(msg.msg.contains("accept")) {
                                      JOptionPane.showMessageDialog(null, "You have been accepted into group "+groupname+".", "Confirmation", JOptionPane.INFORMATION_MESSAGE);
                                      setGroupStatus();
                                    }
                                    else
                                      JOptionPane.showMessageDialog(null, "You have been denied from group "+groupname+".", "Denial", JOptionPane.INFORMATION_MESSAGE);
                                    return ;

                                    //04:FROM:GROUPNAME:TOADD - 04 message from coordinator to server with ID TOADD
                                }else if(msg.type == 04){
                                    continue;
                              //05:FROM:GROUPNAME - message to server requesting list of IDs in GROUPNAME
                                }else if(msg.type == 05){
                                    //06:FROM:GROUPNAME:MSG - response from server with comma seperated MSG as a list of people in GROUPNAME
                                }else if(msg.type == 06){
                                    ArrayList<String> mems = msg.members;
                                    ArrayList<Integer> memIDs = new ArrayList<Integer>();
                                    for(int i = 0; i < mems.size(); i++) {
                                      memIDs.add(Integer.parseInt(mems.get(i)));
                                    }
                                    //call function to send all IDs
                                    continue;
                                    //10:FROM:MSG#TO - 10 Generic message. Send message to the TO
                                }else if(msg.type == 10){
                                    JOptionPane.showMessageDialog(f, msg.msg, "Incoming Message from Client: "+msg.source, JOptionPane.INFORMATION_MESSAGE);
                                }else if(msg.type == 12){
                                    receivedEntities.add(msg.en);
                                }else if(msg.type == 14){
                                    JOptionPane.showMessageDialog(null, msg.msg, "Error!", JOptionPane.ERROR_MESSAGE);
                                }else if(msg.type == 15){
                                    setGroupStatus();
                                    setAsCoordinator();
                                    JOptionPane.showMessageDialog(null, msg.msg, "Group Created", JOptionPane.INFORMATION_MESSAGE);
                                }
                            } catch (IOException e) { e.printStackTrace(); } catch (ClassNotFoundException e) { }
                        }
                    }
                });

        sendMessage.start();
        readMessage.start();

    }
    public void kPrototypes(){

        int NUM_CLUSTERS = 3;
        ArrayList<Entity> dataset = Dataset.build("three.csv");
        /* if coordinator then choose starting centroids, distribute starting cent, sigstart*/
        ArrayList<EntityCluster> clusters = new ArrayList<EntityCluster>();
        if(this.isCoordinator) {
            for(int i = 0; i < NUM_CLUSTERS; i++) {
                EntityCluster c = new EntityCluster(i);
                Entity randomCentroid = Entity.createRandomEntity(3,4); //params for createRandomEntity function depend on the # of attributes
                c.setCentroid(randomCentroid);
                clusters.add(c);
            }
        }
        /*
         * receive cents
         */
        boolean converged = false;
        while(!converged)
        {
            converged = true;
            for(Entity en : dataset)
            {
                //assign to cluster
                boolean interimConverged = assignCluster(en, clusters);
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
            for(EntityCluster c : clusters)
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
                while (receivedEntities.size() < 3){
                    try{
                        Thread.sleep(4000);
                    }catch (InterruptedException e) {}
                }
                for(SharingEntity se : receivedEntities)
                {
                    clusterData.addSharingEntity(se);
                }
                c = new EntityCluster(clusterData.toEntity(), c.getId());
            }
        }
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
