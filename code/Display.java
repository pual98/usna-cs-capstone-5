import java.awt.geom.*;
import java.awt.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

public class Display extends JPanel implements Runnable {

    private BarListener r = null;
    private JPanel actionPanel = new JPanel();
    private EventPanel eventPanel = new EventPanel();


    public Display() {
        super();
        this.setBackground(new Color(245, 243, 213));
        this.setVisible(true);
        
        //Box layout is basically a stack. As things add, they grow downword
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.doLayout();

        actionPanel.setBackground(Color.green);
        eventPanel.setBackground(Color.red);

//        for (int i=0; i< 100; i++){
//            eventPanel.addText("something");
//            eventPanel.updatePanel();
//            this.resize();
//        }

        //Action panel buttons i.e. actions
        //JButton display alert data button
        JButton alertButton = new JButton("Display Alert Data");

        //JButton display high 
        JButton highAlertButton = new JButton("Display High Alert Data");
        
        actionPanel.add(alertButton);
        actionPanel.add(highAlertButton);

        // Add panels back into Display
        this.add(actionPanel);
        this.add(eventPanel);
    }
    public void resize(){
        Dimension size = this.getSize();
        double height = size.getHeight();
        double width = size.getWidth();
        // Fix this 
        actionPanel.setMinimumSize(new Dimension(200,400));
    }

    public void addListener(BarListener r){
        this.addMouseListener(r);
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
