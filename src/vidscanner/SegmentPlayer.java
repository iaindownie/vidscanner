package vidscanner;
//package browser;  // Include in Browser package

import javax.swing.JInternalFrame;
import javax.swing.*;
import java.awt.event.*;
import javax.media.control.FramePositioningControl;
import java.awt.*;
import javax.media.*;
import javax.media.Player;
import java.io.*;
import java.net.MalformedURLException;

/**
	The SegmentPlayer class which implements the playback of
	any chosen segment. Accessed directly through BrowseWindow.
	@author Iain Downie: MSc IT 2000-2001 Summer Project.
*/
public class SegmentPlayer extends JInternalFrame
{
	
	// Declare JMF variables for player, seeking and time
	private Player player;
	private FramePositioningControl fpc;
	private Time duration;
	
	// Declare time and int variables for comparing time with frames
	private Time startT, endT;
	private int startF, endF, allF;
	

	/**
	Constructor for the SegmentPlayer.
	@param ml, a MediaLocator object (JMF).
	@param dw, an int for frame width.
	@param dh, an int for frame height.
	@param begin, an Integer for the start of the video.
	@param stop, an Integer for the end of the video.
	@param totalFrames, an int of the total number of frames..
	*/
	public SegmentPlayer(MediaLocator ml, int dw, int dh, Integer begin, 
										  Integer stop, int totalFrames)
	{
		super("Frames: " + begin + " - " + stop, 
			true, //resizable
			true, //closable
			false, //maximizable
			true);//iconifiable
		
		// Convert the Integers to ints for the frame numbers
		startF = begin.intValue();
		if (stop.intValue() == 0)
			endF = totalFrames;
		else endF = stop.intValue();	
				
		// Create the player method call taking the MediaLocator
		createPlayer(ml);
		
		// Set size and location
		setSize(dw + 10,dh + 50);
		setLocation(315, 210);
		
		// System.outs for comparing window with actual visual component
		Dimension d = getSize();
		System.err.println("Window: " + d.width + "x" + d.height);
		System.err.println("Vid: " + dw + "x" + dh);
	
	} // end Constructor()


	/**
	Method to create player and realize
	@param ml, a MediaLocator object
	*/
	private void createPlayer(MediaLocator ml)
	{
		// Create and realize player
		try
		{
			player = Manager.createPlayer(ml);
			player.addControllerListener( new EventHandler() );
			player.realize(); 
			//player.prefetch();    // Not really necessary
		}
		catch ( Exception e )
		{
			JOptionPane.showMessageDialog( this,
				"Invalid file or location", "Error loading file",
				JOptionPane.ERROR_MESSAGE );
		}
	
	} // end createPlayer()
	
	
	/**
	Method to determine segment limits 
	@param begin, the starting frame number.
	@param end, the end frame number.
	*/
	private void segmentLimits(int begin, int end)
	{
		// Set the JMF control for manipulating time
		fpc = (FramePositioningControl)player.getControl("javax.media.control.FramePositioningControl");
		// Map the start and end frames to times
		startT = fpc.mapFrameToTime(begin);
		endT = fpc.mapFrameToTime(end);
	
	} // end segmentLimits()
	

	/**
	Inner class to handler events from media player
	*/
	private class EventHandler implements ControllerListener {
		public void controllerUpdate( ControllerEvent e )
		{
			// If realizing has completed....
			if ( e instanceof RealizeCompleteEvent)
			{
				// Create swing container
				Container c = getContentPane();

				// load Visual and Control components if they exist
				Component visualComponent = player.getVisualComponent();

				// Add visuals to container
				if ( visualComponent != null )
					c.add( visualComponent, BorderLayout.CENTER );

				Component controlsComponent =
					player.getControlPanelComponent();

				// Add controls to container
				if ( controlsComponent != null )
					c.add( controlsComponent, BorderLayout.SOUTH );

				c.validate(); // ensure both components appear
				
				// Fastforward controls to start, stopping at correct point
				segmentLimits(startF, endF);
				System.out.println("Start: " + startF + " End: " + endF);
				player.setMediaTime(startT);
				player.start();
				player.setStopTime(endT);
			}
			// If video reaches the end of the whole data
			if (e instanceof EndOfMediaEvent)
			{
				// Return player to time zero
				player.setMediaTime(new Time(0));
			}
		
		} // end controllerUpdate()
	
	} // end EventHandler()

} // end SegmentPlayer class




