package components.preview;

import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import util.PdfExtractText;

public class SelectTocDialog extends JDialog implements
		PreviewMarker
{
	private static final long serialVersionUID = 1L;
	private PreviewPanel previewPanel;

	private Map<Integer, Boolean> markedPages;

	private File pdfFile;
	private File procDir;
	private File txtFile;
	private JPanel buttonPane;

	private int minPage;
	private int maxPage;

	private boolean savedFile;

	public static void main(String[] args)
	{
		try
		{
			File temp = new File(args[0]);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			SelectTocDialog dialog = new SelectTocDialog(
					null, new File(temp, "file.pdf"), new File(temp, "home"),
					new File(temp, "content.txt"), 10);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public SelectTocDialog(JFrame owner, File pdfFile, File procDir,
			File txtFile, int maxPage) throws IOException
	{
		super(owner, "pdf to text of selected pages",
				ModalityType.TOOLKIT_MODAL);

		this.pdfFile = pdfFile;
		this.procDir = procDir;
		this.txtFile = txtFile;
		this.savedFile = false;

		markedPages = new HashMap<Integer, Boolean>();

		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());

		// Create the status area.
		JPanel statusPane = new JPanel(new GridLayout(1, 1));
		statusPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPane
				.add(new JLabel(
						"press alt + K to mark the pages containing the table of content"));

		previewPanel = new PreviewPanel(procDir);
		previewPanel.setPdfFile(pdfFile);
		previewPanel.setPreviewMarker(this);

		initButtonPane();

		getContentPane().add(previewPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		// getContentPane().add(statusPane, BorderLayout.);

		previewPanel.setPage(1);

		pack();
	}

	private void initButtonPane()
	{
		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		getRootPane().setDefaultButton(okButton);
		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("Extract text from selected pages");

				minPage = Integer.MAX_VALUE;
				maxPage = Integer.MIN_VALUE;
				boolean foundPage = false;
				for (Integer page : markedPages.keySet())
				{
					if (markedPages.get(page))
					{
						foundPage = true;
						if (page < minPage)
							minPage = page;
						if (page > maxPage)
							maxPage = page;
					}
				}

				if (!foundPage)
				{
					System.out.println("found no marked page");
					return;
				} else
				{
					System.out.println("page " + minPage + "-" + maxPage);
				}

				try
				{
					PdfExtractText.PDFBox(pdfFile, procDir, txtFile, minPage, maxPage);
				} catch (FileNotFoundException e1)
				{
					e1.printStackTrace();
				}
				savedFile = true;

				pressCloseButton();
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(e -> pressCloseButton());

		AbstractAction markAction = new AbstractAction("Mark Page")
		{
			private static final long serialVersionUID = 1L;
			{
				putValue(Action.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_M, Event.CTRL_MASK));
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				markCurrentPage();
			}
		};

		JButton markButton = new JButton(markAction);
		markButton.getActionMap().put("markPage", markAction);
		markButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				(KeyStroke) markAction.getValue(Action.ACCELERATOR_KEY),
				"markPage");
		// TODO display accelerator_key on button
		// markButton.setMnemonic( KeyEvent.VK_M ); //this do not work

		buttonPane = new JPanel();
		//
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));

		buttonPane.add(markButton);
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(okButton);
		buttonPane.add(cancelButton);
	}

	private void pressCloseButton()
	{
		switch (SelectTocDialog.this.getDefaultCloseOperation())
		{
		case HIDE_ON_CLOSE:
			SelectTocDialog.this.setVisible(false);
			break;
		case DISPOSE_ON_CLOSE:
			SelectTocDialog.this.dispose();
			break;
		case EXIT_ON_CLOSE:
			SelectTocDialog.this.dispose();
			break;
		case DO_NOTHING_ON_CLOSE:
		}
	}

	public void markCurrentPage()
	{
		int page = previewPanel.getCurrentPage();
		Boolean marked = markedPages.get(page);
		if (marked == null || !marked)
		{
			markedPages.put(page, true);
		} else
		{
			markedPages.put(page, false);
		}
		previewPanel.setPage(page); // update Border
	}

	@Override
	public boolean isMarked(int page)
	{
		Boolean temp = markedPages.get(page);
		return (temp != null) && temp;
	}

	public int getMinMarkedPage()
	{
		return minPage;
	}

	public int getMaxMarkedPage()
	{
		return maxPage;
	}

	public boolean isGood()
	{
		return savedFile;
	}

}
