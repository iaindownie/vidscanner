package vidscanner;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.io.*;

/**
	A class for the construction of a help window
	@author Iain Downie: MSc IT 2000-2001 Summer Project.
 */
public class Help extends JInternalFrame
{	
	// Window size and location variables
	private final int WIDTH = 675;
	private final int HEIGHT = 450;
	private final int XCOORDINATE = 50;
	private final int YCOORDINATE = 3;
	
	// Components
	private JButton homeButton;		// Allows user to go home
	private JTextField urlField;	// Allows user to enter a URLfield
	private JEditorPane editorPane;	// EditorPane used to display HTML file

	// The Help homepage
	private final String helpURL = "file:"
								  + System.getProperty("user.dir")
								  + System.getProperty("file.separator") 
								  + "/htmlfiles/AVIHelp.html";


	/**
	Constructor for the Help window.
	*/
	public Help()
	{
		// JInternalFrame attributes
		super("Help", 
			true, //resizable
			true, //closable
			false, //maximizable
			false);//iconifiable

		// Add panel method to contentPane.
		getContentPane().add(mkHelpPanel(true, true, helpURL));
                System.out.print(helpURL);
		
		// Set suitable size and location
		setSize(WIDTH,HEIGHT);
		setLocation(XCOORDINATE,YCOORDINATE);
	
	} // end Help()


	/**
	This method returns a help Panel, which displays a HTML file in 
	an editor pane.	The home HTML file  (ExtraHelp.html) provides help, 
	credits and links to other sites. If selected, the pane also provides
	a textfield within which the user can enter a URL and the file at this
	location will be displayed in the editor pane.
	@param withURLField, True if the help panel should include a textfield 
	allowing the user to enter their own URLs, false otherwise. 	
	@param withNavigationPanel, True if the help panel should the whole 
	navigation panel
	@param initialURL, a string for the target HTML page
	@return a help panel container
	 */
	public Container mkHelpPanel(boolean withURLField, 
		boolean withNavigationPanel, String initialURL)
	{
		// Methods to allow external access using browser
		System.setProperty("http.proxyHost","wwwcache.dcs.gla.ac.uk");
		System.setProperty("http.proxyPort","8080");

		// Create browser panel
		JPanel browser = new JPanel(new BorderLayout());

		// Create the utility bar
		JPanel utilPanel = new JPanel();
		utilPanel.setLayout(new BoxLayout(utilPanel, BoxLayout.X_AXIS));

		// Create the homepage button
		ImageIcon homeI = new ImageIcon("images/Home24.gif");
                homeButton = new JButton(homeI);
		homeButton.setPreferredSize(new Dimension(24,24));
		homeButton.setMaximumSize(new Dimension(24,24));
		homeButton.setBorderPainted(false);
		homeButton.setToolTipText("home");
		homeButton.addActionListener(new myActionListener());

		//Create an editor pane.
		try
		{
			editorPane = new JEditorPane(initialURL);
			editorPane.setEditable(false);
			editorPane.addHyperlinkListener(new Hyperactive());
			JScrollPane editorScrollPane = new JScrollPane(editorPane);
			browser.add(editorScrollPane, BorderLayout.CENTER);
		}
		catch(IOException ioe)
		{
		}

		// Populate the utility bar
		utilPanel.add(Box.createHorizontalGlue());

		// If required add URL field with actionlistener on using JTextField
		if (withURLField)
		{
			JLabel urlLabel = new JLabel("  url:");
			urlField = new JTextField(10);
			urlField.setText(SpawnyUtils.reducePath(initialURL, 0, '/'));
			urlField.addActionListener(new myActionListener());
			utilPanel.add(urlLabel);
			utilPanel.add(urlField);
		}		
		utilPanel.add(homeButton);
		if (withNavigationPanel)
		{
			browser.add(utilPanel, BorderLayout.SOUTH);
		}
		return browser;
	
	} // end mkHelpPanel()


	/**
	Actionlisteners to respond to home button or textfield.
	*/
	private class myActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			String url;
			if (event.getSource() == urlField) 
				url = urlField.getText();
			else  // Clicked "home" button instead of entering URL
				url = helpURL;
		  	try 
		  	{
			    editorPane.setPage(new URL(url));
				urlField.setText(url);
				}
			catch(IOException ioe) 
			{
			}
		}
	
	} // end myActionListener()


	/**
	Hyperlinklistener to listen to hyperlinks on webpage.
	*/
	private class Hyperactive implements HyperlinkListener
	{
		public void hyperlinkUpdate(HyperlinkEvent event)
		{
			if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
			{
				try
				{
					editorPane.setPage(event.getURL());
				} 
				catch(IOException ioe)
				{
				}
			}
		}
	
	} // end Hyperactive()
 
} // end Help class








