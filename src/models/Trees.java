package models;

import importer.OBJImporter;
import material.Material;
import math.Vector;
import mesh.Mesh;
import mesh.Triangle;

import org.lwjgl.opengl.GL11;

/**
 * The "Trees" Class. 
 * Purpose: This is the object that holds the data and draws
 * all of the trees on the track. Because the model is the same, making one
 * class with arrays for positions and orientation makes the program much more
 * efficient
 * 
 * @author Peter G.
 * @version Jan. 24, 2012
 */
public class Trees {

	// The mesh for a tree
	private Mesh treeMesh;

	// Array vectors for the positions, look at points and up directions of each
	// of the trees
	private Vector[] positions;
	private Vector[] lookAts;
	private Vector[] upVectors;

	// The display list ID
	private int displayListID;

	// The default material to use if none was loaded
	Material defaultMtl = new Material();

	/**
	 * Creates a new tree class with arrays for the positions, up directions and
	 * look at points of all the trees. Loads the tree model from the given filename
	 * 
	 * @param filename the name of the file containing data for the tree
	 * @param positions the positions of all the trees
	 * @param upVectors the up directions of all the trees
	 * @param lookAts the look at points of all the trees
	 */
	public Trees(String filename, Vector[] positions, Vector[] upVectors,
			Vector[] lookAts) {
		// Load the mesh
		treeMesh = loadMesh(filename);
		
		// Copy the given values to local variables
		this.positions = positions;
		this.upVectors = upVectors;
		this.lookAts = lookAts;
		
		// Set the display list ID to the default 0
		displayListID = 0;
	}

	/**
	 * loads the mesh using the OBJImporter
	 * 
	 * @param filename
	 *            the name of the OBJ file
	 * @return the mesh loaded
	 */
	public Mesh loadMesh(String filename) {
		OBJImporter importer = new OBJImporter();
		return importer.load(filename);
	}

	/**
	 * Returns the total number of trees
	 * 
	 * @return the total number of trees
	 */
	public int getNumberOfTrees() {
		return positions.length;
	}

	/**
	 * Returns all the positions of the trees
	 * 
	 * @return the array containing the positions of the trees
	 */
	public Vector[] getPositions() {
		return positions;
	}

	/**
	 * Returns all the look at points of the trees
	 * 
	 * @return all the look at points
	 */
	public Vector[] getLookAts() {
		return lookAts;
	}

	/**
	 * Returns all the up vectors of the trees
	 * 
	 * @return all the up vectors
	 */
	public Vector[] getUpVectors() {
		return upVectors;
	}

	/**
	 * Draw the model using the triangle data in the mesh
	 */
	public void render() {

		// Get the loaded materials and initialise necessary variables
		Material[] materials = treeMesh.materials;
		Material material;
		Triangle drawTriangle;
		int currentMaterial = -1;
		int triangle = 0;

		// For each triangle in the object
		for (triangle = 0; triangle < treeMesh.triangles.length;) {

			// Get the triangle that needs to be drawn
			drawTriangle = treeMesh.triangles[triangle];

			// Activate a new material and texture
			currentMaterial = drawTriangle.materialID;
			material = (materials != null && materials.length > 0 && currentMaterial >= 0) ? materials[currentMaterial]
					: defaultMtl;
			material.apply();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, material.getTextureHandle());

			// Draw triangles until material changes
			GL11.glBegin(GL11.GL_TRIANGLES);
			while (triangle < treeMesh.triangles.length && drawTriangle != null
					&& currentMaterial == drawTriangle.materialID) {

				GL11.glTexCoord2f(drawTriangle.texture1.x,
						drawTriangle.texture1.y);
				GL11.glNormal3f(drawTriangle.normal1.x, drawTriangle.normal1.y,
						drawTriangle.normal1.z);
				GL11.glVertex3f((float) drawTriangle.point1.pos.x,
						(float) drawTriangle.point1.pos.y,
						(float) drawTriangle.point1.pos.z);

				GL11.glTexCoord2f(drawTriangle.texture2.x,
						drawTriangle.texture2.y);
				GL11.glNormal3f(drawTriangle.normal2.x, drawTriangle.normal2.y,
						drawTriangle.normal2.z);
				GL11.glVertex3f((float) drawTriangle.point2.pos.x,
						(float) drawTriangle.point2.pos.y,
						(float) drawTriangle.point2.pos.z);

				GL11.glTexCoord2f(drawTriangle.texture3.x,
						drawTriangle.texture3.y);
				GL11.glNormal3f(drawTriangle.normal3.x, drawTriangle.normal3.y,
						drawTriangle.normal3.z);
				GL11.glVertex3f((float) drawTriangle.point3.pos.x,
						(float) drawTriangle.point3.pos.y,
						(float) drawTriangle.point3.pos.z);

				triangle++;
				if (triangle < treeMesh.triangles.length)
					drawTriangle = treeMesh.triangles[triangle];
			}
			GL11.glEnd();
		}
	}

	/**
	 * Render mesh into a displayList and store the listID, making the program
	 * run a lot faster
	 */
	public void makeDisplayList() {
		if (displayListID == 0) {
			displayListID = GL11.glGenLists(1); // Allocate a display list
			GL11.glNewList(displayListID, GL11.GL_COMPILE); // Start the list
			render(); // render the mesh
			GL11.glEndList(); // End the list
		}
	}

	/**
	 * Returns the display list ID for the tree
	 * 
	 * @return the display list ID for the tree
	 */
	public int getDisplayListID() {
		return displayListID;
	}
}
