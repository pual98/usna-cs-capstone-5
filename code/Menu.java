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
    JMenuItem addClient;
    public Menu(){
        //Create menus
        file = new JMenu("File");
        help = new JMenu("Help");

        //Add sub menus
        options = new JMenu("options");
        addClient = new JMenuItem("Add a collaborator");
        this.add(file);
        file.add(addClient);
        this.add(help);
        this.add(options);

        //Visuals
        this.setBackground(Color.lightGray);
        Font menuFont = new Font("Times New Roman", Font.PLAIN, 16);
        UIManager.put("Menu.font",menuFont);
        this.setFont(menuFont);
    }
    //Allow "add client" button to listen for button clicks...not currenly
    //working. Additionaly, all future buttons go here.
    //TODO expand for all buttons.
    public void addListener(BarListener r){
        addClient.addActionListener(r);
    }
}
