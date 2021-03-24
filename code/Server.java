import java.io.*;
import java.util.*;
import java.net.*;


import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.*;
/*
   Most of this code is implemented from the following example
https://www.geeksforgeeks.org/multi-threaded-chat-application-set-2/?ref=lb

Thank you, geeksforgreeks for the code example.
Source code has been modified in the run() method to allow users to renaim
themselves in the client vectos.

TODO: Change main loop to be a function in a runnable class, with the main()
function simply making an instance and running a thread
    1) Two course of action would follow: every IDS is also a server. If they
    are the main "collaborator," they are the server reached out to.
    - The IDS.java file would instatiate a new Server()
    or 2) Server is run by calling `java Server`, with the main acting as a
    central server
*/
public class Server {

    // Vector to store active clients
    static volatile Vector<ClientHandler> ar = new Vector<>();
    static volatile Map<String, ArrayList<String>> groups = new HashMap<String, ArrayList<String>>();
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public synchronized static void main(String[] args) throws IOException
    {
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1234);
        Socket s;
        LOGGER.setLevel(Level.WARNING);

        // running infinite loop for getting
        // client request
        while (true)
        {
            // Accept the incoming request
            s = ss.accept();

            LOGGER.log(Level.INFO, "Server: New client request received. Creating a handler for the client.", s);

            // obtain input and output streams
            ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream dis = new ObjectInputStream(s.getInputStream());

            // Create a new handler object for handling this request.
            ClientHandler mtch = new ClientHandler(s, dis, dos);

            // Create a new Thread with this object.
            Thread t = new Thread(mtch);

            // add this client to active clients list
            ar.add(mtch);

            LOGGER.log(Level.INFO, "Server: Client added to the server's cleint list");

            // start the thread.
            t.start();
        }
    }
}

