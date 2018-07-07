package components.tree;

import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import format.Format;
import format.FormatLevel;
import format.FormatPart;

public class MyTreeModel implements TreeModel
{
	private static final int fLevelOffset = 2;

	private Vector<TreeModelListener> treeModelListeners = new Vector<TreeModelListener>();

	private Format format;

	public MyTreeModel(Format format)
	{
		this.format = format;
	}

	@Override
	public Object getRoot()
	{
		return format;
	}

	@Override
	public Object getChild(Object parent, int index)
	{
		// System.out.println("parent=" + parent.toString() + "index=" + index);
		if (parent instanceof Format)
		{
			// System.out.println("getChild: content format");
			Format temp = (Format) parent;

			return temp.fParts.get(index);

		} else if (parent instanceof FormatPart)
		{
			// System.out.println("getChild: content part format");
			FormatPart temp = (FormatPart) parent;

			if (index == 0)
			{
				return temp.getNodefPageNumber();
			} else if (index == 1)
			{
				return temp.getStartEntryNode();
			} else
			{
				return temp.fLevels.get(index - fLevelOffset);
			}

		} else if (parent instanceof FormatLevel)
		{
			// System.out.println("getChild: level format");
			FormatLevel temp = (FormatLevel) parent;

			if (index == 0)
			{
				return temp.getfEntryNumber();
			} else
			{
				return null;
			}
		} else
		{
			// System.out.println("getChild: something else");
			return null;
		}
	}

	@Override
	public int getChildCount(Object parent)
	{
		// System.out.println("parent=" + parent.toString());
		if (parent instanceof Format)
		{
			// System.out.println("getChildCount: content format");
			Format temp = (Format) parent;

			return temp.fParts.size();

		} else if (parent instanceof FormatPart)
		{
			// System.out.println("getChildCount: content part format");
			FormatPart temp = (FormatPart) parent;

			return temp.fLevels.size() + fLevelOffset;

		} else if (parent instanceof FormatLevel)
		{
			return 1;
		} else
		{
			// System.out.println("getChild: something else");
			return 0;
		}
	}

	@Override
	public boolean isLeaf(Object node)
	{
		// System.out.println("node=" + node.toString());
		if (node instanceof Format)
		{
			// System.out.println("isLeaf: content format");
			return false;

		} else if (node instanceof FormatPart)
		{
			// System.out.println("isLeaf: content part format");
			return false;

		} else if (node instanceof FormatLevel)
		{
			// System.out.println("isLeaf: level format");
			return false;
		} else
		{
			return true;
		}
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
		System.out.println(path);
		System.out.println(newValue);

		Object node = path.getLastPathComponent();
		Object parent = path.getParentPath().getLastPathComponent();

		if (parent instanceof FormatPart)
		{
			FormatPart temp = (FormatPart) parent;
			int index = getIndexOfChild(parent, node);

			if (index == 0)
			{
				System.out.println("valueForPathChanged: fPageNumber");
				if (temp.getNodefPageNumber().setfPageNumber((String) newValue))
				{
					nodeChanged(path);
				}

			} else if (index == 1)
			{
				System.out.println("valueForPathChanged: start entry");
				FormatPart fPart = getfPart(path);
				if(fPart.getPart() == 0) {
					System.out.println("part 0 always starts at line 0");
					return;
				}
				
				if (temp.getStartEntryNode().setValue((String) newValue))
				{
					// TODO update table
					nodeChanged(path);
				}
			}

		} else if (parent instanceof FormatLevel)
		{
			FormatLevel temp = (FormatLevel) parent;
			int index = getIndexOfChild(parent, node);

			if (index == 0)
			{
				if (temp.setfEntryNumber((String) newValue))
				{
					nodeChanged(path);
				} else
				{
					System.out.println("valueForPathChanged: invalid number pattern");
				}
			}
		}
	}

