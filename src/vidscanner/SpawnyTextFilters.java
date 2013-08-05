package vidscanner;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
	A class for the filtering out non .txt files
	@author Iain Downie: MSc IT 2000-2001 Summer Project.
 */
public class SpawnyTextFilters extends FileFilter
{

	// Set the String to required file extension type
	public final static String txt = "txt";

	
	/**
	Method to get the extension of the file
	@param f, a File object
	@return a String of the file extension
	 */
	public static String getExtension(File f)
	{
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1)
		{
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	
	} // end getExtension
	
	/**
	Method to get accept the extension
	@param f, a File object
	@return true if extention equals .txt
	 */
	public boolean accept(File f)
	{
		// Tests whether the file is a directory.
		if (f.isDirectory())
		{
			return true;
		}
		
		// Use get extension method
		String extension = getExtension(f);
		if (extension != null)
		{
			if (extension.equals(txt))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		return false;
	
	} // end accept()

	// The description of this filter
	public String getDescription()
	{
		return "Text files (.txt)";
	
	} // end getDescription()

} // end SpawnyTextFilters class


