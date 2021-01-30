import java.awt.BorderLayout;
import java.awt.Color ;
import java.awt.event.* ;
import java.awt.Component ;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer ;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.*;
import java.util.*;
import javax.swing.*;
import java.lang.*;

public class Table extends JPanel {

  // Table Data //
  private String[] columnNames = {"Source IP","Source Port","Dest IP", "Dest Port", "Classification", "Message", "Cluster #"};
  private Object[][] data = null;
  private Color [] clusterColor = { new Color(204, 204, 204), new Color(115, 173, 230), new Color(243, 142, 142), new Color(182, 239, 182), new Color(249, 249, 180), new Color(242, 203, 145), new Color(204, 153, 255), new Color(160, 246, 246), new Color(253, 159, 253), new Color(204, 255, 153) } ;


  // Table Model //
  private DefaultTableModel model;
  private JTable table;
  private JTextField textfield = new JTextField();
  private JButton query = new JButton("Filter");
  public TableRowSorter<TableModel> sorter;

  // Filter Parser for Table //
  private FilterParser fp = new FilterParser() ;

  // Table Cell Renderer to Color rows based on CLuster Number //
  private class ColorRenderer implements TableCellRenderer {

    public DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column){

      // get Component //
      Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

      // get cluster number //
      int actualRow = table.getRowSorter().convertRowIndexToModel(row);
      int clusterNum = (int) table.getModel().getValueAt(actualRow, 6);

      // color row //
      c.setBackground(clusterColor[clusterNum]);

      return c ;
    }
  }

  /* Constructor for Table class */
  public Table() {

    // create Table Model //
    model = new DefaultTableModel(data, columnNames) {
      public boolean isCellEditable(int row, int column) {
        return false;//This causes all cells to be not editable
      }
    };

    // create JTable //
    table = new JTable(model);

    // create Panel and search label //
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JLabel("Filter"), BorderLayout.WEST);

    // create action listener for button //
    query.addActionListener( new ActionListener(){

      public void actionPerformed(ActionEvent e){

        if(data == null){
          JOptionPane.showMessageDialog(null, "The table is empty.  Please run intrusion \ndetection to produce clustered data.", "No Data in Table!", JOptionPane.ERROR_MESSAGE);
          return;
        }
        else {
          String text = textfield.getText() ;

          // do not send empty text //
          if(text.length() == 0){
            textfield.setBackground(Color.RED) ;
          }
          else{ filterTable(text) ;}
        }
      }
    });
    panel.add(query, BorderLayout.EAST);

    // create documnet listener for text field //
    textfield.getDocument().addDocumentListener( new DocumentListener(){
      @Override
      public void insertUpdate(DocumentEvent e){ resetColor(); }

      @Override
      public void removeUpdate(DocumentEvent e){
        resetColor();
        sorter.setRowFilter(null);
      }

      @Override
      public void changedUpdate(DocumentEvent e){resetColor(); }

      private void resetColor(){ textfield.setBackground(Color.WHITE); }
    });
    panel.add(textfield, BorderLayout.CENTER);

    // add rest of components to panel //
    setLayout(new BorderLayout());
    add(panel, BorderLayout.SOUTH);
    add(new JScrollPane(table), BorderLayout.CENTER);
  }

  /* Update table data */
  public void updateData(Object[][] newData) {

    /* create new model with new data */
    data = newData;
    model = new DefaultTableModel(data, columnNames) {
      public boolean isCellEditable(int row, int column) {
        return false;//This causes all cells to be not editable
      }
    };

    // set table to new model //
    table.setModel(model);
    table.revalidate();

    /* create color renderer for rows */
    ColorRenderer rowRenderer = new ColorRenderer();
    table.setDefaultRenderer(Object.class, rowRenderer);

    /* create and add table filter */
    sorter = new TableRowSorter<>(table.getModel());
    table.setRowSorter(sorter);
    table.repaint();

  }

  public void filterTable(String text){

    // use FilterParser //
    RowFilter<Object,Object> filter = fp.parseFilter(text) ;
    if(filter != null){
      sorter.setRowFilter(filter);
      textfield.setBackground(Color.GREEN) ;
    }
    else{ textfield.setBackground(Color.RED); }
  }

  public JTable getTable() {
    return table;
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
