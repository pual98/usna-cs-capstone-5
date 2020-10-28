import java.util.*;
import java.io.*;
import java.awt.*;        // Using AWT layouts
import java.awt.event.*;  // Using AWT event classes and listener interfaces
import javax.swing.*;     // Using Swing components and containers

public class Menu extends JMenuBar {
    JMenu file, help, options;
    JMenuItem addClient;
    public Menu(){
        file = new JMenu("File");
        help = new JMenu("help");
        options = new JMenu("options");
        addClient = new JMenuItem("Add a collaborator");
        this.add(file);
        file.add(addClient);
        this.add(help);
        this.add(options);
        this.setBackground(Color.lightGray);
        Font menuFont = new Font("Times New Roman", Font.PLAIN, 16);
        UIManager.put("Menu.font",menuFont);
        this.setFont(menuFont);
    }
    public void addListener(BarListener r){
        addClient.addActionListener(r);
//        file.addActionListener(r);
    }
}
