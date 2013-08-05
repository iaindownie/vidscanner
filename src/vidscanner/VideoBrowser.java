package vidscanner;
import javax.swing.JInternalFrame;
import javax.swing.JDesktopPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.beans.PropertyVetoException;

/**
	The main VideoBrowser class which controls all the other internal frames.
	Most windows can only have one instance at any given time.
	@author Iain Downie: MSc IT 2000-2001 Summer Project.
*/
public class VideoBrowser extends JFrame
{
	// Swing variables for desktop application
	private JDesktopPane desktop;
	private JInternalFrame JiF;
	private JMenuBar menuBar;
	private JMenu openMenu, helpMenu;
	private JMenuItem playItem, scanItem, browseItem, 
					  helpItem, aboutItem, quitItem;
	private final int inset = 50;

	// Internal variables for varied JInternalFrames
	private VideoBrowser vb;
	private BrowseWindow bframe;
	private Help helpWindow;
	private About aboutWindow;
	private JInternalFrame other;
	protected ScanWindow sframe; // needed by browseWindow
	
	// Internal frame flags (only one open at any given time)
	private boolean browseOpen = false;
	private boolean helpOpen = false;
	private boolean aboutOpen = false;
	protected boolean scanOpen = false; // needed by browseWindow


	/** 
	Main Method.
	Creates a VideoBrowser object.
	*/
	public static void main(String[] args)
	{
		VideoBrowser vb = new VideoBrowser();
		vb.setVisible(true);
	}


	/** 
	Constructor.
	Creates a JFrame holding a JDeskTopPane with menu.
	Size relates to the screen size of user.
	*/
	public VideoBrowser()
	{

		// Set title for the JFrame
		super("AVI Scanner/Browser");

		// Required to allow subsequent windows to appear
		vb = this;		

		//Make the window be inset x pixels from each edge of any screen.
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(inset, inset, screenSize.width - inset*2, 
			screenSize.height - inset*2);

		// a utility closer class
		addWindowListener(new WindowCloser()); 

		//Set up the GUI with a specialized layered pane.
		desktop = new JDesktopPane();
		setContentPane(desktop);

		// Create the menu bar
		setJMenuBar(createMenuBar());

		//Make dragging faster:
		desktop.putClientProperty("JDesktopPane.dragMode", "outline");

	} // end Constructor()



	/**
	Method to create the menubar for the JDesktopPane.
	@return the menu bar.
	*/
	public JMenuBar createMenuBar()
	{
		// Create the entire bar
		menuBar = new JMenuBar();

		// Create a new menu list for File etc.
		openMenu = new JMenu("File");
		openMenu.setMnemonic(KeyEvent.VK_F);

		// Create a new menu item for playWindow
		playItem = new JMenuItem("Play video file");
		playItem.setMnemonic(KeyEvent.VK_P);
		playItem.addActionListener(new MenuListener());

		// Create a new menu item for scanWindow
		scanItem = new JMenuItem("Scan video file");
		scanItem.setMnemonic(KeyEvent.VK_S);
		scanItem.addActionListener(new MenuListener());

		// Create a new menu item for browseWindow
		browseItem = new JMenuItem("Browse indexed file");
		browseItem.setMnemonic(KeyEvent.VK_B);
		browseItem.addActionListener(new MenuListener());

		// Create a new menu item for quiting
		quitItem = new JMenuItem("Quit");
		quitItem.setMnemonic(KeyEvent.VK_Q);
		quitItem.addActionListener(new MenuListener());
		
		
		// Create a new menu list for Help etc.
		helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);

		// Create a new menu item for helpWindow
		helpItem = new JMenuItem("Help");
		helpItem.setMnemonic(KeyEvent.VK_H);
		helpItem.addActionListener(new MenuListener());

		// Create a new menu item for aboutWindow
		aboutItem = new JMenuItem("About");
		aboutItem.setMnemonic(KeyEvent.VK_A);
		aboutItem.addActionListener(new MenuListener());

		// Add each item to the menuBar
		openMenu.add(playItem);
		openMenu.add(scanItem);
		openMenu.add(browseItem);
		openMenu.add(quitItem);
		helpMenu.add(helpItem);
		helpMenu.add(aboutItem);
		menuBar.add(openMenu);
		menuBar.add(helpMenu);

