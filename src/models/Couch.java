package models;

import importer.OBJImporter;
import material.Material;
import math.Vector;
import mesh.Mesh;
import mesh.Triangle;

import org.lwjgl.opengl.GL11;

import collision.WorldCollision;

import application.KouchKarting;

/**
 * The "Couch" Class. 
 * Purpose: A class for the couch models that the player will
 * be driving.
 * 
 * @author Peter G.
 * @version Jan. 24, 2012
 */
public class Couch {

	// The mesh for the couch
	private Mesh couchMesh;

	// The collision checker user for this couch, and the radius of the couch
	// for collision detection
	private WorldCollision collisionChecker;
	private Vector radius;

	// Main vectors for the orientation and position of the couch
	// The real and fake directions are used to make an impression the couch is
	// turning (turn the couch more when turning)
	private Vector position;
	private Vector lookAt;
	private Vector upVector;
	private Vector rightVector;
	private Vector realDirection;
	private Vector fakeDirection;

	// Vectors for the movement of the couch
	private Vector velocity;
	private Vector acceleration;
	private Vector friction;
	private Vector gravityVelocity;
	private Vector gravityAcceleration;

	// Vectors to hold the data for resetting the couch back to the track
	private Vector resetPosition;
	private Vector resetDirection;
	private Vector resetUpVector;
	private Vector resetRightVector;

	// Statistics for the couch that determine how it moves and turns, as well
	// as the price of the couch
	private float accelerationRate;
	private float brakingRate;
	private float frictionRate;
	private float maxSpeed;
	private float turnSpeed;
	private float maxReverseSpeed;
	private float grassFriction;
	private float normalFriction;
	private int price;

	// Track the total menu spin to draw in the couch menu
	private float totalMenuSpin;

	// The material the couch is on
	private String materialOn;

	// Booleans to track what the couch is doing during the frame
	private boolean onTheRoad;
	private boolean onTheGround;
	private boolean checkedCollision;
	private boolean turnRight;
	private boolean turnLeft;

	// The display list index of the couch
	private int displayListID;

	// A final variable to convert to radians easily
	final float PIdiv180 = 0.0174532925f;

	// The default material to draw with if none were loaded
	Material defaultMtl = new Material();

	/**
	 * Creates a new couch, initialising all the variables for it and loading
	 * the model using an OBJImporter
	 * 
	 * @param filename
	 *            the name of the file that contains the model data
	 * @param position
	 *            the position of the couch
	 * @param upVector
	 *            the up direction of the couch
	 * @param lookAt
	 *            where the couch is looking
	 * @param rightVector
	 *            the right vector of the couch
	 * @param accelerationRate
	 *            the couch's acceleration rate
	 * @param maxSpeed
	 *            the couce's maximum speed
	 * @param grassFriction
	 *            the couce's friction rate on grass
	 * @param price
	 *            the couch's price
	 */
	public Couch(String filename, Vector position, Vector upVector,
			Vector lookAt, Vector rightVector, int accelerationRate,
			int maxSpeed, int grassFriction, int price) {
		// Load the mesh for the couch
		couchMesh = loadMesh(filename);

		// Copy the given orientation variables to local ones
		this.position = position;
		this.upVector = upVector;
		this.rightVector = rightVector;
		this.lookAt = lookAt;

		// Copy the other variables to the local ones
		this.maxSpeed = maxSpeed;
		this.accelerationRate = accelerationRate;
		this.grassFriction = grassFriction;
		this.price = price;

		// Set initial values to the gravity vectors
		gravityAcceleration = new Vector(0, 0, 0);
		gravityVelocity = new Vector();

		// Calculate the diriction the couch is facing and set it to both the
		// real and fake direciton
		realDirection = Vector.sub(lookAt, position);
		fakeDirection = Vector.sub(lookAt, position);

		// Set the velocity, acceleration and friction to initial values
		velocity = new Vector();
		acceleration = new Vector();
		friction = new Vector();

		// Set other values for reverse speeds, baking rate, normal friction,
		// the friction rate applied on the couch (will be set to other values
		// later), and the turn speed (will be set to other values later)
		maxReverseSpeed = 100;
		brakingRate = -500;
		normalFriction = -200;
		frictionRate = 0;
		turnSpeed = 0;

		// Set the tracking variables to initial values
		onTheGround = false;
		turnRight = false;
		turnLeft = false;
		checkedCollision = false;

		// Calculate the radius of the mesh
		radius = new Vector(couchMesh.rightmostPoint - couchMesh.leftmostPoint,
				couchMesh.highestPoint - couchMesh.lowestPoint,
				couchMesh.farthestPoint - couchMesh.nearestPoint);

		// Set the initial reset positions
		resetPosition = new Vector(position);
		resetDirection = new Vector(realDirection);
		resetUpVector = new Vector(upVector);
		resetRightVector = new Vector(rightVector);

		// Set other variables to default values
		displayListID = 0;
		totalMenuSpin = 0;
	}

