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
public class IDS extends JFrame {
 
   // Private instance variables
   // ......
 
   // Constructor to setup the GUI components and event handlers
   public IDS() {
      // Retrieve the top-level content-pane from JFrame
      Container cp = getContentPane();
 
      // Content-pane sets layout
      cp.setLayout(new FlowLayout());
 
      // Allocate the GUI components
      // .....
 
      // Content-pane adds components
      cp.add(new JLabel("Intrusion Detection System"));
 
      // Source object adds listener
      // .....
 
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      // Exit the program when the close-window button clicked
      setTitle("Intrusion Detection System");// "super" JFrame sets title
      setSize(300, 150);   // "super" JFrame sets initial size
      setVisible(true);    // "super" JFrame shows
   }
 
   public static void main(String[] args) {
      // Run GUI codes in Event-Dispatching thread for thread-safety
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            new IDS();
         }
      });
   }
}

