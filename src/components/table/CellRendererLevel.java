package components.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import status.EntryStatusLevel;

public class CellRendererLevel extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 1262860561982919669L;

	Font bold;
	Font normal;

	public CellRendererLevel()
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

		l.setForeground(Color.BLACK);
		l.setFont(isSelected ? bold : normal);

		// Get the status for the current row.
		MyTableModel tableModel = (MyTableModel) table.getModel();
		EntryStatusLevel status = tableModel.getLevelStatus(row);

		switch(status){
		case good:
			l.setBackground(Color.GREEN);
			setToolTipText(null);
			break;
		case notSet:
			l.setBackground(Color.RED);
			setToolTipText("notSet");
			break;
		case jumping:
			l.setBackground(Color.RED);
			setToolTipText("jumbing");
			break;
		case missing:
			l.setBackground(Color.YELLOW);
			setToolTipText("missing or order");
			break;
		}

		return l;
	}
}