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

        boolean haveID = false;
        int ID;
        // Initialize ID
        try{
            File myFile = new File(".config");

            if (myFile.createNewFile()){
                System.out.println("File is created!");
            }else{
                System.out.println("File already exists.");
            }
        } catch (FileNotFoundException e) { } catch (IOException e) { }
        
        // Read the content from file
        try{
            Scanner scanner = new Scanner(new File(".config"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                System.out.println(line);
                if (line.contains("id")){
                    String arr[] = line.split(":");
                    if (arr[0].equals("id")){ haveID = true; ID = Integer.parseInt(arr[1]); }
                }
            }
        } catch (IOException e) { } catch (NullPointerException e) { }

        if (haveID == false) {
            try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(".config"))) {
                Random rand = new Random();
                int random = rand.nextInt(10000);
                String fileContent = "id:"+random;
                bufferedWriter.write(fileContent);
            } catch (IOException e) { }
        }

        // Create components
        JFrame f = new JFrame();
        f.setTitle("Intrusion detection system");

        Menu menu = new Menu();
        Display display = new Display();

        // Add listener
        BarListener r = new BarListener(menu, display);
        menu.addListener(r);
        display.addListener(r);

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
