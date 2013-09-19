package importer;

/**
 * The "Face" Class. 
 * Purpose: describes a single face in a model,
 * and contains data for the vertex, texture and normal indices.
 * <P>
 * A face can be any shape that can be formed from adjacent triangles
 * and holds all the data for those triangles
 * 
 * @author Peter G.
 * @version Jan. 24, 2012
 */
public class Face {
	// Arrays for the vertex, texture and normal indices
	private int [] vertexIDs;
	private int [] textureIDs;
	private int [] normalIDs;
	private int materialID;
	
	/**
	 * Create a new face that contains data for vertices, textures
	 * and normals.
	 * 
	 * @param vertexIDs the array containing the vertex indices
	 * @param textureIDs the array containing the texture indices
	 * @param normalIDs the array containing the normal indices
	 */
	Face(int[] vertexIDs, int[] textureIDs, int[] normalIDs) {
		this.vertexIDs = new int[vertexIDs.length];
		this.textureIDs = new int[textureIDs.length];
		this.normalIDs = new int[normalIDs.length];
		if (vertexIDs != null)
			System.arraycopy(vertexIDs, 0, this.vertexIDs, 0, vertexIDs.length);
		if (textureIDs != null)
			System.arraycopy(textureIDs, 0, this.textureIDs, 0, textureIDs.length);
		if (normalIDs != null)
			System.arraycopy(normalIDs, 0, this.normalIDs, 0, normalIDs.length);
	}
	
	/**
	 * Gets the number of triangles in the face 
	 * (always equal to 2 less than the number of vertices)
	 * 
	 * @return the number of triangles in the face or 0 if a bad face
	 */
	public int getNoOfTriangles() {
        if (vertexIDs == null || vertexIDs.length < 3) {
            return 0;  
        }
        return vertexIDs.length - 2;
    }
	
	/**
     * Assigns a material index to the face
     * 
     * @param materialID the material index to assign to the face
     */
    public void assignMaterial (int materialID) {
    	this.materialID = materialID;
    }
    
    /**
     * Returns the array containing the vertex indices for the face
     * 
     * @return the array containing the vertex indices
     */
    public int [] getVertexIDs (){
    	return vertexIDs;
    }
    
    /**
     * Returns the array containing the texture indices for the face
     * 
     * @return the array containing the texture indices
     */
    public int [] getTextureIDs (){
    	return textureIDs;
    }
    
    /**
     * Returns the array containing the normal indices for the face
     * 
     * @return the array containing the normal indices
     */
    public int [] getNormalIDs (){
    	return normalIDs;
    }
    
    /**
     * Returns the material ID of the face
     * 
     * @return the material ID
     */
    public int getMaterialID (){
    	return materialID;
    }
}
