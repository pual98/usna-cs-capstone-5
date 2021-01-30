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
        int ID=0;
        // Initialize ID
        // If config doesn't exist, create it
        try{
            File myFile = new File(".config");

            if (myFile.createNewFile()){
                System.out.println("File is created!");
            }else{
                System.out.println("File already exists.");
            }
        } catch (FileNotFoundException e) { } catch (IOException e) { }

        // Read the content from file
        // Get the id and set variable
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

        // If the file exists but there is no id, save a new id to the config
        // file.
        if (haveID == false) {
            try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(".config"))) {
                Random rand = new Random();
                int random = rand.nextInt(10000);
                String fileContent = "id:"+random;
                bufferedWriter.write(fileContent);
                ID = random;
            } catch (IOException e) { }
        }

        // Create components
        JFrame f = new JFrame();
        f.setTitle("Intrusion Detection System -- ID:"+ID);

        JLabel label1 = new JLabel("Intrusion Detection System -- ID:"+ID);

        //An IDS system has a menu (top bar), a display area for all data, and
        //a client (itself) to send data to other clients.
        Display display = new Display();
        Menu menu = new Menu(display);
        Client client = new Client();

        //Start client thread
        Thread clientThread = new Thread(client);
        clientThread.start();

        // Add listener to menu, display, client
        BarListener r = new BarListener(menu, display, client);
        menu.addListener(r);
        display.addListener(r);

        // Add components
        f.getContentPane().add(BorderLayout.NORTH, menu);
        f.setJMenuBar(menu);
        f.getContentPane().add(BorderLayout.NORTH, label1);
        f.getContentPane().add(BorderLayout.CENTER, display);

        Thread displayThread = new Thread(display);
        displayThread.start();

        // Minor settings.
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);


        if (args[0].equals("-f")){
            display.getEventPanel().setFilePath(args[1]);
        }
    }
}
