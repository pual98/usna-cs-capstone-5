import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
/*
Much of the search functionality was pulled from the following link:
https://www.tutorialspoint.com/how-to-implement-the-search-functionality-of-a-jtable-in-java

This class allows searching a table. This is utilites in the "Event Panel", as
that panel is where the filters and searching of data will take place.
*/
public class JTableSearch extends JPanel {
    //Privates
    //Field to actually type in
    private JTextField jtf;

    //Label for searching
    private JLabel searchLbl;
    private JTable table;
    public DefaultTableModel model;
    private TableRowSorter sorter;

    private JScrollPane jsp;
    private GridBagConstraints gbc = new GridBagConstraints();

    public JTableSearch() {
        jtf = new JTextField(15);
        searchLbl = new JLabel("Search");
        String[] columnNames = {"Arbitrary Name"};
        Object[][] rowData = {};
        model = new DefaultTableModel(rowData, columnNames);
        sorter = new TableRowSorter<>(model);
        table = new JTable(model){
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                Color alternateColor = new Color(200, 201, 210);
                Color whiteColor = Color.WHITE;

                //Alternating colors for visuals
                if(!comp.getBackground().equals(getSelectionBackground())) {
                    Color c = (row % 2 == 0 ? alternateColor : whiteColor);
                    comp.setBackground(c);
                    c = null;
                }
                return comp;
            }
        };
        table.setRowSorter(sorter);

        //Visuals:
        setLayout(new GridBagLayout());
        gbc.fill = GridBagConstraints.BOTH;
        jsp = new JScrollPane(table);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(searchLbl,gbc);
        gbc.gridy = 1;
        add(jtf,gbc);
        gbc.gridy = 2;
        gbc.weighty = 1;
        add(jsp,gbc);

        //Listener functionality
        jtf.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                search(jtf.getText());
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                search(jtf.getText());
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                search(jtf.getText());
            }
            public void search(String str) {
                if (str.length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter(str));
                }
            }
        });
    }
    public static void main(String[] args) {
        new JTableSearch();
    }
}
