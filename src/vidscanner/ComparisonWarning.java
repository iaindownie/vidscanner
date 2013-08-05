package vidscanner;
//package scanner; // Include in Scanner package

import javax.swing.*;
import java.awt.*;


/**
	A class creating an internal frame as a warning that Comparator
	operations are still being carried out.Inherits JInternalFrame.
	@author Iain Downie: MSc IT 2000-2001 Summer Project.
 */
public class ComparisonWarning extends JInternalFrame
{
	// Variables for panel and labels
	private JPanel warnP;
	private String warnL, closeL;
	private JLabel warningLabel, closeLabel;
	
	
	/**
	Constructor, creating the internal warning frame. 
	*/ 
	public ComparisonWarning()
	{
		// The parent constructor
		super("Comparing Images");	// No window attibutes specified
		
		// Create the layout for the window	
		warnP = new JPanel(new GridLayout(2,1,0,0));
		warnL =  "You are comparing images, which may take some time";
		closeL = "This window will close automatically";
		warningLabel = new JLabel(warnL, JLabel.CENTER);
		closeLabel = new JLabel(closeL, JLabel.CENTER);
		
		// Add the labels to the panel, then add to contentpane
		warnP.add(warningLabel);
		warnP.add(closeLabel);
		getContentPane().add(warnP);
		
		// Set size and location
		setSize (350,100);
		setLocation(100, 70);
	
	} // end Constructor()   

} // end ComparisonWarning class       
