package components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;

import com.fasterxml.jackson.databind.ObjectMapper;

import components.preview.PreviewPanel;
import components.preview.SelectTocDialog;
import components.table.CellRendererLevel;
import components.table.CellRendererOffset;
import components.table.CellRendererPage;
import components.table.MyTableModel;
import components.table.CellRendererTitle;
import components.table.CellEditorTitle;
import components.tree.OptionPanel;
import data.Entry;
import format.Format;
import format.FormatPageNumber;
import format.FormatPart;
import format.FormatLevel;

public class TocEditor extends JFrame
{
	private static final long serialVersionUID = 1L;

	File procFile;
	File workingDir;
	File pdfFile;

	private MyTableModel tableModel;
	private JTable table;

	//TODO undo helpers
	protected UndoAction undoAction;
	protected RedoAction redoAction;
	protected UndoManager undo = new UndoManager();

	private JLabel statusLabel;

	private Format format;

	private OptionPanel optionPanel;
	private JScrollPane tablePanel;
	private PreviewPanel previewPanel;
	private JCheckBoxMenuItem syncPreview;

	private AbstractAction saveHtml;

	public TocEditor(File procFile, File workingDir, File formatFile, File pdfFile, File tocFile, File outFile)
			throws FileNotFoundException
	{
		super("TOC Editor");

		this.procFile = procFile;
		this.workingDir = workingDir;
		this.pdfFile = pdfFile;

		// process format file
		if (formatFile != null && formatFile.exists())
		{
			// read format file
			ObjectMapper mapper = new ObjectMapper();
			try
			{
				format = mapper.readValue(formatFile, Format.class);

			} catch (IOException e)
			{
				e.printStackTrace();
			}
		} else
		{
			// std format
			FormatPart fPart = new FormatPart(FormatPageNumber.roman);
			fPart.addfLevel(new FormatLevel(0, "", FormatPageNumber.roman));

			format = new Format();
			format.setAllowedTitles("[a-zA-Z ]+");
			format.addfPart(0, fPart);
		}

		setPreferredSize(new Dimension(1200, 600));

		initTable();

		optionPanel = new OptionPanel(format, formatFile, new TreeModelListener()
		{

			@Override
			public void treeNodesChanged(TreeModelEvent e)
			{
				table.updateUI();
			}

			@Override
			public void treeNodesInserted(TreeModelEvent e)
			{
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent e)
			{
				table.updateUI();
			}

			@Override
			public void treeStructureChanged(TreeModelEvent e)
			{
			}
		});
		tablePanel = new JScrollPane(table);
		previewPanel = new PreviewPanel(procFile);

		// create the status area.
		JPanel statusPane = new JPanel(new GridLayout(1, 1));
		statusLabel = new JLabel("Status");
		statusPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPane.add(statusLabel);

		// init split pane
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, previewPanel);

		splitPane.setResizeWeight(0.5);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);

		JSplitPane splitPaneLeft = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, optionPanel, splitPane);

		splitPaneLeft.setResizeWeight(0.0);
		splitPaneLeft.setOneTouchExpandable(true);
		splitPaneLeft.setDividerLocation(200);
		
		// add all
