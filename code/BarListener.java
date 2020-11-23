import javax.swing.*;
import java.awt.event.*;
import java.lang.*;
import javax.swing.event.*;
import java.util.*;

/**
 * @author MIDN Paul Slife
 **/
public class BarListener implements ActionListener,ChangeListener,MouseListener {

    // Fields
    private Menu menubar;
    private Display d;
    private Client client;
    private Thread th = null;

    /**
     * Constructor for a BarListener
     * @param t The TopBar being listened to.
     * @param d The display
     **/
    public BarListener(Menu menubar, Display d, Client c){
        this.menubar = menubar;
        this.d = d;
        this.client = c;
        //this.groups = new ArrayList<String>();
    }

    /**
     * Performs an action when the TopBar listener is triggered.
     * @param e ActionEvent that triggered the listener.
     **/
    public void actionPerformed(ActionEvent e){
        System.out.println(e.getActionCommand());

            // create a new Group //
            if (e.getActionCommand() == "New Group"){
                //check if Client is already in group
                if(client.getCoordinatorStatus()) {
                  JOptionPane.showMessageDialog(null, "Cannot create more than one group.", "Error!", JOptionPane.ERROR_MESSAGE);
                  return;
                }
                if(client.getGroupStatus()) {
                  JOptionPane.showMessageDialog(null, "You are already in a group.", "Error!", JOptionPane.ERROR_MESSAGE);
                  return ;
                }

                // prompt user for GROUP name //
                String group_name = null;
                group_name = JOptionPane.showInputDialog("Enter Group's Name");

                if(group_name != null) {
                  // send Message Type 01 to Server //
                  String msg = group_name ;
                  //groups.add(group_name);
                  Message mmsg = new Message(01, msg, client.getID(), 0);
                  client.sendMessage(mmsg);
                }

            }

            if (e.getActionCommand() == "Join Group"){

              if(client.getCoordinatorStatus()) {
                JOptionPane.showMessageDialog(null, "You are already the coordinator for a group.", "Error!", JOptionPane.ERROR_MESSAGE);
                return;
              }
              if(client.getGroupStatus()) {
                JOptionPane.showMessageDialog(null, "You have already joined a group.", "Error!", JOptionPane.ERROR_MESSAGE);
                return;
              }

              // prompt user for GROUP name //
              String group_name = null ;
              while(group_name == null) {
                group_name = JOptionPane.showInputDialog("Enter Group's Name");
              }

              // send Message Type 02 to Server //
              Message msg = new Message(02, group_name, client.getID(), 0);
              this.client.sendMessage(msg);

            }
            if (e.getActionCommand() == "Send Message to Group"){
              if(client.getGroupStatus()) {
                String message = null;
                message = JOptionPane.showInputDialog("Desired Message");

                // send Message Type 11 to Server //
                String msg = message+":"+client.groupname;
                Message mmsg = new Message(11, msg, client.getID(), 0);
                //System.out.println("I am Client "+this.client.getID()+" and I am sending: "+msg);
                this.client.sendMessage(mmsg);
              }
              else {
                JOptionPane.showMessageDialog(null, "You are not in a group yet.", "Can't Send Message.", JOptionPane.ERROR_MESSAGE);
              }
            }
            if (e.getActionCommand() == "Send Message to Client"){
              if(client.getGroupStatus()) {
                Message mmsg = new Message(17, client.groupname, client.getID(), 0);
                this.client.sendMessage(mmsg);
              }
              else {
                JOptionPane.showMessageDialog(null, "You are not in a group yet.", "Can't Send Message.", JOptionPane.ERROR_MESSAGE);
              }
            }
            if (e.getActionCommand() == "Run intrusion detection"){
                this.client.kPrototypes();
            }

        }

        /**
         * Performs action when there is a state change in the slider bar.
         * @param e ChangeEvent that triggered the listener.
         **/
        public void stateChanged(ChangeEvent e){
        }
        /**
         * PerformsAction when mouse is clicked
         * @param e MouseEvent that triggered the method.
         **/
        public void mousePressed(MouseEvent e){

        }

        /********* Unused methods from mouse listener **********/
        public void mouseClicked(MouseEvent e){}
        public void mouseEntered(MouseEvent e){}
        public void mouseExited(MouseEvent e){}
        public void mouseReleased(MouseEvent e){}
    }
