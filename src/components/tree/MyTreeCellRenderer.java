package components.tree;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import format.FormatEntryNumber;

public class MyTreeCellRenderer extends DefaultTreeCellRenderer
{
	private static final long serialVersionUID = 1L;

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus)
	{
		Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		JLabel label = (JLabel) comp;

		if (value instanceof FormatEntryNumber)
		{
			label.setText("Pattern: " + value.toString());
		} else if (value instanceof NodeInteger)
		{
			label.setText("Start at: " + value.toString());
		} else if(value instanceof NodeFormatPageNumber) {
			label.setText("Page Label: " + value.toString());
		}

		return comp;
	}
}
