package vidscanner;
import javax.swing.JInternalFrame;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.media.*;
import javax.media.Player;
import java.io.*;


/**
	The WholeVideoPlayer class which implements the playback of
	the whole video file. Accessed from VideoBrowser directly
	or through BrowseWindow. Has two overloaded constructors.
	@author Iain Downie: MSc IT 2000-2001 Summer Project.
*/
public class WholeVideoPlayer extends JInternalFrame
{
	// Declare player variable
	private Player player;
	
	// Declare frame positioning variables
	static int openFrameCount = 0;
	static final int xOffset = 100, yOffset = 40;


	/**
	Constructor A for the WholeVideoPlayer. This version does not
	know the size of the JMF visual component, so uses pack() initially
	but resizes later. Called from VideoBrowser.
	@param ml, a MediaLocator object (JMF).
	*/
	public WholeVideoPlayer(MediaLocator ml)
	{
		// JInternalFrame attributes
		super(" " + (++openFrameCount), 
			true, //resizable
			true, //closable
			true, //maximizable
			true);//iconifiable
		
		// Create the Player
		createPlayer(ml);

		// Perform an initial pack(), but size determined later
		pack();

		// Set title and location
		setTitle(SpawnyUtils.reducePath(ml.toString(),4, '/'));	
		setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
		validate();
		
	} // end Constructor A


	/**
	Constructor B for the WholeVideoPlayer. This version takes size
	attributes and title from BrowseWindow directly,
	@param ml, a MediaLocator object (JMF).
	@param dw, value for the window width.
	@param dh, value for the window height.
	@param s, a string for the title.
	*/
	public WholeVideoPlayer(MediaLocator ml, int dw, int dh, String s)
	{
		// JInternalFrame attributes
		super(s + " " + (++openFrameCount), 
			true, //resizable
			true, //closable
			true, //maximizable
			true);//iconifiable

		// Create the Player
		createPlayer(ml);

		// Set size and location, predetermined
		setSize(dw + 10,dh + 50);
		setLocation(350, 60);
		validate();
	
	} // end Constructor B


	/**
	Method to create player
	@param ml, a MediaLocator object
	*/
	public void createPlayer(MediaLocator ml)
	{
		try
		{
			// Make player from MediaLocator object
			player = Manager.createPlayer(ml);
			
			// Add listener
			player.addControllerListener( new EventHandler() );

			// JMF fast-track to playing video (avoids prefetching etc.)
			player.start(); 
		}
		catch ( Exception e )
		{
			JOptionPane.showMessageDialog( this,
				"Invalid file or location", "Error loading file",
				JOptionPane.ERROR_MESSAGE );
		}
	
	} // end createPlayer()


	/**
	Inner class to handler events from media player
	*/
	private class EventHandler implements ControllerListener {
		public void controllerUpdate( ControllerEvent e )
		{
			// If realizing has completed....
			if ( e instanceof RealizeCompleteEvent )
			{
				// Create swing container and size variable
				Container c = getContentPane();
				Dimension d;

				// load Visual and Control components if they exist
				Component vComponent = player.getVisualComponent();

				if ( vComponent != null )
				{
					// Add visuals to container and reset frame size
					c.add( vComponent, BorderLayout.CENTER );
					d = vComponent.getPreferredSize();
					setSize(d.width + 10,d.height + 50);
				}

				Component cComponent = player.getControlPanelComponent();

				if ( cComponent != null )
				{	
					// Add controls to container
					c.add( cComponent, BorderLayout.SOUTH );
				}

				c.validate(); // ensure both components appear
			}
			// If video reaches the end of the whole data
			if (e instanceof EndOfMediaEvent)
			{
				// Use this line if you want the vid to return to time zero
				//player.setMediaTime(new Time(0));
				
				// Use this line to close after viewing (neater)
				player.close();
			}
		}
	
	} // end EventHandler class

} // end WholeVideoPlayer class





