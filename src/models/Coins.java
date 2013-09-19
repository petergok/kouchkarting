package models;

import importer.OBJImporter;
import material.Material;
import math.Vector;
import mesh.Mesh;
import mesh.Triangle;

import org.lwjgl.opengl.GL11;

import application.KouchKarting;

/**
 * The "Coins" Class. 
 * Purpose: Holds all the data for the coins on the track,
 * allowing for faster drawing
 * 
 * @author Peter G.
 * @version Jan. 24, 2012
 */
public class Coins {

	// The mesh for the coin
	private Mesh coinMesh;

	// Vectors for position and orientation of all the coins
	private Vector[] positions;
	private Vector[] lookAts;
	private Vector[] upVectors;
	private Vector[] rightVectors;
	private Vector[] directions;

	// Keep track of which coins were collected
	private boolean[] collected;

	// The display list ID
	private int displayListID = 0;

	// The spin speeds for all the coins
	private float[] spinSpeeds;

	// A final variable to hold the value of PI over 180 to convert to radians
	static final float PIdiv180 = 0.0174532925f;

	// The default material to use if no material was loaded
	Material defaultMtl = new Material();

	/**
	 * Create a new coins class with arrays for the positions, up directions,
	 * look at points, right directions and spin speeds for each coin. Also,
	 * loads the mesh for the coin from the given filename.
	 * 
	 * @param filename
	 *            the filename of the .obj file containing teh data for the coin
	 * @param positions
	 *            the positions of all the coins
	 * @param upVectors
	 *            the up vectors of all the coins
	 * @param lookAts
	 *            the look at points of all the coins
	 * @param rightVectors
	 *            the right vectors of all the coins
	 * @param spinSpeeds
	 *            the spin speeds of all the coins
	 */
	public Coins(String filename, Vector[] positions, Vector[] upVectors,
			Vector[] lookAts, Vector[] rightVectors, float[] spinSpeeds) {

		// Load the coin mesh from the file
		coinMesh = loadMesh(filename);

		// Copy given values to local variables
		this.positions = positions;
		this.upVectors = upVectors;
		this.lookAts = lookAts;
		this.rightVectors = rightVectors;
		this.spinSpeeds = spinSpeeds;

		// Set all coins to not collected and calculate their directions
		collected = new boolean[positions.length];
		directions = new Vector[positions.length];
		for (int coin = 0; coin < positions.length; coin++) {
			directions[coin] = Vector.sub(lookAts[coin], positions[coin]);
			collected[coin] = false;
		}
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
	 * Returns the total number of coins
	 * 
	 * @return the total number of coins
	 */
	public int getNumberOfCoins() {
		return positions.length;
	}

	/**
	 * Returns the positions of all the coins
	 * 
	 * @return the positions
	 */
	public Vector[] getPositions() {
		return positions;
	}

	/**
	 * Returns the look at points of all the coins
	 * 
	 * @return the look at points
	 */
	public Vector[] getLookAts() {
		return lookAts;
	}

	/**
	 * Returns the up vectors of all the coins
	 * 
	 * @return the up vectors
	 */
	public Vector[] getUpVectors() {
		return upVectors;
	}

	/**
	 * Returns if the indicated coin was collected
	 * 
	 * @param whichOne
	 *            which coin to check
	 * @return if that coin was collected
	 */
	public boolean isCollected(int whichOne) {
		return collected[whichOne];
	}

	/**
	 * Sets the indicated coin to be collected
	 * 
	 * @param whichOne
	 *            which coin to set to collected
	 */
	public void collectCoin(int whichOne) {
		collected[whichOne] = true;
	}

	/**
	 * Set all coins to collected
	 */
	public void collectedAll() {
		for (int coin = 0; coin < positions.length; coin++) {
			collected[coin] = true;
		}
	}

	/**
	 * Set all coins to not collected
	 */
	public void collectedNone() {
		for (int coin = 0; coin < positions.length; coin++) {
			collected[coin] = false;
		}
	}

	/**
	 * Spin all the coins based on their spin speeds
	 */
	public void spin() {
		for (int coin = 0; coin < positions.length; coin++) {
			RotateY(spinSpeeds[coin] * KouchKarting.getSecondsPerFrame(), coin);
			lookAts[coin] = Vector.add(directions[coin], positions[coin]);
		}
	}

	/**
	 * Rotates a coin around its own y axis the given degrees
	 * 
	 * @param angle
	 *            the number of degrees to rotate the coin
	 * @param whichOne
	 *            which coin to rotate
	 */
	public void RotateY(float angle, int whichOne) {

		// Rotate realDirection around the up vector
		directions[whichOne] = Vector.normalize(Vector.sub(
				Vector.multiply(directions[whichOne],
						(float) Math.cos(angle * PIdiv180)),
				Vector.multiply(rightVectors[whichOne],
						(float) Math.sin(angle * PIdiv180))));

		// Calculate the new RightVector (by cross product)
		rightVectors[whichOne] = Vector.crossProduct(directions[whichOne],
				upVectors[whichOne]);
	}

	/**
	 * Draw the model using the triangle data in the mesh
	 */
	public void render() {

		// Get the loaded materials and initialise necessary variables
		Material[] materials = coinMesh.materials;
		Material material;
		Triangle drawTriangle;
		int currentMaterial = -1;
		int triangle = 0;

		// For each triangle in the object
		for (triangle = 0; triangle < coinMesh.triangles.length;) {

			// Get the triangle that needs to be drawn
			drawTriangle = coinMesh.triangles[triangle];

			// Activate a new material and texture
			currentMaterial = drawTriangle.materialID;
			material = (materials != null && materials.length > 0 && currentMaterial >= 0) ? materials[currentMaterial]
					: defaultMtl;
			material.apply();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, material.getTextureHandle());

			// Draw triangles until material changes
			GL11.glBegin(GL11.GL_TRIANGLES);
			while (triangle < coinMesh.triangles.length && drawTriangle != null
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
				if (triangle < coinMesh.triangles.length)
					drawTriangle = coinMesh.triangles[triangle];
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
	 * Returns the display list ID for the coin
	 * 
	 * @return the display list ID for the coin
	 */
	public int getDisplayListID() {
		return displayListID;
	}
}
