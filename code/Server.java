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

            System.out.println("New client request received : " + s); 

            // obtain input and output streams 
            DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 

            System.out.println("Creating a new handler for this client..."); 

            // Create a new handler object for handling this request. 
            ClientHandler mtch = new ClientHandler(s,"client " + i, dis, dos); 

            // Create a new Thread with this object. 
            Thread t = new Thread(mtch); 

            System.out.println("Adding this client to active client list"); 

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
                    System.out.println("Client id initialized: "+this.name);
                    continue;
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
                        mc.dos.writeUTF(this.name+" : "+MsgToSend); 
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
