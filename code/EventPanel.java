import java.awt.geom.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.awt.event.ActionEvent;

public class EventPanel extends JPanel implements Runnable {

    private BarListener r = null;
    private ArrayList<String> events= new ArrayList<String>();

    //filter panel features
    private JPanel filterPanel = new JPanel();
    private JCheckBox filter1 = new JCheckBox("Democracy");
    private JCheckBox filter2 = new JCheckBox("Not Democracy");

    //upload file panel
    private JPanel fileUpload;
    private JButton browse;
    private JButton upload;
    private JLabel fileLabel;
    private JTextField uploadTextField;
    private JFileChooser fileChooser;
    private String filename;
    private String outputCSV;


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

        //filter panel set up
        filterPanel.setPreferredSize(new Dimension(75,100));
        this.add(filterPanel,gbc);
        filterPanel.add(new JLabel("Filter Panel"));
        filterPanel.add(filter1);
        filterPanel.add(filter2);

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

        fileUpload = new JPanel();
        browse = new JButton("Browse...");
        upload = new JButton("Upload");
        fileLabel = new JLabel("Choose Snort Test File");
        uploadTextField = new JTextField(30);
        uploadTextField.setEditable(false);
        fileChooser = new JFileChooser();

        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                browse(evt);
            }
        });

        upload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                upload(evt);
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 1;
        fileUpload.add(fileLabel);
        fileUpload.add(uploadTextField);
        fileUpload.add(browse);
        fileUpload.add(upload);
        this.add(fileUpload, gbc);

        FileTypeFilter fileFilter = new FileTypeFilter("txt", "Snort Text Files");
        fileChooser.addChoosableFileFilter(fileFilter);


        gbc.gridx = 1;
        gbc.gridy = 0;
        this.add(table, gbc);

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

    //when the browse button is pressed
    public void browse(ActionEvent evt) {
      if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            uploadTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
          }
    }

    //when the upload button is pressed
    public void upload(ActionEvent evt) {
      if(uploadTextField.getText().equals("")) {
        JOptionPane.showMessageDialog(null, "No Snort File Selected", "Error!", JOptionPane.ERROR_MESSAGE);
        return;
      }
      filename = uploadTextField.getText();
      if(!filename.contains(".txt")) {
        JOptionPane.showMessageDialog(null, "Please Upload a Snort Text File", "Wrong File Extension!", JOptionPane.ERROR_MESSAGE);
        uploadTextField.setText("");
        return;
      }
    }

    //return filename so that it can be accessed by the client
    public String getFilename() {
      return filename;
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

    public void updatePanel() {
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
