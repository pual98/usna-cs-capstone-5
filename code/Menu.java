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

    Display display;
    //Make menu main options
    JMenu file, help, options;

    //Specific elements--sub menu options
    JMenuItem filterHelp;
    JMenuItem export;

    public Menu(Display d){
        Display display = d;
        //Create menus
        file = new JMenu("File");
        help = new JMenu("Help");

        //Add sub menus
        options = new JMenu("Options");
        filterHelp = new JMenuItem("Filter Commands");
        export = new JMenuItem("Export Table as CSV");
        this.add(file);
        file.add(export);

        this.add(help);
        help.add(filterHelp);

        this.add(options);

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
    }
    //Allow "add client" button to listen for button clicks...not currenly
    //working. Additionaly, all future buttons go here.
    //TODO expand for all buttons.
    public void addListener(BarListener r){
        filterHelp.addActionListener(r);
    }
}
