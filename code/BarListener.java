import javax.swing.*;
import java.awt.event.*;
import java.lang.*;
import javax.swing.event.*;

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
    }

    /**
     * Performs an action when the TopBar listener is triggered.
     * @param e ActionEvent that triggered the listener.
     **/
    public void actionPerformed(ActionEvent e){
        System.out.println(e.getActionCommand());

            // create a new Group //
            if (e.getActionCommand() == "New Group"){
                // prompt user for GROUP name //
                String group_name = null; 
                while(group_name == null)
                    group_name = JOptionPane.showInputDialog("Enter Group's Name");

                // send Message Type 01 to Server //
                String msg = "01:"+this.client.getID()+":"+group_name ;
                this.client.sendMessage(msg, 0);
            }

            if (e.getActionCommand() == "Join Group"){

                // prompt user for GROUP name //
                String group_name = null ;
                while(group_name == null)
                    group_name = JOptionPane.showInputDialog("Enter Group's Name");

                // send Message Type 02 to Server //
                String msg = "02:"+this.client.getID()+":"+group_name ;
                this.client.sendMessage(msg, 0);
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
