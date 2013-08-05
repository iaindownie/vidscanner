package vidscanner;

//package scanner; // Include in Scanner package

import javax.media.jai.*;

import java.awt.*;
import java.awt.image.renderable.*;

/**
 A class for the comparing two images using Mean Pixel Value
 of the whole image, or Mean difference in Histogram values
 for the image. Can be RGB, currently set at G only to try
 to improve performance.
 @author Iain Downie: MSc IT 2000-2001 Summer Project.
 */
public class Comparators {

  // Variables for Histogram analyses
  int[] bins = {
      256, 256, 256}; // The number of bins.
  double[] low = {
      0.0D, 0.0D, 0.0D}; // The low value.
  double[] high = {
      256.0D, 256.0D, 256.0D}; // The high value.
  double overallPixelDiff; // Pixel sensitivity score
  double overallHistoDiff; // Histogram sensitivity score

  /**
    Method for comparing the mean pixel value for each image.
    @param im1, an AWT image.
    @param im2, an AWT image.
    @return overallPixelDiff, a double
   */
  public double compareMeanPixels(Image im1, Image im2) {
    System.out.println("inside compareMeanPixels");
    // Create the array of data using getMean()
    double[] mean1 = getMean(im1);
    double[] mean2 = getMean(im2);
    // Create a target array
    double[] meanDiff = new double[mean1.length];
    // Fill target array with differences between originals
    for (int i = 0; i < mean1.length; i++) {
      meanDiff[i] = mean1[i] - mean2[i];
    }
    // Calculate mean diffrence across the three bands
    overallPixelDiff = Math.abs( (meanDiff[0]
                                  + meanDiff[1]
                                  + meanDiff[2]) / 3.0);

    return overallPixelDiff;

  } // end compareMeanPixels()

  /**
    Method for calculating the mean pixel value of an image.
    @param im, an AWT image.
    @return mean, an array of double values
   */
  public double[] getMean(Image im) {
    System.out.println("inside getMean");
    // Keep this next line for if I need to convert to PlanarImage
    //PlanarImage image = JAI.create("awtimage", im);

    // Create ParameterBlock object
    ParameterBlock pb = new ParameterBlock();
    pb.addSource(im); // add image
    pb.add(null); // add RegionOfInterest or null
    pb.add(1); // sampling
    pb.add(1); // periods
    // Create the mean object
    RenderedOp op = (RenderedOp)JAI.create("mean", pb);
    // Cast the mean object properties as a double array
    double[] mean = (double[]) op.getProperty("mean");

    return mean;

  } // end getMean()

  /**
    Method for comparing the mean histogram value for each image,
    only working on the G band of RGB for efficiency. Can include all RGB.
    @param im1, an AWT image.
    @param im2, an AWT image.
    @return overallHistoDiff, a double
   */
  public double compareHistograms(Image im1, Image im2) {
    System.out.println("inside compareHistograms");
    // Set double to zero intially
    overallHistoDiff = 0.0;
    // Fill arrays with data from images
    int[][] image1Array = mkHistdata(im1);
    int[][] image2Array = mkHistdata(im2);
    // Create target array and fill with differences
    int diffArray[] = new int[256];
    for (int i = 0; i < 256; i++) {
      diffArray[i] = image1Array[1][i] - image2Array[1][i];
      overallHistoDiff += Math.abs(diffArray[i]);
    }

    return overallHistoDiff / 256.0;

  } // end compareHistograms()

  /**
    Method for calculating the Histogram of an image.
    @param im, an AWT image.
    @return myArray, a 2D array of int values
   */
  public int[][] mkHistdata(Image im) {
    System.out.println("inside mkHistdata");
    // Keep this next line for if I need to convert to PlanarImage
    //PlanarImage image = JAI.create("awtimage", im);

    // Construct the Histogram object.
    System.out.println("Making the histogram");
    javax.media.jai.Histogram hist = new javax.media.jai.Histogram(bins, low, high);
    System.out.println("Made the histogram");

    // Create the ParameterBlock object.
    System.out.println("Creating the ParameterBlock object");
    ParameterBlock pb = new ParameterBlock();
    pb.addSource(im); // add image
    pb.add(hist); // add the histogram
    pb.add(null); // add ROI or null
    pb.add(1); // sampling
    pb.add(1); // periods
    System.out.println("Finished the ParameterBlock object");

    // Perform the histogram operation.
    System.out.println("Staring the PlanarImage");
    RenderedOp dst = JAI.create("histogram", pb);
    //RenderedOp dst = JAI.create("histogram", pb, null);
    System.out.println("Finishing the PlanarImage");

    // Retrieve the histogram data.
    hist = (Histogram) dst.getProperty("histogram");
    // Create and fill target array
    int[][] myArray = new int[3][256];
    for (int i = 0; i < 256; i++) {
      myArray[0][i] = hist.getBinSize(0, i);
      myArray[1][i] = hist.getBinSize(1, i);
      myArray[2][i] = hist.getBinSize(2, i);
    }

    return myArray;

  } // end mkHistdata()

  /**
    This method is not used but is useful for comparing all the bins
    in the arrays used by Histogram analysis. Calculates the difference
    between each element of the two arrays.
    @param a1, a 2D int array.
    @param a2, a 2D int array.
    @return a 2D int array from the parameters
   */
  public int[][] compare2DArrays(int[][] a1, int[][] a2) {
    int[][] diffArray = new int[3][256];
    for (int i = 0; i < 256; i++) {
      diffArray[0][i] = a1[0][i] - a2[0][i];
      diffArray[1][i] = a1[1][i] - a2[1][i];
      diffArray[2][i] = a1[2][i] - a2[2][i];
    }
    return diffArray;

  } // end compare2DArrays()

} // end Comparators class
