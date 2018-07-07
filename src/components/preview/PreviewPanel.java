package components.preview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.apache.pdfbox.pdmodel.PDDocument;

import util.PdfPageLoader;

public class PreviewPanel extends JPanel implements PropertyChangeListener
{
	private static final long serialVersionUID = 1L;

	private int maxPage;
	private int curPage;
	private int maxResolution;
	private int stepResolution;
	private int curResolution;
	private PdfPageLoader pdf;

	private PreviewMarker marker;

	private Border borderSelected;
	private Border borderNotSelected;

	private JPanel contentPane;
	private JLabel label;
	private ImageIcon icon;

	private JPanel buttonPane;
	private JButton btnPrev;
	private JButton btnNext;
	private JFormattedTextField pageField;
	private JButton btnZoomPlus;
	private JButton btnZoomMinus;

	public PreviewPanel(File procFile) throws FileNotFoundException
	{
		super();

		maxPage = 1;
		curPage = 1;
		maxResolution = 200;
		curResolution = 50;
		stepResolution = 10;
		pdf = new PdfPageLoader(procFile);

		borderSelected = new LineBorder(Color.BLACK, 3, true);
		borderNotSelected = new LineBorder(Color.WHITE, 3, false);

		initContentPane();
		initButtonPane();

		setLayout(new BorderLayout());
		add(contentPane, BorderLayout.CENTER);
		add(buttonPane, BorderLayout.SOUTH);

		disablePreview();
	}

	public void setPdfFile(File pdfFile)
	{
		if (pdf.setPdfFile(pdfFile))
		{
			curPage = 1;
			try
			{
				PDDocument doc = PDDocument.load(pdfFile);
				maxPage = doc.getNumberOfPages();
				doc.close();
			} catch (IOException e)
			{
				maxPage = 100;
				System.out.println("Can not read number of pages -> set to 100");
				e.printStackTrace();
			}

			pageField.setValue(curPage);
		}
	}

	private void initButtonPane()
	{
		buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
		{
			AbstractAction prevAction = new AbstractAction("Previous")
			{
				private static final long serialVersionUID = 1L;
				{
					putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
							KeyEvent.VK_K, Event.CTRL_MASK));
				}

				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (curPage <= 1)
					{
						System.out.println("There is no previous page");
						return;
					}
					curPage--;
					previewPage(curPage, curResolution);
				}
			};
			btnPrev = new JButton(prevAction);
			btnPrev.getActionMap().put("previousPage", prevAction);
			btnPrev.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					(KeyStroke) prevAction.getValue(Action.ACCELERATOR_KEY),
					"previousPage");
			buttonPane.add(btnPrev);
		}
		{
			NumberFormat pageFormat = NumberFormat.getNumberInstance();
			pageFormat.setMaximumFractionDigits(0);
			pageFormat.setParseIntegerOnly(true);

			pageField = new JFormattedTextField(pageFormat);
			pageField.setHorizontalAlignment(JFormattedTextField.CENTER);
			pageField.setColumns(5);
			pageField.setEditable(true);
			pageField.setValue(curPage);
			pageField.addPropertyChangeListener("value", this); // after set
																// value
			pageField.addFocusListener(new FocusListener()
			{

				@Override
				public void focusGained(FocusEvent e)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							pageField.selectAll();
						}
					});
				}

				@Override
				public void focusLost(FocusEvent e)
				{
				}

			});

			buttonPane.add(pageField);
		}
		{
			AbstractAction nextAction = new AbstractAction("Next")
			{
				private static final long serialVersionUID = 1L;
				{
					putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
							KeyEvent.VK_J, Event.CTRL_MASK));
				}

				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (curPage >= maxPage)
					{
						System.out.println("There is no next page");
						return;
					}
					curPage++;
					previewPage(curPage, curResolution);
				}
			};
			btnNext = new JButton(nextAction);
			btnNext.getActionMap().put("nextPage", nextAction);
			btnNext.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					(KeyStroke) nextAction.getValue(Action.ACCELERATOR_KEY),
					"nextPage");
			buttonPane.add(btnNext);
		}
		{
			AbstractAction zoomPlusAction = new AbstractAction("+")
			{

				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (curResolution + stepResolution > maxResolution)
					{
						System.out.println("Max resolution is already reached");
						return;
					}
					curResolution += stepResolution;
					previewPage(curPage, curResolution);
				}
			};
			btnZoomPlus = new JButton(zoomPlusAction);
			buttonPane.add(btnZoomPlus);
		}
		{
			AbstractAction zoomMinusAction = new AbstractAction("-")
			{

				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (curResolution - stepResolution < 1)
					{
						System.out.println("Min resolution is already reached");
						return;
					}
					curResolution -= stepResolution;
					previewPage(curPage, curResolution);
				}
			};
			btnZoomMinus = new JButton(zoomMinusAction);
			buttonPane.add(btnZoomMinus);
		}
	}

	private void initContentPane()
	{
		icon = new ImageIcon();
		label = new JLabel(icon);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);

		contentPane = new JPanel();
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER));
		contentPane.add(label);
	}

	private void previewPage(int curPage, int curResolution)
	{
		BufferedImage img = pdf.getPage(curPage, curResolution);

		if (img != null)
		{
			icon.setImage(img);

			label.setText(null);
			label.setIcon(icon);
			label.setBorder(isMarked(curPage) ? borderSelected
					: borderNotSelected);

			btnNext.setEnabled(true);
			btnPrev.setEnabled(true);
			btnZoomMinus.setEnabled(true);
			btnZoomPlus.setEnabled(true);

			pageField.setEnabled(true);
			pageField.setValue(curPage);

			label.updateUI();
		} else
		{
			disablePreview();
		}
	}

	private void disablePreview()
	{
		label.setText("preview not available");
		label.setIcon(null);
		label.setBorder(null);

		btnNext.setEnabled(false);
		btnPrev.setEnabled(false);
		btnZoomMinus.setEnabled(false);
		btnZoomPlus.setEnabled(false);

		pageField.setEnabled(false);

		label.updateUI();
	}

	private boolean isMarked(int page)
	{
		return ((marker != null) && marker.isMarked(page));
	}

	public void setPreviewMarker(PreviewMarker marker)
	{
		this.marker = marker;
	}

	public int getCurrentPage()
	{
		return curPage;
	}

	public void setPage(int page)
	{
		if (page <= 0)
		{
			System.out.println("Error: setPage page=" + page + " <= 0");
			return;
		}
		if (page > maxPage)
		{
			System.out.println("Error: setPage page=" + page + " >= maxPage = "
					+ maxPage);
			return;
		}

		curPage = page;
		previewPage(curPage, curResolution);
	}

	public void addKeyListener(KeyListener kl)
	{
		super.addKeyListener(kl);

		btnNext.addKeyListener(kl);
		btnPrev.addKeyListener(kl);

		pageField.addKeyListener(kl);
	}

	public void removeKeyListener(KeyListener kl)
	{
		super.removeKeyListener(kl);

		btnNext.removeKeyListener(kl);
		btnPrev.removeKeyListener(kl);

		pageField.removeKeyListener(kl);
	}

	public void setPreviewEnable(boolean enable)
	{
		if (enable)
		{
			previewPage(curPage, curResolution);

		} else
		{
			disablePreview();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		Object source = evt.getSource();
		if (source == pageField)
		{
			if (evt.getOldValue() != evt.getNewValue())
			{
				if (pageField.hasFocus())
				{
					System.out.println("Edited page field");
					curPage = ((Number) pageField.getValue()).intValue();
					previewPage(curPage, curResolution);
				}
			}
		}
	}
}
