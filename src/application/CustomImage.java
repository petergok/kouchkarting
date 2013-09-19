package application;


import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.imageio.*;
import java.awt.Image;

/**
 * The "CustomImage" Class.
 * Purpose: This class imports and stores an image file as byte buffers.
 * This allows for quick access to the image for drawing textures
 * and backgrounds.
 * <P>
 * Some methods were made by napier @ potatoland.org:
 * allocBytes, convertARGBtoRGBA, getImagePixels, and flipY.
 * 
 * @author Peter G. with help from napier @ potatoland.org
 * @version Jan 12, 2012
 */
public class CustomImage {
	
	// Store the height and width of the image
	public int height = 0;					
    public int width = 0;					
    
    // Store the image as an RGBA byte buffer and an ARGB integer array
    public ByteBuffer pixelBufferRGBA = null;  	
    public int[] pixelsARGB = null;
    
    /**
     * Create an empty image object
     */
    public CustomImage () {
    }

    /**
     * Load pixels from an image file.  Flip Y axis.  Convert to RGBA format.
     * 
     * @param filename the name of the file containing the image
     */
    public CustomImage (String filename)
    {
		this (filename, true, false);
    }

    /**
     * Loads pixels from an image file and converts them to RGBA format and power of two
     * if needed. It then reports the results of the image load 
     * 
     * @param filename the name of the file containing the image
     * @param flipYaxis whether to flip the image vertically or not
     * @param convertPowTwo whether to convert the image to power of two dimensions
     */
    public CustomImage (String filename, boolean flipYaxis, boolean convertPowTwo)
    {
		BufferedImage img = loadImage(filename);
        if (makeImage (img, flipYaxis, convertPowTwo)) {
			System.out.println("CustomImage: loaded " + filename + 
							   ", width = " + width + " height = " + height);
		}
        else {
        	System.out.println("CustomImage: failed to load " + filename);
        }
	}

    /**
     * Create a CustomImage from image file bytes (the contents of a jpg, gif or png file). 
     * Flip Y axis and don't convert to power of two.
     * 
     * @param bytes the array of bytes from the image file
     */
    public CustomImage (byte[] bytes) {
        this (bytes, true, false);
    }

    /**
     * Create a CustomImage from image file bytes (the contents of a jpg, gif or png file).
     * Reports the results of the loading of the image.
     * 
     * @param bytes the array of bytes from the image file
     * @param flipYaxis whether to flip the image vertically or not
     * @param convertPowTwo whether to convert the image to power of two dimensions or not
     */
    public CustomImage (byte[] bytes, boolean flipYaxis, boolean convertPowTwo) {
        BufferedImage newImage = makeBufferedImage(bytes);
        if (makeImage (newImage, flipYaxis, convertPowTwo)) {
			System.out.println("Image: loaded image from bytes[" + bytes.length + "]");
		}
		else {
			System.out.println("Image: could not create Image from bytes[" + bytes.length + "]");
		}
    }

    /**
     * Make a BufferedImage from the contents of an image file.
     *   
     * @param imageFileContents byte array containing the contents of a JPG, GIF, or PNG
     * 
     * @return the BufferedImage made
     */
    public BufferedImage makeBufferedImage (byte[] imageFileContents) {
    	BufferedImage buffImage = null;
    	try {
    		// Create an input stream and load the image to a buffered image
        	InputStream input = new ByteArrayInputStream(imageFileContents);
            buffImage = javax.imageio.ImageIO.read(input);
        }
    	catch (IOException exception) {
    		System.out.println("Image.makeBufferedImage(): " + exception);
    	}  
        return buffImage;
    }
    
    /**
	 * Load a BufferedImage from the given image file name.  
	 * The file has to be in the local file system.
	 * 
	 * @param filename the name of the file to be loaded
	 * 
	 * @return the BufferedImage loaded
	 */
    public BufferedImage loadImage (String filename) {
    	BufferedImage newImage = null;
    	try {
    		newImage = ImageIO.read(new FileInputStream(filename));
    	}
    	catch (Exception e) {
    		System.out.println("CustomImage.loadImage() exception: FAILED TO LOAD IMAGE: " + e);
    	}
    	return newImage;
	}
    
    /**
     * Make a custom image from the given buffered image.  If convertToPow2 is true then convert
     * the image to a power of two.  Store pixels as ARGB integers in the pixels array
     * and as RGBA bytes in the pixelBuffer ByteBuffer.  Hold onto image width and height.
     * 
     * @param newImage the new image to be created in ByteBuffer form
     * @param flipYaxis whether to flip the image along the y axis or not
     * @param convertToPowTwo whether to convert the image to power of two dimensions or not
     * 
     * @return if the image was loaded properly
     */
    public boolean makeImage (BufferedImage newImage, boolean flipYaxis, boolean convertToPowTwo) {
    	// If the image is valid
        if (newImage != null) {
        	// Flip the image and convert it to powers of two if necessary
            if (flipYaxis) {
	            newImage = flipY(newImage);
			}
            if (convertToPowTwo) {
            	newImage = convertToPowerOf2(newImage);
            }
            // Get the width, height, and convert the image to ARGB and RGBA formats
            width = newImage.getWidth(null);
            height = newImage.getHeight(null);
            pixelsARGB = getImagePixels(newImage);
            pixelBufferRGBA = convertImagePixelsRGBA(pixelsARGB);
            return true;
        }
        // If image is not valid, make the object empty (nullified)
        else {
            pixelsARGB = null;
            pixelBufferRGBA = null;
            height = width = 0;
            return false;
        }
    }
    
