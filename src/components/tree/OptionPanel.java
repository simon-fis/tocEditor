package components.tree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TreeModelListener;

import com.fasterxml.jackson.databind.ObjectMapper;

import format.Format;
import format.FormatLevel;
import format.FormatPageNumber;
import format.FormatPart;

public class OptionPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private TreePanel treePanel;
	private JPanel buttonPanel;
	// TODO add panel to set allowed characters for the entry title
	private JPanel titlePanel;

	private File formatFile;
	private Format format;
	private JTextField textAllowedTitles;

	public OptionPanel(Format format, File formatFile, TreeModelListener tml)
	{
		super();

		this.format = format;
		this.formatFile = formatFile;

		System.out.println("Format");
		System.out.println(format.toFormatString());

		treePanel = new TreePanel(this.format, tml);
		JScrollPane scrollPane = new JScrollPane(treePanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		initButtonPanel();
		initTitlePanel();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(scrollPane);
		add(buttonPanel);
		add(titlePanel);

	}

	private void initTitlePanel()
	{
		titlePanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Allowed Titles");
		textAllowedTitles = new JTextField(format.getAllowedTitles());
		titlePanel.add(label, BorderLayout.WEST);
		titlePanel.add(textAllowedTitles, BorderLayout.CENTER);
		titlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

	}

	private void initButtonPanel()
	{
		buttonPanel = new JPanel(new GridLayout(1, 3));
		buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

		{
			JButton addLevelButton = new JButton("+Level");
			addLevelButton.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					treePanel.addNewLevel();
				}

			});
			buttonPanel.add(addLevelButton);
		}
		{
			JButton removeLevelButton = new JButton("-Level");
			removeLevelButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					treePanel.removeCurrentLevel();
				}
			});
			buttonPanel.add(removeLevelButton);
		}
		// {
		// JButton addPartButton = new JButton("+Part");
		// // addButton.setActionCommand(ADD_COMMAND);
		// addPartButton.addActionListener(new ActionListener()
		// {
		// @Override
		// public void actionPerformed(ActionEvent e)
		// {
		// treePanel.addNewPart();
		// }
		// });
		// buttonPanel.add(addPartButton);
		// }
		{
			JButton removePartButton = new JButton("-Part");
			removePartButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					treePanel.removeCurrentPart();
				}

			});
			buttonPanel.add(removePartButton);
		}
	}

	public void setFormatFile(File formatFile)
	{
		this.formatFile = formatFile;
	}

	public void addNewPart(int startEntry)
	{
		treePanel.addNewPart(startEntry);
	}

	public void updateAllowedTitles()
	{
		format.setAllowedTitles(textAllowedTitles.getText());
	}

	public void saveFormat()
	{
		if (formatFile == null)
		{
			return;
		}

		try
		{
			ObjectMapper mapper = new ObjectMapper();
			String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(format);
			// FileWriter fw = new FileWriter(formatFile);
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(formatFile), StandardCharsets.UTF_8);
			fw.write(jsonStr);
			fw.close();
		} catch (IOException e)
		{
			System.out.println("can not write to file " + formatFile.getAbsolutePath());
			e.printStackTrace();
		}
	}

	private static void createAndShowGUI()
	{
		File tempDir = new File("C:\\Users\\Simon Fischer\\simon\\USB\\ToDo\\pdf");

		File homeDir = new File(tempDir, "home");
		// File workingDir = new File(tempDir, "work");
		// File pdfFile = new File(tempDir, "file.pdf");
		File formatFile = new File(homeDir, "defaultFormat.json");

		// read test content format
		// ...

		// create test content format

		Format format = new Format();

		format.setAllowedTitles("[a-zA-Z ]+");

		FormatPart fPart = new FormatPart();

		fPart.addfLevel(new FormatLevel(0, "Kapitel #R0", FormatPageNumber.arabic));
		fPart.addfLevel(new FormatLevel(1, "#R0.#a1", FormatPageNumber.arabic));
		fPart.addfLevel(new FormatLevel(2, "#R0.#a1,#a2", FormatPageNumber.Roman));

		format.addfPart(0, fPart);

		FormatPart fPart2 = new FormatPart();

		fPart2.addfLevel(new FormatLevel(0, "Bla #a0", FormatPageNumber.roman));

		format.addfPart(1, fPart2);

		// //////////////////////

		// Create and set up the window.
		JFrame frame = new JFrame("DynamicTreeDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		OptionPanel newContentPane = new OptionPanel(format, formatFile, null);
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable()
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
				createAndShowGUI();
			}
		});
	}
}
