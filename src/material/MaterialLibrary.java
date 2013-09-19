package material;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import application.CustomImage;
import application.KouchKarting;

/**
 * The "MaterialLibrary" Class.
 * Purpose: Import data from a .mtl file and store it in material objects
 * 
 * @author Peter G. with help from napier @ potatoland.org
 * @version Jan. 24, 2012
 *
 */
public class MaterialLibrary {

	// The filepath and filename
	private String filepath;
	private String filename;
	
	// The materials that will be loaded
	private Material [] materials;
	
	/**
	 * Create a new material library and load the materials from the given file
	 * 
	 * @param mtlFilename the name of the file from which to load the materials
	 */
	public MaterialLibrary (String mtlFilename) {
		String[] pathParts = KouchKarting.getPathAndFile(mtlFilename);
        filepath = pathParts[0];
        filename = pathParts[1];
    	loadMaterials(mtlFilename);
	}
	
	/**
	 * Loads all the materials from the .mtl file given
	 * 
	 * @param filename the name of the .mtl file
	 */
	public void loadMaterials (String filename) {
		// Temporary array list for loaded materials
		ArrayList loadedMaterials = new ArrayList();
		String line = "";
		
		try {
			// Create a buffered reader to load the material file
			BufferedReader MTLFile = new BufferedReader (new FileReader (filename));
			Material newMaterial = null;
			float [] rgba;
			
			while ((line = MTLFile.readLine()) != null){
				
				// Remove extra spaces
				line = line.trim();
				if (line.length() > 0) {
					
    				// Create a new material (In the form: newmtl mtlName)
    				if (line.startsWith("newmtl")) {
    					newMaterial = new Material();
    					newMaterial.setName(line.substring(7));
    					loadedMaterials.add(newMaterial);
    				}
    				
    				// If the line contains diffuse data (In the form: Kd 1.0 0.0 0.5)
    				else if (line.startsWith("Kd")) {
    					if ((rgba = readFloats(line)) != null) {
    						newMaterial.setDiffuse(rgba);
    					}
    				}
    				
    				// If the line contains ambient data (In the form: Ka 1.0 0.0 0.5)
    				else if (line.startsWith("Ka")) {
    					if ((rgba = readFloats(line)) != null) {
    						newMaterial.setAmbient(rgba);
    					}
    				}
    				
    				// If the line contains specular data (In the form: Ks 1.0 0.0 0.5)
    				else if (line.startsWith("Ks")) {
    					if ((rgba = readFloats(line)) != null) {
    						newMaterial.setSpecular(rgba);
    					}
    				}
    				
					// If the line contains data for the shininess (In the form: Ns 500.5)
    				else if (line.startsWith("Ns")) {
    					if ((rgba = readFloats(line)) != null) {
    						// Convert to openGL format: 0-127
    						int shininessValue = (int) ((rgba[0] / 1000f) * 127f);
    						newMaterial.setShininess( shininessValue );
    					}
    				}
    				
					// If the line contains data for alpha (In the form: d 1.0)
					// Note: Alpha value of material: 0=transparent 1=opaque
    				else if (line.startsWith("d")) {
    					if ((rgba = readFloats(line)) != null) {
    						newMaterial.setAlpha( rgba[0] );
    					}
    				}
				
					// If the line contains illumination data (In the form: illum 1)
					// 1 for no emission (emission is black), 2 for lit (emission is specular).
    				else if (line.startsWith("illum")) {
    					if ((rgba = readFloats(line)) != null) {
    						if (rgba[0] == 2) {
    							newMaterial.setEmissionToSpecular();
    						}
    						else {
    							newMaterial.setEmissionToNone();
    						}
    					}
    				}
				
					// If the line contains data for a filename (In the form: map_Kd filename)
    				else if (line.startsWith("map_Kd")) {
    					// Add a texture to the material
    					String textureFile = line.substring(7);
    			        if (textureFile != null && !textureFile.equals("")) {
        					int textureHandle = 0;
        					
        					// Make the texture
    			        	try {
    					    	textureHandle = makeTexture(filepath + textureFile);
    			        	}
    			        	catch (Exception e) {
    			        		System.out.println("MaterialLibrary.loadMaterials(): could not load texture file (" + textureFile + ") " + e);
    			        	}
    			        	
    			        	// Set the texture to the new material
        					newMaterial.setTextureFile(textureFile);
        					newMaterial.setTexture(textureHandle);
    			        }
    				}
				}
			}
		}
		catch (Exception exception) {
			System.out.println("MaterialLibrary.loadMaterials() failed at line: " + line);
			System.out.println("MaterialLibrary.loadMaterials() error: " + exception);
		}
		
		// For debugging purposes:
		System.out.println("GLMaterialLib.loadMaterials(): loaded " + loadedMaterials.size() + " materials ");
		
	    // Return the array of materials
	    materials = new Material[loadedMaterials.size()];
	    loadedMaterials.toArray(materials);
	}
	
