package data;

import format.FormatPageNumber;

public class PageNumber
{
	public String orig;
	public int page;
	public FormatPageNumber fPageNumber;
	public PageNumber(String orig, int page, FormatPageNumber fPageNumber)
	{
		this.orig = orig;
		this.page = page;
		this.fPageNumber = fPageNumber;
	}
}
