package org.ginsim.service.export;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.ginsim.graph.Graph;

import fr.univmrs.tagc.GINsim.graph.GsGraph;

/**
 * export the graph to PNG
 * 
 */
public class ImageExport {

    /**
     * @param graph
     * @param selectedOnly
     * @param fileName
     */
    public static void exportImage( Graph graph, boolean selectedOnly, String fileName) {

//    	BufferedImage img = graph.getImage();
//    	
//    	if (img != null) {
//    			try {
//					ImageIO.write(img, "png", new File(fileName));
//				} catch (IOException e) {
//				}
//	    		return;
//    	}
    }
    
}