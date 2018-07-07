package format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import data.Entry;
import data.EntryNumber;
import data.PageNumber;
import util.RomanNumeral;

public class Format
{
	public static Pattern arabicFormat = Pattern.compile("\\d+\\s");
	public static Pattern romanFormat = Pattern.compile("[IVXLCDMivxlcdm]+\\s");
	public static Pattern fillFormat = Pattern.compile("[\\s\\.,]{2,}");

	public List<FormatPart> fParts;

	private List<String> lines;
	private List<Entry> entries;
	private Pattern titlePattern;

	public Format()
	{
		this.fParts = new ArrayList<FormatPart>();
		this.lines = new ArrayList<String>();
		this.entries = new ArrayList<Entry>();
	}

	public FormatPart getfPart(int entryIndex)
	{
		if (fParts.isEmpty())
		{
			System.out.println("No ContentPartFormat");
			return null;
		}
		FormatPart fPart = fParts.get(0);
		for (int i = 1; i < fParts.size(); ++i)
		{
			FormatPart nextfPart = fParts.get(i);
			if (nextfPart.getStartEntry() > entryIndex)
			{
				return fPart;
			} else
			{
				fPart = nextfPart;
			}
		}
		return fPart;
	}

	@JsonIgnore
	public FormatPart getLargestfPart()
	{
		int maxIndex = 0;
		int maxSize = 0;
		for (int i = 0; i < fParts.size() - 1; ++i)
		{

			int size = fParts.get(i + 1).getStartEntry() - fParts.get(i).getStartEntry();
			if (size > maxSize)
			{
				maxIndex = i;
				maxSize = size;
			}
		}
		return fParts.get(maxIndex);
	}

	public List<data.Entry> parse(File inFile)
	{
		if (inFile == null)
		{
			return null;
		}

		lines = readFile(inFile);
		entries = parseLines(lines);

		return entries;
	}

	public List<data.Entry> parseLines(List<String> theLines)
	{
		List<data.Entry> theEntries = new ArrayList<Entry>(theLines.size());

		for (int i = 0; i < theLines.size(); ++i)
		{
			String str = theLines.get(i);
			Entry e = new Entry(str, -1, null, str, null);
			FormatPart fPart = getfPart(i);

			readEntry(str, e, fPart);
			theEntries.add(e);

		}
		return theEntries;
	}

	// TODO page numbers get sometimes lost. Especially when they are entered
	// manually
	public void parseEntries()
	{
		for (int i = 0; i < entries.size(); ++i)
		{
			Entry e = entries.get(i);
			String str = e.toLine();

			parseEntry(i, str);
		}
	}

	public void parseEntry(int i, String str)
	{
		Entry e = entries.get(i);
		FormatPart fPart = getfPart(i);

		// reset entry
		e.indent = -1;
		e.fLevel = null;
		e.page = -1;
		e.en = null;
		e.title = "";
		e.fPageNumber = FormatPageNumber.arabic;

		readEntry(str, e, fPart);
	}

	private void readEntry(String str, Entry e, FormatPart fPart)
	{
		e.indent = leadingWightspaces(str);

		str = str.trim();

		if (str.isEmpty())
		{
			return;
		}

		// level number
		extractEntryNumber(fPart, e, str);
		if (e.en != null)
		{
			str = str.substring(e.en.getOrig().length()).trim();
		}

		// page number
		PageNumber pn = extractPageNumber(str);
		if (pn != null)
		{
			e.page = pn.page;
			str = str.substring(0, str.length() - pn.orig.length());
		}

		// TODO improve parsing: entry spread over two lines

		String revstr = new StringBuilder(str).reverse().toString();
		Matcher matcher = fillFormat.matcher(revstr);
		if (matcher.lookingAt())
		{
			int end = matcher.end();
			str = str.substring(0, str.length() - end);
		}

		// remove double whitespace
		// str = str.replaceAll("[\\s\\.]{2,}", " ");
		str = str.replaceAll("\\s{1,}", " ");
		// replace strange whitespace by normal ones
		str = str.replaceAll("[\\s]", " ");

		e.title = str;
	}

	private PageNumber extractPageNumber(String str)
	{
		String revStr = new StringBuilder(str).reverse().toString();
		PageNumber pn = null;

		pn = extractRomanPageNumber(str, revStr);

		if (pn == null)
		{
			pn = extractArabicPageNumber(str, revStr);
		}
		
		return pn;

	}

	private PageNumber extractArabicPageNumber(String str, String revStr)
	{
		int page = -1;
		String pageStr = "";
		Matcher matcher;
		matcher = arabicFormat.matcher(revStr);
		if (matcher.lookingAt())
		{
			int size = matcher.end();
			pageStr = str.substring(str.length() - size).trim();
			try
			{
				page = Integer.parseInt(pageStr);
			} catch (NumberFormatException e)
			{
				System.out.println(e);
				page = -1;
			}
		}
		return (page != -1) ? new PageNumber(pageStr, page, FormatPageNumber.arabic) : null;
	}

