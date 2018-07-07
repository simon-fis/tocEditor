package components.tree;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import format.Format;

public class TreePanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	protected MyTreeModel treeModel;
	protected JTree tree;

	public TreePanel(Format format, TreeModelListener tml)
	{
		super(new GridLayout(1, 0));

		treeModel = new MyTreeModel(format);
		tree = new JTree(treeModel);
		tree.setEditable(true);

		MyTreeCellRenderer cellRenderer = new MyTreeCellRenderer();
		tree.setCellRenderer(cellRenderer);

		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(false);
		tree.setRootVisible(false);

		// DefaultTreeCellRenderer renderer =
		// (DefaultTreeCellRenderer)tree.getCellRenderer();
		// String elements[] = { "A", "B", "C", "D"} ;
		// JComboBox comboBox = new JComboBox(elements);
		// comboBox.setEditable(true);
		// TreeCellEditor comboEditor = new DefaultCellEditor(comboBox);
		// TreeCellEditor editor = new DefaultTreeCellEditor(tree, renderer,
		// comboEditor);
		// tree.setCellEditor(editor);

		tree.setCellEditor(new MyTreeCellEditor(tree, cellRenderer));

		add(tree);
		expandAll();

		if (tml != null)
			treeModel.addTreeModelListener(tml);
	}

	private void expandAll()
	{
		for (int i = 0; i < tree.getRowCount(); i++)
		{
			tree.expandRow(i);
		}
	}

	public void removeCurrentLevel()
	{
		TreePath selectionPath = tree.getSelectionPath();

		if (selectionPath == null)
			return;

		treeModel.removeLevel(selectionPath);
		tree.updateUI();
	}

	public void addNewLevel()
	{
		TreePath selectionPath = tree.getSelectionPath();

		if (selectionPath == null)
			return;

		TreePath path = treeModel.addLevel(selectionPath);
		if (path != null)
		{
			tree.expandRow(tree.getRowForPath(path));
			tree.updateUI();
		}
	}

	public void removeCurrentPart()
	{
		TreePath selectionPath = tree.getSelectionPath();

		if (selectionPath == null)
			return;

		treeModel.removePart(selectionPath);
		tree.updateUI();
	}

	public void addNewPart()
	{
		TreePath selectionPath = tree.getSelectionPath();

		if (selectionPath == null)
			return;

		TreePath path = treeModel.addPart(selectionPath);
		if (path != null)
		{
			expandAll();
			tree.updateUI();
		}
	}

	public void addNewPart(int startEntry)
	{
		TreePath path = treeModel.addPart(startEntry);
		if (path != null)
		{
			expandAll();
			tree.updateUI();
		}
	}
}