	@Override
	public int getIndexOfChild(Object parent, Object child)
	{
		// System.out.println("parent=" + parent.toString() + "child=" +
		// child.toString());
		if (parent instanceof Format)
		{
			// System.out.println("getIndexOfChild: content format");
			Format temp = (Format) parent;

			return temp.fParts.indexOf(child);

		} else if (parent instanceof FormatPart)
		{
			// System.out.println("getIndexOfChild: content part format");
			FormatPart temp = (FormatPart) parent;

			if (child == temp.getNodefPageNumber())
			{
				return 0;
			} else if (child == temp.getStartEntryNode())
			{
				return 1;
			} else
			{
				FormatLevel tempfLevel = (FormatLevel) child;
				return tempfLevel.getLevel() + fLevelOffset;
			}

		} else if (parent instanceof FormatLevel)
		{
			// System.out.println("getIndexOfChild: level format");
			FormatLevel temp = (FormatLevel) parent;

			if (child == temp.getfEntryNumber())
			{
				return 0;
			} else
			{
				return -1;
			}
		} else
		{
			// System.out.println("getIndexOfChild: something else");
			return -1;
		}
	}

	@Override
	public void addTreeModelListener(TreeModelListener l)
	{
		treeModelListeners.addElement(l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l)
	{
		treeModelListeners.removeElement(l);
	}

	protected void nodeChanged(TreePath path)
	{
		System.out.println("changed Node");
		Object parent = path.getParentPath().getLastPathComponent();
		Object child = path.getLastPathComponent();

		Object source = this;
		Object[] objPath = path.getParentPath().getPath();

		int[] childIndices = new int[1];
		childIndices[0] = getIndexOfChild(parent, child);

		Object[] children = new Object[1];
		children[0] = child;

		TreeModelEvent e = new TreeModelEvent(source, objPath, childIndices, children);

		for (TreeModelListener tml : treeModelListeners)
		{
			tml.treeNodesChanged(e);
		}
	}

	protected void nodeRemoved(TreePath path)
	{
		System.out.println("removed Node");
		Object parent = path.getParentPath().getLastPathComponent();
		Object child = path.getLastPathComponent();

		Object source = this;
		Object[] objPath = path.getParentPath().getPath();

		int[] childIndices = new int[1];
		childIndices[0] = getIndexOfChild(parent, child);

		Object[] children = new Object[1];
		children[0] = child;

		TreeModelEvent e = new TreeModelEvent(source, objPath, childIndices, children);

		for (TreeModelListener tml : treeModelListeners)
		{
			tml.treeNodesRemoved(e);
		}
	}

	protected void nodeInserted(TreePath path)
	{
		System.out.println("iserted Node");
		Object parent = path.getParentPath().getLastPathComponent();
		Object child = path.getLastPathComponent();

		Object source = this;
		Object[] objPath = path.getParentPath().getPath();

		int[] childIndices = new int[1];
		childIndices[0] = getIndexOfChild(parent, child);

		Object[] children = new Object[1];
		children[0] = child;

		TreeModelEvent e = new TreeModelEvent(source, objPath, childIndices, children);

		for (TreeModelListener tml : treeModelListeners)
		{
			tml.treeNodesInserted(e);
		}
	}

	public void removeLevel(TreePath path)
	{
		FormatLevel fLevel = getfLevel(path);

		if (fLevel == null)
		{
			return;
		}

		FormatPart fPart = getfPart(path);

		if (fPart.fLevels.size() <= 1)
		{
			System.out.println("can not remove last level");
			return;
		}

		nodeRemoved(getPathToLevel(path));

		fPart.removefLevel(fLevel.getLevel());
	}

	public TreePath addLevel(TreePath path)
	{
		FormatPart fPart = getfPart(path);

		if (fPart == null)
		{
			return null;
		}

		if (fPart.fLevels.size() >= 10)
		{
			System.out.println("max number of levels is reached");
			return null;
		}

		FormatLevel fLevel = getfLevel(path);

		int curLevel = 0;

		if (fLevel != null)
		{
			curLevel = fLevel.getLevel() + 1;
		}

		FormatLevel newfLevel = new FormatLevel(curLevel, "", fPart.getfPageNumber());

		fPart.addfLevel(curLevel, newfLevel);

		TreePath pathToNewfLevel = addToPath(getPathTofPart(path), newfLevel);

		nodeInserted(pathToNewfLevel); // notify also for the children?

		return pathToNewfLevel;
	}

	public void removePart(TreePath path)
	{
		if (format.fParts.size() <= 1)
		{
			System.out.println("can not remove last part");
			return;
		}

		FormatPart fPart = getfPart(path);

		if (fPart == null || fPart.getPart() == 0)
		{
			return;
		}

		TreePath newPath = getPathTofPart(path);
		nodeRemoved(newPath);

		format.removefPart(fPart.getPart());
	}

	public TreePath addPart(TreePath path)
	{
		int curPart = 0;

		FormatPart fPart = getfPart(path);

		if (fPart != null)
		{
			curPart = fPart.getPart() + 1;
		}

		FormatPart newfPart = new FormatPart();
		// newfPart.setStartEntry(val);

		format.addfPart(curPart, newfPart);

		TreePath pathToNewfPart = new TreePath(new Object[] { format, newfPart });

		nodeInserted(pathToNewfPart); // notify also for the children?

		return addLevel(pathToNewfPart);
	}

	public TreePath addPart(int startEntry)
	{
		FormatPart fPart = format.getfPart(startEntry);

		if (fPart == null)
		{
			return null;
		}

		FormatPart newfPart = new FormatPart();
		newfPart.setPart(fPart.getPart() + 1);
		newfPart.setStartEntry(startEntry);

		if (!format.addfPart(fPart.getPart() + 1, newfPart))
		{
			return null;
		}

		TreePath pathToNewfPart = new TreePath(new Object[] { format, newfPart });

		nodeInserted(pathToNewfPart); // notify also for the children?

		return addLevel(pathToNewfPart);
	}

	private FormatLevel getfLevel(TreePath path)
	{
		int depth = path.getPathCount();

		if (depth < 3)
		{
			return null;
		}

		Object obj = path.getPathComponent(2);

		return (obj instanceof FormatLevel) ? (FormatLevel) obj : null;
	}

	private TreePath getPathToLevel(TreePath path)
	{
		int depth = path.getPathCount();

		if (depth < 3)
		{
			return null;
		}

		Object obj = path.getPathComponent(2);

		if (!(obj instanceof FormatLevel))
		{
			return null;
		}

		Object[] array = path.getPath();

		Object[] res = new Object[3];
		res[0] = array[0];
		res[1] = array[1];
		res[2] = array[2];

		return new TreePath(res);
	}

	private FormatPart getfPart(TreePath path)
	{
		int depth = path.getPathCount();

		if (depth < 2)
		{
			return null;
		}

		Object obj = path.getPathComponent(1);

		return (obj instanceof FormatPart) ? (FormatPart) obj : null;
	}

	private TreePath getPathTofPart(TreePath path)
	{
		int depth = path.getPathCount();

		if (depth < 2)
		{
			return null;
		}

		Object obj = path.getPathComponent(1);

		if (!(obj instanceof FormatPart))
		{
			return null;
		}

		Object[] array = path.getPath();

		Object[] res = new Object[2];
		res[0] = array[0];
		res[1] = array[1];

		return new TreePath(res);
	}

	private TreePath addToPath(TreePath path, Object lastComponent)
	{
		int count = path.getPathCount();
		Object[] res = new Object[count + 1];
		for (int i = 0; i < count; ++i)
		{
			res[i] = path.getPathComponent(i);
		}

		res[count] = lastComponent;
		return new TreePath(res);
	}
}
