package components.preview;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDPageLabelRange;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.PDFRenderer;

public class TestPDFBox extends JFrame
{
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					TestPDFBox frame = new TestPDFBox();
					frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 * @throws InvalidPasswordException
	 */
	public TestPDFBox() throws InvalidPasswordException, IOException
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		File PDF_Path = new File("file.pdf");
		PDDocument inputPDF = PDDocument.load(PDF_Path);
		// List<PDPage> allPages = inputPDF.getDocumentCatalog().getAllPages();
		// PDPageTree allPages = inputPDF.getDocumentCatalog().getPages();
		// PDPage testPage = allPages.get(0);

		// PDFPagePanel pdfPanel = new PDFPagePanel();

		JPanel status = new JPanel(new GridLayout(1, 1));

		JLabel statusLabel = new JLabel("Status");
		status.add(statusLabel);

		// PDFDebugger db = new PDFDebugger(true);
		// PagePane pp = new PagePane(inputPDF, pageDict, statusLabel);

		// contentPane.add(pp.getPanel(), BorderLayout.CENTER);
		contentPane.add(status, BorderLayout.PAGE_END);

		PDFRenderer renderer = new PDFRenderer(inputPDF);
		statusLabel.setText("Rendering...");
		long t = System.currentTimeMillis();
		BufferedImage bim = renderer.renderImage(5, (float) 0.75);
		// for (int i = 0; i < 10; ++i)
		// {
		// bim = renderer.renderImage(i, (float) 0.75);
		// }

		statusLabel.setText("time = " + (System.currentTimeMillis() - t));

		contentPane.add(new JLabel(new ImageIcon(bim)));

		// //////////////////////////////////////////////

		PDPageLabels labels = new PDPageLabels(inputPDF);

		System.out.println("count=" + labels.getPageRangeCount());

		PDPageLabelRange range = new PDPageLabelRange();
		// range.setPrefix(prefix);
		range.setStart(1);
		range.setStyle(PDPageLabelRange.STYLE_DECIMAL);
		labels.setLabelItem(0, range);

		range = new PDPageLabelRange();
		// range.setPrefix(prefix);
		range.setStart(10);
		range.setStyle(PDPageLabelRange.STYLE_ROMAN_UPPER);
		labels.setLabelItem(3, range);

		inputPDF.getDocumentCatalog().setPageLabels(labels);
		inputPDF.save(PDF_Path);
		inputPDF.close();

		System.out.println("finished");
	}

}
