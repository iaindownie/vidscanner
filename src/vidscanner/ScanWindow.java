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
import java.lang.Integer;
import java.util.Vector;
import java.net.MalformedURLException;

// import scanner specific classes
//import scanner.Comparators;
//import scanner.ComparisonWarning;
/**
The main Scanning application class. This class presents an interface
for the user to scan video files with. A choice of scanning methods is
available, and feedback on the video details is presented too.
It needs to use JMF code to load the vid, then extract the images. If the
comparator methods are chosen, those images are compared against each other.
The chosen frames are written as frame numbers to a text file.
@author Iain Downie: MSc IT 2000-2001 Summer Project.
 */
public class ScanWindow extends JInternalFrame
        implements ControllerListener, ActionListener {
    // Swing variables for scanner window

    private JButton openFile, playFile, segment;
    private JPanel mainP, topP, bottomP, progressP, progressLabel, prefP,
            resultP, infoP, buttonP;
    private JComboBox algorithmList;
    private JProgressBar progressBar;
    private JTextArea showFile, showTime, showFrames, showResults;
    private Font verdana10pt = new Font("Verdana", Font.BOLD, 10);
    private ImageIcon openPicture = new ImageIcon("images/open24.gif");
    private ImageIcon segmentPicture = new ImageIcon("images/scan24.gif");
    private final int XCOORDINATE = 305;
    private final int YCOORDINATE = 5;
    // JMF variables
    private VideoBrowser vb;
    private MediaLocator ml = null;
    private DataSource ds = null;
    private Player p;
    private FramePositioningControl fpc;
    private FrameGrabbingControl fgc;
    private Object waitSync = new Object();
    private boolean stateTransitionOK = true;
    // File writing variables
    private FileWriter fw;
    private PrintWriter out;
    private String outputFileName;
    // Instance variables for counters etc.
    private int totalFrames = FramePositioningControl.FRAME_UNKNOWN;
    private int algFrameRate = 0;
    private int segmentNum = 0;
    private String scene = null;
    // Image manipulation variables
    private Buffer buffy;
    private Image im;
    private Comparators pc, hc;
    private double comp, comp2;
    private final int PIXELSENSITIVITY = 10;
    private final int HISTOSENSITIVITY = 240;
    private final int SEEDTHRESHOLD = 50;
    // Image and frame storage variables
    private Vector finalFrames = new Vector();
    private Vector initialFrames = new Vector();
    private Vector initialImages = new Vector();

    /**
    Constructor, creating the internal ScanWindow.
    @param vb, a VideoBrowser object.
    @param inset, an int to determine screensize and frame location
     */
    public ScanWindow(VideoBrowser vb, int inset) {
        super("Video Scanner",
                false, //resizable
                true, //closable
                false, //maximizable
                true);//iconifiable

        this.vb = vb;

        /***********************************************************
        Swing Stuff
         ***********************************************************/
        // Create main Swing panel
        mainP = new JPanel(new GridLayout(2, 1, 0, 0));

        // Create the top panel for buttons and prefs/progressbar
        topP = new JPanel();
        topP.setLayout(new BoxLayout(topP, BoxLayout.X_AXIS));

        // Create the actual button panel
        buttonP = new JPanel(new GridLayout(2, 1, 5, 5));

        // Create the two buttons
        openFile = new JButton("Open a file", openPicture);
        openFile.addActionListener(this);
        openFile.setVerticalTextPosition(AbstractButton.CENTER);
        openFile.setHorizontalTextPosition(AbstractButton.LEFT);
        segment = new JButton("Scan the file", segmentPicture);
        segment.addActionListener(this);
        segment.setEnabled(false);
        segment.setVerticalTextPosition(AbstractButton.CENTER);
        segment.setHorizontalTextPosition(AbstractButton.LEFT);

        // Add suitables spaces, buttons and button panel to top
        topP.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonP.add(openFile);
        buttonP.add(segment);
        topP.add(buttonP);
        topP.add(Box.createRigidArea(new Dimension(20, 0)));

        // Create the preference panel
        prefP = new JPanel(new GridLayout(2, 2, 3, 3));

        // Create a nice border with formatting
        Border lowBevel = BorderFactory.createLoweredBevelBorder();
        TitledBorder segmentBorder = BorderFactory.createTitledBorder(
                lowBevel, "Segment preferences");
        segmentBorder.setTitlePosition(TitledBorder.ABOVE_TOP);
        segmentBorder.setTitleJustification(TitledBorder.RIGHT);

        // Create and add label for the prefs panel
        JLabel algLabel = new JLabel("Scanning method  ",
                SwingConstants.RIGHT);
        prefP.add(algLabel);

        // An array for the combobox
        String[] algorithms = {"Select a frame grab method",
            "Every 20th frame",
            "Every 50th frame",
            "Every 100th frame",
            "Mean pixel comparison",
            "Mean histogram comparison"};

        // Create the drop down combobox
        algorithmList = new JComboBox(algorithms);
        //algorithmList.setMaximumSize(new Dimension(140,20));
        //algorithmList.setPreferredSize(new Dimension(140,20));
        //algorithmList.setMinimumSize(new Dimension(140,20));
        algorithmList.setSelectedIndex(0);  // Set at array element 0

        // Actionlistener responding to different choices from combobox
        algorithmList.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String ss = (String) cb.getSelectedItem();
                if (ss == "Select a frame grab method") {
                    scene = null;
                }
                if (ss == "Every 20th frame") {
                    algFrameRate = 20;
                    scene = "20";
                } else if (ss == "Every 50th frame") {
                    algFrameRate = 50;
                    scene = "50";
                } else if (ss == "Every 100th frame") {
                    algFrameRate = 100;
                    scene = "100";
                } else if (ss == "Mean pixel comparison") {
                    algFrameRate = totalFrames / SEEDTHRESHOLD;
                    scene = "mean Pixel comparison";
                } else if (ss == "Mean histogram comparison") {
                    algFrameRate = totalFrames / SEEDTHRESHOLD;
                    scene = "mean Histogram comparison";
                }
            }
        });

        // Add to prefs panel
        prefP.add(algorithmList);

        // Create and add a label for progress
        JLabel scanning = new JLabel("Scanning progress  ",
                SwingConstants.RIGHT);
        prefP.add(scanning);

        // Create a progressbar set at 0
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        //progressBar.setMaximumSize(new Dimension(140,20));
        //progressBar.setPreferredSize(new Dimension(140,20));
        //progressBar.setMinimumSize(new Dimension(140,20));

        // Add progressbar and border to prefs
        prefP.add(progressBar);
        prefP.setBorder(segmentBorder);

        // add prefs to top panel
        topP.add(prefP);

        // Create bottom panel for feedback with nice border
        bottomP = new JPanel(new GridLayout(2, 1, 0, 0));
        Border etched = BorderFactory.createEtchedBorder();
        TitledBorder bottomBorder = BorderFactory.createTitledBorder(
                lowBevel, "File info");
        bottomBorder.setTitlePosition(TitledBorder.ABOVE_TOP);

        // The file info panel
        infoP = new JPanel(new BorderLayout());

        // Create three JTextAreas (name, time, frames). Add to infoP
        showFile = new JTextArea();
        showFile.setText("");
        showFile.setFont(verdana10pt);
        showFile.setBackground(Color.lightGray);
        infoP.add(showFile, BorderLayout.WEST);

        showTime = new JTextArea();
        showTime.setText("");
        showTime.setFont(verdana10pt);
        showTime.setBackground(Color.lightGray);
        infoP.add(showTime, BorderLayout.CENTER);

        showFrames = new JTextArea();
        showFrames.setText("");
        showFrames.setFont(verdana10pt);
        showFrames.setBackground(Color.lightGray);
        infoP.add(showFrames, BorderLayout.EAST);

        // Create final results panel
        resultP = new JPanel(new BorderLayout());

        // Create a new JTextArea and add to results panel
        showResults = new JTextArea();
        showResults.setText("");
        showResults.setFont(verdana10pt);
        showResults.setBackground(Color.lightGray);
        resultP.add(showResults);

        // Add feedback panels and border to bottom panel
        bottomP.add(infoP);
        bottomP.add(resultP);
        bottomP.setBorder(bottomBorder);

        // Add top and bottom panels to main, and add to contentpane
        mainP.add(topP);
        mainP.add(bottomP);
        getContentPane().add(mainP);

        /***********************************************************
        End of swing Stuff
         ***********************************************************/
        // Set size and location
        pack();
        //setSize(300,200);
        setLocation(XCOORDINATE, YCOORDINATE);

    } // end Constructor()

    /**
    Method to convert MediaLocator oblect to a DataSource object.
    Uses the JMF Manager to create ds, the DataSource. Then works out
    the filename and creates an output file with the same name.
    Contains the call to the realize method which creates the player
    etc., and the feedback panel data.
    @param ml, A MediaLocator object.
     */
    public void MediaLocatorTOdatasource(MediaLocator ml) {
        // Create datasource
        try {
            ds = Manager.createDataSource(ml);
            System.out.println("Created Data Source " + ml.getURL());
        } catch (Exception e) {
            System.err.println("Cannot create DataSource from: " + ml);
            System.exit(0);
        }
        // Create output file with same name
        try {
            System.out.println("1outputFileName " + outputFileName);
            outputFileName = ("/" + SpawnyUtils.alterAVItoTXT(ml.toString(), 3));
            System.out.println("2outputFileName " + outputFileName);
            fw = new FileWriter(outputFileName);
            System.out.println("3outputFileName " + outputFileName);
            out = new PrintWriter(fw);
            System.out.println("4outputFileName " + outputFileName);
            
        } catch (java.io.IOException ioe) {
            System.out.println("Sorry, could not create output file");
        }
        // Call the rest of the JMF code
        realizeAVI(ds);
        // Fill in user feedback panel
        SpawnyUtils.fillFileNameTextArea(showFile, ml.toString(), '/');

    } // end MediaLocatorTOdatasource()

    /**
    Method to create JMF player and obtain all the details such as time
    duration, frame number etc.
    @param ds, A DataSource object.
    @return true if creation, realization and prefetching dreated OK
     */
    public boolean realizeAVI(DataSource ds) {
        System.err.println("Create player for: " + ds.getContentType());
        // Create the player
        try {
            p = Manager.createPlayer(ds);
        } catch (Exception e) {
            System.err.println("Failed to create a player from the given DataSource: " + e);
            return false;
        }

        // Add the controller listener
        p.addControllerListener(this);

        // Realize the player
        p.realize();
        // Wait until reaization finished
        if (!waitForState(p.Realized)) {
            System.err.println("Failed to realize the player.");
            return false;
        }

        // Prefetch the player.
        p.prefetch();
        // Wait until prefetching finished
        if (!waitForState(p.Prefetched)) {
            System.err.println("Failed to prefetch the player.");
            return false;
        }


        // Try to retrieve a FramePositioningControl from the player.
        fpc = (FramePositioningControl) p.getControl("javax.media.control.FramePositioningControl");
        // Try to retrieve a FrameGrabbingControl from the player.
        fgc = (FrameGrabbingControl) p.getControl("javax.media.control.FrameGrabbingControl");

        // Only applies if the video format cannot accept FPC
        if (fpc == null) {
            System.err.println("The player does not support FramePositioningControl.");
            return false;
        }

        // Calculate time duration of video
        Time duration = p.getDuration();
        if (duration != Duration.DURATION_UNKNOWN) {
            // If longer than 60 secs, add time to feedback panel
            if (duration.getSeconds() > 60) {
                int mins = (int) duration.getSeconds() / 60;
                int secs = (int) duration.getSeconds() % 60;
                showTime.setText("  " + mins + " min " + secs + " sec  ");
            } // If less than 60 secs, only display secs in feedback panel
            else {
                showTime.setText("  " + (int) duration.getSeconds() + " secs  ");
            }

            // Calculate the total number of frames for the video
            totalFrames = fpc.mapTimeToFrame(duration);
            // Reset progressbar to total frames and show feedback data
            progressBar.setMaximum(totalFrames);
            showFrames.setText("Frames: " + totalFrames + " ");

            // Only applies if the video has no frames!
            if (totalFrames != FramePositioningControl.FRAME_UNKNOWN) {
                System.err.println("Total # of video frames in the movies: "
                        + totalFrames);
            } else {
                System.err.println("The FramePositiongControl does not support mapTimeToFrame.");
            }
        } else {
            System.err.println("Movie duration: unknown");
        }
        return true;

    } // end realizeAVI()

    /**
    Block until the player has transitioned to the given state.
    Return false if the transition failed.
    @param state, an int produced by JMF Controller getState()
    @return boolean, true if getState() is realised/prefetched
     */
    boolean waitForState(int state) {
        synchronized (waitSync) {
            try {
                while (p.getState() < state && stateTransitionOK) {
                    waitSync.wait();
                }
            } catch (Exception e) {
            }
        }
        return stateTransitionOK;

    } // end waitForState()

    /**
    Controller Listener for JMF elements.
     */
    public void controllerUpdate(ControllerEvent evt) {

        if (evt instanceof ConfigureCompleteEvent
                || evt instanceof RealizeCompleteEvent
                || evt instanceof PrefetchCompleteEvent) {
            synchronized (waitSync) {
                stateTransitionOK = true;
                waitSync.notifyAll();
            }
        } else if (evt instanceof ResourceUnavailableEvent) {
            synchronized (waitSync) {
                stateTransitionOK = false;
                waitSync.notifyAll();
            }
        }

    } // end controllerUpdate()

    /**
    Method to create the initial key frames, the heart of this class.
    This method detects the state of the algorith (labelled "scene") and
    acts accordingly.
    If no algorithm set, then show a dialog box to prompt user.
    Create header data for output file.
    If regular methods chosen
    - Fill the output with the regular keyframe data (int only, no images selected)
    If comparator methods chosen
    - Extract the images as seeds for comparison
    - Determine comparison method and proceed with comparison
    - Fill the output file with the keyframes from algorithm.
     */
    public void createInitialKeyFrames() {
        // Prompt user if they have not seleced a scanning method
        if (scene == null) {
            // User has to respond to this
            JOptionPane.showInternalMessageDialog(this,
                    "Please choose a scanning method");
        } else {

            // Start output file headers
            System.out.println("Got here!");
            out.println("<index>");
            out.println("<file>" + outputFileName + "</file>");
            out.println("<frame total>" + totalFrames + "</frame total>");

            // Regular methods
            if (scene.equals("20") || scene.equals("50") || scene.equals("100")) {
                // Fill in shot information
                out.println("<shot total>" + segmentNum + "</shot total>");
                out.println("<shot>");
                for (int i = 0; i < totalFrames; i += algFrameRate) {
                    out.println(i);
                    progressBar.setValue(i);
                    progressBar.update(progressBar.getGraphics());
                    segmentNum++;
                }
                out.println("</shot>");

                // Fil in keyframe information
                out.println("<keyframe total>" + segmentNum + "</keyframe total>");
                out.println("<keyframe>");
                for (int i = 0; i < totalFrames; i += algFrameRate) {
                    out.println(i);
                }
                out.println("</keyframe>");

                // Make progress bar completed
                progressBar.setValue(totalFrames);
                progressBar.update(progressBar.getGraphics());

                // Provide feedback to interface
                showResults.setText("Scanning completed - initial number of frames: "
                        + segmentNum);
                showResults.append("\nFinal number of frames indexed in "
                        + SpawnyUtils.reducePath(outputFileName, 4, '/')
                        + ".txt: " + segmentNum);

            }// end regular choice
            // Comparator methods
            else {
                // Reset progressbar string
                progressBar.setString("Video pre-scan");
                // Perform initial scan through data, dependent on SEEDTHRESHOLD
                for (int i = 0; i < totalFrames; i += algFrameRate) {
                    // Seek to frame i and add location to initialFrames
                    fpc.seek(i);
                    initialFrames.add(new Integer(i));

                    // Create buffer and grab image
                    buffy = fgc.grabFrame();

                    // Return AWT image	and add to initialImages
                    im = SpawnyUtils.returnImage(buffy);
                    initialImages.add(im);

                    // Update progressBar and number of segments
                    progressBar.setValue(i);
                    progressBar.update(progressBar.getGraphics());
                    segmentNum++;
                }

                // Make progressbar complete for first stage
                progressBar.setValue(totalFrames);
                progressBar.update(progressBar.getGraphics());

                // Provide feedback to interface
                showResults.setText("Scanning completed - initial number of frames: "
                        + segmentNum);

                // Then perform the actual image comparisons
                if (scene.equals("mean Pixel comparison")) {
                    binSearchForMatches("pixel", PIXELSENSITIVITY);
                }
                if (scene.equals("mean Histogram comparison")) {
                    binSearchForMatches("histo", HISTOSENSITIVITY);
                }

                // Show final results in feedback panel
                showResults.append("\nFinal number of frames indexed in "
                        + SpawnyUtils.reducePath(outputFileName, 4, '/')
                        + ".txt: " + finalFrames.size());

            }// end comparator choice

            // Finish and close output file
            out.println("</index>");
            out.close();
        }

    } // end createInitialKeyFrames()

    /**
    The main comparison algorithm. Uses a binary search method to locate the
    actual frame that represents the boundary between video shots. Dependent
    on the sensitivity of the method.
    @param method, a string for the method chosen
    @param sensitivity, the chosen sensitivity for the method
     */
    public void binSearchForMatches(String method, int sensitivity) {
        // Open the comparison warning window for user feedback
        ComparisonWarning warn = new ComparisonWarning();
        vb.addOtherFrame(warn);
        warn.update(warn.getGraphics());

        // Stub to show what method chosen
        System.out.println(method + " using binSearchForMatches called!");

        // Instantiate new Comparators object
        System.out.println("About to call the comparator");
        hc = new vidscanner.Comparators();
        System.out.println("Finished the comparator");

        // Add first frame (always)
        finalFrames.add(new Integer(0));

        // Compare frames along initial storage Vector
        for (int i = 0; i < initialFrames.size(); i++) {
            // So that you don't compare the last frame with nothing
            if (i < (initialFrames.size() - 1)) {
                // If the method chosen was the pixel.....
                if (method.equals("pixel")) {
                    // Return the mean pixel value
                    comp = hc.compareMeanPixels((Image) initialImages.get(i), (Image) initialImages.get(i + 1));
                } // ....or the method was the histogram
                else {
                    // Return the mean histogram value
                    comp = hc.compareHistograms((Image) initialImages.get(i), (Image) initialImages.get(i + 1));
                }

                // Test value returns against sensitivity
                if (comp > sensitivity) {
                    // Binary search mechanism to find actual boundary frame
                    int start = ((Integer) initialFrames.get(i)).intValue();
                    int end = ((Integer) initialFrames.get(i + 1)).intValue() - 1;
                    int mid = 0;

                    // Keep whittling down to the last frame
                    while (end - start > 1) {
                        // Return image from mid point
                        mid = (start + end) / 2;
                        fpc.seek(mid);
                        buffy = fgc.grabFrame();
                        im = SpawnyUtils.returnImage(buffy);

                        // Perform another comparison with either pixel or histogram
                        if (method.equals("pixel")) {
                            comp2 = hc.compareMeanPixels((Image) initialImages.get(i), im);
                        } else {
                            comp2 = hc.compareHistograms((Image) initialImages.get(i), im);
                        }

                        // Test that value again
                        if (comp2 > sensitivity) {
                            end = mid;	// test lower half
                        } else {
                            start = mid;  // test upper half
                        }
                    }
                    // Add frame to final storage Vector
                    finalFrames.add(new Integer(mid + 2));
                    System.out.println("Frame " + (mid + 2) + " added");

                } // end sensitivity comparison if

            } // end if (2nd last data image)

        } // end for loop though data

        // Send final choice of data to output file.
        out.println("<shot total>" + finalFrames.size() + "</shot total>");
        out.println("<shot>");
        writeComparatorSegmentNumbers();
        out.println("</shot>");
        out.println("<keyframe total>" + finalFrames.size() + "</keyframe total>");
        out.println("<keyframe>");
        writeComparatorSegmentNumbers();
        out.println("</keyframe>");
        // Close warning window once all scanning finished
        warn.dispose();

    } // end binSearchForMatches()

    /**
    Method to write the exctracted frame numbers to the file.
     */
    public void writeComparatorSegmentNumbers() {
        for (int i = 0; i < finalFrames.size(); i++) {
            // Get and write to output file
            out.println((Integer) finalFrames.get(i));
        }

    } // end writeComparatorSegmentNumbers()

    /**
    Method to clear existing progressbar, feedback data and
    storage Vectors if scanning another file.
     */
    public void clearAllprogressIndicators() {
        // Clear storage if not empty
        if (initialFrames.size() > 0) {
            initialFrames.clear();
            initialImages.clear();
            finalFrames.clear();
        }
        // Reset progress bar and clear feedback data panels
        progressBar.setValue(0);
        showFile.setText("");
        showTime.setText("");
        showFrames.setText("");
        showResults.setText("");
        // Reset the number of segments to zero
        segmentNum = 0;

    } // end clearAllprogressIndicators()

    /**
    ActionListener for open and segment buttons.
    @param event, an ActionEvent on buttons
     */
    public void actionPerformed(ActionEvent event) {
        // Open file for scanning
        if (event.getSource() == openFile) {
            // Ensure all data is clear from system
            clearAllprogressIndicators();
            // Load up filechooser, return MediaLocator and return datasource
            // This also initiates the player and realizes
            MediaLocatorTOdatasource(SpawnyUtils.returnML(this));
            // Enable the segment button
            segment.setEnabled(true);
        }

        // Segment video
        if (event.getSource() == segment) {
            // Set initial time flag
            double time1 = (double) System.currentTimeMillis();

            // Create initial keyframes
            createInitialKeyFrames();

            // Set end time flag
            double time2 = (double) System.currentTimeMillis();

            // Calculate time taken
            double timeTaken = (time2 - time1) / 1000;
            System.out.println("Time taken: " + timeTaken + " seconds.");
        }

    } // end actionPerformed()
} // end ScanWindow class

