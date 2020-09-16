import java.awt.geom.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

public class EventPanel extends JPanel implements Runnable {

    private BarListener r = null;
    private ArrayList<String> events= new ArrayList<String>();

//    private JPanel searchAndTable = new JPanel();
//    private JPanel inside = new JPanel();
    private JPanel filterPanel = new JPanel();
//    private JScrollPane eventPane = new JScrollPane(inside, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    private Color col = Color.lightGray;

    private GridBagConstraints gbcEvent = new GridBagConstraints();
    private GridBagConstraints gbcMaster = new GridBagConstraints();
    private GridBagConstraints gbcSearch = new GridBagConstraints();

    private int displayedCount = 0;

    private JPanel searchBox = new JPanel();
    private JTextField searchField = new JTextField(45);
    private JButton searchButton = new JButton("Search");

    private JTableSearch dataWithSearch = new JTableSearch();
    public EventPanel() {
        super();
        this.setBackground(new Color(245, 243, 213));
        this.setLayout(new GridBagLayout());
        gbcMaster.fill = GridBagConstraints.BOTH;
        gbcMaster.gridx = 0;
        gbcMaster.gridy = 0;
        gbcMaster.weightx = 1;
        gbcMaster.weighty = 1;
        this.add(filterPanel,gbcMaster);
        filterPanel.add(new JLabel("This will be the filter panel"));

        gbcMaster.gridx = 1;
        gbcMaster.gridy = 0;

        this.add(dataWithSearch,gbcMaster);
//        this.add(searchAndTable,gbcMaster);


//        searchAndTable.setLayout(new GridBagLayout());
//        gbcSearch.fill = GridBagConstraints.BOTH;
//        gbcSearch.gridy = 0;
//        gbcSearch.gridx = 0;
//        gbcSearch.ipady = 20;
//        searchBox.add(searchField);
//        searchBox.add(searchButton);
//        this.searchAndTable.add(searchBox,gbcSearch);
//        gbcSearch.gridy = 1;
//        gbcSearch.weighty = 1;
//        gbcSearch.weightx = 1;
//        this.searchAndTable.add(eventPane,gbcSearch);
//
//        this.inside.setLayout(new GridBagLayout());
//        gbcEvent.fill = GridBagConstraints.HORIZONTAL;
//        gbcEvent.gridx = 0;
//        gbcEvent.gridy = 0;
//        gbcEvent.weightx = 1;
//        inside.setVisible(true);
//        eventPane.setVisible(true);

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
//            JLabel label = new JLabel(l);
//            label.setOpaque(true);
//            label.setBackground(this.col);
//            gbcEvent.gridy = this.displayedCount;
//            gbcEvent.fill = GridBagConstraints.HORIZONTAL;
//            this.displayedCount++;
//            this.alternateColor();
//            this.inside.add(label,gbcEvent);
//            this.repaint();
            this.dataWithSearch.model.addRow(new Object[]{l});
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
//                    inside.repaint();
                    Thread.sleep(20);
                }catch(InterruptedException e){}
            }
        } catch(Throwable e){}
    }
}
