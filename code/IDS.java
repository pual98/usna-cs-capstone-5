import java.util.*;
import java.io.*;
import java.awt.*;        // Using AWT layouts
import java.awt.event.*;  // Using AWT event classes and listener interfaces
import javax.swing.*;     // Using Swing components and containers
 
// A Swing GUI application inherits from top-level container javax.swing.JFrame
/**
 * Main IDS class
 * - Swing Gui application;  java.swing.JFrame
 * @author Bishop, Mokry, Quiroz, Slife
 **/
public class IDS {
    
    public static void main(String[] args) {

        // Create components
        JFrame f = new JFrame();
        f.setTitle("Intrusion detection system");

        Menu menu = new Menu();
        Display display = new Display();

        // Add listener
        BarListener r = new BarListener(menu, display);
        menu.addListener(r);

        // Add components
        f.getContentPane().add(BorderLayout.NORTH, menu);
        f.setJMenuBar(menu);
        f.getContentPane().add(BorderLayout.CENTER, display); 

        Thread displayThread = new Thread(display);
        displayThread.start();

        // Minor settings.
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