		return menuBar;

	} // end createMenuBar()


	/**
	Method to return a new Scanner Window.
	Sets the scanOpen flag to true.
	@param vb, a VideoBrowser main application window.
	*/
	public void createScanWindow(VideoBrowser vb)
	{
		sframe = new ScanWindow(vb, inset);
		sframe.addInternalFrameListener(new MyInternalFrameListener());
		sframe.setVisible(true);
		desktop.add(sframe);
		try
		{
			sframe.setSelected(true);
		}
		catch (PropertyVetoException e)
		{
			System.out.println("Error creating scanning window: " + e);
		}
		scanOpen = true;

	} // end createScanWindow()


	/**
	Method to return a new Browser Window.
	Sets the browseOpen flag to true.
	@param vb, the main application window.
	*/
	public void createBrowseWindow(VideoBrowser vb)
	{
		bframe = new BrowseWindow(vb, inset);
		bframe.addInternalFrameListener(new MyInternalFrameListener());
		bframe.setVisible(true);
		desktop.add(bframe);
		try
		{
			bframe.setSelected(true);
		}
		catch (PropertyVetoException e)
		{
			System.out.println("Error creating browsing window: " + e);
		}
		browseOpen = true;

	} // end createBrowseWindow()


	/**
	Method to return a new Help Window.
	Sets the helpOpen flag to true.
	@param vb, the main application window.
	*/
	public void createHelp(VideoBrowser vb)
	{
		helpWindow = new Help();
		helpWindow.addInternalFrameListener(new MyInternalFrameListener());
		helpWindow.setVisible(true);
		desktop.add(helpWindow);
		try
		{
			helpWindow.setSelected(true);
		}
		catch (PropertyVetoException e)
		{
			System.out.println("Error creating help window: " + e);
		}
		helpOpen = true;

	} // end createHelp()


	/**
	Method to return a new About Window.
	Sets the aboutOpen flag to true.
	@param vb, the main application window.
	*/
	public void createAbout(VideoBrowser vb)
	{
		aboutWindow = new About();
		aboutWindow.addInternalFrameListener(new MyInternalFrameListener());
		aboutWindow.setVisible(true);
		desktop.add(aboutWindow);
		try
		{
			aboutWindow.setSelected(true);
		}
		catch (PropertyVetoException e)
		{
			System.out.println("Error creating about window: " + e);
		}
		aboutOpen = true;

	} // end createAbout()


	/**
	Method to return a new JInternalFrame.
	No flags attached so that any number can be opened simultaneously.
	@param internal, opens another internal window.
	*/
	public void addOtherFrame(JInternalFrame other)
	{
		other.setVisible(true);
		other.addInternalFrameListener(new MyInternalFrameListener());
		desktop.add(other);
		try
		{
			other.setSelected(true);
		} 
		catch (PropertyVetoException e)
		{
			System.out.println("Error creating internal window: " + e);
		}

	} // end addOtherFrame()


	/**
	Method to set a particular open JIframe as selected.
	@param internal, selects relevant internal window.
	@param tf, a boolean to select or not.
	*/
	protected void JIFsetSelected(JInternalFrame internal, boolean tf)
	{
		try
		{
			internal.setSelected(tf);
		} 
		catch (PropertyVetoException e)
		{
			System.out.println("Error setting internal window: " + e);
		}

	} // end JIFsetSelected()


	/**
	Private class for the menu bar events.
	If each item already open, automatic selection of the requested
	frame takes place. If not open, it calls relevant opening method.
	*/
	private class MenuListener implements ActionListener
	{						   
		public void actionPerformed (ActionEvent event)
		{
			if (event.getSource() == playItem)
			{
				WholeVideoPlayer play = new WholeVideoPlayer(SpawnyUtils.returnML(vb));
				addOtherFrame(play);
			}
			if (event.getSource() == scanItem)
			{
				if (scanOpen == false)
				{
					createScanWindow(vb);
				}
				else JIFsetSelected(sframe, true);
			}
			if (event.getSource() == browseItem)
			{
				if (browseOpen == false)
				{
					createBrowseWindow(vb);
				}
				else JIFsetSelected(bframe, true);
			}
			if (event.getSource() == quitItem)
			{
				dispose();
				System.exit(0);
			}
			if (event.getSource() == helpItem)
			{
				if (helpOpen == false)
				{
					createHelp(vb);
				}
				else JIFsetSelected(helpWindow, true);
			}
			if (event.getSource() == aboutItem)
			{
				if (aboutOpen == false)
				{
					createAbout(vb);
				}
				else JIFsetSelected(aboutWindow, true);
			}
		}
	} // end MenuListener()


	/**
	Private class for the internal frame event events.
	Only acts on the windows closing.
	*/
	private class MyInternalFrameListener implements InternalFrameListener
	{						   
		public void internalFrameClosing(InternalFrameEvent e)
		{
			if (e.getSource() == bframe)
				browseOpen = false;
			if (e.getSource() == sframe)
				scanOpen = false;
			if (e.getSource() == helpWindow)	
				helpOpen = false;
			if (e.getSource() == aboutWindow)
				aboutOpen = false;

		}
		public void internalFrameClosed(InternalFrameEvent e)
		{
			if (e.getSource() == bframe)
				browseOpen = false;
			if (e.getSource() == sframe)
				scanOpen = false;
			if (e.getSource() == helpWindow)	
				helpOpen = false;
			if (e.getSource() == aboutWindow)
				aboutOpen = false;

		}
		public void internalFrameOpened(InternalFrameEvent e)
		{
		}
		public void internalFrameIconified(InternalFrameEvent e)
		{
		}
		public void internalFrameDeiconified(InternalFrameEvent e)
		{
		}
		public void internalFrameActivated(InternalFrameEvent e)
		{
		}
		public void internalFrameDeactivated(InternalFrameEvent e)
		{
		}

	} // end MyInternalFrameListener()
	
	
	/**
	Inner class to confirm the closing of the entire programme from 
	the frame close button. Extends WindowAdapter
	*/
	private class WindowCloser extends WindowAdapter
	{
		public void windowClosing (WindowEvent event)
		{
			System.exit(0);		// Exits programme
		}
		
	} // end WindowCloser
	
}