	private PageNumber extractRomanPageNumber(String str, String revStr)
	{
		Matcher matcher = romanFormat.matcher(revStr);
		if (matcher.lookingAt())
		{
			int size = matcher.end();
			String pageStr = str.substring(str.length() - size).trim();
			int page = RomanNumeral.RomanStringToInt(pageStr);
			// TODO distinguish lower or upper case Roman numbering
			return new PageNumber(pageStr, page, FormatPageNumber.Roman);

		}
		return null;
	}

	public void extractEntryNumber(FormatPart fPart, Entry e, String str)
	{
		for (FormatLevel fLevel : fPart.fLevels)
		{
			EntryNumber tempEn = fLevel.getfEntryNumber().match(str, fLevel.getLevel());

			if (tempEn != null)// level fits
			{
				if (e.en == null || tempEn.getLTNSize() > e.en.getLTNSize())
				// || tempEn.getOrig().length() > en.getOrig().length())
				{
					e.en = tempEn;
					e.fLevel = fLevel;
					e.fPageNumber = fLevel.getfPageNumber();
				}
			}
		}
	}

	private List<String> readFile(File file)
	{
		List<String> theLines = new ArrayList<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				if (line.isEmpty())
				{
					continue;
				}

				theLines.add(line);
			}

			reader.close();

		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return theLines;
	}

	private int leadingWightspaces(String s)
	{
		for (int i = 0; i < s.length(); ++i)
		{
			if (s.charAt(i) != ' ')
			{
				return i;
			}
		}
		return s.length();
	}

	public String getAllowedTitles()
	{
		return this.titlePattern.pattern();
	}

	public void setAllowedTitles(String titlePattern)
	{
		this.titlePattern = Pattern.compile(titlePattern);
	}

	public int isAllowedTitle(String title)
	{
		// return the amout of matched characters (from the start)
		Matcher matcher = titlePattern.matcher(title);
		boolean res = matcher.matches();

		if (res)
		{
			return title.length();
		}

		boolean res2 = matcher.lookingAt();

		if (!res2)
		{
			return 0;
		}

		return matcher.end();
	}

	public boolean addfPart(int index, FormatPart fPart)
	{
		if (index < 0 || index > fParts.size())
		{
			return false;
		}

		if (index == 0)
		{
			fPart.setStartEntry(0);
		} else
		{
			int start = fPart.getStartEntry();
			int startPrev = fParts.get(index - 1).getStartEntry();

			if (startPrev >= start)
			{
				System.out.println("Adapt start Entry: prev + 1");
				start = fParts.get(index - 1).getStartEntry() + 1;
				fPart.setStartEntry(start);
			}

			if (index < fParts.size() - 1)
			{
				int startNext = fParts.get(index + 1).getStartEntry();

				if (startNext <= startPrev + 1)
				{
					System.out.println("not posible to insert part here");
					return false;
				}

				if (startNext <= start)
				{
					System.out.println("Adapt start Entry: next - 1");
					start = startNext - 1;
					fPart.setStartEntry(start);
				}
			}
		}

		fParts.add(index, fPart);

		adaptfParts();

		return true;
	}

	private void adaptfParts()
	{
		for (int i = 0; i < fParts.size(); ++i)
		{

			if (fParts.get(i).getPart() != i)
			{
				System.out.println("adapted part " + i);
				fParts.get(i).setPart(i);
			}
		}
	}

	public void deletedEntry(int index)
	{
		FormatPart fPart = getfPart(index);

		for (int i = fPart.getPart() + 1; i < fParts.size(); ++i)
		{
			FormatPart temp = fParts.get(i);
			int start = temp.getStartEntry();
			temp.setStartEntry(start - 1);
		}
	}

	public void addedEntry(int index)
	{
		FormatPart fPart = getfPart(index);

		for (int i = fPart.getPart() + 1; i < fParts.size(); ++i)
		{
			FormatPart temp = fParts.get(i);
			int start = temp.getStartEntry();
			temp.setStartEntry(start + 1);
		}
	}

	public boolean isfPartBeginning(int row)
	{
		for (FormatPart fPart : fParts)
		{
			if (fPart.getStartEntry() == row)
			{
				return true;
			}
		}
		return false;
	}

	public void removefPart(int fPart)
	{
		fParts.remove(fPart);

		adaptfParts();
	}

	public String toString()
	{
		return "format";
	}

	public String toFormatString()
	{
		String str = "";
		for (FormatPart fPart : fParts)
		{
			str += fPart.toFormatString() + "\n";
		}
		return str;
	}
}
