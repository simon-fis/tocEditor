package components.table;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import status.EntryStatusTitle;

public class CellEditorTitle extends AbstractCellEditor implements TableCellEditor
{
	private static final long serialVersionUID = 1L;

	JTextField textField = new JTextField();

	public CellEditorTitle()
	{
	}

	@Override
	public Object getCellEditorValue()
	{
		return textField.getText();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column)
	{
		textField.setText((String) value);
		// textField.selectAll();
		MyTableModel model = (MyTableModel) table.getModel();
		textField.setCaretPosition(0);
		
		if(model.getTitleStatus(row) == EntryStatusTitle.unusualChars)
		{
			textField.setCaretPosition(model.getEntry(row).titleUnusualCharsAt);
		}

		return textField;
	}

	public boolean isCellEditable(EventObject e)
	{

		if (e instanceof MouseEvent)
		{
			return false;
		}

		return true;
	}

}
