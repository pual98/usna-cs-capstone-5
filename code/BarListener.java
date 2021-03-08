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
  private boolean modelCreated;
  private Thread th = null;

  /**
   * Constructor for a BarListener
   **/
  public BarListener(Menu menubar, Display d, Client c){
    modelCreated = false;
    this.menubar = menubar;
    this.d = d;
    this.client = c;
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
      group_name = JOptionPane.showInputDialog("Enter Group's Name");

      while(group_name.equals("")) {
        JOptionPane.showMessageDialog(null, "Please input a name of a group.", "No Group Name Provided", JOptionPane.ERROR_MESSAGE);
        group_name = JOptionPane.showInputDialog("Enter Group's Name");
      }

      if(group_name != null) {
        // send Message Type 02 to Server //
        Message msg = new Message(02, group_name, client.getID(), 0);
        this.client.sendMessage(msg);
      }
    }
    if (e.getActionCommand() == "Run Intrusion Detection"){
      if(!this.client.getGroupStatus()) {
        JOptionPane.showMessageDialog(null, "You need to be in a group before running IDS!", "Denial", JOptionPane.ERROR_MESSAGE);
        return;
      }
      String fname = d.getFilename();
      if(fname.equals("")) {
        JOptionPane.showMessageDialog(null, "You need to upload Snort Data prior to running IDS!", "No File Selected", JOptionPane.ERROR_MESSAGE);
        return;
      }
      if(modelCreated) {
        JOptionPane.showMessageDialog(null, "You already ran intrusion detection to create learned model", "Model already created", JOptionPane.ERROR_MESSAGE);
        return;
      }
      //Parse filename into CSV file to be sent to kPrototypes
      if(client.getAlgorithm().equals("Distributed (none)"))
        this.client.kPrototypes(d.getEntitiesFromFile());
      else if(client.getAlgorithm().equals("Secret Sharing"))
        this.client.SecretShareDiff(d.getEntitiesFromFile());
      else if(client.getAlgorithm().equals("Differential Privacy"))
        this.client.DifferentialPrivacy(d.getEntitiesFromFile());
      this.d.populateTable();
      modelCreated = true;
      this.d.setAlgorithmComplete();
    }

    if (e.getActionCommand() == "Correlate New Data"){
      if(!modelCreated) {
        JOptionPane.showMessageDialog(null, "You must run intrusion detection\non initial data before correlating new data.", "No Learned Model", JOptionPane.ERROR_MESSAGE);
        return;
      }
      if(d.getUploadCount() > 1) {
        this.client.correlateNewData(d.getNewData());
        this.d.populateTable();
      }
    }

  }

  /**
   * Performs action when there is a state change in the slider bar.
   * @param e ChangeEvent that triggered the listener.
   **/
  public void stateChanged(ChangeEvent e){;}
  /**
   * PerformsAction when mouse is clicked
   * @param e MouseEvent that triggered the method.
   **/
  public void mousePressed(MouseEvent e){;}

  /********* Unused methods from mouse listener **********/
  public void mouseClicked(MouseEvent e){}
  public void mouseEntered(MouseEvent e){}
  public void mouseExited(MouseEvent e){}
  public void mouseReleased(MouseEvent e){}
}
