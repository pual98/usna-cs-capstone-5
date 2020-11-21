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
    boolean isCoordinator = false; 
    DataInputStream dis;
    DataOutputStream dos;
    Scanner scn;
    ArrayList<Integer> requests = new ArrayList<Integer>();


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
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
        } catch(UnknownHostException e) {} catch (IOException e) {}
    }
    public int getID() {
        return ID;
    }
    public void sendMessage(String msg, int toSendTo) {
        msg+="#";
        msg+=toSendTo;
        try {
            // write on the output stream
            dos.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg, String group) {
        msg+="#";
        msg+=group;
        try {
            // write on the output stream
            dos.writeUTF(msg);
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
                            // write on the output stream
                            dos.writeUTF("new name id:"+ID);
                        } catch (IOException e) { e.printStackTrace(); }

                        while (true) {
                            // read the message to deliver.
                            String msg = scn.nextLine();
                            try {
                                // write on the output stream
                                dos.writeUTF(msg);
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
                                String msg = dis.readUTF();
                                JFrame f = new JFrame();

                                System.out.println("Client "+ID+" log msg: "+msg);

                                //01:FROM:GROUPNAME - 01 message to server requesting to make GROUP, named
                                if(msg.substring(0,2).equals("01")){
                                    continue;
                                //02:FROM:GROUPNAME - 02 request to join GROUPNAME - (server should forward to
                                }else if(msg.substring(0,2).equals("02")){
                                    String arr[] = msg.split(":");
                                    String groupname = arr[2].split("#")[0];
                                    String reqID = arr[1];
                                    int reply = JOptionPane.showConfirmDialog(f, "Do you want to allow ID: "+reqID+" to join "+groupname+"?\n", "Collaboration Request", JOptionPane.YES_NO_OPTION);
                                    if (reply == JOptionPane.YES_OPTION) {
                                        sendMessage("03:"+ID+":accept", Integer.parseInt(reqID));
                                        sendMessage("04:"+ID+":"+groupname+":"+reqID, Integer.parseInt(reqID));
                                    }else{
                                        sendMessage("03:"+ID+":deny", Integer.parseInt(reqID));
                                    }
                                    continue;
                                //03:FROM:MSG#TO - 03 response from coordinator to TO with "accept" or "deny"
                                }else if(msg.substring(0,2).equals("03")){
                                    continue;
                                //04:FROM:GROUPNAME:TOADD - 04 message from coordinator to server with ID TOADD
                                }else if(msg.substring(0,2).equals("04")){
                                    continue;

                                //05:FROM:GROUPNAME - message to server requesting list of IDs in GROUPNAME
                                }else if(msg.substring(0,2).equals("05")){
                                    continue;
                                //06:FROM:GROUPNAME:MSG - response from server with comma seperated MSG as a list of people in GROUPNAME
                                }else if(msg.substring(0,2).equals("06")){
                                    continue;
                                //10:FROM:MSG#TO - 10 Generic message. Send message to the TO
                                }else if(msg.substring(0,2).equals("10")){
                                    JOptionPane.showMessageDialog(f,msg);
                                } 
                            } catch (IOException e) { e.printStackTrace(); }
                        }
                    }
                });

        sendMessage.start();
        readMessage.start();

//        /*
//         *TO DO: readInEntities
//         */
//
//        Dataset D = new Dataset();
//        ArrayList<Entity> dataset = D.build("three.csv");
//        /* if coordinator then choose starting centroids, distribute starting cent, sigstart*/
//        int NUM_CLUSTERS = 3;
//        ArrayList<EntityCluster> clusters;
//        if(this.isCoordinator) {
//            for(int i = 0; i < NUM_CLUSTERS; i++) {
//                EntityCluster c = new EntityCluster(i);
//                Entity randomCentroid = Entity.createRandomEntity(3,4); //params for createRandomEntity function depend on the # of attributes
//                c.setCentroid(randomCentroid);
//                clusters.add(c);
//            }
//        }
//        /*
//         * receive cents
//         */
//        boolean converged = false;
//        while(!converged)
//        {
//            converged = true;
//            for(Entity en : dataset)
//            {
//                //assign to cluster
//                interimConverged = en.assignClusters(clusters);
//                //assign to cluster
//                if(!interimConverged)
//                {
//                    converged = false;
//                }
//            }
//            if(converged)
//            {
//                break;
//            }
//            //for each cluster:
//            for(EntityCluster c : clusters)
//            {
//                SharingEntity clusterData = new SharingEntity();
//                // sumlocal
//                for(Entity en : dataset)
//                {
//                    if(en.getAssignedCluster() == c.getId())
//                    {
//                        clusterData.addEntity(en);
//                    }
//                }
//                //send sum to all
//                for(OtherClient ot : others)
//                {
//                    ot.send(clusterData);
//                }
//                // wait on sums
//                received = waitOnSums();
//
//                for(SharingEntity se : received)
//                {
//                    clusterData.addSharingEntity(se);
//                }
//                c = clusterData.toEntity();
//            }
//
//        }

        //display centroids

    }


//    public boolean assignClusters(ArrayList<EntityCluster> clusters)
//    {
//        double max = Double.MAX_VALUE;
//        double min = max;
//        int cluster = 0;
//        double distance = 0.0;
//        boolean converged = true;
//        for(Entity en : data) {
//            min = max;
//            for(int i = 0; i < clusters.size(); i++) {
//                EntityCluster c = clusters.get(i);
//
//                distance = Entity.distanceEuclidean(point, c.getCentroid());
//                if(distance < min) {
//                    min = distance;
//                    cluster = i;
//                }
//            }
//
//            if(cluster != en.getCluster()) {
//                converged = false;
//            }
//            en.setCluster(cluster);
//        }
//        //possible update to total number of entities in cluster
//        return converged;
//    }
//

    public static void main(String args[]) throws UnknownHostException, IOException {
        Client client = new Client();
        Thread clientThread = new Thread(client);
        clientThread.start();
    }
}
