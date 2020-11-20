import java.io.*; 
import java.util.*; 
import java.net.*; 
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
public class Server 
{ 

    // Vector to store active clients 
    static Vector<ClientHandler> ar = new Vector<>(); 
    static Map<String, ArrayList<String>> groups = new HashMap<String, ArrayList<String>>();

    // counter for clients 
    static int i = 0; 

    public static void main(String[] args) throws IOException 
    { 
        // server is listening on port 1234 
        ServerSocket ss = new ServerSocket(1234); 

        Socket s; 

        // running infinite loop for getting 
        // client request 
        while (true) 
        { 
            // Accept the incoming request 
            s = ss.accept(); 

            System.out.println("Server log: New client request received : " + s); 

            // obtain input and output streams 
            DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 

            System.out.println("Server log: Creating a new handler for this client..."); 

            // Create a new handler object for handling this request. 
            ClientHandler mtch = new ClientHandler(s,"client " + i, dis, dos); 

            // Create a new Thread with this object. 
            Thread t = new Thread(mtch); 

            System.out.println("Server log: Adding this client to active client list"); 

            // add this client to active clients list 
            ar.add(mtch); 

            // start the thread. 
            t.start(); 

            // increment i for new client. 
            // i is used for naming only, and can be replaced 
            // by any naming scheme 
            i++; 

        } 
    } 
} 

// ClientHandler class 
class ClientHandler implements Runnable 
{ 
    Scanner scn = new Scanner(System.in); 
    private String name; 
    final DataInputStream dis; 
    final DataOutputStream dos; 
    Socket s; 
    boolean isloggedin; 

    // constructor 
    public ClientHandler(Socket s, String name, 
            DataInputStream dis, DataOutputStream dos) { 
        this.dis = dis; 
        this.dos = dos; 
        this.name = name; 
        this.s = s; 
        this.isloggedin=true; 
    } 
    @Override
    public void run() { 
        String received; 
        while (true) 
        { 
            try
            { 
                // receive the string 
                received = dis.readUTF(); 

                System.out.println(received); 

                if(received.equals("logout")){ 
                    this.isloggedin=false; 
                    this.s.close(); 
                    break; 
                } 
                if(received.contains("new name id")){ 
                    String arr[] = received.split(":");
                    this.name = arr[1];
                    System.out.println("Server log: client id initialized: "+this.name);
                    continue;
                } 
                //01:FROM:GROUPNAME - 01 message to server requesting to make GROUP, named
                if(received.substring(0,2).equals("01")){
                    String arr[] = received.split(":");
                    String groupname = arr[2];
                    if (!Server.groups.containsKey(groupname)){
                        Server.groups.put(groupname,new ArrayList<String>());
                    }
                    Server.groups.get(groupname).add(arr[1]);
                    continue;
                //02:FROM:GROUPNAME - 02 request to join GROUPNAME - (server should forward to
                }else if(received.substring(0,2).equals("02")){
                    String arr[] = received.split(":");
                    String groupname = arr[2];
                    if (Server.groups.containsKey(groupname)){
                        String recipient = Server.groups.get(groupname).get(0);
                        String MsgToSend = received;
                        for (ClientHandler mc : Server.ar) 
                        { 
                            if (recipient.contains(mc.name) && mc.isloggedin==true) 
                            { 
                                mc.dos.writeUTF(MsgToSend); 
                                break; 
                            } 
                        } 
                    }
                    continue;
                //03:FROM:MSG#TO - 03 response from coordinator to TO with "accept" or "deny"
                }else if(received.substring(0,2).equals("03")){
                    System.out.println("Server log: received an accept");
                //04:FROM:GROUPNAME:TOADD - 04 message from coordinator to server with ID TOADD
                }else if(received.substring(0,2).equals("04")){
                    String arr[] = received.split(":");
                    String groupname = arr[2];
                    System.out.println("Server log: received an addition to " + groupname);
                    if (Server.groups.containsKey(groupname))
                       Server.groups.get(groupname).add(arr[3]);
                    continue;

                //05:FROM:GROUPNAME - message to server requesting list of IDs in GROUPNAME
                }else if(received.substring(0,2).equals("05")){
                    String arr[] = received.split(":");
                    String groupname = arr[2];
                    if (Server.groups.containsKey(groupname)){
                       ArrayList<String> partners = Server.groups.get(groupname);
                       String MsgToSend = "06:0:"+groupname+":"+String.join(",",partners);
                       String recipient = arr[1];
                       for (ClientHandler mc : Server.ar) 
                       { 
                           if (recipient.contains(mc.name) && mc.isloggedin==true) 
                           { 
                               mc.dos.writeUTF(MsgToSend); 
                               break; 
                           } 
                       } 
                    }
                    continue;
                //06:FROM:GROUPNAME:MSG - response from server with comma seperated MSG as a list of people in GROUPNAME
                }else if(received.substring(0,2).equals("06")){
                    continue;
                //10:FROM:MSG#TO - 10 Generic message. Send message to the TO
                }else if(received.substring(0,2).equals("10")){
                }
                // break the string into message and recipient part 
                StringTokenizer st = new StringTokenizer(received, "#"); 
                String MsgToSend = st.nextToken(); 
                String recipient = st.nextToken(); 

                // search for the recipient in the connected devices list. 
                // ar is the vector storing client of active users 
                for (ClientHandler mc : Server.ar) 
                { 
                    // if the recipient is found, write on its 
                    // output stream 
                    if (recipient.contains(mc.name) && mc.isloggedin==true) 
                    { 
                        mc.dos.writeUTF(MsgToSend); 
                        break; 
                    } 
                } 
            } catch (EOFException e) {} catch (IOException e) { e.printStackTrace(); }

        } 
        try
        { 
            // closing resources 
            this.dis.close(); 
            this.dos.close(); 
        }catch(IOException e){ 
            e.printStackTrace(); 
        } 
    } 
} 
