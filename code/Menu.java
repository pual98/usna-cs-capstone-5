import java.util.*;
import java.io.*;
import java.awt.*;        // Using AWT layouts
import java.awt.event.*;  // Using AWT event classes and listener interfaces
import javax.swing.*;     // Using Swing components and containers

/*
The Menu is the top bar; aka File, Options, Help.
TODO: menu buttons need to work with action listeners. They currently do
      nothing. If they are made to work, they will replace some of the buttons
      located in ControlPanel.java
*/
public class Menu extends JMenuBar {
    //Make menu main options
    JMenu file, help, options;

    //Specific elements--sub menu options
    JMenuItem filterHelp;
    public Menu(){
        //Create menus
        file = new JMenu("File");
        help = new JMenu("Help");

        //Add sub menus
        options = new JMenu("Options");
        filterHelp = new JMenuItem("Filter Commands");
        this.add(file);
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
                } catch (IOException e) { e.printStackTrace();
        }
                JScrollPane scrollPane = new JScrollPane(text);
                commands.add(scrollPane, BorderLayout.CENTER);
                commands.pack();
                commands.setVisible(true);
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
