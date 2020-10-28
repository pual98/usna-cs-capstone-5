import java.awt.geom.*;
import java.awt.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

public class Display extends JPanel implements Runnable {

    private BarListener r = null;
    private ControlPanel controlPanel = new ControlPanel();
    private EventPanel eventPanel = new EventPanel();


    public Display() {
        super();
        this.setBackground(new Color(245, 243, 213));
        this.setVisible(true);
        
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Add panels back into Display
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.fill = GridBagConstraints.VERTICAL/3;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipady = 10;
        gbc.insets = new Insets(10,10,10,10);

        this.add(controlPanel,gbc);

        gbc.ipady = 0;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        this.add(eventPanel, gbc);
        this.doLayout();
    }
    public void resize(){
        // Fix this 
        //actionPanel.setMinimumSize(new Dimension(200,200));
    }

    public void addListener(BarListener r){
        this.addMouseListener(r);
        controlPanel.addListener(r);
    }

    /**
     * Runnable method for threads.
     **/
    public void run() {
        try{
              RandomAccessFile in = new RandomAccessFile("file.txt", "r");
              String line;
            while(true){
                try{
                     if((line = in.readLine()) != null) {
                        eventPanel.addText(line);
                        eventPanel.updatePanel();
                        this.resize();
                        this.revalidate();
                     } else {
                        Thread.sleep(20);
                     }
                }catch(InterruptedException e){}
            }
        } catch(Throwable e){}
    }
}
