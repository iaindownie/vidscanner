package vidscanner;
import javax.swing.JInternalFrame;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.border.*;
import javax.media.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.control.FramePositioningControl;
import javax.media.control.FrameGrabbingControl;
import javax.media.util.BufferToImage;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.format.VideoFormat;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Vector;
import java.lang.Integer;

// Import the browser specific classes
//import browser.TimeChart;
//import browser.SegmentPlayer;


/**
	The main browser interface. Once the user has scanned a file, they
	can either play the entire video from here, or play any of the
	segments that are displayed on loading the index file. A time chart
	of the video is also displayed which presents info relating to the
	lenght in frames of the vid, and where the segments occur along
	the data time.
	@author Iain Downie: MSc IT 2000-2001 Summer Project.
 */
public class BrowseWindow extends JInternalFrame implements
			ControllerListener, ActionListener
{
	// Variables for the JMF aspect
	private Player p;
	private FramePositioningControl fpc;
	private FrameGrabbingControl fgc;
	private MediaLocator ml = null;
	private DataSource ds = null;
	private Component vc, v;

	// 	Video frame variables
	private int algFrameRate = 20;
	private int totalFrames = FramePositioningControl.FRAME_UNKNOWN;
	private Buffer buffy; // Image manipulation
	private JButton button;
	private Vector buttons = new Vector();
	private Vector index = new Vector();
	private int segmentNum = 0;

	// ThreadSafe variables
	private Object waitSync = new Object();
	private boolean stateTransitionOK = true;

	// Dimension and look and feel variables
	private Dimension d;
	private int dw, dh;
	private Font verdana10pt = new Font("Verdana",Font.BOLD,10);
	private Font verdana9pt = new Font("Verdana",Font.BOLD,9);
	private Font verdana9Ppt = new Font("Verdana",Font.PLAIN,9);
	private Font helvetica9pt = new Font("Helvetica",Font.BOLD,9);

	// Swing variables
	private JButton openFile, playFile;
	private JPanel mainP, topP, buttonP, infoP, detailsP, newResultsP, timeLineP;
	private JTextArea showFile, showTime, showFrames, showResults;
	private final int XCOORDINATE = 5;
	private final int YCOORDINATE = 5;
	private final int winWIDTH = 295;

	// File and Main Browser class variables
	private File file;
	private VideoBrowser vb;

	// File reader stuff
	private FileReader reader;
	private BufferedReader in;
	private Vector readInFrames = new Vector();
	private Vector extractedFrames = new Vector();
	private JProgressBar progressBar;
	private TimeChart tc;

	/**
	Constructor, creating the internal browseWindow.
	@param vb, a VideoBrowser object.
	@param inset, an int to determine screensize and frame location
	*/
	public BrowseWindow(VideoBrowser vb, int inset)
	{
		super("Video Browser",
			true, //resizable
			true, //closable
			false, //maximizable
			true);//iconifiable

		this.vb = vb;
		//Manager.setHint(Manager.PLUGIN_PLAYER, new Boolean(true));


		/***********************************************************
		Swing Stuff
		***********************************************************/

		// Create the overall panel
		mainP = new JPanel(new BorderLayout());

		// Create the top panel for the buttons
		topP = new JPanel(new GridLayout(2,1,0,0));

		// Create the two buttons
		openFile = new JButton( "Open index file" );
		openFile.addActionListener(this);
		openFile.setPreferredSize(new Dimension(75,20));
		openFile.setMinimumSize(new Dimension(75,20));
		playFile = new JButton( "Play video" );
		playFile.addActionListener(this);
		playFile.setEnabled(false); // Disable until file opened

		// Add buttons to top panel
		topP.add(openFile);
		topP.add(playFile);

		// Create the results panel for timeLine and segments
		newResultsP = new JPanel(new BorderLayout());

		// Create the timeLine panel
		timeLineP = new JPanel(new BorderLayout());
		timeLineP.setMaximumSize(new Dimension(100,50));
		timeLineP.setPreferredSize(new Dimension(100,45));
		timeLineP.setMinimumSize(new Dimension(100,40));
		Border etched = BorderFactory.createEtchedBorder();
		TitledBorder etchedBorder = BorderFactory.createTitledBorder(
			etched, "Video time-line");
		etchedBorder.setTitleFont(verdana10pt);
		timeLineP.setBorder(etchedBorder);

		// Create the segment button panel with JScrollPane
		buttonP = new JPanel(new GridLayout(0,3,0,0));
		JScrollPane jpane = new JScrollPane(buttonP);
		jpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		// Create a JScrollBar for the vertical part of the JScrollPane
		JScrollBar vertJBar = jpane.getVerticalScrollBar();
		// Set the scroll button click increment to the size of the main buttons
		vertJBar.setUnitIncrement(70);

		// Add panels to results panel
		newResultsP.add(timeLineP, BorderLayout.NORTH);
		newResultsP.add(jpane);

		// Create a data feedback panel
		detailsP = new JPanel(new GridLayout(2,1,0,0));

		// Create the information feedback panel
		infoP = new JPanel(new BorderLayout());

		// Create the File Name text area
		showFile = new JTextArea();
		showFile.setText("");
		showFile.setFont(verdana10pt);
		showFile.setBackground(Color.lightGray);
		showFile.setMaximumSize(new Dimension(100,17));
		showFile.setPreferredSize(new Dimension(100,17));
		showFile.setMinimumSize(new Dimension(100,17));

		// Create the File Time text area
		showTime = new JTextArea();
		showTime.setText("");
		showTime.setFont(verdana10pt);
		showTime.setBackground(Color.lightGray);

		// Create the File noFrames text area
		showFrames = new JTextArea();
		showFrames.setText("");
		showFrames.setFont(verdana10pt);
		showFrames.setBackground(Color.lightGray);

		// Add text areas to the information feedback panel
		infoP.add(showFile, BorderLayout.WEST);
		infoP.add(showTime, BorderLayout.CENTER);
		infoP.add(showFrames, BorderLayout.EAST);

		// Create a progressBar for feedback on loading images
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setFont(verdana9pt);
		progressBar.setMaximumSize(new Dimension(100,2));
		progressBar.setPreferredSize(new Dimension(100,2));
		progressBar.setMinimumSize(new Dimension(100,2));

		// Add information and progressBar to data feedback panel
		detailsP.add(infoP);
		detailsP.add(progressBar);

		// Add each higher panel to the main panel
		mainP.add(topP, BorderLayout.NORTH);
		mainP.add(newResultsP);
		mainP.add(detailsP, BorderLayout.SOUTH);

		// Add to the internal frame
		getContentPane().add(mainP);

		/***********************************************************
		End of swing Stuff
		***********************************************************/

		// Code to get screen resolution and resize browser to fit perfectly
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(winWIDTH,screenSize.height - (inset * 2 + 90));
		setLocation(XCOORDINATE, YCOORDINATE);

	} // end Constructor()


	/**
	Method to convert MediaLocator to DataSource from a File object.
	Takes the TXT file object, creates a temp AVI file object from the
	TXT object (converting the extension).
	Then converts this to a MediaLocator object (for the actual video).
	Then converts this to a DataSource object (for the actual video).
	Then reads the txt file data into a Vector of strings.
	Sets the player with the actual video and the feedback info.
	@param file, A File object.
	 */
	public void MediaLocatorTOdatasource(File file)
	{
		// Create the file object....
		File temp = new File(SpawnyUtils.alterTXTtoAVI(file.toString(),3));

		// ....Convert to MediaLocator object
		try
		{
			ml = new MediaLocator(temp.toURL());
		}
		catch(MalformedURLException mue)
		{
		}

		// ....Convert to DataSource object
		try
		{
			ds = Manager.createDataSource(ml);
		}
		catch (Exception e)
		{
			System.err.println("Cannot create DataSource from: " + file);
			System.exit(0);
		}

		// Reads the contents of the text file into a Vector
		try
		{
			reader = new FileReader(file);
			in = new BufferedReader(reader);
			String inputLine1;

			// If the data file is empty....
			if (in.readLine() == null)
			{
				// Show warning dialog box
				JOptionPane.showInternalMessageDialog(this,
					"No Data is available, please index the video first");

				// If scanWindow not already open, open it
				if (vb.scanOpen == false)
				{
					vb.createScanWindow(vb);
				}
				// else set the scanWindow to the front
				else vb.JIFsetSelected(vb.sframe, true);
			}
			// ....else data ready for reading in
			else
			{
				while(!(inputLine1 = in.readLine()).equals("</shot>"))
				{
					readInFrames.add(inputLine1);
				}
				reader.close();
			}
		}
		catch (java.io.IOException ioe)
		{
			System.out.println("No data available, please index the video");
		}

		// Adds the DataSource object to the JMF code (creates player etc.)
		browseAVI(ds);
		// Adds the file info to the info feedback panel
		SpawnyUtils.fillFileNameTextArea(showFile, file.toString(), '\\');

	} // end MediaLocatorTOdatasource()


	/**
	Method to extract the frame information from the Vector
	containing all the TXT file input data. Only reads the Strings that
	relate to ints for the frame data.
	*/
	public void extractCorrectFrames()
	{
		// First five lines contain all the into information
		for(int i = 4; i < readInFrames.size(); i ++)
		{
			// Get each frame data from first input
			extractedFrames.add((String)readInFrames.get(i));
		}

	} // end extractCorrectFrames()



	/**
	Method to create player, realize and prefetch.
	@param ds, a JMF DataSource object.
	@return boolean, true if player creation, realization and prefetching
	is OK.
	*/
	public boolean browseAVI(DataSource ds)
	{
		System.err.println("create player for: " + ds.getContentType());
		try
		{
			p = Manager.createPlayer(ds);
		}
		catch (Exception e)
		{
			System.err.println("Failed to create a player from the given DataSource: " + e);
			return false;
		}

		p.addControllerListener(this);

		// Realize the player
		p.realize();
		if (!waitForState(p.Realized))
		{
			System.err.println("Failed to realize the player.");
			return false;
		}
		// Prefetch the player.
		p.prefetch();
		if (!waitForState(p.Prefetched))
		{
			System.err.println("Failed to prefetch the player.");
			return false;
		}


		// Try to retrieve a FramePositioningControl from the player.
		fpc = (FramePositioningControl)p.getControl("javax.media.control.FramePositioningControl");
		// Try to retrieve a FrameGrabbingControl from the player.
		fgc = (FrameGrabbingControl)p.getControl("javax.media.control.FrameGrabbingControl");

		// Only applies if the video format cannot accept FPC
		if (fpc == null)
		{
			System.err.println("The player does not support FramePositioningControl.");
			System.err.println("There's no reason to go on for the purpose of this demo.");
			return false;
		}

		// Calculate time duration of video
		Time duration = p.getDuration();
		if (duration != Duration.DURATION_UNKNOWN)
		{
			// If longer than 60 secs, add time to feedback panel
			if (duration.getSeconds() > 60)
			{
				int mins = (int)duration.getSeconds() / 60;
				int secs = (int)duration.getSeconds() % 60;
				showTime.setText(mins + " min " + secs + " sec");
			}
			// If less than 60 secs, only display secs in feedback panel
			else
			{
				showTime.setText(" " + (int)duration.getSeconds() + " secs");
			}

			// Calculate the total number of frames for the video
			totalFrames = fpc.mapTimeToFrame(duration);
			showFrames.setText("Frames: " + totalFrames + " ");

			// Only applies if the video has no frames!
			if (totalFrames != FramePositioningControl.FRAME_UNKNOWN)
				System.err.println("Total # of video frames in the movies: " + totalFrames);
			else
				System.err.println("The FramePositiongControl does not support mapTimeToFrame.");
		}
		else
		{
			System.err.println("Movie duration: unknown");
		}


		// Calculate visual component size and height for segment player
		v = p.getVisualComponent();
		d = v.getPreferredSize();
		dw = d.width;
		dh = d.height;
		return true;

	} // end browseAVI()


	/**
	Block until the player has transitioned to the given state.
	Return false if the transition failed.
	@param state, an int produced by JMF Controller getState()
	@return boolean, true if getState() is realised/prefetched
	*/
	boolean waitForState(int state)
	{
		synchronized (waitSync)
		{
			try
			{
				while (p.getState() < state && stateTransitionOK)
					waitSync.wait();
			} catch (Exception e)
			{
			}
		}
		return stateTransitionOK;

	} // end waitForState()


	/**
	Method to create the different segment buttons from the indexed file.
	Initiallt extracts the actual frames from the first Vector, and
	sets the progress bar maximum to the numver of segments required.
	For each segment
	Seek to frame in video
	Calculate the time of that frame
	Grab frame
	Set frame image to button
	Update progress bar
	*/
	public void createSegmentButtons()
	{
		// Get actual frames and set progress bar maximum
		extractCorrectFrames();
		progressBar.setMaximum(extractedFrames.size());

		// Loop for every frame in list
		for(int i = 0; i < extractedFrames.size(); i ++)
		{
			// Seek to frame i
			int j = Integer.parseInt((String)extractedFrames.get(i));
			fpc.seek(j);

			// Work out rough time value
			Time startT = fpc.mapFrameToTime(j);
			int roughStartTime = (int)startT.getSeconds();
			int roughMins = roughStartTime/60;
			int roughSecs = roughStartTime%60;
			String roughTime = null;
			if (roughSecs < 10)
			{
				roughTime = (roughMins + ":0" + roughSecs);
			}
			else roughTime = (roughMins + ":" + roughSecs);

			// Prepare and then grab
			buffy = fgc.grabFrame();

			// Create button and add time and image (latter from static method)
			button = new JButton(roughTime, SpawnyUtils.returnImageIcon(buffy));

			// Set button size, layout and add listener
			button.setPreferredSize(new Dimension(60,70));
			button.setMaximumSize(new Dimension(60,70));
			button.setMinimumSize(new Dimension(60,70));
			button.addActionListener(new ButtonListener());
			button.setVerticalTextPosition(AbstractButton.TOP);
			button.setHorizontalTextPosition(AbstractButton.CENTER);
			button.setFont(verdana9Ppt);
			button.setToolTipText("Segment " + (i + 1) + ", Frame " + j);
			button.repaint();

			// add to buttons Vector
			buttonP.add(button);
			buttons.add(button);
			index.add(new Integer(j));

			// Update progressBar
			progressBar.setValue(i);
			progressBar.update(progressBar.getGraphics());

			// Update number of segments grabbed
			segmentNum ++;

		}
		// Update progress bar
		progressBar.setValue(extractedFrames.size());
		progressBar.update(progressBar.getGraphics());

	} // end createSegmentButtons()


	/**
	Method to clear existing progressbar, keyframe buttons and timeline
	data if loading another file.
	*/
	public void clearAllLoadedData()
	{
		// Clear vectors
		if (readInFrames.size() > 0)
		{
			readInFrames.clear();
			extractedFrames.clear();
			index.clear();
			buttons.clear();
		}
		// Reset progressBar and empty panels
		progressBar.setValue(0);
		buttonP.removeAll();
		timeLineP.removeAll();

	} // end clearAllLoadedData()


	/**
	ActionListener for open and play buttons
	@param event, the response of either button.
	*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == openFile)
		{
			// Loase the data already loaded
			clearAllLoadedData();
			// Get the file
			file = SpawnyUtils.returnTXT(vb);
			// Set the dataSource
			MediaLocatorTOdatasource(file);
			// Create the keyframe buttons
			createSegmentButtons();
			// Enable the play button
			playFile.setEnabled(true);
			// Create and add the timeLine panel
			tc = new TimeChart(extractedFrames, totalFrames);
			timeLineP.add(tc, BorderLayout.CENTER);
		}
		else if (event.getSource() == playFile)
		{
			// Create the whole video player window
			WholeVideoPlayer wvp = new WholeVideoPlayer(ml, dw, dh,
				SpawnyUtils.reducePath(ml.toString(),4, '/'));
			vb.addOtherFrame(wvp);

		} // end if/else

	} // end actionPerformed()


	/**
	Private inner class Listener for keyframe buttons.
	*/
	private class ButtonListener implements ActionListener
	{
		public void actionPerformed (ActionEvent event)
		{
			for (int i = 0; i < buttons.size(); i ++)
			{
				// Action on the segment buttons
				if (event.getSource() == (JButton)buttons.get(i))
				{
					tc.setRedLineSegment(i);
					System.out.println("You accessed frame button " + (i + 1));
					System.out.println("You accessed frame index " +
						(Integer)index.get(i));
					SegmentPlayer seg;

					/* Test to see if only one element in index or
					if accessing the last button in the array
					(avoids out of range errors)*/
					if (index.size() == 1 || i == index.size() - 1)
					{
						seg = new SegmentPlayer(ml, dw, dh,
							(Integer)index.get(i),
							new Integer(totalFrames),
							totalFrames);
					}
					else
					{
						seg = new SegmentPlayer(ml, dw, dh,
							(Integer)index.get(i),
							(Integer)index.get(i + 1),
							totalFrames);

					}
					// Add segment to VideoBrowser window
					vb.addOtherFrame(seg);

				} // end if/else

			} // end for
		}
	} // end ButtonListener()


	/**
	Controller Listener for JMF elements.
	*/
	public void controllerUpdate(ControllerEvent evt)
	{
		if (evt instanceof ConfigureCompleteEvent ||
			evt instanceof RealizeCompleteEvent ||
			evt instanceof PrefetchCompleteEvent)
		{
			synchronized (waitSync)
			{
				stateTransitionOK = true;
				waitSync.notifyAll();
			}
		}
		else if (evt instanceof ResourceUnavailableEvent)
		{
			synchronized (waitSync)
			{
				stateTransitionOK = false;
				waitSync.notifyAll();
			}

		} // end if/else

	} // end controllerUpdate()


} // end BrowseWindow class











