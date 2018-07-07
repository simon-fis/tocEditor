package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class PdfPageLoader
{
	class PageResolution
	{
		public final int page;
		public final int resolution;

		public PageResolution(int page, int resolution)
		{
			this.page = page;
			this.resolution = resolution;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null)
			{
				return false;
			}

			if (!(obj instanceof PageResolution))
			{
				return false;
			}

			PageResolution temp = (PageResolution) obj;

			return (page == temp.page) && (resolution == temp.resolution);
		}

		@Override
		public int hashCode()
		{
			return Integer.hashCode(page) * 31 + Integer.hashCode(resolution);
		}
	}

	private ProcessBuilder builder;
	private ArrayList<String> cmd;
	private Map<PageResolution, BufferedImage> imgs;

	private File pdfFile;

	private final static int resolutionIndex = 2;
	private final static int firstPageIndex = 4;
	private final static int lastPageIndex = 6;
	private final static int fileIndex = 7;

	public PdfPageLoader(File procFile) throws FileNotFoundException
	{
		pdfFile = null;
//		File procFile = new File(procDir, "pdftopng.exe");

		if (procFile == null || !procFile.exists())
		{
			throw new FileNotFoundException("Can not find file " + procFile);
		}

		imgs = new HashMap<PageResolution, BufferedImage>();

		cmd = new ArrayList<String>();
		cmd.add(procFile.getAbsolutePath());
		cmd.add("-r");
		cmd.add(Integer.toString(50));
		cmd.add("-f");
		cmd.add(Integer.toString(1));
		cmd.add("-l");
		cmd.add(Integer.toString(1));
		cmd.add("");
		cmd.add("-");

		builder = new ProcessBuilder(cmd);

		// builder.directory(dir);
		builder.redirectErrorStream(false);
		// builder.redirectError(Redirect.INHERIT);
		// builder.inheritIO();
		// builder.redirectInput(Redirect.PIPE).
	}

	public BufferedImage getPage(int page, int resolution)
	{
		if (pdfFile == null)
			return null;

		if (page < 0)
			return null;

		if (resolution < 1)
			return null;

		PageResolution pr = new PageResolution(page, resolution);
		BufferedImage img = imgs.get(pr);

		if (img == null)
		{
			// System.out.println("Generate page " + page + " with resolution "
			// + resolution);

			cmd.set(resolutionIndex, Integer.toString(resolution));
			cmd.set(firstPageIndex, Integer.toString(page));
			cmd.set(lastPageIndex, Integer.toString(page));

			builder.command(cmd);

			try
			{
				Process p = builder.start();
				img = ImageIO.read(p.getInputStream());
				imgs.put(pr, img);
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

			} catch (IOException e1)
			{
				e1.printStackTrace();
			} catch (InterruptedException e1)
			{
				e1.printStackTrace();
			}

		} else
		{
			// System.out.println("Load page " + page);
		}
		return img;
	}

	public boolean setPdfFile(File pdfFile)
	{
		if (pdfFile == null){
			return false;
		}
		
		if(!pdfFile.exists())
		{
			System.out.println("File \"" + pdfFile.getAbsolutePath()
					+ "\" does not exist.");
			
			if (this.pdfFile != null)
			{
				System.out.println("Keep the old one \""
						+ this.pdfFile.getAbsolutePath() + "\"");
			}
			return false;
		}

		// new file is different
		if (this.pdfFile == null || pdfFile.compareTo(this.pdfFile) != 0)
		{
			System.out.println("Load new pdf file");
			this.pdfFile = pdfFile;
			cmd.set(fileIndex, this.pdfFile.getAbsolutePath());
			imgs.clear();
			return true;
		} else
		{
			System.out.println("new file coincide with the old one");
			return false;
		}

	}
}
