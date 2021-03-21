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
    private ArrayList<Entity> newData = new ArrayList<Entity>();
    private int uploaded = 0;
    private boolean runKProto = false;


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

    public EventPanel() {
        super();
        this.setBackground(new Color(133, 199, 127));
        this.setLayout(new BorderLayout());
        parsedLines = new ArrayList<String>();

        fileUpload = new JPanel();
        browse = new JButton("Browse...");
        upload = new JButton("Upload");
        fileLabel = new JLabel("Choose Snort Text File");
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

        fileUpload.add(fileLabel);
        fileUpload.add(uploadTextField);
        fileUpload.add(browse);
        fileUpload.add(upload);
        this.add(fileUpload, BorderLayout.PAGE_START);

        FileTypeFilter fileFilter = new FileTypeFilter("txt", "Snort Text Files");
        fileChooser.addChoosableFileFilter(fileFilter);


        this.add(table, BorderLayout.CENTER);
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
        if(uploaded == 1 && !runKProto) {
          JOptionPane.showMessageDialog(null, "You must run intrusion detection before correlating \nnew data to clusters.", "No Learned Model", JOptionPane.ERROR_MESSAGE);
          return;
        }
        if(uploaded == 0) {
          try{
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            String line;

            //
            while ((line = br.readLine()) != null) {
              AlertParser a = new AlertParser(line);
              a.parseLine();

              if(a.isTCP()) {
                //keep track of parsed lines as strings
                String parsedLine = a.genCSVOutput() + ";"+filename;
                parsedLines.add(parsedLine);
                //keep track of parsed lines as Entities
                Entity en = a.genEntityFromLine(parsedLine);
                if(en != null)
                  entitiesFromFile.add(en);
              }
            }
            JOptionPane.showMessageDialog(null, "Upload Complete: \nReady to Run Intrusion Detection!", "Data Uploaded", JOptionPane.INFORMATION_MESSAGE);
            uploadTextField.setText("");
            uploaded++;
          } catch (Throwable e) {
              JOptionPane.showMessageDialog(null, "File provided is in wrong format!", "File Not Supported!", JOptionPane.ERROR_MESSAGE);
              filename = "";
              uploadTextField.setText("");
              return;
          }
        }
        if(uploaded > 0 && runKProto) {
          try{
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            String line;

            //
            while ((line = br.readLine()) != null) {
              AlertParser a = new AlertParser(line);
              a.parseLine();

              if(a.isTCP()) {
                //keep track of parsed lines as strings
                String parsedLine = a.genCSVOutput() + ";"+filename;
                parsedLines.add(parsedLine);
                //keep track of parsed lines as Entities
                Entity en = a.genEntityFromLine(parsedLine);
                if(en != null)
                  newData.add(en);
              }
            }
            JOptionPane.showMessageDialog(null, "Upload Complete: \nReady to Correlate New Data", "Data Uploaded", JOptionPane.INFORMATION_MESSAGE);
            filename = "";
            uploadTextField.setText("");
            uploaded++;
          } catch (Throwable e) {
              JOptionPane.showMessageDialog(null, "File provided is in wrong format!", "File Not Supported!", JOptionPane.ERROR_MESSAGE);
              filename = "";
              uploadTextField.setText("");
              return;
          }
        }
      }
    }

    public void setFilePath(String text){
        uploadTextField.setText(text);
    }

    public void setAlgorithmComplete() {
      runKProto = true;
    }

    public void populateTable() {
      Object[][] data = new Object[parsedLines.size()][8];

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
        if(i < entitiesFromFile.size()) {
          data[i][6] = entitiesFromFile.get(i).getAssignedCluster();
        }
        else {
          data[i][6] = newData.get(i-entitiesFromFile.size()).getAssignedCluster();
        }
        //filename
        data[i][7] = row[10];
      }
      table.updateData(data);
      this.revalidate();
      this.repaint();
    }

    public JTable getTable() {
      return table.getTable();
    }

    public int getUploadCount() { return uploaded; }

    public ArrayList<Entity> getDataset() {
      return entitiesFromFile;
    }

    public ArrayList<Entity> getNewData() {
      return newData;
    }

    //return filename so that it can be accessed by the client
    public String getFilename() {
      return filename;
    }

    public void addText(String s){
        this.events.add(s);
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
