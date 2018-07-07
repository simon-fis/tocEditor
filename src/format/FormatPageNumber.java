package format;

import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;

import util.RomanNumeral;

public enum FormatPageNumber
{
	arabic, roman, Roman;

	public String intToString(int val)
	{
		switch (this)
		{
		case Roman:
			return RomanNumeral.intToRomanString(val, true);
		case arabic:
			return Integer.toString(val);
		case roman:
			return RomanNumeral.intToRomanString(val, false);
		}
		System.out.println("FormatPageNumber: unknown type");
		return Integer.toString(val);
	}

	public String toPDF()
	{
		switch (this)
		{
		case Roman:
			return "/S /R";
		case arabic:
			return "/S /D";
		case roman:
			return "/S /r";
		}
		System.out.println("FormatPageNumber: unknown type");
		return "/S /D";

	}

	public String toPDFBox()
	{
		switch (this)
		{
		case Roman:
			return PDPageLabelRange.STYLE_ROMAN_UPPER;
		case arabic:
			return PDPageLabelRange.STYLE_DECIMAL;
		case roman:
			return PDPageLabelRange.STYLE_ROMAN_LOWER;
		}
		System.out.println("FormatPageNumber: unknown type");
		return PDPageLabelRange.STYLE_DECIMAL;
	}
}
