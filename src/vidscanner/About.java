package vidscanner;
/**
	A class for the construction of an about window, 
	inherits from Help.
	@author Iain Downie: MSc IT 2000-2001 Summer Project.
 */
public class About extends Help
{	
	// Window size and location constants
	private final int WIDTH = 200;
	private final int HEIGHT = 220;
	private final int XCOORDINATE = 430;
	private final int YCOORDINATE = 150;
	
	
	// The About homepage location
	private final String aboutURL = "file:" 
			+ System.getProperty("user.dir")
			+ System.getProperty("file.separator") 
			+ "htmlfiles/About.html";
	
	
	/**
	Constructor for the About window. 
	Inherits from Help so little detail needed.
	*/
	public About()
	{
		// Add to contentPane, no Navigation Panel needed
		getContentPane().add(mkHelpPanel(true, false, aboutURL));
		System.out.println(aboutURL);
		// Force the correct title (ie. not help)
		setTitle("About");
		
		// Set suitable size a location
		setSize(WIDTH,HEIGHT);
		setLocation(XCOORDINATE,YCOORDINATE);
	
	} // end About()

} // end About class







