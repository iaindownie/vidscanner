package vidscanner;

import javax.swing.*;
import java.awt.*;
import javax.media.Buffer;
import javax.media.util.BufferToImage;
import javax.media.format.VideoFormat;
import javax.swing.JFileChooser;
import java.io.File;
import javax.media.MediaLocator;
import java.net.MalformedURLException;

/**
	A class containing a collection of static methods/utilities.
	@author Iain Downie: MSc IT 2000-2001 Summer Project.
*/
public class SpawnyUtils
{
	private static MediaLocator ml;
	private static File file;


	/**
	A method to forcably invoke the garbage collector.
	Not actually used, but useful to keep in utilities.
	*/
	public static void garbageCollection()
	{
		Runtime rt = Runtime.getRuntime();
		rt.gc();
		long mem = rt.freeMemory();
		System.out.println("Free memory = " + mem);

	} // end garbageCollection()


	/**
	A method to set an imageIcon from a framegrab.
	@param buffy, a Buffer object.
	@return icon, an ImageIcon.
	*/
	public static ImageIcon returnImageIcon(Buffer buffy)
	{
		BufferToImage bti = new BufferToImage((VideoFormat)buffy.getFormat());
		Image im = bti.createImage(buffy);
		Image im2 = im.getScaledInstance(58,-6,Image.SCALE_DEFAULT);
		ImageIcon icon = new ImageIcon(im2);
		return icon;

	} // end returnImageIcon()


	/**
	A method to set an Image from a framegrab.
	@param buffy, a Buffer object.
	@return im, an Image.
	*/
	public static Image returnImage(Buffer buffy)
	{
		BufferToImage bti = new BufferToImage((VideoFormat)buffy.getFormat());
		Image im = bti.createImage(buffy);
		return im;

	} // end returnImage()


	/**
	A method to return a MediaLocator object using FileChooser.
	Filter designed for avi, mov and mpg file formats.
	@param component, a component for the FileChooser dialog to adhere to.
	@return ml, a JMF object.
	*/
	public static MediaLocator returnML(Component component)
	{
		JFileChooser fileChooser = new JFileChooser();
		//fileChooser.setCurrentDirectory(new File("h:/downieis/"));
		fileChooser.setCurrentDirectory(new File("/Users/Iain"));
		fileChooser.addChoosableFileFilter(new SpawnyVideoFilters());
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );

		int result = fileChooser.showOpenDialog(component);

		if ( result == JFileChooser.CANCEL_OPTION )
			file = null;
		else file = fileChooser.getSelectedFile();

		try
		{
			ml = new MediaLocator(file.toURL());
		}
		catch(MalformedURLException mue)
		{
			System.out.println("No file selected");
		}

		return ml;

	} // end returnML()


	/**
	A method to return a File object using FileChooser.
	Filter designed for txt file format.
	@param component, a component for the FileChooser dialog to adhere to.
	@return file, in this case a text file.
	*/
	public static File returnTXT(Component component)
	{
		JFileChooser fileChooser = new JFileChooser();
		//fileChooser.setCurrentDirectory(new File("h:/downieis/"));
		fileChooser.setCurrentDirectory(new File("/Users/Iain"));
		fileChooser.addChoosableFileFilter(new SpawnyTextFilters());
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );

		int result = fileChooser.showOpenDialog(component);

		if ( result == JFileChooser.CANCEL_OPTION )
			file = null;
		else file = fileChooser.getSelectedFile();

		return file;

	} // end returnTXT()


	/**
	Method to set the feedback information panel with the video name
	@param filedata, a JTextArea containing the file data
	@param fileName, a String of the full file path
	@param divider, a char for the standard \ or / pathway dividers
	*/
	public static void fillFileNameTextArea(JTextArea filedata, String fileName, char divider)
	{
		// Set text area size
		filedata.setMaximumSize(new Dimension(30,10));
		// Set actual text using another static method
		filedata.setText("" + reducePath(fileName, 4, divider) + "  ");

	} // end fillFileNameTextArea()



	/**
	Method to take a long filepath and extract then display the filename
	The int allows the definition of the extension (4 for html, 0 for nowt)
	@param filename, a string of the entire file pathway,
	@param fromEnd, defines how many chars the extension has (e.g. .avi or .html),
	@param slash, a char based on the path, either / or \
	@return s1, a String.
	*/
	public static String reducePath(String filename, int fromEnd, char slash)
	{
		int score = filename.lastIndexOf(slash);
		StringBuffer sb = new StringBuffer(filename);
		sb.delete(0,score + 1);
		sb.delete(sb.length() - fromEnd, sb.length());
		String s1 = sb.toString();
		return s1;

	} // reducePath()


	/**
	Method to alter a video file path to a text file path. Used in filewriting.
	The int allows the definition of the extension (4 for html, 0 for nowt)
	@param filename, a string of the entire file pathway,
	@param fromEnd, defines how many chars the extension has (e.g. .avi or .html),
	@return si, a String with .txt attached.
	*/
	public static String alterAVItoTXT(String filename, int fromEnd)
	{
		StringBuffer sb = new StringBuffer(filename);
		int len = sb.length();
		sb.delete(0,6); // removes the file:/ from the start
		sb.delete(sb.length() - fromEnd, sb.length());
		sb.append("txt");
		String s1 = sb.toString();
		return s1;

	} // end alterAVItoTXT()


	/**
	Method to alter a text file path to a video file path. Used in filereading.
	The int allows the definition of the extension (4 for html, 0 for nowt)
	@param filename, a string of the entire file pathway,
	@param fromEnd, defines how many chars the extension has (e.g. .avi or .html),
	@return si, a String with .avi attached.
	*/
	public static String alterTXTtoAVI(String filename, int fromEnd)
	{
		StringBuffer sb = new StringBuffer(filename);
		int len = sb.length();
		sb.delete(sb.length() - fromEnd, sb.length());
		sb.append("avi");
		String s1 = sb.toString();
		return s1;

	} // end alterTXTtoAVI()



	/**
	Method to set the Look and Feel to Native system.
	*/
	public static void setNativeLookAndFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
			System.out.println("Error setting native L&F: " + e);
		}
	} // end setNativeLookAndFeel()


	/**
	Method to set the Look and Feel to Java system.
	*/
	public static void setJavaLookAndFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}
		catch(Exception e)
		{
			System.out.println("Error setting Java L&F: " + e);
		}
	} // end setJavaLookAndFeel()


	/**
	Method to set the Look and Feel to Motif system.
	*/
	public static void setMotifLookAndFeel()
	{
		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
		}
		catch(Exception e)
		{
			System.out.println("Error setting Motif L&F: " + e);
		}
	} // end setMotifLookAndFeel()

} // end SpawnyUtils class