	/**
	 * Changes the orientation and position vectors of the couch directly
	 * 
	 * @param position
	 *            the position the couch should be moved to
	 * @param upVector
	 *            the up direction of the couch
	 * @param lookAt
	 *            the look at point of the couch
	 * @param rightVector
	 *            the right vector of the couch
	 */
	public void changeVectors(Vector position, Vector upVector, Vector lookAt,
			Vector rightVector) {

		// Copy given values to local variables
		this.position = position;
		this.upVector = upVector;
		this.lookAt = lookAt;
		this.rightVector = rightVector;

		// Recalculate the real and fake directions (both the same)
		realDirection = Vector.sub(lookAt, position);
		fakeDirection = Vector.sub(lookAt, position);
	}

	/**
	 * Loads the mesh using the OBJImporter
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
	 * Returns the price of the couch
	 * 
	 * @return the price of the couch
	 */
	public int getPrice() {
		return price;
	}

	/**
	 * Returns the maximum speed of the couch
	 * 
	 * @return the max speed
	 */
	public float getMaxSpeed() {
		return maxSpeed;
	}

	/**
	 * Returns the acceleration rate of the couch
	 * 
	 * @return the acceleration rate
	 */
	public float getAccelerationRate() {
		return accelerationRate;
	}

	/**
	 * Returns the grass friction rate of the couch
	 * 
	 * @return the grass friciton rate
	 */
	public float getGrassFriction() {
		return grassFriction;
	}

	/**
	 * Returns the position of the couch
	 * 
	 * @return the position of the couch
	 */
	public Vector getPosition() {
		return new Vector(position);
	}

	/**
	 * Returns the look at point of the couch
	 * 
	 * @return the look at point
	 */
	public Vector getLookAt() {
		return new Vector(lookAt);
	}

	/**
	 * Returns the couch's up vector
	 * 
	 * @return the couch's up vector
	 */
	public Vector getUpVector() {
		return new Vector(upVector);
	}

	/**
	 * Returns the couch's real direction
	 * 
	 * @return the couch's real direction
	 */
	public Vector getRealDirection() {
		return new Vector(realDirection);
	}

	/**
	 * Returns the couch's fake (what it appears to be) direction
	 * 
	 * @return the couch's fake direction
	 */
	public Vector getFakeDirection() {
		return new Vector(fakeDirection);
	}

