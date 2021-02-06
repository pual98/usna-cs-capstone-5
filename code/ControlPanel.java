import java.awt.geom.*;
import java.awt.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class ControlPanel extends JPanel implements Runnable {


    JButton newCollaborator = new JButton("New Group");
    ImageIcon joinButton = new ImageIcon("join.png");
    JButton pingCollaborator = new JButton("Join Group");
    JButton runIDS = new JButton("Run Intrusion Detection");
    JButton correlate = new JButton("Correlate New Data");

    public ControlPanel() {
        super();
        this.setBackground(new Color(182, 209, 208));
        this.setLayout(new FlowLayout());

        this.add(newCollaborator);
        this.add(pingCollaborator);
        this.add(runIDS);
        this.add(correlate);
    }

    public void addListener(BarListener r){
        this.addMouseListener(r);
        newCollaborator.addActionListener(r);
        pingCollaborator.addActionListener(r);
        runIDS.addActionListener(r);
        correlate.addActionListener(r);
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
