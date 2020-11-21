import java.util.*;
import java.io.*;

public class Message implements Serializable {
    /*
    01:FROM:GROUPNAME
        01 message to server requesting to make GROUP, named GROUPNAME
    02:FROM:GROUPNAME
        02 request to join GROUPNAME - (server should forward to coordinator)
    03:FROM:MSG#TO
        03 response from coordinator to TO with "accept" or "deny"
    04:FROM:GROUPNAME:TOADD
        04 message from coordinator to server with ID TOADD to join have join the GROUPNAME
    05:FROM:GROUPNAME
        message to server requesting list of IDs in GROUPNAME
    06:FROM:MSG
        response from server with comma seperated MSG as a list of people in
        GROUPNAME MSG is something like BILL,12,13,1,2 where BILL is GROUPNAME and
        everything else is the IDs
    10:FROM:MSG#TO
        10 Generic message. Send message to the TO
    11:FROM:MSG#GROUP
        10 Generic message. Send message to everyone in the GROUP (good for testing
        if group is established correctly)
    */
    public int type;
    public String source;
    public String dest;
    public String msg = null;
    public SharingEntity en;

    public Message(int type, String msg, String source, String dest){
        this.type = type;
        this.msg = msg;
        this.source = source;
        this.dest = dest;
    }
    public void setEntity(SharingEntity en){
        this.en = en;
    }
}
