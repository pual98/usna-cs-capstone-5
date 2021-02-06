import java.awt.geom.*;
import java.awt.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.time.*;

public class Display extends JPanel implements Runnable {
    public ControlPanel controlPanel = new ControlPanel();
    public EventPanel eventPanel = new EventPanel();


    public Display() {
        super();
        this.setBackground(new Color(182, 209, 208));
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
        gbc.insets = new Insets(15,15,15,15);

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

    public String getFilename() {
      return eventPanel.getFilename();
    }

    public ArrayList<Entity> getEntitiesFromFile() {
      return eventPanel.getDataset();
    }

    public void populateTable() {
      eventPanel.populateTable();
    }

    public EventPanel getEventPanel(){
        return eventPanel;
    }

    public void setAlgorithmComplete() {
      eventPanel.setAlgorithmComplete();
    }

    public int getUploadCount() {
      return eventPanel.getUploadCount();
    }

    public ArrayList<Entity> getNewData() {
      return eventPanel.getNewData();
    }

    public JTable getTable() {
      return this.eventPanel.getTable();
    }

    /**
     * Runnable method for threads.
     **/
    public void run() {


    }
}
