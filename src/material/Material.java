package material;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;

import application.KouchKarting;

/**
 * The "Material" Class.
 * Purpose: holds data for a material that will be loaded into openGL later
 * 
 * @author Peter G.
 * @version Jan. 24, 2012
 *
 */
public class Material {
	// A sampling of colour values (Provided by napier @ potatoland.org)
	// Only the ones that the program uses are uncommented in order to save memory
	
    // private static final float colourClear[]          = {  0f,  0f,  0f,  0f};
    // private static final float colourGreen[]          = {  0f,  1f,  0f,  1f};
    // private static final float colourBlue[]           = {  0f,  0f,  1f,  1f};
    // private static final float colourYellow[]         = {  1f,  1f,  0f,  1f};
    // private static final float colourCyan[]           = {  0f,  1f,  1f,  1f};
    // private static final float colourMagenta[]        = {  1f,  0f,  1f,  1f};
    // private static final float colourGrayLight[]      = { .8f, .8f, .8f,  1f};
    // private static final float colourGrayMedium[]     = { .5f, .5f, .5f,  1f};
    // private static final float colourGrayDark[]       = { .2f, .2f, .2f,  1f};
    // private static final float colourWhite[]          = {  1f,  1f,  1f,  1f};
    // private static final float colourBlack[]          = {  0f,  0f,  0f,  1f};
    // private static final float colourBeige[]          = { .7f, .7f, .4f,  1f};
	private static final float colourNone[]           = {  0f,  0f,  0f,  1f};
    public static final float colourRed[]            = {  1f,  0f,  0f,  1f};
    private static final float colourDefaultDiffuse[] = { .8f, .8f, .8f,  1f};
    private static final float colourDefaultAmbient[] = { .2f, .2f, .2f,  1f};
    private static final float minShine   = 0.0f;
    private static final float maxShine   = 127.0f;	

    // The colour values for this material
    private FloatBuffer diffuse;      	// Colour of the lit surface
    private FloatBuffer ambient;      	// Colour of the shadowed surface
    private FloatBuffer specular;     	// Reflection colour (typically this is a shade of gray)
    private FloatBuffer emission;     	// Glow colour
    private FloatBuffer shininess;    	// Size of the reflection highlight

    // hold name and texture values for this material
    private String materialName = "noname";  	// Name of this material in the .mtl and .obj files
    private String textureFile = null;  	// Texture filename (null if no texture)
    private int textureHandle = 0;      	// OpenGL handle to the texture (0 if no texture)
    
    /**
     * Create a default material
     */
    public Material() {
        setDefaults();
    }
    
    /**
     * Create a default material with the given colour
     * @param colour a float array containing values for the colour
     */
    public Material(float[] colour) {
        setDefaults();
        setColour(colour);
    }
    
    /**
	 *  Set the material to OpenGL's default values (gray, no reflection and no glow)
	 */
    public void setDefaults() {
        setDiffuse(colourDefaultDiffuse);
        setAmbient(colourDefaultAmbient);
        setSpecular(colourNone);
        setEmissionToNone();
        setShininess(minShine);
    }
    
    /**
	 *  Set the diffuse material colour.  This is the colour of the material
	 *  where it is directly lit.
	 *  
	 *  @param colour the colour to assign to diffuse
	 */
    public void setDiffuse(float[] colour) {
        diffuse = KouchKarting.allocFloats(colour);
    }

	/**
	 *  Set the ambient material colour.  This is the colour of the material
	 *  where it is lit by indirect light (light scattered off the environment).
	 *  I.e. the shadowed side of an object.
	 *  
	 *  @param colour the colour to assign to ambient
	 */
    public void setAmbient(float[] colour) {
        ambient = KouchKarting.allocFloats(colour);
    }

	/**
	 *  Set the specular material colour.  This controls how much light
	 *  is reflected off a glossy surface.  This colour value describes
	 *  the brightness of the reflection and is typically a shade of gray.
	 *  Pure black means that no light is reflected (I.e. a very rough matte
	 *  surface).  Pure white means that the surface is highly reflective.
	 *
	 *  @param colour the colour to assign to specular
	 */
    public void setSpecular(float[] colour) {
        specular = KouchKarting.allocFloats(colour);
    }
    
    /**
     * Set the emission value to specular (will be called when an "illum"
     * line indicates if there is emission or not)
     */
    public void setEmissionToSpecular() {
    	emission = specular;
    }
    
    /**
     * Set the emission value to default (no colour emitted)
     */
    public void setEmissionToNone() {
    	emission = KouchKarting.allocFloats(colourNone);
    }

    /**
     *  Set size of the reflection highlight.  Must also set the specular colour for
     *  shininess to have any effect.
     *
     * @param shininessValue how sharp the reflection is: 0 - 127 (127 = very sharp)
     */
    public void setShininess(float shininessValue) {
        if (shininessValue >= minShine && shininessValue <= maxShine) {
            float[] tmp = {shininessValue,0,0,0};
            shininess = KouchKarting.allocFloats(tmp);
        }
    }
    
    /**
     * alpha value is set in the diffuse material colour.  Other material
     * colours are not affected by alpha value.
     * 
     * @param alpha the alpha value to be assigned  0 - 1
     */
    public void setAlpha(float alpha) {
        diffuse.put(3, alpha);
    }
    
    /**
	 *  Sets the material colour to approximate a "real" surface colour.
	 *  
	 *  @param colour the colour to be replicated in the material
	 */
    public void setColour(float[] colour) {
        setDiffuse(colour);
        setAmbient(colour);
    }
    
    /**
     * Set the name of the material name
     *
     * @param name the name of the material
     */
    public void setName(String name) {
        materialName = name;
    }
    
    /**
     * Store the texture filename
     * 
     * @param filename the filename to be stored
     */
    public void setTextureFile(String filename) {
        textureFile = filename;
    }
    
    /**
     * Assign a texture handle
     * 
     * @param handleValue
     */
    public void setTexture(int handleValue) {
        textureHandle = handleValue;
    }
    
    /**
     * Gives the name of the material
     * 
     * @return then name of the material
     */
    public String getName() {
    	return materialName;
    }
    
    /**
     * Returns the texture handle for the material
     * 
     * @return the texture handle for the material
     */
    public int getTextureHandle () {
    	return textureHandle;
    }
    
    /**
     *  Call glMaterial() to activate these material properties in the OpenGL environment.
     *  These properties will stay in effect until you change them or disable lighting.
     */
    public void apply() {
    	// GL_FRONT: affect only front facing triangles
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, diffuse);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, ambient);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, specular);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, emission);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SHININESS, shininess);
    }
}
