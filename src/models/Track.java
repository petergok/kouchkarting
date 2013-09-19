package models;

import importer.OBJImporter;
import material.Material;
import math.Vector;
import mesh.Mesh;
import mesh.Triangle;

import org.lwjgl.opengl.GL11;

/**
 * The "Track" Class. 
 * Purpose: The simplest model class to hold onto the track's
 * data and draw it
 * 
 * @author Peter G.
 * @version Jan. 24, 2012
 */
public class Track {

	// The mesh for the track
	private Mesh trackMesh;

	// Vectors for the position and orientation of the track
	private Vector position;
	private Vector lookAt;
	private Vector upVector;

	// The display list ID
	private int displayListID = 0;

	// The default material to use if none were loaded
	Material defaultMtl = new Material();

	/**
	 * Create a new track with the given position, up vector direction and load
	 * the model from the given filename
	 * 
	 * @param filename the name of the file containing the data for the track
	 * @param position the position of the track
	 * @param upVector the up vector of the track
	 * @param lookAt the tack's lookAt point
	 */
	public Track(String filename, Vector position, Vector upVector,
			Vector lookAt) {
		// Load the mesh from the given filename
		trackMesh = loadMesh(filename);
		
		// Copy given values to local variables
		this.position = position;
		this.upVector = upVector;
		this.lookAt = lookAt;
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
	 * Returns the position of the track
	 * 
	 * @return the position
	 */
	public Vector getPosition() {
		return position;
	}

	/**
	 * Returns the look at point of the track
	 * 
	 * @return the look at point
	 */
	public Vector getLookAt() {
		return lookAt;
	}

	/**
	 * Returns the track's up vector
	 * 
	 * @return the up vector
	 */
	public Vector getUpVector() {
		return upVector;
	}
	
	/**
	 * Returns the mesh for the track
	 * 
	 * @return the mesh
	 */
	public Mesh getMesh () {
		return trackMesh;
	}

	/**
	 * Draw the model using the triangle data in the mesh
	 */
	public void render() {

		// Get the loaded materials and initialise necessary variables
		Material[] materials = trackMesh.materials;
		Material material;
		Triangle drawTriangle;
		int currentMaterial = -1;
		int triangle = 0;

		// For each triangle in the object
		for (triangle = 0; triangle < trackMesh.triangles.length;) {

			// Get the triangle that needs to be drawn
			drawTriangle = trackMesh.triangles[triangle];

			// Activate a new material and texture
			currentMaterial = drawTriangle.materialID;
			material = (materials != null && materials.length > 0 && currentMaterial >= 0) ? materials[currentMaterial]
					: defaultMtl;
			material.apply();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, material.getTextureHandle());

			// Draw triangles until material changes
			GL11.glBegin(GL11.GL_TRIANGLES);
			while (triangle < trackMesh.triangles.length
					&& drawTriangle != null
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
				if (triangle < trackMesh.triangles.length)
					drawTriangle = trackMesh.triangles[triangle];
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
	 * Returns the display list ID for the track
	 * 
	 * @return the display list ID for the track
	 */
	public int getDisplayListID() {
		return displayListID;
	}

}
