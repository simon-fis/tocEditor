package data;

import java.util.ArrayList;
import java.util.List;

import format.FormatLevel;
import format.FormatPageNumber;
import status.EntryStatusLevel;
import status.EntryStatusPage;
import status.EntryStatusTitle;

public class Entry
{
	public static List<Character> alphabet = new ArrayList<Character>();
	{
		for (char i = 'a'; i <= 'z'; i++)
		{
			alphabet.add(i);
		}

		for (char i = 'A'; i <= 'Z'; i++)
		{
			alphabet.add(i);
		}

		for (char i = '0'; i <= '9'; i++)
		{
			alphabet.add(i);
		}

		alphabet.add('ä');
		alphabet.add('ö');
		alphabet.add('ü');
		alphabet.add('ß');

		alphabet.add('.');
		alphabet.add(',');
		alphabet.add(';');
		alphabet.add('-');
		alphabet.add(' ');
	}

	String str;

	public int indent;
	public EntryNumber en;
	private int level;
	public String title;
	public int page;
	public int offset;

	private boolean offsetSet;

	public FormatLevel fLevel;
	public FormatPageNumber fPageNumber;

	public EntryStatusLevel levelStatus;
	public EntryStatusTitle titleStatus;
	public EntryStatusPage pageStatus;
	public int titleUnusualCharsAt;

	public Entry(String str, int indent, EntryNumber en, String title, FormatPageNumber fPageNumber)
	{
		this.str = str;
		this.indent = indent;
		this.fLevel = null;
		this.level = -1;
		this.en = en;
		this.title = title;
		this.page = -1;
		this.fPageNumber = fPageNumber;

		levelStatus = EntryStatusLevel.notSet;
		titleStatus = EntryStatusTitle.unusualChars;
		pageStatus = EntryStatusPage.notFound;
		titleUnusualCharsAt = 0;

		offsetSet = false;
		offset = 0;

	}

	public Entry()
	{
		this("", 0, null, "", null);
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("\"");
		sb.append(str);
		sb.append("\"");
		sb.append("\nindent=");
		sb.append(indent);
		sb.append("(");
		sb.append(getLevel());
		sb.append(")");
		sb.append("\ntitle=");
		sb.append("\"");
		sb.append(title != null ? title : "-");
		sb.append("\"");
		sb.append("\npage=");
		sb.append(page);
		sb.append(" + ");
		sb.append(offset);
		sb.append(" = ");
		sb.append(page + offset);
		sb.append("(");
		sb.append(fPageNumber.toString());
		sb.append(")");

		return sb.toString();
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
		this.offsetSet = true;
	}

	public boolean getOffsetSet()
	{
		return offsetSet;
	}

	public void setLevel(Integer val)
	{
		if (val == null || val < -1)
		{
			level = -1;
		}
		{
			level = val;
		}
		fLevel = null;
	}

	public int getLevel()
	{
		return (fLevel != null) ? fLevel.getLevel() : level;
	}

	public String toPDFTKString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("BookmarkBegin");
		sb.append("\nBookmarkTitle: ");
		sb.append(getBookmarkTitle());
		sb.append("\nBookmarkLevel: ");
		int level = getLevel() + 1;
		if (level < 1)
		{
			System.out.println("Warning: level < 1");
		}
		// sb.append(level);
		sb.append(getLevel());
		sb.append("\nBookmarkPageNumber: ");
		sb.append(page + offset);

		return sb.toString();
	}

	public String toLine()
	{
		String numberStr = (en != null) ? en.toFormatedString() + " " : "";
		String pageStr = "  ";
		if (page > 0)
		{
			if (fPageNumber != null)
			{
				pageStr = pageStr + fPageNumber.intToString(page);
			} else
			{
				pageStr = pageStr + Integer.toString(page);
			}
		}
		String indentStr = new String(new char[indent]).replace('\0', ' ');

		return indentStr + numberStr + title + pageStr;
	}

	public String getBookmarkTitle()
	{
		String titleStr = title.trim();
		if (en != null)
		{
			String numberStr = en.toFormatedString();
			if (numberStr.length() == 0)
			{
				return titleStr;
			} else
			{
				return en.toFormatedString() + " " + titleStr;
			}
		} else
		{
			return titleStr;
		}
	}

	public String toHTML()
	{
		return "<li><a href=\"#" + (page + offset) + "\">" + getBookmarkTitle() + "</a></li>";
	}
}
