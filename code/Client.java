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
    final static int CheckerPort = 1235;
    static int ID = 0;
    static int NUM_CLUSTERS = 0;
    static String algorithm = "";
    private boolean isCoordinator = false;
    public boolean inGroup = false; //used to check if Client tries to join more than one CIDS
    public String groupname = null;
    public volatile DataOutputStream dos;
    public volatile DataInputStream dis;
    public volatile Socket s;
    public volatile long bytesSent = 0;

    public volatile ArrayList<SharingEntity> receivedEntities = new ArrayList<SharingEntity>();
    public volatile ArrayList<SharingEntity> receivedShares = new ArrayList<SharingEntity>();
    public volatile ArrayList<EntityCluster> clusters = null;
    public volatile ArrayList<Integer> memIDs = new ArrayList<Integer>();
    public volatile ArrayList<Boolean> clustersPresent = new ArrayList<Boolean>();
    public volatile ArrayList<Entity> uploadedData = new ArrayList<Entity>();
    public String filename;
    private Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public int numMembersinGroup = 0;
    public final static int DISTRIBUTED = 1;
    public final static int SECRET_SHARE = 1;
    public final static int DIFFERENTIAL_PRIVACY = 1;

    // for purposes of calculating MSE //
    public ArrayList<SharingEntity> clusterEntity  ;
    private double epsilon = 1.0 ;
    
    public Client() {
        LOGGER.setLevel(Level.SEVERE);
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
                ID = random;
                String fileContent = "id:"+random;
                bufferedWriter.write(fileContent);
            } catch (IOException e) { }
        }

        try {
            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");
            //InetAddress ip = InetAddress.getByName("csmidn.academy.usna.edu");

            // establish the connection
            s = new Socket(ip, ServerPort);
            while (!s.isConnected()){}

            // obtaining input and out streams
            dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));

            LOGGER.log(Level.INFO, "Client requesting rename");
            System.out.flush();
            // write on the output stream
            Message m = new Message(1000, "new name id:"+ID, ID, 0);
            sendMessage(m);
        } catch(UnknownHostException e) {} catch (IOException e) {}
    }
    public int getID() {
        return ID;
    }
    public  void sendMessage(Message m) {
        byte[] yourBytes;
        try {
            yourBytes = serialize(m);

            dos.writeInt(yourBytes.length);
            dos.flush();
            dos.write(yourBytes,0, yourBytes.length);
            dos.flush();
            this.bytesSent = this.bytesSent + yourBytes.length;
        } catch (IOException e) {} 
    }
    public void logout(){
        Message m = new Message(0,"logout",ID,0);
        sendMessage(m);
    }
    public  void run() {
        Message msg;
        while (true) {
            try {
                // https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
                // read the message sent to this client
                
                while (dis.available() == 0){}
                int size = dis.readInt();
                byte[] yourBytes = new byte[size];

                dis.mark(2*size);
                int available = 0;
                while (available < size){
                    try{
                        dis.readByte();
                        available++;
                    }catch (EOFException e){
                        available = 0;
                        dis.reset();
                        dis.mark(2*size);
                    }
                }
                dis.reset();
                dis.readFully(yourBytes);

                msg = (Message)deserialize(yourBytes);

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
                        Message toSend = new Message(03, gn+":accept:"+NUM_CLUSTERS+":"+algorithm, ID, requestingID);
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
                        algorithm = msg.msg.split(":")[3];
                        JOptionPane.showMessageDialog(null, "You have been accepted into group "+gn+".\n          Number of clusters = "+NUM_CLUSTERS+"\n       Algorithm = "+algorithm, "Confirmation", JOptionPane.INFORMATION_MESSAGE);
                        setGroupStatus();
                        groupname = gn;
                    }
                    else
                        JOptionPane.showMessageDialog(null, "You have been denied from group "+gn+".", "Denial", JOptionPane.INFORMATION_MESSAGE);
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
                }else if(msg.type == 06){
                    ArrayList<String> mems = msg.members;
                    numMembersinGroup = mems.size();
//                    System.out.println(ID+": "+numMembersinGroup+" group mems "+mems.size());
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
                    continue;
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
                    continue;
                }
                else if(msg.type == 19) {
                    ArrayList<String> options = new ArrayList<String>();
                    for(int i = 3; i < 11; i++)
                        options.add(""+i);
                    Object[] choices = options.toArray();
                    String prompt = "         Enter the number \nof clusters for k-Prototypes\n                  (3-10)";
                    String num = (String) JOptionPane.showInputDialog(null, prompt, "Select Cluster Number", JOptionPane.INFORMATION_MESSAGE, null, choices, choices[0]);

                    int selectedAlg = 0;
                    do{
                        prompt = "Select the algorithm to be used";
                        options = new ArrayList<String>();
                        options.add("Distributed (none)");
                        options.add("Secret Sharing");
                        options.add("Differential Privacy");
                        choices = options.toArray();
                        algorithm = (String) JOptionPane.showInputDialog(null, prompt, "Select algorithm", JOptionPane.INFORMATION_MESSAGE, null, choices, choices[0]);
                        if (algorithm.equals("Distributed (none)")){
                            selectedAlg = DISTRIBUTED;
                        }else if (algorithm.equals("Secret Sharing")){
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
                    continue;
                }
                else if(msg.type == 21) {
                    receivedShares.add(msg.en);
                    continue;
                }
                else if(msg.type == 24){
                    setGroupStatus();
                    int toAdd = Integer.parseInt(msg.msg);
                    if (!memIDs.contains(toAdd)){
                        memIDs.add(toAdd);
                        numMembersinGroup++;
                    }
                    continue;
                }
            } catch (IOException e) { e.printStackTrace(); return;} catch (ClassNotFoundException e) { }
        }
    }

    public  void kPrototypes(ArrayList<Entity> dataset) {
        for(int i = 0; i < dataset.size(); i++){
          uploadedData.add(dataset.get(i));
        }

        JFrame f = new JFrame();
        /* if coordinator then choose starting centroids, distribute starting cent, sigstart*/

        if (isCoordinator){
            this.clusters = new ArrayList<EntityCluster>();
            if(this.isCoordinator) {
                for(int i = 0; i < NUM_CLUSTERS; i++) {
                    EntityCluster c = new EntityCluster(i);               //last param here is a seed for the random creation
                    Entity randomCentroid = Entity.createRandomEntity(3,4,3+i); //params for createRandomEntity function depend on the # of attributes
                    c.setCentroid(randomCentroid);
                    this.clusters.add(c);
                }
            }
            Message msg = new Message(16, groupname, ID, 0);
            msg.setClusters(this.clusters);
            sendMessage(msg);
        }
        
        // create array of sharingentity //
        clusterEntity = new ArrayList<SharingEntity>(NUM_CLUSTERS);
        for(int i=0; i < NUM_CLUSTERS; i++){
            clusterEntity.add(new SharingEntity()) ;
        }

        while (this.clusters == null){ }

        Random r = new Random(20);
        boolean converged = false;
        int itt = 0;

        for(int i = 0; i < NUM_CLUSTERS; i++)
            clustersPresent.add(false);

        while(!converged) {
            int clabel = 0;
            for(int i = 0; i<clusters.size(); i++){
                clusters.get(i).setId(clabel);
                clabel++;
            }
            for (int i = 0; i < NUM_CLUSTERS; i++)
                LOGGER.log(Level.INFO, ID+": kPrototypes iteration "+itt+", cluster "+ i + ", centroid: "+ clusters.get(i).getCentroid());

            converged = true;

            for(int i = 0; i < dataset.size(); i++){
                //assign to cluster
                if(itt == 0){
                    assignRandomCluster(dataset.get(i),this.clusters,r);
                    converged = false;
                }
                else{
                    boolean interimConverged = assignCluster(dataset.get(i), this.clusters);
                    //assign to cluster
                    if(!interimConverged)
                        converged = false;
                }
            }
            itt++;

            //for each cluster:
            ArrayList<EntityCluster> nc = new ArrayList<EntityCluster>();
            for(int i = 0; i < this.clusters.size(); i++)
            {
                EntityCluster c = this.clusters.get(i);
                SharingEntity clusterData = new SharingEntity();
                clusterData.setConv(converged);
                // sumlocal
                for(int j = 0; j < dataset.size();j++){
                    if(dataset.get(j).getAssignedCluster() == c.getId()) {
                      clusterData.addEntity(dataset.get(j));
                    }
                }

                Message msg = new Message(12, groupname, ID, 0);
                clusterData.setClusterLabel(c.getId());
                clusterData.setIterationLabel(itt);
                msg.setEntity(clusterData);
                LOGGER.log(Level.WARNING, "ID: " + ID + " iteration " + itt + " cluster " + c.getId() + " sending " + clusterData.toEntity() );
                sendMessage(msg);

                while (countFromIteration(receivedEntities, itt, c.getId()) < memIDs.size()-1){ }
                ArrayList<SharingEntity> confirmedSharingEntities = new ArrayList<SharingEntity>();
                SharingEntity se;
                for (int j = 0; j < receivedEntities.size(); j++){
                    se = receivedEntities.get(j);
                    if(se.getClusterLabel() == c.getId() && se.getIterationLabel() == itt)
                        confirmedSharingEntities.add(se);
                }
                for (int j = 0; j < confirmedSharingEntities.size(); j++){
                    se = receivedEntities.get(j);
                    if(se.getConv() == false)
                        converged = false;
                    clusterData.addSharingEntity(se);
                }
                c = new EntityCluster(clusterData.toEntity(), c.getId());
                nc.add(c);
                receivedEntities.removeAll(confirmedSharingEntities);

                // save copy of sharing entity //
                clusterEntity.set(i, clusterData); 
            }
            this.clusters = nc;
        }

        //identify which clusters this client's data belong to
        for(int i = 0; i < dataset.size(); i++) {
            int num = dataset.get(i).getAssignedCluster();
            uploadedData.add(dataset.get(i));
            clustersPresent.set(num, true);
        }

        String centroidPopup = "                                            Cluster Centroids:\n";
        for(int i = 0; i < NUM_CLUSTERS; i++) {
            Entity centroid = clusters.get(i).getCentroid();
            centroid.setCluster(i);
            centroidPopup += centroid.toString()+"\n";
        }
        // JOptionPane.showMessageDialog(null, centroidPopup, "Cluster Centroids", JOptionPane.INFORMATION_MESSAGE);
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
//                System.out.println("Entity: "+ en + " new assignment: "+cluster);
            }
            en.setCluster(cluster);
        }
        //possible update to total number of entities in cluster
        return converged;
    }

    public HashMap<Integer,Integer> binaryLocalHash(int c){
        // Based on max port number
        HashMap<Integer, Integer> vector = new HashMap<Integer, Integer>();
        vector.put(c, 1);
        return vector;
    }
    public HashMap<Integer,Integer> perturbHash(HashMap<Integer, Integer> vector, int c){
        // Based on max port number
        Random rand = new Random();
        double p = 1/2;
        double q = 1/(java.lang.Math.exp(this.epsilon) + 1);
        if (vector.get(c) == 1)
            if(rand.nextFloat() <= p)
                vector.put(c, 0);
        return vector;
    }
    public int decodeHash(ArrayList<HashMap<Integer,Integer>> vectorAggregate, int c){
        double p = 1/2;
        double q = 1/(java.lang.Math.exp(this.epsilon) + 1);

        int count_vPrime = 0;
        for (HashMap<Integer,Integer> hm : vectorAggregate){
            count_vPrime+=hm.get(c);
        }

        // TODO CHECK THIS!!! whyyyy
        if (count_vPrime == 0)
            count_vPrime = 1;

        int ret = (int) Math.abs(Math.round((count_vPrime - (vectorAggregate.size()*q))/(p-q)));

        return ret;
    }

    public int[] unaryEncode(int c){
        // Based on max port number
        int vector[] = new int[65535];
        if (c >= 65535)
            return vector;
        for (int i = 0; i < vector.length; i++)
            vector[i] = 0;
        vector[c] = 1;
        return vector;
    }


    public int[] perturb(int[] vector){
        Random rand = new Random();
        double p = 1/2;
        double q = 1/(java.lang.Math.exp(this.epsilon) + 1);
        for (int i = 0; i < vector.length; i++)
            if (vector[i] == 1){
                if(rand.nextFloat() <= p)
                    vector[i] = 0;
            } else if (vector[i] == 0)
                if(rand.nextFloat() <= q)
                    vector[i] = 1;
        return vector;
    }
    public int decode(ArrayList<int[]> vectorAggregate, int c){
        if (c >= 65535)
            return 1;
        double p = 1/2;
        double q = 1/(java.lang.Math.exp(this.epsilon) + 1);

        int count_vPrime = 0;
        for (int[] v : vectorAggregate){
            count_vPrime += v[c];
        }

        int ret = (int) Math.abs(Math.round((count_vPrime - (vectorAggregate.size()*q))/(p-q)));

        return ret;
    }
    public void setGroupStatus() { this.inGroup = true; }

    public boolean getGroupStatus() { return inGroup; }

    public void setAsCoordinator() { isCoordinator = true; }

    public boolean getCoordinatorStatus() { return isCoordinator; }

    public  void correlateNewData(ArrayList<Entity> newData) {
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
            uploadedData.add(newData.get(i));

            //check for data that appears in a new cluster (cluster that wasn't used by initial data)
            if(!clustersPresent.get(clusterID)) {
                newClusters.add(clusterID);
                clustersPresent.set(clusterID, true);
            }
        }

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

    public  void SecretSharing(ArrayList<Entity> dataset) {
        for(int i = 0; i < dataset.size(); i++){
          uploadedData.add(dataset.get(i));
        }

        JFrame f = new JFrame();
        /* if coordinator then choose starting centroids, distribute starting cent, sigstart*/
        LOGGER.log(Level.WARNING, ID+": is COORDINATOR");
        if (isCoordinator){
            this.clusters = new ArrayList<EntityCluster>();
            if(this.isCoordinator) {
                for(int i = 0; i < NUM_CLUSTERS; i++) {
                    EntityCluster c = new EntityCluster(i);               //last param here is for the seed
                    Entity randomCentroid = Entity.createRandomEntity(3,4,3+i); //params for createRandomEntity function depend on the # of attributes
                    c.setCentroid(randomCentroid);
                    this.clusters.add(c);
                }
            }
            Message msg = new Message(16, groupname, ID, 0);
            msg.setClusters(this.clusters);
            sendMessage(msg);
        }

        // create array of sharingentity //
        clusterEntity = new ArrayList<SharingEntity>(NUM_CLUSTERS);
        for(int i=0; i < NUM_CLUSTERS; i++){
            clusterEntity.add(new SharingEntity()) ;
        }

        // Wait to receive the clusters from the coordinator
        while (this.clusters == null){ }

        Random r = new Random(20);
        boolean converged = false;

        // Iteration counter
        int itt = 0;
        for(int i = 0; i < NUM_CLUSTERS; i++)
            clustersPresent.add(false);

        while(!converged) {
            if(isCoordinator)
            LOGGER.log(Level.WARNING, "ID: START OF ITERATION "+itt+"\n");
            int clabel = 0;
            for(EntityCluster c: clusters){
                c.setId(clabel);
                clabel++;
            }
            for (int i = 0; i < NUM_CLUSTERS; i++)
                LOGGER.log(Level.WARNING, ID+": secret sharing iteration "+itt+", cluster "+ i + ", centroid: "+ clusters.get(i).getCentroid());

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
            //for each cluster:
            ArrayList<EntityCluster> nc = new ArrayList<EntityCluster>();
            for(int i=0; i < this.clusters.size(); i++)
            {
                EntityCluster c = this.clusters.get(i);
                SharingEntity clusterData = new SharingEntity();
                clusterData.setConv(converged);

                clusterData.setIterationLabel(itt);
                clusterData.setClusterLabel(c.getId());
                // sumlocal
                for(Entity en : dataset){
                    if(en.getAssignedCluster() == c.getId()) {
                      clusterData.addEntity(en);
                    }
                }

                Message requestForPartners = new Message(5, groupname, ID, 0);
                sendMessage(requestForPartners);
                //wait for server to respond
                while(numMembersinGroup < 3){
                  numMembersinGroup = memIDs.size();
                }

                ArrayList<SharingEntity> shares = clusterData.makeShares(numMembersinGroup, new Random());

                int assignedShare = 0;
                for (int id : memIDs){
                    if (id == ID) {
                      receivedShares.add(shares.get(assignedShare));
                    }
                    else{
                      // Send shares
                      Message msg = new Message(21, groupname, ID, id);
                      msg.setEntity(shares.get(assignedShare));
                      sendMessage(msg);
                      LOGGER.log(Level.INFO, "ID: " + ID + " sending share "+assignedShare);
                    }
                    assignedShare++;
                }

                while (countFromIteration(receivedShares, itt, c.getId()) < memIDs.size()){ }

                // All shares received by now
                LOGGER.log(Level.INFO, "ID: " + ID + " received 3 shares (including own)");

                SharingEntity intermediateEntity = new SharingEntity();
                intermediateEntity.setConv(converged);
                ArrayList<SharingEntity> intermediateConfirmed = new ArrayList<SharingEntity>();

                for (SharingEntity en : receivedShares){
                    if(en.getClusterLabel() == c.getId() && en.getIterationLabel() == itt){
                      intermediateEntity.addSharingEntity(en);
                      intermediateConfirmed.add(en);
                    }
                }

                ArrayList<SharingEntity> confirmedSharingEntities = new ArrayList<SharingEntity>();

                Message msg = new Message(12, groupname, ID, 0);
                LOGGER.log(Level.INFO, "ID: " + ID + " setting cluster label to c.getId(): "+c.getId());
                intermediateEntity.setClusterLabel(c.getId());
                intermediateEntity.setIterationLabel(itt);

                if (isCoordinator)
                LOGGER.log(Level.WARNING, "ID: " + ID + " iteration " + itt + " cluster " + c.getId() + " sending " + intermediateEntity.toEntity() );

                msg.setEntity(intermediateEntity);
                sendMessage(msg);

                confirmedSharingEntities.add(intermediateEntity);
                while (countFromIteration(receivedEntities,itt, c.getId()) < memIDs.size()-1){ }

                for(SharingEntity se : receivedEntities) {
                    if(se.getClusterLabel() == c.getId() && se.getIterationLabel() == itt)
                        confirmedSharingEntities.add(se);
                }

                clusterData = new SharingEntity();
                clusterData.setConv(converged);
                for(SharingEntity se : confirmedSharingEntities) {
                    if(se.getConv() == false)
                        converged = false;
                    clusterData.addSharingEntity(se);
                }
                if (isCoordinator)
                LOGGER.log(Level.WARNING, "countShare = " + clusterData.getCountShare());

                c = new EntityCluster(clusterData.toEntity(), c.getId());
                nc.add(c);

                receivedEntities.removeAll(confirmedSharingEntities);
                receivedShares.removeAll(intermediateConfirmed);
                LOGGER.log(Level.INFO, "ID: " + ID + " completed one revolution");
                LOGGER.log(Level.WARNING, "ID: " + ID + " final centroid val: "+ c.getCentroid().toString() + " iteration = "+itt);
                
                // save copy of sharing entity //
                clusterEntity.set(i, clusterData);                 
            }
            if (isCoordinator)
            LOGGER.log(Level.WARNING, "ID: END OF ITERATION "+itt+"\n\n\n");
            itt++;
            this.clusters = nc;
        }

        //identify which clusters this client's data belong to
        for(int i = 0; i < dataset.size(); i++) {
            int num = dataset.get(i).getAssignedCluster();
            uploadedData.add(dataset.get(i));
            clustersPresent.set(num, true);
        }

        String centroidPopup = "                                            Cluster Centroids:\n";
        for(int i = 0; i < NUM_CLUSTERS; i++) {
            Entity centroid = clusters.get(i).getCentroid();
            centroid.setCluster(i);
            centroidPopup += centroid.toString()+"\n";
        }
    }

    public  void DifferentialPrivacy(ArrayList<Entity> dataset) {
        for(int i = 0; i < dataset.size(); i++){
          uploadedData.add(dataset.get(i));
        }

        JFrame f = new JFrame();
        /* if coordinator then choose starting centroids, distribute starting cent, sigstart*/
        LOGGER.log(Level.WARNING, ID+": is COORDINATOR");
        if (isCoordinator){
            //int NUM_CLUSTERS = 3;
            this.clusters = new ArrayList<EntityCluster>();
            if(this.isCoordinator) {
                for(int i = 0; i < NUM_CLUSTERS; i++) {
                    EntityCluster c = new EntityCluster(i);
                    Entity randomCentroid = Entity.createRandomEntity(3,4,3+i); //params for createRandomEntity function depend on the # of attributes
                    c.setCentroid(randomCentroid);
                    this.clusters.add(c);
                }
            }
            Message msg = new Message(16, groupname, ID, 0);
            msg.setClusters(this.clusters);
            sendMessage(msg);
        }

        // create array of sharingentity //
        clusterEntity = new ArrayList<SharingEntity>(NUM_CLUSTERS);
        for(int i=0; i < NUM_CLUSTERS; i++){
            clusterEntity.add(new SharingEntity()) ;
        }
        
        // Wait to receive the clusters from the coordinator
        while (this.clusters == null){ }

        Random r = new Random();
        boolean converged = false;

        // Iteration counter
        int itt = 0;
        for(int i = 0; i < NUM_CLUSTERS; i++)
            clustersPresent.add(false);

        while(!converged) {
            if(isCoordinator)
            LOGGER.log(Level.WARNING, "ID: START OF ITERATION "+itt+"\n");
            int clabel = 0;
            for(EntityCluster c: clusters){
                c.setId(clabel);
                clabel++;
            }
            for (int i = 0; i < NUM_CLUSTERS; i++)
                LOGGER.log(Level.WARNING, ID+": secret sharing iteration "+itt+", cluster "+ i + ", centroid: "+ clusters.get(i).getCentroid());

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

            //for each cluster:
            ArrayList<EntityCluster> nc = new ArrayList<EntityCluster>();
            for(int i=0; i < this.clusters.size(); i++)
            {
                EntityCluster c = this.clusters.get(i) ;
                SharingEntity clusterData = new SharingEntity();
                clusterData.setConv(converged);

                clusterData.setIterationLabel(itt);
                clusterData.setClusterLabel(c.getId());
                // sumlocal
                for(Entity en : dataset){
                    if(en.getAssignedCluster() == c.getId()) {
                      clusterData.addEntity(en);
                    }
                }

                ArrayList<HashMap<Integer,Integer>> categoricalModeMap = clusterData.getModeMap();

                // Confusing, but each integer should be mapped to an aggregate (arraylist) of unary encoded values
                ArrayList<HashMap<Integer,Integer>> categoricalDeduced = new ArrayList<HashMap<Integer, Integer>>();

                for (HashMap<Integer,Integer> m : categoricalModeMap){
                    HashMap<Integer, Integer> nmap = new HashMap<Integer,Integer>();
                    for (int key : m.keySet()){
                        //ArrayList<int[]> toAdd = new ArrayList<int[]>();
                        ArrayList<HashMap<Integer,Integer>> toAdd = new ArrayList<HashMap<Integer,Integer>>();

                        int number = m.get(key);
                        for (int j = 0; j< number; j++){
                            //int vector[] = unaryEncode(key);
                            //vector = perturb(vector);
                            //toAdd.add(vector);
                            HashMap<Integer,Integer> binaryHashMap = binaryLocalHash(key);
                            binaryHashMap = perturbHash(binaryHashMap, key);
                            toAdd.add(binaryHashMap);
                        }
                        //nmap.put(key,decode(toAdd, key));
                        nmap.put(key,decodeHash(toAdd, key));
                    }
                    categoricalDeduced.add(nmap);
                }

                Message requestForPartners = new Message(5, groupname, ID, 0);
                sendMessage(requestForPartners);
                //wait for server to respond
                while(numMembersinGroup < 3){
                  numMembersinGroup = memIDs.size();
                }

                ArrayList<SharingEntity> shares = clusterData.makeShares(numMembersinGroup, new Random());


                int assignedShare = 0;
                for (int id : memIDs){
                    if (id == ID) {
                      receivedShares.add(shares.get(assignedShare));
                    }
                    else{
                      // Send shares
                      Message msg = new Message(21, groupname, ID, id);
                      msg.setEntity(shares.get(assignedShare));
                      sendMessage(msg);
                      LOGGER.log(Level.INFO, "ID: " + ID + " sending share "+assignedShare);
                    }
                    assignedShare++;
                }

                while (countFromIteration(receivedShares, itt, c.getId()) < memIDs.size()){ }

                // All shares received by now
                LOGGER.log(Level.INFO, "ID: " + ID + " received 3 shares (including own)");

                SharingEntity intermediateEntity = new SharingEntity();
                intermediateEntity.setConv(converged);
                ArrayList<SharingEntity> intermediateConfirmed = new ArrayList<SharingEntity>();

                for (SharingEntity en : receivedShares){
                    if(en.getClusterLabel() == c.getId() && en.getIterationLabel() == itt){
                      intermediateEntity.addSharingEntity(en);
                      intermediateConfirmed.add(en);
                    }
                }

                ArrayList<SharingEntity> confirmedSharingEntities = new ArrayList<SharingEntity>();

                Message msg = new Message(12, groupname, ID, 0);
                LOGGER.log(Level.INFO, "ID: " + ID + " setting cluster label to c.getId(): "+c.getId());
                intermediateEntity.setClusterLabel(c.getId());
                intermediateEntity.setIterationLabel(itt);

                intermediateEntity.setModeMap(categoricalDeduced);

                if (isCoordinator)
                LOGGER.log(Level.WARNING, "ID: " + ID + " iteration " + itt + " cluster " + c.getId() + " sending intermediate " + intermediateEntity.toEntity() );

                msg.setEntity(intermediateEntity);
                sendMessage(msg);

                confirmedSharingEntities.add(intermediateEntity);
                while (countFromIteration(receivedEntities,itt, c.getId()) < memIDs.size()-1){ }

                for(SharingEntity se : receivedEntities) {
                    if(se.getClusterLabel() == c.getId() && se.getIterationLabel() == itt)
                        confirmedSharingEntities.add(se);
                }

                clusterData = new SharingEntity();
                clusterData.setConv(converged);
                for(SharingEntity se : confirmedSharingEntities) {
                    if(se.getConv() == false)
                        converged = false;
                    clusterData.addSharingEntity(se);
                }
                if (isCoordinator)
                LOGGER.log(Level.WARNING, "countShare = " + clusterData.getCountShare());

                c = new EntityCluster(clusterData.toEntity(), c.getId());
                nc.add(c);

                receivedEntities.removeAll(confirmedSharingEntities);
                receivedShares.removeAll(intermediateConfirmed);
                LOGGER.log(Level.INFO, "ID: " + ID + " completed one revolution");
                LOGGER.log(Level.WARNING, "ID: " + ID + " final centroid val: "+ c.getCentroid().toString() + " iteration = "+itt);
                
                // save copy of sharing entity //
                clusterEntity.set(i, clusterData); 
            }
            if (isCoordinator)
            LOGGER.log(Level.WARNING, "ID: END OF ITERATION "+itt+"\n\n\n");
            itt++;
            this.clusters = nc;
        }

        //identify which clusters this client's data belong to
        for(int i = 0; i < dataset.size(); i++) {
            int num = dataset.get(i).getAssignedCluster();
            uploadedData.add(dataset.get(i));
            clustersPresent.set(num, true);
        }

        String centroidPopup = "                                            Cluster Centroids:\n";
        for(int i = 0; i < NUM_CLUSTERS; i++) {
            Entity centroid = clusters.get(i).getCentroid();
            centroid.setCluster(i);
            centroidPopup += centroid.toString()+"\n";
        }
    }
    public static int countFromIteration(ArrayList<SharingEntity> se, int itt, int cl){
        int count = 0;
        SharingEntity e;
        try{
            for (int i = 0; i < se.size(); i++){
                e = se.get(i);
                if (e.getIterationLabel() == itt && e.getClusterLabel()==cl)
                    count++;
            }
        } catch (NullPointerException npe) { return 0; }
        return count;
    }

    public String getAlgorithm() {
      return algorithm;
    }

    public ArrayList<Entity> getDataset() {
      return uploadedData;
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.flush();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        os.flush();
        byte[] toReturn = out.toByteArray();
        out.close();
        os.close();
        return toReturn;
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        Object toReturn = is.readObject();
        in.close();
        is.close();
        return toReturn;
    }

    public  void initializePartyTestingConnection(int clusters, String algorithm, double epsi){
//        System.out.println(ID+": starting initialize");
        Message mmsg;
        //algorithm = "Distributed (none)";
        NUM_CLUSTERS = clusters;
        groupname = "testing_group"+algorithm+clusters;
        this.epsilon = epsi ;

        while (this.inGroup == false){
            if (this.isCoordinator){
                mmsg = new Message(22, groupname+":"+clusters+":"+algorithm, this.getID(), 0);
                this.sendMessage(mmsg);
            }
            try{
                Thread.sleep(300);
            }catch (InterruptedException e) {}
            if (!this.isCoordinator){
                mmsg = new Message(23, groupname+":"+this.getID(), this.getID(), 0);
                this.sendMessage(mmsg);
            }
            try{
                Thread.sleep(300);
            }catch (InterruptedException e) {}
        }
        while(this.numMembersinGroup < 3){
            mmsg = new Message(05,groupname, ID, 0);
            this.sendMessage(mmsg);
            try{
                Thread.sleep(100);
            }catch (InterruptedException e) {}
        }
//        System.out.println(ID+": finished initialize");
    }
    
    public ArrayList<Entity> getEntitiesFromFile(String filename){
        ArrayList<Entity> entitiesFromFile = new ArrayList<Entity>();
        try{
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            String line;
            ArrayList<String> parsedLines = new ArrayList<String>();
            while ((line = br.readLine()) != null) {
                AlertParser a = new AlertParser(line);
                a.parseLine();

                if(a.isTCP()) {
                    //keep track of parsed lines as strings
                    String parsedLine = a.genCSVOutput() + ";"+filename;
                    parsedLines.add(parsedLine);
                    //keep track of parsed lines as Entities
                    Entity en = a.genEntityFromLine(parsedLine);
                    if(en != null)
                        entitiesFromFile.add(en);
                }
            }
        } catch (Exception e)  {}
        return entitiesFromFile;

    }

    public ArrayList<ArrayList<Integer>> getFrequencies(){
        ArrayList<ArrayList<Integer>> freq = new ArrayList<ArrayList<Integer>>(NUM_CLUSTERS);
        for(int i=0; i < NUM_CLUSTERS; i++){
            ArrayList<Integer> clusterFreq = clusterEntity.get(i).getFrequencies();
            freq.add(clusterFreq) ;
        }
        return freq;
    }

    public ArrayList<ArrayList<Integer>> getDiffFrequencies(){
        ArrayList<ArrayList<Integer>> freq = new ArrayList<ArrayList<Integer>>(NUM_CLUSTERS);
        int[][] mode = {{2010935, 80, 55845, 1},
                            {2013659, 443, 445, 1},
                            {2003068, 3859, 80, 1}};
        freq.add(clusterEntity.get(0).getFrequencies(mode[0]));
        freq.add(clusterEntity.get(1).getFrequencies(mode[1]));
        freq.add(clusterEntity.get(2).getFrequencies(mode[2]));
        return freq;
    }


    public static void main(String args[]) throws UnknownHostException, IOException {
        Client client = new Client();
        Thread clientThread = new Thread(client);
        clientThread.start();
        
        // epsilon values //
        double [] epsilon = {0.5, 1, 2, 5, 10, 20};
        // Create file to save frequencies //
        BufferedWriter bw = null;
        try{
            bw = new BufferedWriter(new FileWriter("frequencies.txt"));
        }catch(IOException e){;}

        String algorithm = "Distributed (none)";
        // Client -testing -host -file [filename]
        for (int i = 0; i < epsilon.length; i++){
            for (int j = 0; j < 3; j++){
                if (j == 0)
                    algorithm = "Distributed (none)";
                else if (j == 1)
                    algorithm = "Secret Sharing";
                else if (j == 2 )
                    algorithm = "Differential Privacy";
                if (args.length > 1){
                    if (args[0].contains("-testing")){
                        if (args[1].contains("-host")){
                            client.isCoordinator = true;
                            if (args[2].contains("-file")){
                                client.filename = args[3];
                            }
                        } else if (args[1].contains("-file"))
                            client.filename = args[2];
                        client.initializePartyTestingConnection(3, algorithm, epsilon[i]);
                        ArrayList<Entity> entitiesFromFile = client.getEntitiesFromFile(client.filename);
                        long startTime = System.nanoTime();
                        if (j == 0)
                            client.kPrototypes(entitiesFromFile);
                        else if (j == 1)
                            client.SecretSharing(entitiesFromFile);
                        else if (j == 2 )
                            client.DifferentialPrivacy(entitiesFromFile);
                        long endTime = System.nanoTime();
                        long duration = (endTime - startTime); 
                        duration = duration / 1000000;
                        //System.out.println(algorithm+","+i+",duration (ms) =,"+duration+",bytes shared =,"+ client.bytesSent);
                        //System.out.flush();

                        //write down frequencies //
                        if(client.getCoordinatorStatus()){
                            String header = "Algorithm: "+ algorithm + " Clusters: " + String.valueOf(i) ; 
                            System.out.println(header);

                            if(algorithm.equals("Distributed (none)")){
                                ArrayList<ArrayList<Integer>> frequencies = client.getFrequencies();
                                System.out.println("frequencies = " + frequencies);
                            }
                            else{
                                ArrayList<ArrayList<Integer>> frequencies = client.getDiffFrequencies();
                                System.out.println("frequencies = " + frequencies);
                            }

                            /*
                            ArrayList<ArrayList<Integer>> frequencies = client.getFrequencies();
                            System.out.println("frequencies = " + frequencies);
                            */
                        } 
                    }
                }
                clientThread.stop();
                client.logout();
                client = new Client();
                clientThread = new Thread(client);
                clientThread.start();
            }
        }
    }
}
