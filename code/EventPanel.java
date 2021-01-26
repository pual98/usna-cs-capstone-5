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

    private ArrayList<String> parsedLines ;
    private ArrayList<Entity> entitiesFromFile = new ArrayList<Entity>();

    //filter panel features
    private JPanel filterPanel = new JPanel();
    private JCheckBox filter1 = new JCheckBox("Web Application Attacks");
    private JCheckBox filter2 = new JCheckBox("Attempted Information Leak");

    //upload file panel
    private JPanel fileUpload;
    private JButton browse;
    private JButton upload;
    private JLabel fileLabel;
    private JTextField uploadTextField;
    private JFileChooser fileChooser;
    private String filename = "";
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
        parsedLines = new ArrayList<String>();

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
              table.rowSorter.setRowFilter(RowFilter.regexFilter("Web Application Attack"));
            }
            else
              table.rowSorter.setRowFilter(null);
          }
        });

        filter2.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if(filter2.isSelected()) {
              table.rowSorter.setRowFilter(RowFilter.regexFilter("Attempted Information Leak"));
            }
            else
              table.rowSorter.setRowFilter(null);
          }
        });

        fileUpload = new JPanel();
        browse = new JButton("Browse...");
        upload = new JButton("Upload");
        fileLabel = new JLabel("Choose Snort Test File");
        uploadTextField = new JTextField(25);
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
        JOptionPane.showMessageDialog(null, "No File Selected", "Error!", JOptionPane.ERROR_MESSAGE);
        return;
      }
      filename = uploadTextField.getText();
      if(!filename.contains(".txt")) {
        JOptionPane.showMessageDialog(null, "Please Upload a Snort Text File", "Wrong File Extension!", JOptionPane.ERROR_MESSAGE);
        uploadTextField.setText("");
        filename = "";
        return;
      }
      else{
        try{
          FileReader fr = new FileReader(filename);
          BufferedReader br = new BufferedReader(fr);
          FileWriter fw = new FileWriter("output.csv");
          BufferedWriter bw = new BufferedWriter(fw);
          String line;

          //
          while ((line = br.readLine()) != null) {
            AlertParser a = new AlertParser(line);
            a.parseLine();

            if(a.isTCP()) {
              //keep track of parsed lines as strings
              String parsedLine = a.genCSVOutput();
              parsedLines.add(parsedLine);
              //keep track of parsed lines as Entities
              Entity en = a.genEntityFromLine(parsedLine);
              if(en != null)
                entitiesFromFile.add(en);
            }
          }
          JOptionPane.showMessageDialog(null, "Upload Complete!", "Data Uploaded", JOptionPane.INFORMATION_MESSAGE);
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(null, "File provided is in wrong format!", "File Not Supported!", JOptionPane.ERROR_MESSAGE);
            filename = "";
            uploadTextField.setText("");
            return;
        }
      }
    }

    public void setFilePath(String text){
        uploadTextField.setText(text);
    }

    public void populateTable() {
      Object[][] data = new Object[parsedLines.size()][7];
      for(int i = 0; i < parsedLines.size(); i++) {
        String[] row = parsedLines.get(i).split(";");
        //source IP
        data[i][0] = row[6];
        //source port
        data[i][1] = row[7];
        //dest IP
        data[i][2] = row[8];
        //dest port
        data[i][3] = row[9];
        //classification
        data[i][4] = row[3];
        //message
        data[i][5] = row[2];
        //cluster number
        data[i][6] = entitiesFromFile.get(i).getAssignedCluster();
      }
      table.updateData(data);
      this.revalidate();
      this.repaint();
    }

    public ArrayList<Entity> getDataset() {
      return entitiesFromFile;
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
