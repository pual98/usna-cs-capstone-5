// Java implementation for multithreaded chat client
// Save file as Client.java
import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.*;

public class Client implements Runnable
{
    final static int ServerPort = 1234;
    static int ID = 0;
    static int NUM_CLUSTERS = 0;
    static String ALGORITHM = "Distributed";
    private boolean isCoordinator = false;
    public boolean inGroup = false; //used to check if Client tries to join more than one CIDS
    public String groupname = null;
    public ObjectInputStream dis;
    public ObjectOutputStream dos;
    public Scanner scn;

    public ArrayList<SharingEntity> receivedEntities = new ArrayList<SharingEntity>();
    public ArrayList<SharingEntity> receivedShares = new ArrayList<SharingEntity>();
    public ArrayList<EntityCluster> clusters = null;
    public ArrayList<Integer> memIDs = new ArrayList<Integer>();
    public String filename;
    public ArrayList<Boolean> clustersPresent = new ArrayList<Boolean>();
    private Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public final static int DISTRIBUTED = 1;
    public final static int SECRET_SHARE = 0;
    public final static int DIFFERENTIAL_PRIVACY = 0;

    public Client() {
        boolean haveID = false;
        try{
            File myFile = new File(".config");
            if (myFile.createNewFile()){
                LOGGER.log(Level.INFO, "Client config file is created");
            }
            else{
                LOGGER.log(Level.INFO,"Client config file already exists");
            }
        } catch (FileNotFoundException e) { } catch (IOException e) { }
        try{
            Scanner scanner = new Scanner(new File(".config"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
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
                            LOGGER.log(Level.INFO, "Client requesting rename");
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

                                LOGGER.log(Level.INFO, ID+": client read msg: "+msg);

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
                                        Message toSend = new Message(03, gn+":accept:"+NUM_CLUSTERS+":"+ALGORITHM, ID, requestingID);
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
                                        ALGORITHM = msg.msg.split(":")[3];
                                        JOptionPane.showMessageDialog(null, "You have been accepted into group "+gn+".\n          Number of clusters = "+NUM_CLUSTERS+"\n       Algorithm = "+ALGORITHM, "Confirmation", JOptionPane.INFORMATION_MESSAGE);
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
                                    for(int i = 0; i < mems.size(); i++) {
                                        int idToAdd = Integer.parseInt(mems.get(i));
                                        if (!memIDs.contains(idToAdd)){
                                            memIDs.add(idToAdd);
                                        }
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

                                    int selectedAlg = 0;
                                    String algorithm = "";
                                    do{
                                        prompt = "Select the algorithm to be used";
                                        options = new ArrayList<String>();
                                        options.add("Distributed (none)");
                                        options.add("Secret sharing");
                                        options.add("Differential privacy");
                                        choices = options.toArray();
                                        algorithm = (String) JOptionPane.showInputDialog(null, prompt, "Select algorithm", JOptionPane.INFORMATION_MESSAGE, null, choices, choices[0]);
                                        if (algorithm.equals("Distributed (none)")){
                                            selectedAlg = DISTRIBUTED;
                                        }else if (algorithm.equals("Secret sharing")){
                                            selectedAlg = SECRET_SHARE;
                                        }else if (algorithm.equals("Differential Privacy")){
                                            selectedAlg = DIFFERENTIAL_PRIVACY;
                                        }
                                        if (selectedAlg == 0)
                                            JOptionPane.showMessageDialog(null,"Selected algorithm not yet implemented; please choose another." , "Error!", JOptionPane.ERROR_MESSAGE);
                                    }while(selectedAlg == 0);

                                    if(num != null) {
                                        NUM_CLUSTERS = Integer.parseInt(num);
                                        Message m = new Message(20, msg.msg+":"+NUM_CLUSTERS+":"+algorithm, ID, 0);
                                        sendMessage(m);
                                    }
                                }
                                else if(msg.type == 21) {
                                    receivedShares.add(msg.en);
                                    continue;
                                }
                            } catch (IOException e) { e.printStackTrace(); return;} catch (ClassNotFoundException e) { }
                        }
                    }
                });

        sendMessage.start();
        readMessage.start();

    }

    public void kPrototypes(ArrayList<Entity> dataset) {
        JFrame f = new JFrame();
        /* if coordinator then choose starting centroids, distribute starting cent, sigstart*/

        //ArrayList<Entity> dataset = Dataset.build("threeClusters.csv");
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

        Random r = new Random();
        boolean converged = false;
        int itt = 0;

        for(int i = 0; i < NUM_CLUSTERS; i++)
            clustersPresent.add(false);

        while(!converged) {
            int clabel = 0;
            for(EntityCluster c: clusters){
                c.setId(clabel);
                clabel++;
            }
            for (int i = 0; i < NUM_CLUSTERS; i++)
                LOGGER.log(Level.INFO, ID+": kPrototypes iteration "+itt+", cluster "+ i + ", centroid: "+ clusters.get(i).getCentroid());

            converged = true;

            for(Entity en : dataset) {
                //assign to cluster
                if(itt == 0){
                    assignRandomCluster(en,this.clusters,r);
                    converged = false;
                }
                else{
                    boolean interimConverged = assignCluster(en, this.clusters);
                    //assign to cluster
                    if(!interimConverged)
                        converged = false;
                }
            }
            itt++;

            //for each cluster:
            ArrayList<EntityCluster> nc = new ArrayList<EntityCluster>();
            for(EntityCluster c : this.clusters)
            {
                SharingEntity clusterData = new SharingEntity();
                clusterData.setConv(converged);
                // sumlocal
                for(Entity en : dataset){
                    if(en.getAssignedCluster() == c.getId())
                        clusterData.addEntity(en);
                }

                Message msg = new Message(12, groupname, ID, 0);
                clusterData.setClusterLabel(c.getId());
                clusterData.setIterationLabel(itt);
                msg.setEntity(clusterData);
                LOGGER.log(Level.INFO, "ID: " + ID + " iteration " + itt + " cluster " + c.getId() + " sending " + clusterData.toEntity() );
                sendMessage(msg);
                // wait on sums
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e) {}
                while (countFromIteration(receivedEntities, itt, c.getId()) < 2){
                    try{
                        Thread.sleep(500);
                    }catch (InterruptedException e) {}
                }
                ArrayList<SharingEntity> confirmedSharingEntities = new ArrayList<SharingEntity>();
                for(SharingEntity se : receivedEntities) {
                    if(se.getClusterLabel() == c.getId() && se.getIterationLabel() == itt)
                        confirmedSharingEntities.add(se);
                }
                for(SharingEntity se : confirmedSharingEntities) {
                    if(se.getConv() == false)
                        converged = false;
                    clusterData.addSharingEntity(se);
                }
                c = new EntityCluster(clusterData.toEntity(), c.getId());
                nc.add(c);
                receivedEntities.removeAll(confirmedSharingEntities);
            }
            this.clusters = nc;
        }

        //identify which clusters this client's data belong to
        for(int i = 0; i < dataset.size(); i++) {
            int num = dataset.get(i).getAssignedCluster();
            clustersPresent.set(num, true);
        }

        String centroidPopup = "                                            Cluster Centroids:\n";
        for(int i = 0; i < NUM_CLUSTERS; i++) {
            Entity centroid = clusters.get(i).getCentroid();
            centroid.setCluster(i);
            centroidPopup += centroid.toString()+"\n";
        }
        JOptionPane.showMessageDialog(null, centroidPopup, "Cluster Centroids", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void assignRandomCluster(Entity en, ArrayList<EntityCluster> clusters, Random r) {
        int cluster = r.nextInt(clusters.size());
        en.setCluster(cluster);
    }

    public static boolean assignCluster(Entity en, ArrayList<EntityCluster> clusters) {
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

    public int[] unaryEncode(int c){
        // Based on max port number
        int vector[] = new int[65535];
        for (int i = 0; i < vector.length; i++)
            vector[i] = 0;
        vector[c] = 1;
        return vector;
    }

    public int[] perturb(int[] vector){
        Random rand = new Random();
        double p = 1/2;
        double epsilon = 1;
        double q = 1/(java.lang.Math.exp(epsilon) + 1);
        for (int i = 0; i < vector.length; i++)
            if (vector[i] == 1)
                if(rand.nextFloat() <= p)
                    vector[i] = 0;
                else if (vector[i] == 0)
                    if(rand.nextFloat() <= q)
                        vector[i] = 1;
        return vector;
    }
    public int[] decode(int[][] vectorAggregate){
        // TODO : Should return a vector of integers where vector[i] represents the
        // estimated frequency i occurred in the aggregate of vectors
        int[] vector = new int[vectorAggregate[0].length];
        return vector;
    }
    public void setGroupStatus() { this.inGroup = true; }

    public boolean getGroupStatus() { return inGroup; }

    public void setAsCoordinator() { isCoordinator = true; }

    public boolean getCoordinatorStatus() { return isCoordinator; }

    public void correlateNewData(ArrayList<Entity> newData) {
        ArrayList<Integer> newClusters = new ArrayList<Integer>();

        for(int i = 0; i < newData.size(); i++) {
            int minIndex = 0;
            double min = -1 ;
            for(int j = 0; j < clusters.size(); j++) {
                if(clusters.get(j).getCentroid().isNaN()) // skip NaN centroids
                    continue;

                if(min == -1){
                    min = Entity.distanceEuclidean(newData.get(i), clusters.get(j).getCentroid());
                    minIndex = j ;
                }
                else{
                    double dist = Entity.distanceEuclidean(newData.get(i), clusters.get(j).getCentroid());
                    if(dist < min){
                        minIndex = j;
                        min = dist;
                    }
                }
            }
            //assign a cluster to the new instance of data
            int clusterID = clusters.get(minIndex).getId();
            newData.get(i).setCluster(clusterID);

            //check for data that appears in a new cluster (cluster that wasn't used by initial data)
            if(!clustersPresent.get(clusterID)) {
                newClusters.add(clusterID);
                clustersPresent.set(clusterID, true);
            }
        }

        System.out.println("newClusters = " + newClusters.size());
        int len = newClusters.size();
        if(len != 0) {
            String alert = "Data Correlated to New Cluster";
            if(len == 1)
                alert += ": " + newClusters.get(0);
            else {
                alert += "s: ";
                for(int j = 0; j < len; j++) {
                    if(j == len-1)
                        alert += newClusters.get(j);
                    else
                        alert += newClusters.get(j) + ", ";
                }
            }
            JOptionPane.showMessageDialog(null, alert, "New Alert Type(s) Found!", JOptionPane.INFORMATION_MESSAGE);
        }
        else
            JOptionPane.showMessageDialog(null, "Correlation Complete", "No New Alert Types Found!", JOptionPane.INFORMATION_MESSAGE);




    }

    public void SecretShareDiff(ArrayList<Entity> dataset) {
        JFrame f = new JFrame();
        /* if coordinator then choose starting centroids, distribute starting cent, sigstart*/

        //ArrayList<Entity> dataset = Dataset.build("threeClusters.csv");
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

        Random r = new Random();
        boolean converged = false;
        int itt = 0;

        for(int i = 0; i < NUM_CLUSTERS; i++)
            clustersPresent.add(false);

        while(!converged) {
            int clabel = 0;
            for(EntityCluster c: clusters){
                c.setId(clabel);
                clabel++;
            }
            for (int i = 0; i < NUM_CLUSTERS; i++)
                LOGGER.log(Level.INFO, ID+": secret sharing iteration "+itt+", cluster "+ i + ", centroid: "+ clusters.get(i).getCentroid());

            converged = true;

            for(Entity en : dataset) {
                //assign to cluster
                if(itt == 0){
                    assignRandomCluster(en,this.clusters,r);
                    converged = false;
                }
                else{
                    boolean interimConverged = assignCluster(en, this.clusters);
                    //assign to cluster
                    if(!interimConverged)
                        converged = false;
                }
            }
            itt++;

            //for each cluster:
            ArrayList<EntityCluster> nc = new ArrayList<EntityCluster>();
            for(EntityCluster c : this.clusters)
            {
                SharingEntity clusterData = new SharingEntity();
                clusterData.setIterationLabel(itt);
                clusterData.setClusterLabel(c.getId());
                clusterData.setConv(converged);
                // sumlocal
                for(Entity en : dataset){
                    if(en.getAssignedCluster() == c.getId())
                      clusterData.addEntity(en);
                }

                System.out.println("AFTER ADDING ENTITIES TO CLUSTER DATA: "+ ID + " countShare = " + clusterData.getCountShare());

                ArrayList<SharingEntity> shares = clusterData.makeShares(3, new Random());

                while (memIDs.size() < 3){
                    Message requestForPartners = new Message(5, groupname, ID, 0);
                    // TODO: verify better message sending protocol
                    sendMessage(requestForPartners);
                    LOGGER.log(Level.INFO, "ID: " + ID + " requesting list of partners");
                    try{
                        Thread.sleep(1000);
                    }catch (InterruptedException e) {}
                }

                int count = 0;
                for ( int id : memIDs ){
                    if (id == ID){
                        receivedShares.add(shares.get(count));
                    }else{
                        // Send shares
                        Message msg = new Message(21, groupname, ID, id);
                        msg.setEntity(shares.get(count));
                        sendMessage(msg);
                        LOGGER.log(Level.INFO, "ID: " + ID + " sending share "+count);
                    }
                    count++;
                }

                // wait on shares
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e) {}
                while (countFromIteration(receivedShares, itt, c.getId()) < 3){
                    LOGGER.log(Level.INFO, "ID: " + ID + " waiting for receivedShares");
                    try{
                        Thread.sleep(500);
                    }catch (InterruptedException e) {}
                }

                LOGGER.log(Level.INFO, "ID: "+ ID + " received: ");
                for(SharingEntity se: receivedShares) {
                  LOGGER.log(Level.INFO, se.toEntity().toString());
                }
                // All shares received by now
                LOGGER.log(Level.INFO, "ID: " + ID + " received 3 shares (including own)");

                SharingEntity intermediateEntity = new SharingEntity();
                ArrayList<SharingEntity> intermediateConfirmed = new ArrayList<SharingEntity>();

                for (SharingEntity en : receivedShares){
                    if(en.getClusterLabel() == c.getId() && en.getIterationLabel() == itt){
                        intermediateEntity.addSharingEntity(en);
                        intermediateConfirmed.add(en);
                    }
                }

                // Add categorical data

                /*
                Message msg = new Message(12, groupname, ID, 0);
                clusterData.setClusterLabel(c.getId());
                clusterData.setIterationLabel(itt);
                msg.setEntity(clusterData);
                LOGGER.log(Level.INFO, "ID: " + ID + " iteration " + itt + " cluster " + c.getId() + " sending " + clusterData.toEntity() );
                sendMessage(msg);
                */

                ArrayList<SharingEntity> confirmedSharingEntities = new ArrayList<SharingEntity>();

                Message msg = new Message(12, groupname, ID, 0);
                LOGGER.log(Level.INFO, "ID: " + ID + " setting cluster label to c.getId(): "+c.getId());
                intermediateEntity.setClusterLabel(c.getId());
                intermediateEntity.setIterationLabel(itt);
                msg.setEntity(intermediateEntity);
                LOGGER.log(Level.INFO, "ID: " + ID + " iteration " + itt + " cluster " + c.getId() + " sending " + intermediateEntity.toEntity() );
                sendMessage(msg);

                confirmedSharingEntities.add(intermediateEntity);
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e) {}
                while (countFromIteration(receivedEntities,itt, c.getId()) < 2){
                    LOGGER.log(Level.INFO, "ID: " + ID + " waitingfor receivedEntities");
                    try{
                        Thread.sleep(500);
                    }catch (InterruptedException e) {}
                }

                for(SharingEntity se : receivedEntities) {
                    if(se.getClusterLabel() == c.getId() && se.getIterationLabel() == itt)
                        confirmedSharingEntities.add(se);
                }

                clusterData = new SharingEntity();
                for(SharingEntity se : confirmedSharingEntities) {
                    if(se.getConv() == false)
                        converged = false;
                    clusterData.addSharingEntity(se);
                }
                LOGGER.log(Level.INFO, "countShare = " + clusterData.getCountShare());
                c = new EntityCluster(clusterData.toEntity(), c.getId());
                nc.add(c);

                receivedEntities.removeAll(confirmedSharingEntities);
                receivedShares.removeAll(intermediateConfirmed);
                LOGGER.log(Level.INFO, "ID: " + ID + " completed one revolution");
                LOGGER.log(Level.INFO, "ID: " + ID + " final centroid val: "+ c.getCentroid().toString());
            }

            this.clusters = nc;
        }

        //identify which clusters this client's data belong to
        for(int i = 0; i < dataset.size(); i++) {
            int num = dataset.get(i).getAssignedCluster();
            clustersPresent.set(num, true);
        }

        String centroidPopup = "                                            Cluster Centroids:\n";
        for(int i = 0; i < NUM_CLUSTERS; i++) {
            Entity centroid = clusters.get(i).getCentroid();
            centroid.setCluster(i);
            centroidPopup += centroid.toString()+"\n";
        }
        JOptionPane.showMessageDialog(null, centroidPopup, "Cluster Centroids", JOptionPane.INFORMATION_MESSAGE);
    }
    /*
Given:
p, the number of parties
cluster the entityCluster to have it's centroid recalculated
An entity consists of qualities(a list of real numbers), and categories(integers)
An entityCluster consists of qualities(a list of real numbers), and categories(a list of hashmaps of a key paired with an integer)
     *
     Cluster Centroid Calculation with Distributed Secret Sharing and Differential Privacy
     for each cluster:
     set clusterSum as new EntityCluster
     set bitmaps as empty list
     for each entity, e:
     if e is in cluster:
     set clusterSum = clusterSum + entity
     for each category, c in clusterSum:
     for each key in c:
     set c(key) = perturb( UnaryEncode(c(key)))
     set centroidCategories = aggregate(clusterSum.categories)
     shares = makeSharesNoCategories(p, clusterSum)
     for each share, s in shares:
     send s to respective party
     wait to receive one share from each party
     for each received share, r:
     clusterSum = clusterSum + r
     set clusterSum.categories = centroidCategories
     for each party:
     send clusterSum
     wait to receive otherClusterSums
     for each receivedClusterSum r:
     clusterSum = clusterSum + r
     for each quality q in clusterSum:
     set q  in newCentroid as  (q / number of entries in cluster)
     for each category c in clusterSum:
     set c in newCentroid as (key with the highest value in c)
     return newCentroid

Given: limit, a constant, large prime number
*/


    public static int countFromIteration(ArrayList<SharingEntity> se, int itt, int cl){
        int count = 0;
        for ( SharingEntity e : se){
            if (e.getIterationLabel() == itt && e.getClusterLabel()==cl)
                count++;
        }
        return count;
    }

    public static void main(String args[]) throws UnknownHostException, IOException {
        Client client = new Client();
        Thread clientThread = new Thread(client);
        clientThread.start();
    }
}
