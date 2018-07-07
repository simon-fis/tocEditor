package components.tree;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

import format.FormatPageNumber;

public class MyTreeCellEditor extends DefaultTreeCellEditor
{
	private DefaultTreeCellEditor fPageNumberEditor;
	private boolean fPageNumberEditorUsed = false;

	public MyTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer)
	{
		super(tree, renderer);

		JComboBox<FormatPageNumber> comboBox = new JComboBox<FormatPageNumber>(FormatPageNumber.values());
		fPageNumberEditor = new DefaultTreeCellEditor(tree, renderer, new DefaultCellEditor(comboBox));
	}

	public boolean isCellEditable(EventObject event)
	{
		boolean editable = super.isCellEditable(event);

		if (editable)
		{
			Object node = tree.getLastSelectedPathComponent();
			if (node == null)
				return false;

			return tree.getModel().isLeaf(node);
		}
		return editable;
	}

	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
			boolean leaf, int row)
	{
		if (value instanceof NodeFormatPageNumber)
		{
			fPageNumberEditorUsed = true;

			return fPageNumberEditor.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		}

		fPageNumberEditorUsed = false;

		return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
	}

	public Object getCellEditorValue()
	{
		if (fPageNumberEditorUsed)
		{
			return fPageNumberEditor.getCellEditorValue().toString();
		}

		return super.getCellEditorValue();
	}

	@Override
	public boolean shouldSelectCell(EventObject anEvent)
	{
		if (fPageNumberEditorUsed)
		{
			return fPageNumberEditor.shouldSelectCell(anEvent);
		}
		return super.shouldSelectCell(anEvent);
	}

	@Override
	public boolean stopCellEditing()
	{
		if (fPageNumberEditorUsed)
		{
			fPageNumberEditor.stopCellEditing();
		}
		return super.stopCellEditing();
	}

	@Override
	public void cancelCellEditing()
	{
		if (fPageNumberEditorUsed)
		{
			fPageNumberEditor.cancelCellEditing();
			return;
		}

		super.cancelCellEditing();
	}

	@Override
	public void addCellEditorListener(CellEditorListener l)
	{
		super.addCellEditorListener(l);
		fPageNumberEditor.addCellEditorListener(l);
	}

	@Override
	public void removeCellEditorListener(CellEditorListener l)
	{
		super.removeCellEditorListener(l);
		fPageNumberEditor.removeCellEditorListener(l);
	}

}