// ClientHandler class
class ClientHandler implements Runnable
{
    Scanner scn = new Scanner(System.in);
    public String name;
    final ObjectInputStream dis;
    final ObjectOutputStream dos;
    Socket s;
    boolean isloggedin;
    private Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // constructor
    public ClientHandler(Socket s, ObjectInputStream dis, ObjectOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.s = s;
        this.isloggedin=true;

    }
    // ClientHandler class
    public synchronized void sendMessage(Message m) throws IOException{
        ClientHandler mc;
        for (int i = 0; i < Server.ar.size(); i++){
            mc = Server.ar.get(i);
            if (m.dest == Integer.parseInt(mc.name) && mc.isloggedin==true) {
                mc.dos.writeObject(m);
                break;
            }
        }
    }
    public synchronized void messageHandler(Message received) throws IOException {
        //01:FROM:GROUPNAME#0 - 01 message to server requesting to make GROUP, named
        if(received.type == 1){
            String groupname = received.msg;
            if (Server.groups.containsKey(groupname)){
                Message exists = new Message(14, "Group "+groupname+" already exists.  Please choose another name.", 0, received.source);
                this.sendMessage(exists);
            }
            else if(received.msg.equals("")) {
                Message blank = new Message(14, "Name for group cannot be the empty string.", 0, received.source);
                this.sendMessage(blank);
            }
            else {
                Message m = new Message(19, groupname, 0, received.source);
                this.sendMessage(m);
            }
            return;
            //02:FROM:GROUPNAME#0 - 02 request to join GROUPNAME - (server should forward to coordinator)
        }else if(received.type == 2){
            String groupname = received.msg;
            if (Server.groups.containsKey(groupname)){
                int coordinator = Integer.parseInt(Server.groups.get(groupname).get(0));
                Message m = new Message(2, groupname, received.source, coordinator);
                this.sendMessage(m);
            }
            else {
                Message noGroup = new Message(14, "Group "+groupname+" does not exist.", 0, received.source);
                this.sendMessage(noGroup);
            }
            return;
            //03:FROM:MSG#TO - 03 response from coordinator to TO with "accept" or "deny"
        }else if(received.type == 3){
            // break the string into message and recipient part
            Message m = new Message(3, received.msg, received.source, received.dest);
            this.sendMessage(m);
            return;
            //04:FROM:GROUPNAME:TOADD - 04 message from coordinator to server with ID TOADD
        }else if(received.type == 4){
            String arr[] = (received.msg).split(":");
            String groupname = arr[0];
            String toAdd = arr[1];
            if (Server.groups.containsKey(groupname))
                Server.groups.get(groupname).add(toAdd);
            return;
            //05:FROM:GROUPNAME#0 - message to server requesting list of IDs in GROUPNAME
        }else if(received.type == 5){
            String groupname = received.msg;
            if (Server.groups.containsKey(groupname)){
                ArrayList<String> partners = Server.groups.get(groupname);
                Message m = new Message(6,"", 0, received.source);
                m.setMembers(partners);
                this.sendMessage(m);
            }
            return;
            //06:FROM:GROUPNAME:MSG - response from server with comma seperated MSG as a list of people in GROUPNAME
        }else if(received.type == 6){
            return;
            //10:FROM:MSG#TO - 10 Generic message. Send message to the TO
        }else if(received.type == 10){
            this.sendMessage(received);
            return;
            //11:FROM:MSG#GROUP
        }else if(received.type == 11){
            String message = received.msg.split(":")[0];

            if(!message.equals("") && !message.equals(null)) {
                String group_name = received.msg.split(":")[1];

                if (Server.groups.containsKey(group_name)){
                    ArrayList<String> partners = Server.groups.get(group_name);
                    String p;
                    for(int i = 0; i < partners.size(); i++){
                        p = partners.get(i);
                        if(Integer.parseInt(p) != received.source) {
                            Message m = new Message(10, message, received.source, Integer.parseInt(p));
                            this.sendMessage(m);
                        }
                    }
                }
            }
            else {
                Message noMessage = new Message(14, "Blank message.", 0, received.source);
                this.sendMessage(noMessage);
            }
            return;
        }else if (received.type == 12) {
            if (Server.groups.containsKey(received.msg)){
                ArrayList<String> partners = Server.groups.get(received.msg);
                String p;
                for(int i = 0; i < partners.size(); i++){
                    p = partners.get(i);
                    if (Integer.parseInt(p) != received.source){
                        received.dest = Integer.parseInt(p);
                        this.sendMessage(received);
                    }
                }
            }
            return;
        } else if (received.type == 16) {
            if (Server.groups.containsKey(received.msg)){
                ArrayList<String> partners = Server.groups.get(received.msg);
                String p;
                for(int i = 0; i < partners.size(); i++){
                    p = partners.get(i);
                    if (Integer.parseInt(p) != received.source){
                        received.dest = Integer.parseInt(p);
                        this.sendMessage(received);
                    }
                }
            }
            return;
        } else if (received.type == 17) {
            String group_name = received.msg;
            if (Server.groups.containsKey(group_name)){
              ArrayList<String> partners = Server.groups.get(group_name);
              Message m = new Message(18, group_name, 0, received.source);
              m.setMembers(partners);
              this.sendMessage(m);
            }
            return;
        } else if(received.type == 20) {
            //extract the group name and number of clusters
            String groupname = received.msg.split(":")[0];
            int numClusters = Integer.parseInt(received.msg.split(":")[1]);
            String algorithm = received.msg.split(":")[2];
            //create new group
            Server.groups.put(groupname,new ArrayList<String>());
            //add client as the first member (coordinator)
            Server.groups.get(groupname).add(Integer.toString(received.source));
            //send confirmation message
            Message success = new Message(15, "Success! You have created group: "+groupname, 0, received.source);
            this.sendMessage(success);
            return;
            // TODO: Check for error w/ type code 21...make sure sending to
            // right dest
        } else if(received.type == 21) {
            sendMessage(received);
          return;
        } else if(received.type == 22) {
            String groupname = received.msg.split(":")[0];
            int numClusters = Integer.parseInt(received.msg.split(":")[1]);
            String algorithm = received.msg.split(":")[2];

            //create new group
            if (!Server.groups.containsKey(groupname)){
                Server.groups.put(groupname,new ArrayList<String>());
                Server.groups.get(groupname).add(Integer.toString(received.source));
                Message msg = new Message(24, Integer.toString(received.source), 0, received.source);
                sendMessage(msg);
            }
          return;
        } else if(received.type == 23) {
            // NO AUTH version
            String arr[] = (received.msg).split(":");
            String groupname = arr[0];
            String toAdd = arr[1];
            if (Server.groups.containsKey(groupname)){
                if (!Server.groups.get(groupname).contains(toAdd)){
                    Server.groups.get(groupname).add(toAdd);

                    ArrayList<String> partners = Server.groups.get(groupname);
                    Message m = new Message(24,toAdd, 0, 0);

                    for (int i = 0; i < partners.size(); i++){
                        int dest = Integer.parseInt(partners.get(i));
                        m.dest = dest;
                        this.sendMessage(m);
                    }

                    Message nm = new Message(06,"", 0, 0);
                    nm.setMembers(partners);
                    sendMessage(nm);
                }
            }
            return;
        }
    }
    @Override
    public synchronized void run() {
        Message received;
        while (true) {
            try {
                // receive the string
                //
                received = (Message)dis.readObject();
                if((received.msg).contains("new name id")){ // this will always be TRUE
                    this.name = received.msg.split(":")[1];
                    LOGGER.log(Level.INFO, "Server: client id initialied: "+ this.name);
                }else if((received.msg).equals("logout")){
                    this.isloggedin=false;
//                    this.dis.close();
//                    this.dos.close();
//                    this.s.close();
                    break;
                } else {
                    this.messageHandler(received);
                }
            } catch (EOFException e) {} catch (IOException e) { e.printStackTrace(); } catch (ClassNotFoundException e) { }
        } 
    }
}
