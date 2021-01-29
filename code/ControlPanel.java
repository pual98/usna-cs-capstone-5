import java.awt.geom.*;
import java.awt.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class ControlPanel extends JPanel implements Runnable {

    // JButton kMeansButton = new JButton("Run K-means training");
    JButton newCollaborator = new JButton("New Group");
    ImageIcon joinButton = new ImageIcon("join.png");
    JButton pingCollaborator = new JButton("Join Group");
    JButton sendMessage = new JButton("Send Message to Group");
    JButton sendToClient = new JButton("Send Message to Client");
    JButton runIDS = new JButton("Run Intrusion Detection");

    public ControlPanel() {
        super();
        this.setBackground(new Color(182, 209, 208));
        this.setLayout(new FlowLayout());

        // GridBagConstraints gbc = new GridBagConstraints();
        // gbc.gridx = 1;
        // gbc.gridy = 0;
        // gbc.insets = new Insets(5,5,5,5);
        this.add(newCollaborator);
        // gbc.gridx = 0;
        // gbc.gridy = 1;
        // gbc.gridx = 2;
        // gbc.gridy = 0;
        this.add(pingCollaborator);
        this.add(runIDS);
        // gbc.gridx = 1;
        // gbc.gridy = 1;
        this.add(sendMessage);
        // gbc.gridx = 2;
        // gbc.gridy = 1;
        this.add(sendToClient);
    }
    public void addListener(BarListener r){
        this.addMouseListener(r);
        newCollaborator.addActionListener(r);
        pingCollaborator.addActionListener(r);
        sendMessage.addActionListener(r);
        sendToClient.addActionListener(r);
        runIDS.addActionListener(r);
    }


    /**
     * Runnable method for threads.
     **/
    public void run() {
        try{
            while(true){
                try{
                    Thread.sleep(20);
                }catch(InterruptedException e){}
            }
        } catch(Throwable e){}
    }
}
