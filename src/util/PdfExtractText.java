package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfExtractText
{
	public static void pdftotext(File pdfFile, File procDir, File txtFile,
			int minPage, int maxPage) throws FileNotFoundException
	{
		File procFile = new File(procDir, "pdftotext.exe");

		if (!pdfFile.exists())
		{
			throw new FileNotFoundException("Can not find file " + pdfFile);
		}

		if (!procFile.exists())
		{
			throw new FileNotFoundException("Can not find file " + pdfFile);
		}

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add(procFile.getAbsolutePath());
		cmd.add("-layout");
		cmd.add("-f");
		cmd.add(Integer.toString(minPage));
		cmd.add("-l");
		cmd.add(Integer.toString(maxPage));
		cmd.add(pdfFile.getAbsolutePath());
		cmd.add(txtFile.getAbsolutePath());

		ProcessBuilder builder = new ProcessBuilder(cmd);

		// builder.directory(dir);
		builder.redirectErrorStream(false);
		// builder.redirectError(Redirect.INHERIT);
		// builder.inheritIO();
		// builder.redirectInput(Redirect.PIPE).

		builder.command(cmd);

		Process p;
		try
		{
			p = builder.start();

			p.waitFor();
			if (p.exitValue() != 0)
			{
				System.out.println("something went wrong, exit value="
						+ p.exitValue());
				switch (p.exitValue())
				{
				case 1:
					System.out.println("Error: opening PDF file");
					break;
				case 2:
					System.out.println("Error: opening an output file");
					break;
				case 3:
					System.out.println("Error: related to PDF permissions");
					break;
				}
			}
		} catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public static void PDFBox(File pdfFile, File procDir, File txtFile,
			int minPage, int maxPage) throws FileNotFoundException
	{
		try
		{
			PDDocument doc = PDDocument.load(pdfFile);
			PDFTextStripper pdfStripper = new PDFTextStripper();
			pdfStripper.setStartPage(minPage);
			pdfStripper.setEndPage(maxPage);
			pdfStripper.setShouldSeparateByBeads(false);
			String parsedText = pdfStripper.getText(doc);
			doc.close();

			FileWriter fw = new FileWriter(txtFile);
			fw.write(parsedText);
			fw.close();
			
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
