import java.awt.geom.*;
import java.awt.event.*;
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

    private JPanel filterPanel = new JPanel();
    private JCheckBox filter1 = new JCheckBox("Democracy");
    private JCheckBox filter2 = new JCheckBox("Not Democracy");

    private Table table = new Table();
    private Color col = Color.lightGray;

    // Visuals
    private GridBagConstraints gbc = new GridBagConstraints();
    private JTableSearch dataWithSearch = new JTableSearch();

    public EventPanel() {
        super();
        this.setBackground(new Color(245, 243, 213));
        this.setLayout(new GridBagLayout());

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;

        filterPanel.setPreferredSize(new Dimension(75,100));
        this.add(filterPanel,gbc);
        filterPanel.add(new JLabel("Filter Panel"));
        filterPanel.add(filter1);
        filterPanel.add(filter2);

        gbc.gridx = 1;
        gbc.gridy = 0;
        this.add(table, gbc);

        filter1.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if(filter1.isSelected()) {
              table.rowSorter.setRowFilter(RowFilter.regexFilter("true"));
            }
            else
              table.rowSorter.setRowFilter(null);
          }
        });

        filter2.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if(filter2.isSelected()) {
              table.rowSorter.setRowFilter(RowFilter.regexFilter("false"));
            }
            else
              table.rowSorter.setRowFilter(null);
          }
        });

        //this.add(dataWithSearch,gbc);
    }

    // public void actionPerformed(ActionEvent e) {
    //   if(filter1.isSelected())
    //     table.rowSorter.setRowFilter(RowFilter.regexFilter("true"));
    //   if(filter2.isSelected())
    //     table.rowSorter.setRowFilter(RowFilter.regexFilter("false"));
    //   else
    //     table.rowSorter.setRowFilter(RowFilter.regexFilter(""));
    // }

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
