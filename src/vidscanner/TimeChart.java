package vidscanner;
//package browser;  // Include in Browser package

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;


/**
	A class for the construction of a timechart JPanel which 
	shows the user where the segment exists in video space.
	Contains eventhandlers to cater for moving the mouse over
	the time-line panel, and responds to segment playing with
	red line representing playing segment.
	@author Iain Downie: MSc IT 2000-2001 Summer Project.
*/
public class TimeChart extends JPanel implements MouseMotionListener
{
	// Variables and constants
	private final int XORG = 1;			// The x-origin
	private final int YORG = 0;			// The y-origin
	private final int TLWIDTH = 271;	// The timeline width
	private final int TLHEIGHT = 14;	// The timeline height
	private final int SENSITIVITY = 2;	// Value for the mouse to use
	private Vector indexedFrames;		// Vector for extraced frames
	private int allFrames;				// The total number of frames
	private boolean redLine = false;	// Whether or not red line is set
	private int whichLine, translatedLine;	// Vars for setting the red line	

	// Suitable colours for the timeline
	private Color baseColor = new Color(0.9F,0.9F,0.8F);
	private Color frameColor = new Color(0.3F,0.3F,0.8F);
	
	/**
	Constructor for the browser video timeline.
	@param extractedFrames, a Vector of extracted frames as numbers.
	@param totalFrames, an int of the total number of frames.
	*/
	public TimeChart(Vector extractedFrames, int totalFrames)
	{
		indexedFrames = extractedFrames;	// pass over the extracted frames
		allFrames = totalFrames;			// pass over the total no. frames
		addMouseMotionListener(this);

	} // end Timechart constructor
	
	
	/**
	This method finds the right line for the segment chosen.
	@param seg, an int corresponding to the segment playing.
	*/
	public void setRedLineSegment(int seg)
	{
		redLine = true;
		whichLine = Integer.parseInt((String)indexedFrames.get(seg));
		translatedLine = (TLWIDTH * whichLine)/allFrames;
	
	} // end setRedLineSegment()
	
	
	/**
	This method paints the TimeLine panel.
	@param g, a Graphic object.
	*/
	public void paintComponent(Graphics g)
	{	
		super.paintComponent(g);
		g.setColor(baseColor);
		g.fillRect(XORG,YORG,TLWIDTH,TLHEIGHT);
		g.setColor(frameColor);
		for (int framePos = 0; framePos < indexedFrames.size(); framePos ++)
		{
			int temp = Integer.parseInt((String)indexedFrames.get(framePos));
			int linePos = (TLWIDTH * temp)/allFrames;
			g.drawLine(linePos,YORG,linePos,TLHEIGHT);
		}
		if (redLine == true)
		{
			g.setColor(Color.red);
			g.drawLine(translatedLine,YORG,translatedLine,TLHEIGHT);
		}

	} // end paintComponent()


	/**
	Private method to compare the mouse position with the elements
	in the vector holding the segment frame position info.
	@param pos, the x-position returned from e.getX().
	@return framePos or -1 depending on sensitivity of mouse.
	*/
	private int segmentLocator(int pos)
	{	
		for (int framePos = 0; framePos < indexedFrames.size(); framePos ++)
		{
			int temp = Integer.parseInt((String)indexedFrames.get(framePos));
			int linePos = (TLWIDTH * temp)/allFrames;

			if (pos >= (linePos - SENSITIVITY) && pos <= (linePos + SENSITIVITY))
				return framePos;
		}
		return -1;

	} // end segmentLocator()
	
	
	/**
	EventHandler for the mouse moving over the time-line.
	Calculate the cursor position in relation to the video length and
	automatically adjusts for if hovering over a segment line or not.
	@param e, a mouseEvent.
	*/
	public void mouseMoved(MouseEvent e)
	{
		// Create a tool tips manager
		ToolTipManager toolTipManager = ToolTipManager.sharedInstance();        
		// Time to wait before showing tooltip when mouse lingers over time line      
		toolTipManager.setInitialDelay(10);  // 10 millieseconds only
		// Get mouse position
		int pos = e.getX();
		// Find the right segment
		int temp = segmentLocator(pos);
		// If hovering over a segment bar.....
		if (temp > -1) 
			setToolTipText("Segment " + (temp + 1));
		// .....else hovering over frame space
		else	
			setToolTipText("Frame " + (((pos - 1) * allFrames) / TLWIDTH) 
				+ " from " + allFrames);
	
	} // end mouseMoved()


	/**
	EventHandler for the mouse dragging over the time-line.
	No method body specified, but inclusion necessary for interface.
	@param e, a mouseEvent.
	*/
	public void mouseDragged(MouseEvent e)
	{
	} // end mouseDragged()

} // end TimeChart class