    /**
     * Flip the given BufferedImage vertically and return the new BufferedImage.
     * <p>
	 * Note: method by napier @ potatoland.org
     * 
     * @param image the buffered image to be flipped
     * 
     * @return the flipped image in buffered form
     */
    public BufferedImage flipY(BufferedImage image) {
        AffineTransform texture = AffineTransform.getScaleInstance(1, -1);
        texture.translate(0, -image.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(texture, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }
    
    /**
     * Scale the given BufferedImage to width and height that are powers of two.
     * Return the new scaled BufferedImage.
     * <p>
	 * Note: method by napier @ potatoland.org
	 * 
	 * @param image the image to be converted
	 * 
	 * @return the scaled BufferedImage
     */
    public BufferedImage convertToPowerOf2(BufferedImage image) {
        // Find powers of 2 equal to or greater than current dimensions
        int newWidth = KouchKarting.getPowerOfTwoBiggerThan(image.getWidth());
        int newHeight = KouchKarting.getPowerOfTwoBiggerThan(image.getHeight());
        // If the image is already a power of 2
        if (newWidth == image.getWidth() && newHeight == image.getHeight()) {
        	return image;
        }
        // Otherwise, transform the image to make its dimensions a power of 2
        else {
	        AffineTransform texture = AffineTransform.getScaleInstance((double) newWidth / image.getWidth(), (double) newHeight / image.getHeight());
	        BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
	        Graphics2D graphics = newImage.createGraphics();
	        graphics.drawRenderedImage(image, texture);
	        return newImage;
        }
    }
    
    /**
     * Return the Image pixels in default Java integer ARGB format.
     * 
     * @param image the image (from java.awt) to extract data from
     * 
     * @return the array of integers in ARGB format
     * 
     * Note: method by napier <at> potatoland.org
     */
    public int[] getImagePixels(Image image)
    {
    	int[] pixelsARGB = null;
        if (image != null) {
        	int imageWidth = image.getWidth(null);
        	int imageHeight = image.getHeight(null);
        	pixelsARGB = new int[ imageWidth * imageHeight];
            PixelGrabber pg = new PixelGrabber(image, 0, 0, imageWidth, imageHeight, pixelsARGB, 0, imageWidth);
            try {
                pg.grabPixels();
            }
            catch (Exception e) {
            	System.out.println("Image.getImagePixels(): Pixel Grabbing interrupted!");
                return null;
            }
        }
        return pixelsARGB;
    }
    
    /**
     * Convert ARGB pixels to a ByteBuffer containing RGBA pixels. The RGBA format is
     * a default format used in OpenGL 1.0
     * <P>
     * Note: method by napier @ potatoland.org
     * 
     * @param pixels an array of integers containing pixel data
     * 
     * @return the ByteBuffer containing the RGBA pixel data
     */
    public ByteBuffer convertImagePixelsRGBA(int[] pixels) {
        byte[] bytes;     						// Holds pixels as RGBA bytes
        bytes = convertARGBtoRGBA(pixels);		// Convert the pixels
        return allocBytes(bytes);  				// Convert to ByteBuffer and return
    }
    
    /**
     * Convert pixels from the default java ARGB int [] format to byte array in RGBA format.
     * <P>
     * Note: method by napier @ potatoland.org
     * 
     * @param pixels an array of integers that hold the data for the pixels
     * 
     * @return a new byte array in RGBA format
     */
    public static byte[] convertARGBtoRGBA(int[] pixels)
    {
    	// Hold pixels as RGBA bytes and integers
        byte[] bytes = new byte[pixels.length*4];
        int pixel, red, green, blue, alpha;
        int byteIndex = 0;
        
        // For each pixel
        for (int pixelIndex = 0; pixelIndex < pixels.length; pixelIndex++) {
        	
        	// Convert the pixel to RGBA format
            pixel = pixels [pixelIndex];
            alpha = (pixel >> 24) & 0xFF;
            red = (pixel >> 16) & 0xFF;
            green = (pixel >> 8) & 0xFF;
            blue = (pixel >> 0) & 0xFF;
            
            // Store the results in a byte array
            bytes[byteIndex + 0] = (byte) red;  // fill in bytes in RGBA order
            bytes[byteIndex + 1] = (byte) green;
            bytes[byteIndex + 2] = (byte) blue;
            bytes[byteIndex + 3] = (byte) alpha;
            byteIndex += 4;
        }
        return bytes;
    }
    
    /**
     * Allocates a ByteBuffer to hold the given array of bytes.
     * <P>
     * Note: method by napier @ potatoland.org
     *
     * @param byteArray the array of bytes to be moved
     * 
     * @return a ByteBuffer containing the contents of the byte array
     */
    public static ByteBuffer allocBytes(byte[] byteArray) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(byteArray.length).order(ByteOrder.nativeOrder());
        byteBuffer.put(byteArray).flip();
        return byteBuffer;
    }
    
    /**
     * Return true if image has been loaded successfully (pixel buffer is not empty)
     * 
     * @return if the image has been loaded successfully
     */
    public boolean isLoaded() {
        return (pixelBufferRGBA != null);
    }
    
    /**
     * A method that returns the width of the image
     * 
     * @return the width of the image
     */
    public int getWidth() {
    	return width;
    }
    
    /**
     * A method that returns the height of the image
     * 
     * @return the height of the image
     */
    public int getHeight() {
    	return height;
    }
    
    /**
     * A method that returns the pixel bytes buffer for the image
     * 
     * @return the ByteBuffer containing pixel data
     */
    public ByteBuffer getPixelBytes () {
    	return pixelBufferRGBA;
    }
}
