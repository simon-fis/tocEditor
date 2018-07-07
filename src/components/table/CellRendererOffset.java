package components.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class CellRendererOffset extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 1262860561982919669L;

	Font bold;
	Font normal;

	public CellRendererOffset()
	{
		normal = new Font("Arial", Font.PLAIN, 11);
		bold = new Font("Arial", Font.BOLD, 11);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col)
	{

		// Cells are by default rendered as a JLabel.
		JLabel l = (JLabel) super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, col);

		// Get the status for the current row.
		MyTableModel tableModel = (MyTableModel) table.getModel();
		boolean offsetSet= tableModel.getOffsetSet(row);
		
		l.setForeground(Color.BLACK);
		l.setFont((isSelected || offsetSet)? bold : normal);
		
		return l;
	}
}