	/**
	 * Read the floats from the line, 
	 * and always return a float array with 4 elements
	 * 
	 * @param line the line to read the values from
	 * @return an array containing the float data read from the line (4 elements)
	 */
    private float[] readFloats(String line)
    {
    	try
    	{
    		// Create a StringTokenizer from the line and throw out the identifier (Kd, Ka, etc.)
    		StringTokenizer st = new StringTokenizer(line, " ");
    		st.nextToken();
    		
    		// If the line contains shininess or alpha data, make the last three elements 0
    		if (st.countTokens() == 1) {
    			return new float[] {Float.parseFloat(st.nextToken()), 0f, 0f, 0f};
    		}
    		
    		// If the line contains RGB data, make an RGBA array with A forced to 1
    		else if (st.countTokens() == 3) {
    			return new float[] {Float.parseFloat(st.nextToken()),
    					Float.parseFloat(st.nextToken()),
    					Float.parseFloat(st.nextToken()),
    					1f };
    		}
    	}
    	catch (Exception e)
    	{
    		System.out.println("MaterialLiberary.read3Floats(): error on line '" + line + "', " + e);
    	}
    	return null;
    }
    
    /**
     * find a material by name in an array of Material objects
     * return the array index of the material
     * 
     * @param materialName the name of the material
     * @return the index of the material in the array (or -1 if not found)
     */
    public int findID (String materialName) {
    	if (materials != null && materialName != null) {
    		for (int material = 0; material < materials.length; material++) {
    			if (materials[material].getName().equals(materialName)) {
    				return material;
    			}
    		}
    	}
    	return -1;
    }

	/**
	 * Create a texture and mipmap from the given image file.
	 * 
	 * @param textureImagePath the path and name of the image file
	 * @return the texture handle of the image
	 */
	public static int makeTexture(String textureImagePath)
	{
		int textureHandle = 0;
		CustomImage textureImage = loadImage(textureImagePath);
		if (textureImage != null) {
			textureHandle = makeTexture(textureImage);
		}
		return textureHandle;
	}
	
	/**
     * Load an image from the given file and return an Image object.
	 *  
     * @param ImageFilename the name of the image file
     * @return the loaded Image object
     */
    public static CustomImage loadImage(String imageFilename) {
    	CustomImage newImage = new CustomImage(imageFilename, true, true);
    	if (newImage.isLoaded()) {
    		return newImage;
    	}
    	return null;
    }
	
	/**
     * Create a texture from the given image.
     * @return the texture handle of the image
     */
    public static int makeTexture(CustomImage textureImage)
    {
    	if (textureImage != null ) {
    		return makeTexture(textureImage.pixelBufferRGBA, textureImage.getWidth(), textureImage.getHeight(), false);
    	}
    	return 0;
    }
    
    /**
     * Returns the materials in the group
     * 
     * @return the array of materials
     */
    public Material [] getMaterials () {
    	return materials;
    }
    
    /**
     * Returns a material from the material library
     * 
     * @param materialID the index of the material
     * @return the material needed
     */
    public Material getMaterial(int materialID) {
    	return materials [materialID];
    }
    
    /**
     * Create a texture from the given pixels in the default OpenGL RGBA pixel format.
     * Configure the texture to repeat in both directions and use LINEAR for magnification.
     * <P>
     * Note: Method made by napier @ potatoland.org\
     * 
     * @param pixels the ByteBuffer containing the pixels for the texture
     * @return  the texture handle
     */
    public static int makeTexture(ByteBuffer pixels, int w, int h, boolean anisotropic)
    {
    	// a new empty texture handle
    	int textureHandle = allocateTexture();
    	// preserve currently bound texture, so glBindTexture() below won't affect anything)
    	GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
    	// 'select' the new texture by it's handle
    	GL11.glBindTexture(GL11.GL_TEXTURE_2D,textureHandle);
    	// set texture parameters
    	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
    	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
    	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR); //GL11.GL_NEAREST);
    	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); //GL11.GL_NEAREST);

    	// make texture "anisotropic" so it will 'min'ify more gracefully
    	if (anisotropic && KouchKarting.extensionExists("GL_EXT_texture_filter_anisotropic")) {
    		// Due to LWJGL buffer check, you can't use smaller sized buffers (min_size = 16 for glGetFloat()).
    		FloatBuffer max_a = KouchKarting.allocFloats(16);
    		// Grab the maximum anisotropic filter.
    		GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max_a);
    		// Set up the anisotropic filter.
    		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, max_a.get(0));
    	}

    	// Create the texture from pixels
    	GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
    			0,                              // level of detail
    			GL11.GL_RGBA8,                  // internal format for texture is RGB with Alpha
    			w, h,                           // size of texture image
    			0,                              // no border
    			GL11.GL_RGBA,                   // incoming pixel format: 4 bytes in RGBA order
    			GL11.GL_UNSIGNED_BYTE,  		// incoming pixel data type: unsigned bytes
    			pixels);                       	// incoming pixels

    	// restore previous texture settings
    	GL11.glPopAttrib();

    	return textureHandle;
    }
    
    /**
     * Allocate a texture (glGenTextures) and return the handle to it.
     * <P>
     * Note: method made by napier @ potatoland.org
     * 
     * @return the texture handle for the texture
     */
    public static int allocateTexture()
    {
    	IntBuffer textureHandle = KouchKarting.allocInts(1);
    	GL11.glGenTextures(textureHandle);
    	return textureHandle.get(0);
    }
}
