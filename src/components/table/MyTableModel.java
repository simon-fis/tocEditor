package components.table;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitHeightDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;

import data.Entry;
import format.Format;
import format.FormatPageNumber;
import format.FormatPart;
import status.EntryStatusLevel;
import status.EntryStatusPage;
import status.EntryStatusTitle;

public class MyTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;
	// private List<String> lines = new ArrayList<String>();
	private List<Entry> entries = new ArrayList<Entry>();
	private Format format;
	private final String[] columnNames = { "Level", "Number", "Title", "Page", "Offset" };

	public MyTableModel(Format format)
	{
		super();
		this.format = format;
	}

	@Override
	public int getRowCount()
	{
		return entries.size();
	}

	@Override
	public int getColumnCount()
	{
		return columnNames.length;
	}

	public String getColumnName(int col)
	{
		return columnNames[col];
	}

	public Class<?> getColumnClass(int columnIndex)
	{
		switch (columnIndex)
		{
		case 0:
			return Integer.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		case 3:
			return Integer.class;
		case 4:
			return Integer.class;
		}
		return Object.class;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		Entry entry = entries.get(rowIndex);

		switch (columnIndex)
		{
		case 0:
			return (entry.getLevel() >= 0) ? entry.getLevel() : "";
		case 1:
			return (entry.en != null) ? entry.en.toFormatedString() : "";
		case 2:
			return entry.title;
		case 3:
			return entry.page <= 0 ? "" : entry.page;
		case 4:
			return entry.offset;
		}
		return "-";
	}

	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return true;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		Entry entry = entries.get(rowIndex);

		switch (columnIndex)
		{
		case 0: // level
			entry.setLevel((Integer) aValue);
			checkLevel(rowIndex);
			fireTableCellUpdated(rowIndex, 0);

			if (rowIndex > 0)
			{
				checkLevel(rowIndex - 1);
				fireTableCellUpdated(rowIndex - 1, 0);
			}
			if (rowIndex < getRowCount() - 1)
			{
				checkLevel(rowIndex + 1);
				fireTableCellUpdated(rowIndex + 1, 0);
			}

			break;
		case 1: // numbering
			String temp = (String) aValue;

			if (entry.en == null || entry.en.toFormatedString().compareTo(temp) != 0)
			{
				FormatPart fPart = format.getfPart(rowIndex);

				// EntryNumbering tempEn = entry.en; // store old value
				entry.en = null; // delete old value
				entry.setLevel(-1); // delete old value
				format.extractEntryNumber(fPart, entry, temp + " ");
				// if (entry.en == null)
				// {
				// // restore old value because no other was found
				// entry.en = tempEn;
				// }

				checkLevel(rowIndex);
				fireTableCellUpdated(rowIndex, 0);
				fireTableCellUpdated(rowIndex, 1);

				if (rowIndex > 0)
				{
					checkLevel(rowIndex - 1);
					fireTableCellUpdated(rowIndex - 1, 0);
					fireTableCellUpdated(rowIndex - 1, 1);
				}
				if (rowIndex < getRowCount() - 1)
				{
					checkLevel(rowIndex + 1);
					fireTableCellUpdated(rowIndex + 1, 0);
					fireTableCellUpdated(rowIndex + 1, 1);
				}
			}
			break;
		case 2: // title
			entry.title = (String) aValue;
			checkTitle(rowIndex);
			fireTableCellUpdated(rowIndex, 2);
			break;
		case 3: // page
			entry.page = (Integer) aValue;

			// for(int i=0; i<getRowCount(); ++i){
			// checkPage(i);
			// fireTableCellUpdated(i, 3);
			// }

			checkPage(rowIndex);
			fireTableCellUpdated(rowIndex, 3);

			if (rowIndex > 0)
			{
				checkPage(rowIndex - 1);
				fireTableCellUpdated(rowIndex - 1, 3);
			}
			if (rowIndex < getRowCount() - 1)
			{
				checkPage(rowIndex + 1);
				fireTableCellUpdated(rowIndex + 1, 3);
			}
			break;
		case 4: // offset
			int tempInt = (Integer) aValue;
			entry.setOffset(tempInt);
			int i = rowIndex + 1;
			while (i < getRowCount())
			{
				Entry tempEntry = entries.get(i);

				if (tempEntry.getOffsetSet())
				{
					break;
				} else
				{
					tempEntry.offset = tempInt;
				}
				++i;
			}
			fireTableRowsUpdated(rowIndex, i - 1);
			fireTableCellUpdated(rowIndex, 4);
		}
	}

	public void fillTable(List<Entry> entries)
	{
		this.entries = entries;

		updateStatus();
	}

	public void updateStatus()
	{
		for (int i = 0; i < this.entries.size(); ++i)
		{
			updateStatus(i);
		}
	}

	public void updateStatus(int row)
	{
		checkLevel(row);
		checkTitle(row);
		checkPage(row);
	}

	public EntryStatusLevel checkLevel(int row)
	{
		if (!validRowIndex(row))
		{
			return null;
		}

		Entry entry = entries.get(row);
		int level = entry.getLevel();

		if (level < 0)
		{
			entry.levelStatus = EntryStatusLevel.notSet;
			return entry.levelStatus;
		}

		if (row - 1 >= 0)
		{
			Entry prevEntry = entries.get(row - 1);
			int prevLevel = prevEntry.getLevel();
			if (prevLevel >= 0 && level > prevLevel + 1)
			{
				entry.levelStatus = EntryStatusLevel.jumping;
				return entry.levelStatus;
			}

			if (prevEntry.fLevel != null && prevEntry.en != null && entry.fLevel != null && entry.en != null)
			{
				if (!entry.en.isDirectSuccessorOf(prevEntry.en))
				{
					entry.levelStatus = EntryStatusLevel.missing;
					return entry.levelStatus;
				}
			}
		}

		if (row + 1 < entries.size())
		{
			Entry nextEntry = entries.get(row + 1);
			int nextLevel = nextEntry.getLevel();
			if (nextLevel >= 0 && nextLevel > level + 1)
			{
				entry.levelStatus = EntryStatusLevel.jumping;
				return entry.levelStatus;
			}

			if (nextEntry.fLevel != null && nextEntry.en != null && entry.fLevel != null && entry.en != null)
			{
				if (!nextEntry.en.isDirectSuccessorOf(entry.en))
				{
					entry.levelStatus = EntryStatusLevel.missing;
					return entry.levelStatus;
				}
			}
		}

		entry.levelStatus = EntryStatusLevel.good;
		return entry.levelStatus;
	}

	public EntryStatusPage checkPage(int row)
	{
		if (!validRowIndex(row))
		{
			return null;
		}
		Entry e = entries.get(row);

		if (e.page <= 0)
		{

			e.pageStatus = EntryStatusPage.notFound;
			return e.pageStatus;
		}

		if (row - 1 >= 0)
		{
			if (entries.get(row - 1).page > e.page)
			{
				e.pageStatus = EntryStatusPage.notMonotonic;
				return e.pageStatus;
			}
		}

		if (row + 1 < entries.size())
		{
			if (entries.get(row + 1).page < e.page)
			{
				e.pageStatus = EntryStatusPage.notMonotonic;
				return e.pageStatus;
			}
		}

		e.pageStatus = EntryStatusPage.good;
		return e.pageStatus;
	}

	public EntryStatusTitle checkTitle(int row)
	{
		if (!validRowIndex(row))
		{
			return null;
		}
		Entry e = entries.get(row);

		//
		int temp = format.isAllowedTitle(e.title);
		
		if(temp == e.title.length())
		{
			e.titleStatus = EntryStatusTitle.good;
		} else
		{
			e.titleStatus = EntryStatusTitle.unusualChars;
			e.titleUnusualCharsAt = temp;
		}

		// for (int i = 0; i < e.title.length(); ++i)
		// {
		// if (!Entry.alphabet.contains(e.title.charAt(i)))
		// {
		// e.titleUnusualCharsAt = i;
		// e.titleStatus = EntryStatusTitle.unusualChars;
		// return e.titleStatus;
		// }
		// }
		// e.titleStatus = EntryStatusTitle.good;

		return e.titleStatus;
	}

	public void checkRow(int row)
	{
		checkPage(row);
		checkTitle(row);
		checkLevel(row);
	}

	public EntryStatusLevel getLevelStatus(int row)
	{
		return entries.get(row).levelStatus;
	}

	public EntryStatusTitle getTitleStatus(int row)
	{
		return entries.get(row).titleStatus;
	}

	public EntryStatusPage getPageStatus(int row)
	{
		return entries.get(row).pageStatus;
	}

	public boolean entryGood(int row)
	{
		return (getTitleStatus(row) == EntryStatusTitle.good)
				&& (getPageStatus(row) == EntryStatusPage.good && (getLevelStatus(row) == EntryStatusLevel.good));
	}

	public void setFirstValidPageNumberToPage(int page)
	{
		if (page < 1)
		{
			return;
		}

		for (int i = 0; i < entries.size(); ++i)
		{
			Entry e = entries.get(i);
			if (e.pageStatus != EntryStatusPage.notFound)
			{
				setOffsetToPage(i, page);
				return;
			}
		}
	}

	public void combineEntries(int row)
	{
		if (!validRowIndex(row))
			return;

		Entry first = entries.get(row);
		Entry second = entries.get(row + 1);

		if (null == first || null == second)
		{
			return;
		}

		String str = first.toLine() + " " + second.toLine();

		format.parseEntry(row, str);

		checkTitle(row);
		checkPage(row);

		checkPage(row - 1);

		entries.remove(row + 1);
		format.deletedEntry(row + 1);

		fireTableRowsUpdated(row, row);
		fireTableRowsDeleted(row + 1, row + 1);
	}

	public void deleteEntry(int row)
	{
		if (!validRowIndex(row))
			return;

		entries.remove(row);
		format.deletedEntry(row);

		updateStatus(row);
		updateStatus(row - 1);
		fireTableRowsDeleted(row, row);
	}

	public void newEntry(int row)
	{
		// empty entry list
		if (row == -1 && getRowCount() == 0)
		{
			row = 0;
		}

		if (validRowIndex(row) || row == getRowCount())
		{
			entries.add(row, new Entry());
			format.addedEntry(row);
			fireTableRowsInserted(row, row);
		}
	}

	public int getNextRowNotGood(int rowIndex)
	{
		if (!validRowIndex(rowIndex))
		{
			return -1;
		}

		for (int i = rowIndex; i < getRowCount(); ++i)
		{
			if (!entryGood(i))
			{
				return i;
			}
		}

		return -1;
	}

	public int getPrevRowNotGood(int rowIndex)
	{
		if (!validRowIndex(rowIndex))
		{
			return -1;
		}

		for (int i = rowIndex; i >= 0; --i)
		{
			if (!entryGood(i))
			{
				return i;
			}
		}

		return -1;
	}

	public int getNextLevelRow(int row)
	{
		if (!validRowIndex(row))
		{
			return -1;
		}

		int level = entries.get(row).getLevel();
		for (int i = row + 1; i < entries.size(); ++i)
		{
			if (entries.get(i).getLevel() == level)
			{
				return i;
			}
		}

		return row;
	}

	public int getPrevLevelRow(int row)
	{
		if (!validRowIndex(row))
		{
			return -1;
		}

		int level = entries.get(row).getLevel();
		for (int i = row - 1; i >= 0; --i)
		{
			if (entries.get(i).getLevel() == level)
			{
				return i;
			}
		}

		return row;
	}

	public String getPageLabels()
	{
		String str = "/PageLabels << /Nums [\n";

		// start page labels
		int prevOffset = 0;
		FormatPageNumber prevfPageNumber = format.fParts.get(0).getfPageNumber();
		str += "0 << " + prevfPageNumber.toPDF() + " >> \n";

		//
		for (int i = 0; i < entries.size(); ++i)
		{
			// TODO what are valid page numbers
			if (getPageStatus(i) == EntryStatusPage.notFound)
			{
				continue;
			}
			Entry e = entries.get(i);
			FormatPageNumber pt = format.getfPart(i).getfPageNumber();

			if ((pt != prevfPageNumber) || (e.offset != prevOffset))

				if (e.page >= 0 && e.getOffsetSet() && e.offset != prevOffset)
				{
					prevfPageNumber = pt;
					prevOffset = e.offset;

					str += (e.page + prevOffset - 1) + " << " + pt.toPDF() + " /St " + e.page + " >>\n";
				}
		}

		str += "] >>\n";

		return str;
	}

	public PDPageLabels generatePageLabels(PDDocument document)
	{
		PDPageLabels labels = new PDPageLabels(document);

		// start page labels
		int prevOffset = 0;
		FormatPageNumber prevfPageNumber = format.fParts.get(0).getfPageNumber();

		PDPageLabelRange range = new PDPageLabelRange();
		range.setStart(1); // page number of the labels
		range.setStyle(prevfPageNumber.toPDFBox());
		labels.setLabelItem(0, range);

		for (int i = 0; i < entries.size(); ++i)
		{
			// TODO what are valid page numbers
			if (getPageStatus(i) == EntryStatusPage.notFound)
			{
				continue;
			}

			Entry e = entries.get(i);
			FormatPageNumber pt = format.getfPart(i).getfPageNumber();

			if ((pt != prevfPageNumber) || (e.offset != prevOffset))
			{
				prevfPageNumber = pt;
				prevOffset = e.offset;

				range = new PDPageLabelRange();
				// range.setPrefix(prefix);
				range.setStart(e.page); // page number of the labels
				range.setStyle(pt.toPDFBox());

				// page number in pdf
				labels.setLabelItem(e.page + e.offset - 1, range);
			}
		}

		return labels;
	}

	public PDDocumentOutline generateOutline(PDDocumentOutline outline2)
	{
		PDDocumentOutline outline;
		if (outline2 == null)
		{
			outline = new PDDocumentOutline();
		} else
		{
			outline = outline2;
		}
		updateStatus();
		appendChildren(outline, 0, 0);
		return outline;
	}

	// return number of appended children
	private int appendChildren(PDOutlineNode parent, int level, int index)
	{
		int i = index;
		PDOutlineItem prevItem = null;
		while (i < entries.size())
		{
			// System.out.println("i=" + i);
			Entry e = entries.get(i);

			// skip invalid entries
			if (getLevelStatus(i) == EntryStatusLevel.notSet)
			{
				System.out.println("appendChildren: skip entry " + i + ": invalid level (" + e.toLine() + ")");
				++i;
				continue;
			}
			// TODO what are valid page numbers
			if (getPageStatus(i) == EntryStatusPage.notFound)
			{
				System.out.println("appendChildren: skip entry " + i + ": invalid page number (" + e.toLine() + ")");
				++i;
				continue;
			}

			// check level
			if (e.getLevel() == level)
			{
				// construct item
				// TODO get from formatLevel
				PDPageDestination dest = new PDPageFitHeightDestination();
				// pdf pages start with 0
				dest.setPageNumber(e.page + e.offset - 1);

				PDOutlineItem item = new PDOutlineItem();
				item.setDestination(dest);
				item.setTitle(e.getBookmarkTitle());
				if (format.getfPart(i) == format.getLargestfPart())
				{
					if (e.getLevel() == 0)
					{
						// item.setBold(bold); //TODO get formating from
						// formatLevel
						// item.setItalic(italic);
						item.setTextColor(Color.BLUE);
					}
				}

				parent.addLast(item);
				prevItem = item;
				++i;

			} else if (e.getLevel() == level + 1)
			{
				int temp = appendChildren(prevItem, level + 1, i);
				i += temp;
			} else if (e.getLevel() < level)
			{
				return i - index;
			} else
			{
				System.out.println("appendChildren: Error level is jumping --> skip");
				System.out.println(e.toLine());
				++i;
			}
		}
		return entries.size() - index;
	}

	public void writeHtml(File outFile) throws IOException
	{

		FileWriter fw = new FileWriter(outFile);

		fw.write("<html>\n");
		fw.write("<body>\n");
		fw.write("<ul>\n");

		int curLevel = 0;
		String curIndent = "\t";
		for (int i = 0; i < entries.size(); ++i)
		{
			Entry e = entries.get(i);

			// skip invalid entries
			if (getLevelStatus(i) == EntryStatusLevel.notSet)
			{
				System.out.println("writeHtml: skip entry " + i + ": invalid level (" + e.toLine() + ")");
				continue;
			}
			// TODO what are valid page numbers
			if (getPageStatus(i) == EntryStatusPage.notFound)
			{
				System.out.println("writeHtml: skip entry " + i + ": invalid page number (" + e.toLine() + ")");
				continue;
			}

			if (e.getLevel() == curLevel + 1)
			{
				fw.write(curIndent);
				fw.write("<ul>\n");
				curIndent = curIndent + "\t";
			} else if (e.getLevel() < curLevel)
			{
				for (int j = curLevel; j > e.getLevel(); --j)
				{
					curIndent = curIndent.substring(1);
					fw.write(curIndent);
					fw.write("</ul>\n");
				}
			} else if (e.getLevel() > curLevel + 1)
			{
				System.out.println("writeHtml: Error level is jumping --> skip");
				System.out.println(e.toLine());
				continue;
			}

			fw.write(curIndent);
			fw.write(e.toHTML());
			fw.write("\n");

			curLevel = e.getLevel();
		}

		fw.write("</ul>\n");
		fw.write("</body>\n");
		fw.write("</html>\n");
		fw.close();
	}

	public String getPDFTKString(int i)
	{
		return entries.get(i).toPDFTKString();
	}

	private boolean validRowIndex(int row)
	{
		return row >= 0 && row < getRowCount();
	}

	public Entry getEntry(int row)
	{
		return entries.get(row);
	}

	public boolean getOffsetSet(int row)
	{
		return entries.get(row).getOffsetSet();
	}

	public void setOffsetToPage(int row, int page)
	{
		if (!validRowIndex(row))
			return;

		Entry entry = entries.get(row);
		if (entry == null)
			return;

		if (entry.page >= 1 && page >= 1) // valid page
		{
			setValueAt(page - entry.page, row, 4); // offset
		}
	}

	public void swap(int row1, int row2)
	{
		if (!validRowIndex(row1) || !validRowIndex(row2))
		{
			return;
		}

		Entry temp = entries.get(row1);
		entries.set(row1, entries.get(row2));
		entries.set(row2, temp);

		checkPage(row1);
		checkPage(row1 + 1);
		checkPage(row1 - 1);

		checkLevel(row1);
		checkLevel(row1 + 1);
		checkLevel(row1 - 1);

		checkPage(row2);
		checkPage(row2 + 1);
		checkPage(row2 - 1);

		checkLevel(row2);
		checkLevel(row2 + 1);
		checkLevel(row2 - 1);

		fireTableDataChanged();
	}

	public void deletePageNumbers()
	{
		for (int i = 0; i < getRowCount(); ++i)
		{
			Entry e = entries.get(i);
			e.page = -1;
			e.pageStatus = EntryStatusPage.notFound;
		}
	}
}
