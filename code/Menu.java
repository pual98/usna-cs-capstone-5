import java.util.*;
import java.io.*;
import java.awt.*;        // Using AWT layouts
import java.awt.event.*;  // Using AWT event classes and listener interfaces
import javax.swing.*;     // Using Swing components and containers

public class Menu extends JMenuBar{
    JMenu file, help;
    public Menu(){
        file = new JMenu("File");
        help = new JMenu("help");
        this.add(file);
        this.add(help);
        this.setBackground(Color.gray);
    }
    public void addListener(BarListener r){
        this.addMouseListener(r);
    }
}
