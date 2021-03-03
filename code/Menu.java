import java.util.*;
import java.io.*;
import java.awt.*;        // Using AWT layouts
import java.awt.event.*;  // Using AWT event classes and listener interfaces
import javax.swing.*;     // Using Swing components and containers
import javax.swing.table.TableModel;

/*
The Menu is the top bar; aka File, Options, Help.
TODO: menu buttons need to work with action listeners. They currently do
      nothing. If they are made to work, they will replace some of the buttons
      located in ControlPanel.java
*/
public class Menu extends JMenuBar {

    private Client client;
    private Display display;
    //Make menu main options
    private JMenu file, help, options;

    //Specific elements--sub menu options
    private JMenuItem filterHelp;
    private JMenuItem export;
    private JMenuItem getData;
    private JMenuItem sendToClient;
    private JMenuItem sendToGroup;

    public Menu(Display d, Client c){
        display = d;
        client = c;

        //Create menus
        file = new JMenu("File");
        help = new JMenu("Help");
        options = new JMenu("Options");
        this.add(file);
        this.add(help);
        this.add(options);

        //Add sub menus
        export = new JMenuItem("Export Table as CSV");
        getData = new JMenuItem("Export Data as CSV");
        file.add(export);
        file.add(getData);

        filterHelp = new JMenuItem("Filter Commands");
        help.add(filterHelp);

        sendToClient = new JMenuItem("Send Message to Client");
        sendToGroup = new JMenuItem("Send Message to Group");
        options.add(sendToGroup);
        options.add(sendToClient);


        //Visuals
        this.setBackground(Color.lightGray);
        Font menuFont = new Font("Times New Roman", Font.PLAIN, 16);
        UIManager.put("Menu.font",menuFont);
        this.setFont(menuFont);

        filterHelp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JFrame commands = new JFrame("Filter Commands Help");
                commands.setLayout(new BorderLayout());
                commands.setSize(400,500);
                JTextArea text = new JTextArea(30,40);
                text.setEditable(true);
                text.setLineWrap(true);
                Font font = new Font("Segoe Script", Font.BOLD, 15);
                text.setFont(font);
                try {
                    text.read(new InputStreamReader(getClass().getResourceAsStream("filterCommands.txt")),null);
                } catch (IOException e) { e.printStackTrace(); }
                JScrollPane scrollPane = new JScrollPane(text);
                commands.add(scrollPane, BorderLayout.CENTER);
                commands.pack();
                commands.setVisible(true);
            }
        });

        export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
              //access table//
              JTable table = display.getTable();
              if(table.getRowCount()==0) {
                JOptionPane.showMessageDialog(null, "Table is Empty", "No Data to Export", JOptionPane.ERROR_MESSAGE);
                return;
              }
              else {
                try {
                  TableModel model = table.getModel();
                  JFileChooser chooser = new JFileChooser();
                  int ret = chooser.showSaveDialog(null);
                  if(ret == JFileChooser.APPROVE_OPTION) {
                    File fname = chooser.getSelectedFile();
                    if(fname == null)
                      return;
                    if (!fname.getName().toLowerCase().endsWith(".csv"))
                      fname = new File(fname.getParentFile(), fname.getName() + ".csv");

                    FileWriter csv = new FileWriter(fname);
                    for (int i = 0; i < model.getColumnCount(); i++) {
                      csv.write(model.getColumnName(i) + ",");
                    }

                    csv.write("\n");
                    for (int i = 0; i < model.getRowCount(); i++) {
                      for (int j = 0; j < model.getColumnCount(); j++) {
                        String field = model.getValueAt(i,j).toString().replace("\n","");
                        csv.write(field + ",");
                      }
                      csv.write("\n");
                    }
                    csv.close();
                  JOptionPane.showMessageDialog(null, "Table Exported as \n" + fname, "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                }

                } catch (IOException e) { e.printStackTrace(); }
              }
            }
        });

        getData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {

              ArrayList<Entity> data = client.getDataset();
              if(data.size() == 0) {
                JOptionPane.showMessageDialog(null, "You must upload data and run instrusion\n on data prior to exporting!", "Upload Required", JOptionPane.ERROR_MESSAGE);
                return;
              }
              else {
                try {
                  JFileChooser chooser = new JFileChooser();
                  int ret = chooser.showSaveDialog(null);
                  if(ret == JFileChooser.APPROVE_OPTION) {
                    File fname = chooser.getSelectedFile();
                    if(fname == null)
                      return;
                    if (!fname.getName().toLowerCase().endsWith(".csv"))
                      fname = new File(fname.getParentFile(), fname.getName() + ".csv");

                    FileWriter csv = new FileWriter(fname);
                    for(Entity e: data) {
                      String line = e.genCSVOutput();
                      csv.write(line);
                    }
                    csv.close();
                    JOptionPane.showMessageDialog(null, "Data Exported as \n" + fname, "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                  }
                } catch (IOException e) {
                  System.out.println("Export Failed!");
                  e.printStackTrace();
                }
              }
            }
        });

        sendToClient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
              if(client.getGroupStatus()) {
                Message mmsg = new Message(17, client.groupname, client.getID(), 0);
                client.sendMessage(mmsg);
              }
              else {
                JOptionPane.showMessageDialog(null, "You are not in a group yet.", "Can't Send Message.", JOptionPane.ERROR_MESSAGE);
              }
            }
          });

        sendToGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
              if(client.getGroupStatus()) {
                String message = null;
                message = JOptionPane.showInputDialog("Desired Message");

                // send Message Type 11 to Server //
                String msg = message+":"+client.groupname;
                Message mmsg = new Message(11, msg, client.getID(), 0);
                //System.out.println("I am Client "+this.client.getID()+" and I am sending: "+msg);
                client.sendMessage(mmsg);
              }
              else {
                JOptionPane.showMessageDialog(null, "You are not in a group yet.", "Can't Send Message.", JOptionPane.ERROR_MESSAGE);
              }
            }
          });

    }
    //Allow "add client" button to listen for button clicks...not currenly
    //working. Additionaly, all future buttons go here.
    //TODO expand for all buttons.
    public void addListener(BarListener r){
        filterHelp.addActionListener(r);
    }
}
