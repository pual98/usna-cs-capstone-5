import java.awt.geom.*;
import java.awt.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

public class ControlPanel extends JPanel implements Runnable {

//    JButton kMeansButton = new JButton("Run K-means training");
    JButton newCollaborator = new JButton("New Group");
    JButton pingCollaborator = new JButton("Join Group");
    JButton sendMessage = new JButton("Send message");
    JButton runIDS = new JButton("Run intrusion detection");

    public ControlPanel() {
        super();
        this.setBackground(Color.lightGray);
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5,5,5,5);
        this.add(newCollaborator,gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        this.add(runIDS,gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        this.add(pingCollaborator,gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        this.add(sendMessage,gbc);
    }
    public void addListener(BarListener r){
        this.addMouseListener(r);
        newCollaborator.addActionListener(r);
        pingCollaborator.addActionListener(r);
        sendMessage.addActionListener(r);
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
