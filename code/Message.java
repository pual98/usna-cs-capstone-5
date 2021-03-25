import java.util.*;
import java.io.*;

public class Message implements Serializable {
    public static final long serialVersionUID=1L;
    /*
    01: msg = GROUPNAME
        01 message to server requesting to make GROUP, named GROUPNAME
    02: msg = GROUPNAME
        02 request to join GROUPNAME - (server should forward to coordinator)
    03: msg = MSG:"accept" || MSG:"deny"
        03 response from coordinator to TO with "accept" or "deny"
    04: msg = GROUPNAME:TOADD
        04 message from coordinator to server with ID TOADD to join have join the GROUPNAME
    05: msg = GROUPNAME
        message to server requesting list of IDs in GROUPNAME
    06: TODO: Deprecated msg = MSG
        response from server with comma seperated MSG as a list of people in
        EXAMPLE: 12,13,1,2 where each is an ID
    10: msg = MSG
        10 Generic message. Send message to the TO
    11: msg = MSG:GROUP
        10 Generic message. Send message to everyone in the GROUP (good for testing
        if group is established correctly)
    12: msg = MSG#TO
        Send clusterData object (sharingEntity)
        should set entity
    14: msg = Error Message to Client trying to Create/Join Group
        Response from server to requesting client
    15: msg = "Success, you have created group :GROUPNAME"
        Response from server to client after creatign a group
    16: msg = GROUP
        Send clusters (ArrayList<EntityCluster)...typically for intial points.
    17: msg = list of clients within group
        Client requesting server to send a list of clients within group
    18: msg = Group
        Response from server to client with ArrayList<String> of client IDs
    19: msg = group_name
        Response from server to coordinator (from message type 01) to choose the number of clusters in CIDS
    20: msg = groupname:numOfCluster
        Response to server from coordinatior with number of clusters.

    */
    public int type;
    public int source;
    public int dest;
    public String msg = null;
    public SharingEntity en;
    public ArrayList<EntityCluster> clusters;
    public ArrayList<String> members;

    public Message(int type, String msg, int source, int dest){
        this.type = type;
        this.msg = msg;
        this.source = source;
        this.dest = dest;
    }
    public void setEntity(SharingEntity en){
        this.en = en;
    }

    public void setClusters(ArrayList<EntityCluster> c){
        this.clusters = c;
    }

    public void setMembers(ArrayList<String> m){
        this.members = m;
    }
}
