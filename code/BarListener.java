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
        if (e.getActionCommand() == "New collaborator"){
            String name = JOptionPane.showInputDialog("What is the user ID you want to connect with");
            int idToCollaborate = Integer.parseInt(name);
            this.client.addCollaborator(idToCollaborate);
        }
        if (e.getActionCommand() == "Ping collaborator"){
            this.client.sendMessage("Hello this is a test");
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
