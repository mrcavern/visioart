package visionartificial;
import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.alg.tracker.circulant.CirculantTracker;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.gui.image.ShowImages;
import boofcv.gui.tracker.TrackerObjectQuadPanel;
import boofcv.io.MediaManager;
import boofcv.io.image.SimpleImageSequence;
import boofcv.io.webcamcapture.UtilWebcamCapture;
import boofcv.io.wrapper.DefaultMediaManager;
import boofcv.misc.BoofMiscOps;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageUInt8;
import georegression.struct.shapes.Quadrilateral_F64;

import java.awt.*;
import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.Webcam;

/**
 * Demonstration on how to use the high level {@link TrackerObjectQuad} interface for tracking objects in a
 * video sequence.  This interface allows the target to be specified using an arbitrary quadrilateral.  Specific
 * implementations might not support that shape, so they instead will track an approximation of it.  The
 * interface also allows information on target visibility to be returned.  As is usually the case, tracker
 * specific information is lost in the high level interface and you should consider using the trackers
 * directly if more control is needed.
 *
 * This is an active area of research and all of the trackers eventually diverge given a long enough sequence.
 *
 * @author Peter Abeles
 */
public class ExampleTrackerObjectQuad {

	public static void main(String[] args) {
		MediaManager media = DefaultMediaManager.INSTANCE;
		
		String fileName = "tracking(1).mjpeg";

		// Create the tracker.  Comment/Uncomment to change the tracker.  Mean-shift trackers have been omitted
		// from the list since they use color information and including color images could clutter up the example.
		TrackerObjectQuad tracker =
				FactoryTrackerObjectQuad.circulant(null, ImageUInt8.class);
//				FactoryTrackerObjectQuad.sparseFlow(null,ImageUInt8.class,null);
//				FactoryTrackerObjectQuad.tld(null,ImageUInt8.class);
//				FactoryTrackerObjectQuad.meanShiftComaniciu2003(new ConfigComaniciu2003(), ImageType.ms(3,ImageUInt8.class));
//				FactoryTrackerObjectQuad.meanShiftComaniciu2003(new ConfigComaniciu2003(true),ImageType.ms(3,ImageUInt8.class));

				// Mean-shift likelihood will fail in this video, but is excellent at tracking objects with
				// a single unique color.  See ExampleTrackerMeanShiftLikelihood
//				FactoryTrackerObjectQuad.meanShiftLikelihood(30,5,255, MeanShiftLikelihoodType.HISTOGRAM,ImageType.ms(3,ImageUInt8.class));

		SimpleImageSequence video = media.openVideo(fileName, tracker.getImageType());

		// specify the target's initial location and initialize with the first frame
		Quadrilateral_F64 location = new Quadrilateral_F64(775,500,875,500,775,625,875,625);
		
		ImageBase frame = video.next();
		tracker.initialize(frame,location);

		// For displaying the results
		TrackerObjectQuadPanel gui = new TrackerObjectQuadPanel(null);
		gui.setPreferredSize(new Dimension(frame.getWidth(),frame.getHeight()));
		gui.setBackGround((BufferedImage)video.getGuiImage());
		gui.setTarget(location,true);
		ShowImages.showWindow(gui,"Tracking Results");

		// Track the object across each video frame and display the results
		while( video.hasNext() ) {
			frame = video.next();

			boolean visible = tracker.process(frame,location);

			gui.setBackGround((BufferedImage) video.getGuiImage());
			gui.setTarget(location,visible);
			gui.repaint();

			BoofMiscOps.pause(20);
		}
	}
}