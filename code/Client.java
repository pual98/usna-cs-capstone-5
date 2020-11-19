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
    DataInputStream dis;
    DataOutputStream dos;
    Scanner scn;
    ArrayList<Integer> collaborators = new ArrayList<Integer>();
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

        // Read the content from file

        // Read the content from file
        try{
            Scanner scanner = new Scanner(new File(".config"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                System.out.println(line);
                if (line.contains("id")){
                    String arr[] = line.split(":");
                    if (arr[0].equals("id")){ haveID = true; ID = Integer.parseInt(arr[1]); }
                }
                if (line.contains("collaborator")){
                    String arr[] = line.split(":");
                    if (arr[0].equals("collaborator")){ collaborators.add(Integer.parseInt(arr[1])); }
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

    public void addCollaborator(int idCollab) {
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(".config", true))) {
            String fileContent = "\ncollaborator:"+idCollab;
            bufferedWriter.write(fileContent);
            collaborators.add(idCollab);
        } catch (IOException e) { }
    }

    public int getID() {
        return ID;
    }

    public void addRequest(int waitingRequest) {
        this.requests.add(waitingRequest);
        this.sendMessage("Collaboration request:"+ID,waitingRequest);
    }

    public void sendMessageAll(String msg) {
        if (collaborators.size() < 2){
            JFrame f = new JFrame();
            JOptionPane.showMessageDialog(f,"You need three collaborators to send a message");
            return;
        }
        for ( int collab : collaborators ){
            sendMessage(msg, collab);
        }
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
                            msg+="#";
                            for ( int collab : collaborators ){
                                msg+=collab+",";
                            }
                            msg = msg.substring(0, msg.length() - 1);
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
                                if (msg.contains("Collaboration request")){
                                    String reqID = msg.split(":")[0];
                                    reqID = reqID.replaceAll("\\s+","");
                                    int reply = JOptionPane.showConfirmDialog(f, "Do you want to collaborate with ID: "+reqID+"\n", "Collaboration Request", JOptionPane.YES_NO_OPTION);
                                    if (reply == JOptionPane.YES_OPTION) {
                                        addCollaborator(Integer.parseInt(reqID));
                                        sendMessage("Collaboration approval:"+ID, Integer.parseInt(reqID));
                                    }
                                } else if (msg.contains("Collaboration approval")){
                                    String reqID = msg.split(":")[0];
                                    reqID = reqID.replaceAll("\\s+","");
                                    int ireqID = Integer.parseInt(reqID);
                                    if (requests.contains(ireqID)){
                                        //              requests.remove(ireqID);
                                        addCollaborator(ireqID);
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(f,msg);
                                }
                            } catch (IOException e) { e.printStackTrace(); }
                        }
                    }
                });

        sendMessage.start();
        readMessage.start();

        /*
         *TO DO: readInEntities
         */

        ArrayList<Entity> dataset = Dataset.build();
        /* if coordinator then choose starting centroids, distribute starting cent, sigstart*/
        ArrayList<EntityCluster> clusters = coordinator.getInitialCluster();//to do
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
                interimConverged = en.assignClusters(clusters);
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
                //send sum to all
                for(OtherClient ot : others)
                {
                    ot.send(clusterData);
                }
                // wait on sums
                received = waitOnSums();

                for(SharingEntity se : received)
                {
                    clusterData.addSharingEntity(se);
                }
                c = clusterData.toEntity();
            }

        }

        //display centroids

    }


    public boolean assignClusters(ArrayList<EntityCluster> clusters)
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

                distance = Entity.distanceEuclidean(point, c.getCentroid());
                if(distance < min) {
                    min = distance;
                    cluster = i;
                }
            }

            if(cluster != en.getCluster()) {
                converged = false;
            }
            en.setCluster(cluster);
        }
        //possible update to total number of entities in cluster
        return converged;
    }


    public static void main(String args[]) throws UnknownHostException, IOException {
        Client client = new Client();
        Thread clientThread = new Thread(client);
        clientThread.start();
    }
}