	/**
	 * Resets the couches velocity to the given value
	 * 
	 * @param velocity
	 *            the velocity to be set
	 */
	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}

	/**
	 * Accelerates the couch by making an acceleration vector in the direction
	 * the couch is facing, but only if the maximum speed hasn't been reached
	 */
	public void accelerate() {
		if (velocity.length() < maxSpeed) {
			acceleration = Vector.normalize(realDirection)
					.mult(accelerationRate)
					.mult(KouchKarting.getSecondsPerFrame());
		}
	}

	/**
	 * Makes the couch brake by making an acceleration vector in the opposite
	 * direction the couch is facing
	 */
	public void brake() {
		acceleration = Vector.normalize(realDirection).mult(brakingRate)
				.mult(KouchKarting.getSecondsPerFrame());
	}

	/**
	 * Tells the couch to turn left the next frame
	 */
	public void turnLeft() {
		turnLeft = true;
	}

	/**
	 * Tells the couch to turn right the next frame
	 */
	public void turnRight() {
		turnRight = true;
	}

	/**
	 * Makes the couch spin a certain given amount
	 * 
	 * @param spinSpeed
	 *            the amount the couch should spin in a second
	 * @return how much the couch spun that frame
	 */
	public float spin(int spinSpeed) {

		// Keeping track of the total spin, rotate the couch around the Y axis
		totalMenuSpin += spinSpeed * KouchKarting.getSecondsPerFrame();
		RotateY(spinSpeed * KouchKarting.getSecondsPerFrame());

		// Calculate the new look at vector
		lookAt = Vector.add(fakeDirection, position);

		// Return how much the couch spun
		return spinSpeed * KouchKarting.getSecondsPerFrame();
	}

	/**
	 * Rotates the couch so it matches the spin needed
	 * 
	 * @param spinAmount
	 *            the amount the couch should be spun
	 */
	public void startSpin(int spinAmount) {

		// Rotate the couch the amount left to achieve the spin needed
		RotateY(spinAmount - totalMenuSpin);
		totalMenuSpin = spinAmount;

		// Calculate the new look at vector
		lookAt = Vector.add(fakeDirection, position);
	}

	/**
	 * Check for collisions, and using the collision detector results, move the
	 * couch
	 * 
	 * @param world
	 *            the world mesh that the couch is colliding with
	 */
	public void checkCollisionsAndMove(Mesh world) {

		// Only check and move if the couch is moving
		if (velocity.length() != 0 || gravityVelocity.length() != 0) {

			// Create a new collision checker
			collisionChecker = new WorldCollision(
					new Vector(velocity)
							.mult(KouchKarting.getSecondsPerFrame()),
					new Vector(gravityVelocity).mult(KouchKarting
							.getSecondsPerFrame()), new Vector(position),
					new Vector(radius).mult(0.5f), new Vector(upVector));

			// Collide with the world using the collision checker
			collisionChecker.collideAndSlide(world);

			// Keep track that collision was checked (it is not checked when the
			// couch isn't moving)
			checkedCollision = true;

			// Check which material the couch collided with, set to "none" if
			// there was no collision
			if (collisionChecker.getCollisionTriangle() != null) {
				materialOn = world.materials[collisionChecker
						.getCollisionTriangle().materialID].getName().trim();
			} else {
				materialOn = "none";
			}

			// If the material is on the grass
			if (materialOn.equals("Grass")) {

				// If the couch just moved from the road to the grass, change
				// the reset positions to that section
				if (onTheRoad) {
					resetPosition = new Vector(position);
					resetDirection = new Vector(realDirection);
					resetUpVector = new Vector(upVector);
					resetRightVector = new Vector(rightVector);
				}

				// Keep track that the couch is not on the road
				onTheRoad = false;
			}

			// If the couch is on the road, keep track that it is on the road
			else if (materialOn.equals("Road")
					|| materialOn.equals("Checkerboard")) {
				onTheRoad = true;
			}

			// Move the couch
			move();
		}
	}

	/**
	 * Moves the couch based on the results of the collision detection
	 */
	public void move() {
		// If the couch is on the bounce material, make the couch bounce
		// backwards
		if (materialOn.equals("Bounce")) {
			velocity.normalize().mult(-200f);
			gravityVelocity.y = 100;
		}
		// If the couch is on the boost material, boost the couch forward, and a
		// little up for effect
		if (materialOn.equals("Boost")) {
			velocity.normalize().mult(1000f);
			gravityVelocity.y = 50;
		}

		// Calculate the change in position to see how much the couch moved
		Vector changeInPosition = Vector.sub(collisionChecker.getMoveTo(),
				position);

		// Move the couch to the new postion calculated by the collision checker
		position = collisionChecker.getMoveTo();

		// If the couch barely moved or is embedded in the mesh, make it jump up
		// so it doesn't get stuck
		if ((changeInPosition.length() < 0.05f && velocity.length() != 0)
				|| collisionChecker.isEmbedded()) {
			position.y += 0.2f;
		}

		// Set the couch's up vector to the collision normal to make it look
		// like the couch is going along the surface
		upVector = collisionChecker.getCollisionNormal();

		// If there was a collision with gravity, reset the gravity vector to 0.
		if (collisionChecker.foundGravityCollision() && gravityVelocity.y <= 0) {
			gravityVelocity = new Vector(0, 0, 0);
			onTheGround = true;
		}

		// Otherwise, make the couch upright again because it is in the air
		else {
			upVector = new Vector(0, 1, 0);
			onTheGround = false;
		}

		// Calculate the new real direction
		realDirection = Vector.crossProduct(upVector, rightVector);

		// Baed on the couch's position and velocity, turn the velocity of the
		// couch so it is the same way the couch is facing
		if (movingForward() && onTheGround && !materialOn.equals("Bounce")) {
			velocity = new Vector(realDirection).normalize().mult(
					velocity.length());
		} else if (onTheGround && !materialOn.equals("Bounce")) {
			velocity = new Vector(realDirection).normalize().mult(
					velocity.length());
			velocity.mult(-1);
		}

		// If the couch is off the screen below, reset it
		if (position.y < -300) {
			reset();
		}
	}

	/**
	 * Calculates the couche's velocity based on tis position, direction and
	 * acceleration
	 */
	public void calculateVelocity() {
		// If the current velocity is 0, simply add the acceleration to it
		if (velocity.equals(new Vector())) {
			velocity.add(acceleration);
		}

		// Otherwise,
		else {
			// If the couch is moving forward
			if (movingForward()) {

				// And it is accelerating backwards (braking)
				if (Vector.opposites(acceleration, realDirection)) {

					// And the acceleration is greater than the velocity, set
					// velocity to 9
					if (acceleration.length() > velocity.length()) {
						velocity = new Vector();
					}

					// Otherwise, just keep braking
					else {
						velocity.add(acceleration);
					}
				}

				// If it isn't accelerating backwards (speeding up), add it to
				// the velocity
				else {
					velocity.add(acceleration);
				}
			}

			// If the couch is moving backwards
			else {

				// And the acceleration is not opposite the way the couch is
				// facing (the couch braking in reverse)
				if (!Vector.opposites(acceleration, realDirection)) {

					// And the acceleration is greater than the velocity, reset
					// the velocity to 0
					if (acceleration.length() > velocity.length()) {
						velocity = new Vector();
					}

					// Otherwise, just add the acceleration to the velocity
					// (brake in reverse)
					else {
						velocity.add(acceleration);
					}
				}

				// If the couch is accelerating backwards, add the acceleration
				// to the velocity if the velocity has not reached the maximum
				// reverse speed
				else if (velocity.length() < maxReverseSpeed) {
					velocity.add(acceleration);
				}
			}
		}

		// Reset the acceleration
		acceleration = new Vector();
	}

	/**
	 * Apply friction to the couch by decreasing the acceleration vector
	 */
	public void applyFriction() {

		// If collisions were checked
		if (checkedCollision) {

			// And the couch is on the ground
			if (onTheGround) {

				// Make the friction based on what material the couch is on
				if (materialOn.equals("Grass")) {
					frictionRate = grassFriction;
				} else {
					frictionRate = normalFriction;
				}
			}

			// If the couch is not on the ground, make friction 0
			else {
				frictionRate = 0;
			}
		}

		// If collisions were not checked (the couch isn't moving), make the
		// friction rate 0
		else {
			frictionRate = 0;
		}

		// Check if friction should be applied
		if (velocity.length() != 0
				&& (acceleration.equals(new Vector()) || ((turnLeft
						|| turnRight || (frictionRate == grassFriction && grassFriction != normalFriction)) && velocity
						.length() > maxSpeed / 3))) {

			// Calculate friction in the opposite direction of motion
			if (movingForward()) {
				friction = Vector.normalize(realDirection).mult(frictionRate)
						.mult(KouchKarting.getSecondsPerFrame());
			} else {
				friction = Vector.normalize(realDirection).mult(-frictionRate)
						.mult(KouchKarting.getSecondsPerFrame());
			}

			// Add the calculated friction to the velocity
			acceleration.add(friction);
		}
	}

	/**
	 * Apply gravity to the couch if there was no collision or the couch isn't
	 * moving
	 */
	public void applyGravity() {
		if (!checkedCollision || !collisionChecker.foundGravityCollision()) {
			gravityAcceleration = new Vector(0, -500, 0).mult(KouchKarting
					.getSecondsPerFrame());
			gravityVelocity.add(gravityAcceleration);
		}
	}

	/**
	 * Turns the couch
	 */
	public void turn() {

		// Calculate the turning direction and magnitude
		// Turn speed is positive for left and negative for right

		// If the couch is supposed to turn left (and not right)
		if (turnLeft && !turnRight) {

			// If the couch was turning right before, make it turn more than if
			// it was turning left already, and don't let it turn more than a
			// certain amount
			if (turnSpeed < 0) {
				turnSpeed += 320 * KouchKarting.getSecondsPerFrame();
			} else if (turnSpeed < 40) {
				turnSpeed += 160 * KouchKarting.getSecondsPerFrame();
			}
			turnLeft = false;
		}

		// If the couch is supposed to turn right (and not left)
		else if (turnRight && !turnLeft) {

			// If the couch was turning right before, make it turn more than if
			// it was turning left already, and don't let it turn more than a
			// certain amount
			if (turnSpeed > 0) {
				turnSpeed += -320 * KouchKarting.getSecondsPerFrame();
			} else if (turnSpeed > -40) {
				turnSpeed += -160 * KouchKarting.getSecondsPerFrame();
			}
			turnRight = false;
		}

		// Otherwise (if the couch is not turning at all or turning in both
		// directions)
		else {

			// Return the couch back to 0 turning speed slowly to create a
			// realistic effect
			if (turnSpeed > 320 * KouchKarting.getSecondsPerFrame()) {
				turnSpeed += -320 * KouchKarting.getSecondsPerFrame();
			} else if (turnSpeed < -320 * KouchKarting.getSecondsPerFrame()) {
				turnSpeed += 320 * KouchKarting.getSecondsPerFrame();
			} else {
				turnSpeed = 0;
			}
			turnLeft = false;
			turnRight = false;
		}

		// Based on which way the couch is moving, turn it and then reset the
		// correct velocity
		if (!movingForward()) {
			RotateY(turnSpeed * KouchKarting.getSecondsPerFrame());
			velocity.mult(-1f);
		} else {
			RotateY(turnSpeed * KouchKarting.getSecondsPerFrame());
		}

		// Calculate the new point the couch is looking at (using the fake
		// direction)
		lookAt = Vector.add(fakeDirection, position);

	}

	/**
	 * Rotates the couch the given degrees around its own y axis (up vector)
	 * 
	 * @param angle
	 *            how many degrees to turn the couch
	 */
	public void RotateY(float angle) {

		// Rotate realDirection around the up vector:
		realDirection = Vector.normalize(Vector.sub(Vector.multiply(
				realDirection, (float) Math.cos(angle * PIdiv180)), Vector
				.multiply(rightVector, (float) Math.sin(angle * PIdiv180))));

		// Rotate fakeDirection around the up vector (slightly more than
		// realDirection):
		fakeDirection = Vector.normalize(Vector.sub(
				Vector.multiply(realDirection,
						(float) Math.cos(angle * 10 * PIdiv180)),
				Vector.multiply(rightVector,
						(float) Math.sin(angle * 10 * PIdiv180))));

		// Calculate the new velocity
		velocity = new Vector(realDirection).mult(velocity.length());

		// Now compute the new RightVector (by cross product)
		rightVector = Vector.crossProduct(realDirection, upVector);
	}

	/**
	 * Returns if the couch is moving forward by checking if the velocity and
	 * direction are in opposite directions or not
	 * 
	 * @return if the couch is moving forward
	 */
	public boolean movingForward() {
		if (!Vector.opposites(velocity, realDirection)) {
			return true;
		}
		return false;
	}

	/**
	 * Resets the couch to the last saved position (on the track)
	 */
	public void reset() {
		position = new Vector(resetPosition);
		realDirection = new Vector(resetDirection);
		upVector = new Vector(resetUpVector);
		rightVector = new Vector(resetRightVector);
		onTheRoad = true;
		velocity = new Vector();
	}

	/**
	 * Draw the model using the triangle data in the mesh
	 */
	public void draw() {

		// Get the loaded materials and initialise necessary variables
		Material[] materials = couchMesh.materials;
		Material material;
		Triangle drawTriangle;
		int currentMaterial = -1;
		int triangle = 0;

		// For each triangle in the object
		for (triangle = 0; triangle < couchMesh.triangles.length;) {

			// Get the triangle that needs to be drawn
			drawTriangle = couchMesh.triangles[triangle];

			// Activate a new material and texture
			currentMaterial = drawTriangle.materialID;
			material = (materials != null && materials.length > 0 && currentMaterial >= 0) ? materials[currentMaterial]
					: defaultMtl;
			material.apply();
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, material.getTextureHandle());

			// Draw triangles until material changes
			GL11.glBegin(GL11.GL_TRIANGLES);
			while (triangle < couchMesh.triangles.length
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
				if (triangle < couchMesh.triangles.length)
					drawTriangle = couchMesh.triangles[triangle];
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
			draw(); // render the mesh
			GL11.glEndList(); // End the list
		}
	}

	/**
	 * Returns the display list ID for the kart
	 * 
	 * @return the display list ID for the kart
	 */
	public int getDisplayListID() {
		return displayListID;
	}
}
