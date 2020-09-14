import java.awt.geom.*;
import java.awt.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

public class EventPanel extends JPanel implements Runnable {

    private BarListener r = null;
    private ArrayList<String> events= new ArrayList<String>();
    private JPanel inside = new JPanel();
    private JScrollPane eventPane = new JScrollPane(inside, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    private Color col = Color.lightGray;

    public EventPanel() {
        super();
        this.setBackground(new Color(245, 243, 213));
        
        //Box layout is basically a stack. As things add, they grow downword
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(eventPane);
        this.inside.setLayout(new BoxLayout(this.inside,BoxLayout.Y_AXIS));
    }

    public void addListener(BarListener r){
        this.addMouseListener(r);
    }
    private void alternateColor(){
        if (this.col == Color.lightGray)
            this.col = Color.white;
        else
            this.col = Color.lightGray;
    }
    public void addText(String s){
        this.events.add(s);
    }
    public void updatePanel(){
        for(String l : this.events){
            JLabel label = new JLabel(l);
            label.setMinimumSize(new Dimension(100, 15));
            label.setOpaque(true);
            label.setBackground(this.col);
            this.alternateColor();
            this.inside.add(label);
            this.repaint();
        }
        this.events = new ArrayList<String>();
    }

    /**
     * Runnable method for threads.
     **/
    public void run() {
        try{
            while(true){
                try{
                    inside.repaint();
                    Thread.sleep(20);
                }catch(InterruptedException e){}
            }
        } catch(Throwable e){}
    }
}