//		getContentPane().add(optionPanel, BorderLayout.WEST);
//		getContentPane().add(splitPane, BorderLayout.CENTER);
		getContentPane().add(splitPaneLeft, BorderLayout.CENTER);
		getContentPane().add(statusPane, BorderLayout.PAGE_END);

		// Set up the menu bar.
		JMenu fileMenu = createFileMenu();
		JMenu editMenu = createEditMenu();
		JMenu viewMenu = createViewMenu();

		JMenuBar mb = new JMenuBar();
		mb.add(fileMenu);
		mb.add(editMenu);
		mb.add(viewMenu);
		setJMenuBar(mb);

		pack();
		splitPane.setDividerLocation(0.5);

		// process pdf file
		previewPanel.setPdfFile(this.pdfFile);
		previewPanel.setPage(1);

		// process toc file
		parseTocFile(tocFile);
	}

	private void initTable()
	{
		tableModel = new MyTableModel(format);
		table = new JTable(tableModel)
		{
			private static final long serialVersionUID = 1L;

			private MatteBorder borderNewPart = new MatteBorder(2, 0, 0, 0, Color.BLACK);

			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
			{
				JComponent c = (JComponent) super.prepareRenderer(renderer, row, column);

				if (format.isfPartBeginning(row))
				{
					c.setBorder(borderNewPart);
				}

				return c;
			}
		};
		table.setFillsViewportHeight(true);

		// selection properties
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.getColumn("Level").setMaxWidth(50);
		table.getColumn("Level").setMinWidth(50);
		table.getColumn("Number").setMaxWidth(100);
		table.getColumn("Number").setMinWidth(100);
		table.getColumn("Page").setMaxWidth(50);
		table.getColumn("Page").setMinWidth(50);
		table.getColumn("Offset").setMaxWidth(50);
		table.getColumn("Offset").setMinWidth(50);

		table.setRowHeight(20);

		table.getColumn("Title").setCellEditor(new CellEditorTitle());
		// TODO implement deleting offset
		// table.getColumn("Offset").setCellEditor(new MyOffsetEditor());

		table.getColumn("Title").setCellRenderer(new CellRendererTitle());
		table.getColumn("Page").setCellRenderer(new CellRendererPage());
		table.getColumn("Offset").setCellRenderer(new CellRendererOffset());
		table.getColumn("Level").setCellRenderer(new CellRendererLevel());

		table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
		table.clearSelection();
		table.getTableHeader().setReorderingAllowed(false);

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{

			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
				{
					// System.out.println("still selecting");
					return;
				} else
				{
					if (syncPreview.getState())
					{
						int row = table.getSelectedRow();
						previewRow(row);
					}
				}
			}
		});
		tableModel.addTableModelListener(new TableModelListener()
		{

			@Override
			public void tableChanged(TableModelEvent e)
			{
				// page of offset updated
				if (e.getType() == TableModelEvent.UPDATE && (e.getColumn() == 3 || e.getColumn() == 4))
				{
					if (syncPreview.getState())
					{
						// int row = e.getFirstRow();
						int row = table.getSelectedRow();
						previewRow(row);
					}
				}
			}

		});
	}

	private JMenu createFileMenu()
	{
		JMenu menu = new JMenu("File");

		menu.add(new AbstractAction("Open PDF")
		{
			private static final long serialVersionUID = 1L;
			private final JFileChooser fc = new JFileChooser();

			{
				fc.setFileFilter(new FileFilter()
				{

					@Override
					public String getDescription()
					{
						return ".pdf";
					}

					@Override
					public boolean accept(File f)
					{
						return f.getName().endsWith(".pdf") || f.isDirectory();
					}
				});

				fc.setCurrentDirectory(workingDir);
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				// choose pdf file
				int res = fc.showOpenDialog(TocEditor.this);

				if (res != JFileChooser.APPROVE_OPTION)
				{
					System.out.println("open file canceled");
					return;
				}

				pdfFile = fc.getSelectedFile();

				System.out.println("open pdf file: " + pdfFile.getAbsolutePath());

				previewPanel.setPdfFile(pdfFile);
				previewPanel.setPage(1);
			}
		});

		menu.addSeparator();

		menu.add(new AbstractAction("Open TOC")
		{
			private static final long serialVersionUID = 1L;
			private final JFileChooser fc = new JFileChooser();

			{
				fc.setFileFilter(new FileFilter()
				{
					@Override
					public String getDescription()
					{
						return ".txt and .html";
					}

					@Override
					public boolean accept(File f)
					{
						return f.getName().endsWith(".txt") || f.getName().endsWith(".html") || f.isDirectory();
					}
				});

				fc.setCurrentDirectory(workingDir);
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				// choose toc file
				int res = fc.showOpenDialog(TocEditor.this);

				if (res != JFileChooser.APPROVE_OPTION)
				{
					System.out.println("open toc file canceled");
					return;
				}

				File tocFile = fc.getSelectedFile();

				File formatFile = new File(workingDir, basename(tocFile) + "_format.txt");
				optionPanel.setFormatFile(formatFile);

				System.out.println("open toc file: " + tocFile.getAbsolutePath());

				String name = tocFile.getName();

				if (name.endsWith(".html"))
				{
					System.out.println("not implemented jet");
				} else if (name.endsWith(".txt"))
				{
					parseTocFile(tocFile);
				} else
				{
					int index = name.lastIndexOf('.');
					System.out.println("Reading " + name.substring(index) + " files is not supported");
				}
			}

		});

		menu.add(new AbstractAction("Extract TOC from PDF")
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (pdfFile == null)
				{
					System.out.println("no preview pdf loaded");
					return;
				}

				File tocFile = new File(workingDir, basename(pdfFile) + "_toc.txt");

				File formatFile = new File(workingDir, basename(tocFile) + "_format.txt");
				optionPanel.setFormatFile(formatFile);

				int max = -1;

				// extract toc as .txt file
				if (!tocFile.exists())
				{
					try
					{
						SelectTocDialog stoc = new SelectTocDialog(TocEditor.this, pdfFile, procFile, tocFile, 100);
						stoc.setModalityType(ModalityType.TOOLKIT_MODAL);
						stoc.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
						stoc.setVisible(true);

						if (!stoc.isGood())
						{
							System.out.println("User canceled TOC selection");
							return;
						}

						int min = stoc.getMinMarkedPage();
						max = stoc.getMaxMarkedPage();
						previewPanel.setPage(min);

					} catch (IOException e1)
					{
						e1.printStackTrace();
						return;
					}
				} else
				{
					System.out.println("table of content already extracted");
					System.out.println("open file: " + tocFile.getAbsolutePath());
				}

				parseTocFile(tocFile);

				tableModel.setFirstValidPageNumberToPage(max + 1);
			}

		});

		menu.addSeparator();

		menu.add(new AbstractAction("Save PDF as ... (and embed TOC and Page Labes)")
		{
			private static final long serialVersionUID = 1L;
			private File outFile;
			private final JFileChooser fc = new JFileChooser()
			{
				private static final long serialVersionUID = 1L;

				public void approveSelection()
				{
					File f = getSelectedFile();
					if (f.exists() && getDialogType() == SAVE_DIALOG)
					{
						int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file",
								JOptionPane.YES_NO_OPTION);
						switch (result)
						{
						case JOptionPane.YES_OPTION:
							super.approveSelection();
							return;
						case JOptionPane.CANCEL_OPTION:
							cancelSelection();
							return;
						default:
							return;
						}
					}
					super.approveSelection();
				}
			};
			{
				fc.setFileFilter(new FileFilter()
				{
					@Override
					public String getDescription()
					{
						return ".pdf";
					}

					@Override
					public boolean accept(File f)
					{
						return f.getName().endsWith(".pdf") || f.isDirectory();
					}
				});
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				// TODO add option include pagelabels / bookmarks
				// choose output file
				{
					fc.setSelectedFile(pdfFile);
					int res = fc.showSaveDialog(TocEditor.this);

					if (res != JFileChooser.APPROVE_OPTION)
					{
						System.out.println("save file canceled");
						return;
					}
					outFile = fc.getSelectedFile();
				}

				PDDocument document = null;

				try
				{
					document = PDDocument.load(pdfFile);
				} catch (IOException e1)
				{
					document = null;
					System.out.println("could not open pdf file: " + pdfFile.getAbsolutePath());
				}

				if (document == null)
				{
					return;
				}

				PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();
				if (outline != null && outline.hasChildren())
				{
					int res = JOptionPane.showConfirmDialog(TocEditor.this,
							"Would you like to overwrite existing bookmarks?", "Question", JOptionPane.YES_NO_OPTION);
					switch (res)
					{
					case JOptionPane.YES_OPTION:
						outline = null;
						break;
					case JOptionPane.NO_OPTION:
						break;
					}
				}
				outline = tableModel.generateOutline(outline);
				document.getDocumentCatalog().setDocumentOutline(outline);
				PDPageLabels labels = tableModel.generatePageLabels(document);
				document.getDocumentCatalog().setPageLabels(labels);

				int res = JOptionPane.YES_OPTION;
				while (res == JOptionPane.YES_OPTION)
				{

					try
					{
						document.save(outFile);
						res = JOptionPane.NO_OPTION;
					} catch (IOException e1)
					{
						System.out.println("could not write to file: " + outFile.getAbsolutePath());
						res = JOptionPane.showConfirmDialog(TocEditor.this,
								"Can not write to File. Would you like to retry?", "Question",
								JOptionPane.YES_NO_OPTION);
					}

				}

				try
				{
					document.close();
				} catch (IOException e1)
				{
					System.out.println("could not close pdf file: " + pdfFile.getAbsolutePath());
				}

				statusLabel.setText("Wrote file: " + outFile.getAbsolutePath());

				saveHtml.actionPerformed(null);
			}
		});

		menu.add(new AbstractAction("Save TOC for Pdftk")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				File outFile = new File(workingDir, basename(pdfFile) + "_pdftk.txt");
				try
				{
					FileWriter fw = new FileWriter(outFile);
					for (int i = 0; i < table.getRowCount(); ++i)
					{

						fw.write(tableModel.getPDFTKString(i));
						fw.write("\n");
					}
					fw.close();

				} catch (IOException e1)
				{
					e1.printStackTrace();
					statusLabel.setText("Can not write to file: " + outFile.getAbsolutePath());
				}

				statusLabel.setText("Wrote file: " + outFile.getAbsolutePath());
			}
		});

		saveHtml = new AbstractAction("Save TOC as Html")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				File outFile = new File(workingDir, basename(pdfFile) + ".html");
				try
				{
					tableModel.writeHtml(outFile);

				} catch (IOException e1)
				{
					e1.printStackTrace();
					statusLabel.setText("Can not write to file: " + outFile.getAbsolutePath());
				}

				statusLabel.setText("Wrote file: " + outFile.getAbsolutePath());
			}
		};
		menu.add(saveHtml);

		menu.add(new AbstractAction("Save Page Labels")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				String tempStr = tableModel.getPageLabels();
				System.out.println(tempStr);

				File outFile = new File(workingDir, basename(pdfFile) + "_pageLabes.txt");

				try
				{
					FileWriter fw = new FileWriter(outFile);
					fw.write(tempStr);
					fw.close();

				} catch (IOException e1)
				{
					e1.printStackTrace();
					statusLabel.setText("Can not write to file: " + outFile.getAbsolutePath());
				}

				statusLabel.setText("Wrote file: " + outFile.getAbsolutePath());
			}
		});

		return menu;
	}

	private JMenu createEditMenu()
	{
		JMenu menu = new JMenu("Edit");

		// TODO implement undo and redo
		// undoAction = new UndoAction();
		// menu.add(undoAction);
		//
		// redoAction = new RedoAction();
		// menu.add(redoAction);

		// menu.addSeparator();

		// menu.add(new AbstractAction("Delete First Lines")
		// {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void actionPerformed(ActionEvent e)
		// {
		// tableModel.delteFirstLines();
		// table.updateUI();
		// }
		// });

		menu.add(new AbstractAction("New Part")
		{
			private static final long serialVersionUID = 1L;
			{
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int row = table.getSelectedRow();

				optionPanel.addNewPart(row);
				table.updateUI();
			}
		});

		menu.addSeparator();

		menu.add(new AbstractAction("Reparse")
		{
			private static final long serialVersionUID = 1L;
			{
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK));
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("reparse");
				optionPanel.updateAllowedTitles();
				format.parseEntries();
				optionPanel.saveFormat();
				tableModel.updateStatus();
				table.updateUI();
			}
		});

		menu.add(new AbstractAction("Check")
		{
			private static final long serialVersionUID = 1L;
			{
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("check");
				optionPanel.updateAllowedTitles();
				tableModel.updateStatus();
				table.updateUI();
			}
		});

		menu.addSeparator();

		menu.add(new AbstractAction("Delete Entry")
		{
			private static final long serialVersionUID = 1L;
			{
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK));
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int row = table.getSelectedRow();
				tableModel.deleteEntry(row);

				table.getSelectionModel().setSelectionInterval(0, row);

				table.updateUI();
				optionPanel.updateUI();
			}
		});

		menu.add(new AbstractAction("New Entry")
		{
			private static final long serialVersionUID = 1L;
			{
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK));
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int row = table.getSelectedRow();

				tableModel.newEntry(row);

				table.updateUI();
				optionPanel.updateUI();
			}
		});

		menu.add(new AbstractAction("Move Entry Up")
		{

			private static final long serialVersionUID = 1L;
			{
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK));
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int row = table.getSelectedRow();

				tableModel.swap(row, row - 1);

				table.getSelectionModel().setSelectionInterval(0, row - 1);
			}

		});

		menu.add(new AbstractAction("Move Entry Down")
		{

			private static final long serialVersionUID = 1L;
			{
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK));
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int row = table.getSelectedRow();

				tableModel.swap(row, row + 1);

				table.getSelectionModel().setSelectionInterval(0, row + 1);
			}

		});

		menu.add(new AbstractAction("Combine Entries")
		{
			private static final long serialVersionUID = 1L;

			{
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK));
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int row = table.getSelectedRow();
				tableModel.combineEntries(row);

				// int newRow = tableModel.getNextRowNotGood(row);
				// table.getSelectionModel().setSelectionInterval(0, newRow);

				table.updateUI();
			}
		});

		menu.addSeparator();

		menu.add(new AbstractAction("Set Offset")
		{
			private static final long serialVersionUID = 1L;

			{
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int row = table.getSelectedRow();
				int page = previewPanel.getCurrentPage();

				tableModel.setOffsetToPage(row, page);
			}

		});

		// TODO add delete page, title, ... and allow multiple line selections
		menu.add(new AbstractAction("Delete Page Numbers")
		{
			private static final long serialVersionUID = 1L;

			// {
			// putValue(Action.ACCELERATOR_KEY,
			// KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
			// }

			@Override
			public void actionPerformed(ActionEvent e)
			{
				tableModel.deletePageNumbers();

				table.updateUI();
			}

		});

		return menu;
	}

	private JMenu createViewMenu()
	{
		JMenu menu = new JMenu("View");

		syncPreview = new JCheckBoxMenuItem("Sync Preview");
		menu.add(syncPreview);

		menu.addSeparator();

		menu.add(new AbstractAction("Next Bad Entry")
		{
			private static final long serialVersionUID = 1L;
			{
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int curIndex = Math.max(table.getSelectionModel().getMinSelectionIndex(), 0);
				int index = tableModel.getNextRowNotGood(curIndex + 1);

				if (index < 0)
					return;

				table.getSelectionModel().setSelectionInterval(0, index);

				int viewIndex = Math.max(index + 3, 0);
				table.scrollRectToVisible(new Rectangle(table.getCellRect(viewIndex, 0, true)));
			}

		});

		menu.add(new AbstractAction("Previous Bad Entry")
		{
			private static final long serialVersionUID = 1L;
			{
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, Event.CTRL_MASK));
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int curIndex = Math.max(table.getSelectionModel().getMinSelectionIndex(), 0);
				int index = tableModel.getPrevRowNotGood(curIndex - 1);

				if (index < 0)
					return;

				table.getSelectionModel().setSelectionInterval(0, index);

				int viewIndex = Math.max(index - 3, 0);
				table.scrollRectToVisible(new Rectangle(table.getCellRect(viewIndex, 0, true)));
			}
		});

		menu.addSeparator();

		menu.add(new AbstractAction("Next Level Entry")
		{
			private static final long serialVersionUID = 1L;
			{
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, Event.CTRL_MASK));
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int curIndex = Math.max(table.getSelectionModel().getMinSelectionIndex(), 0);
				int index = tableModel.getNextLevelRow(curIndex);

				if (index < 0)
					return;

				table.getSelectionModel().setSelectionInterval(0, index);

				int viewIndex = Math.max(index + 3, 0);
				table.scrollRectToVisible(new Rectangle(table.getCellRect(viewIndex, 0, true)));
			}

		});

		menu.add(new AbstractAction("Previous Level Entry")
		{
			private static final long serialVersionUID = 1L;
			{
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK));
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int curIndex = Math.max(table.getSelectionModel().getMinSelectionIndex(), 0);
				int index = tableModel.getPrevLevelRow(curIndex);

				if (index < 0)
					return;

				table.getSelectionModel().setSelectionInterval(0, index);

				int viewIndex = Math.max(index - 3, 0);
				table.scrollRectToVisible(new Rectangle(table.getCellRect(viewIndex, 0, true)));
			}
		});

		return menu;
	}

	private void parseTocFile(File tocFile)
	{
		if (tocFile == null || !tocFile.exists())
		{
			return;
		}

		// parse
		List<Entry> entryList = format.parse(tocFile);
		tableModel.fillTable(entryList);

		// set selection
		int index = tableModel.getNextRowNotGood(0);
		table.getSelectionModel().setSelectionInterval(0, index);

		table.updateUI();
	}

	private void previewRow(int row)
	{
		if (pdfFile == null)
		{
			System.out.println("no pdf file");
			return;
		}

		if (row < 0)
		{
			return;
		}

		Entry entry = tableModel.getEntry(row);

		if (entry.page >= 0)
		{
			// System.out.println("set page to selection");
			previewPanel.setPage(entry.page + entry.offset);
		} else
		{
			// System.out.println("page not valid");
		}
	}

	private String basename(File file)
	{
		String filename = file.getName();
		int index = filename.indexOf(".");
		if (index > 0)
		{
			return filename.substring(0, index);
		} else
		{
			return filename;
		}
	}

	// This one listens for edits that can be undone.
	protected class MyUndoableEditListener implements UndoableEditListener
	{
		public void undoableEditHappened(UndoableEditEvent e)
		{
			// Remember the edit and update the menus.
			undo.addEdit(e.getEdit());
			undoAction.updateUndoState();
			redoAction.updateRedoState();
		}
	}

	class UndoAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public UndoAction()
		{
			super("Undo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e)
		{
			try
			{
				undo.undo();
			} catch (CannotUndoException ex)
			{
				System.out.println("Unable to undo: " + ex);
				ex.printStackTrace();
			}
			updateUndoState();
			redoAction.updateRedoState();
		}

		protected void updateUndoState()
		{
			if (undo.canUndo())
			{
				setEnabled(true);
				putValue(Action.NAME, undo.getUndoPresentationName());
			} else
			{
				setEnabled(false);
				putValue(Action.NAME, "Undo");
			}
		}
	}

	class RedoAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public RedoAction()
		{
			super("Redo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e)
		{
			try
			{
				undo.redo();
			} catch (CannotRedoException ex)
			{
				System.out.println("Unable to redo: " + ex);
				ex.printStackTrace();
			}
			updateRedoState();
			undoAction.updateUndoState();
		}

		protected void updateRedoState()
		{
			if (undo.canRedo())
			{
				setEnabled(true);
				putValue(Action.NAME, undo.getRedoPresentationName());
			} else
			{
				setEnabled(false);
				putValue(Action.NAME, "Redo");
			}
		}
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e)
				{
					e.printStackTrace();
				}

				File procFile = null;
				File workingDir = null;
				File pdfFile = null;
				File tocFile = null;
				File formatFile = null;
				File outFile = null;

				if (args.length > 0)
				{
					System.out.println("procFile: " + args[0]);
					procFile = new File(args[0]);
				}
				if (args.length > 1)
				{
					System.out.println("workDir: " + args[1]);
					workingDir = new File(args[1]);
				}
				if (args.length > 2)
				{
					System.out.println("formatFile: " + args[2]);
					formatFile = new File(args[2]);
				}
				if (args.length > 3)
				{
					System.out.println("pdfFile: " + args[3]);
					pdfFile = new File(args[3]);
				}
				if (args.length > 4)
				{
					System.out.println("tocFile: " + args[4]);
					tocFile = new File(args[4]);
				}

				// create and set up the window
				try
				{
					TocEditor frame = new TocEditor(procFile, workingDir, formatFile, pdfFile, tocFile, outFile);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.setVisible(true);
				} catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}
