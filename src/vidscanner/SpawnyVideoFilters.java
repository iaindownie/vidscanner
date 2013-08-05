package vidscanner;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
 A class for the filtering out non .avi files
 @author Iain Downie: MSc IT 2000-2001 Summer Project.
 */
public class SpawnyVideoFilters
    extends FileFilter {

  // Set the String to required file extension type
  public final static String avi = "avi";
  public final static String mpg = "mpg";
  public final static String mov = "mov";

  /**
    Method to get the extension of the file
    @param f, a File object
    @return a String of the file extension
   */
  public static String getExtension(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');

    if (i > 0 && i < s.length() - 1) {
      ext = s.substring(i + 1).toLowerCase();
    }
    return ext;

  } // end getExtension()

  /**
    Method to get accept the extension
    @param f, a File object
    @return true if extention equals .avi
   */
  public boolean accept(File f) {
    // Tests whether the file is a directory.
    if (f.isDirectory()) {
      return true;
    }

    // Use get extension method
    String extension = getExtension(f);
    if (extension != null) {
      // Options for extra video formats
      if (extension.equals(avi)
          || extension.equals(mpg)
          || extension.equals(mov)) /**/
          {
        return true;
      }
      else {
        return false;
      }
    }
    return false;

  } // end accept()

  public String getDescription(File f) {
    return this.getExtension(f);
  }

  // The description of this filter
  public String getDescription() {
    return "AVI format files (.avi)";

  } // end getDescription()

} // end SpawnyVideoFilters class
