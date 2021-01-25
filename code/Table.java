import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class Table extends JPanel {

    //private String[] columnNames
      //      = {"sid", "rev", "Message", "Priority", "epochTime", "srcIP", "srcPort", "destIP", "destPort"};

    private String[] columnNames = {"Source IP","Source Port","Dest IP", "Dest Port", "Classification", "Message", "Cluster #"};
    private Object[][] data = null;
    // private Object[][] data = {
    //     {"USA", "Washington DC", 280, true},
    //     {"Canada", "Ottawa", 32, true},
    //     {"United Kingdom", "London", 60, true},
    //     {"Germany", "Berlin", 83, false},
    //     {"France", "Paris", 60, true},
    //     {"Norway", "Oslo", 4.5, false},
    //     {"India", "New Delhi", 1046, true}
    // };


    private DefaultTableModel model;
    private JTable table;
    public TableRowSorter<TableModel> rowSorter;

    private JTextField textfield = new JTextField();

    public Table() {

        model = new DefaultTableModel(data, columnNames) {
          public boolean isCellEditable(int row, int column) {
            return false;//This causes all cells to be not editable
          }
        };
        table = new JTable(model);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Search"), BorderLayout.WEST);
        panel.add(textfield, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.SOUTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

  public void updateData(Object[][] newData) {
      data = newData;
      model = new DefaultTableModel(data, columnNames) {
        public boolean isCellEditable(int row, int column) {
          return false;//This causes all cells to be not editable
        }
      };
      table.setModel(model);
      table.revalidate();
      table.repaint();

      rowSorter = new TableRowSorter<>(table.getModel());
      table.setRowSorter(rowSorter);
      textfield.getDocument().addDocumentListener(new DocumentListener(){

          @Override
          public void insertUpdate(DocumentEvent e) {
              String text = textfield.getText();

              if (text.trim().length() == 0) {
                  rowSorter.setRowFilter(null);
              } else {
                  rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
              }
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
              String text = textfield.getText();

              if (text.trim().length() == 0) {
                  rowSorter.setRowFilter(null);
              } else {
                  rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
              }
          }

          @Override
          public void changedUpdate(DocumentEvent e) {
              throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
          }
      });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
               JFrame frame = new JFrame("Row Filter");
               frame.add(new Table());
               frame.pack();
               frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
               frame.setLocationRelativeTo(null);
               frame.setVisible(true);
            }

        });
    }
}
