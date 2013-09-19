package collision;

import math.Vector;
import mesh.Mesh;
import mesh.Triangle;

/**
 * The "WorldCollision" Class. 
 * Purpose: This class if for collision detection
 * between an object and the world. A new world collision class should be
 * created each time a collision is checked
 * 
 * @author Peter G.
 * @version Jan. 24, 2012
 * 
 */
public class WorldCollision {

	// Information about the move being requested in 3D space
	private Vector changeOfBasis;
	private Vector R3Velocity;
	private Vector R3Position;
	private Vector R3Gravity;

	// Information about the move being requested in eSpace
	private Vector velocity;
	private Vector basePoint;

	// Keep track of recursion depth in testing and settings
	private float veryCloseDistance;
	private int collisionRecursionDepth;
	private boolean checkingGravity;

	// Information about the sliding plane
	private Vector slidingPlaneNormal;
	private Vector slidingPlaneOrigin;

	// Hit information, such as if a collision was found and the triangle with
	// which the collision occured
	private boolean collisionFound;
	private float nearestDistance;
	private Vector intersectionPoint;
	private Vector finalPosition;
	private Triangle collisionTriangle;
	private boolean collisionWithGravity;
	private boolean embedded;

	/**
	 * Creates a new world collision class to use for collision detection. This
	 * method also initialises other important variables
	 * 
	 * @param velocity
	 *            the velocity of the object colliding
	 * @param gravity
	 *            the gravity velocity of the object colliding
	 * @param position
	 *            the position of the object colliding
	 * @param radius
	 *            the radius of the object colliding
	 * @param upVector
	 *            the objects up vector
	 */
	public WorldCollision(Vector velocity, Vector gravity, Vector position,
			Vector radius, Vector upVector) {

		// Copy given variables to local variables
		changeOfBasis = radius;
		R3Velocity = velocity;
		R3Gravity = gravity;
		R3Position = position;

		// Default data for collisions
		collisionTriangle = new Triangle();
		collisionTriangle.faceNormal = upVector;
		collisionFound = false;
		collisionWithGravity = false;
		checkingGravity = false;

		// Set the distance for a collision (for calculation errors)
		veryCloseDistance = 0.5f;

		// If the object is embedded in the world or not
		embedded = false;
	}

	/**
	 * Collides with the mesh and reacts accordingly in a sliding motion by
	 * using a sliding plane.
	 * 
	 * @param mesh
	 *            the mesh the object is colliding with (world mesh)
	 */
	public void collideAndSlide(Mesh mesh) {

		// Set up checking for gravity
		velocity = Vector.div(R3Gravity, changeOfBasis);
		basePoint = Vector.div(R3Position, changeOfBasis);
		checkingGravity = true;

		// Keep track of the recursion depth
		collisionRecursionDepth = 0;

		// Check for collisions with gravity
		finalPosition = collideWithMesh(mesh);
		checkingGravity = false;

		// Convert the velocity and position to eSpace and reset other variables
		velocity = Vector.div(R3Velocity, changeOfBasis);
		basePoint = finalPosition;
		collisionFound = false;
		nearestDistance = 100;

		// Keep track of the recursion depth
		collisionRecursionDepth = 0;

		// Collide with the mesh, getting the final position back
		finalPosition = collideWithMesh(mesh);

		// Convert back to R3 Space
		finalPosition.mult(changeOfBasis);
	}

	/**
	 * Checks for collisions and reacts based on the results, returning the end
	 * position after sliding. Only checks for one vector.
	 * 
	 * @param mesh
	 *            the mesh the object is colliding with
	 * @return the final position of the object after sliding
	 */
	public Vector collideWithMesh(Mesh mesh) {

		// If recursion is over 5, it should be over
		if (collisionRecursionDepth > 1000) {
			return basePoint;
		}

		// Check for collisions
		for (int triangle = 0; triangle < mesh.triangles.length; triangle++) {
			checkTriangleForCollision(mesh.triangles[triangle]);
		}

		// If checking for gravity, indicate if there is a collision in the
		// first place
		if (checkingGravity && collisionFound) {
			collisionWithGravity = true;
		}

		// If collision didn't occur, move along the velocity
		if (!collisionFound) {
			return Vector.add(basePoint, velocity);
		}

		// If collision did occur,
		// Store the original destination point and make a new base point
		Vector destinationPoint = Vector.add(basePoint, velocity);
		Vector newBasePoint = new Vector(basePoint);

		// If the object is farther than the very close distance
		if (nearestDistance >= veryCloseDistance) {

			// Move it up to the plane at the very close distance away
			Vector v = new Vector(velocity);
			Vector newVelocity = new Vector(v).normalize().mult(
					nearestDistance - veryCloseDistance);
			newBasePoint = Vector.add(basePoint, newVelocity);

			// Adjust the intersection point so that the sliding plane
			// is unaffected by the object being slightly off the surface
			intersectionPoint = Vector.sub(intersectionPoint, v.normalize()
					.mult(veryCloseDistance));
		}

		// Determine the sliding plane
		slidingPlaneOrigin = intersectionPoint;
		slidingPlaneNormal = Vector.sub(newBasePoint, intersectionPoint);
		slidingPlaneNormal.normalize();

		// Calculate the new destination point
		Vector newDestinationPoint = Vector.sub(destinationPoint, new Vector(
				slidingPlaneNormal).mult(Triangle.signedDistanceTo(
				destinationPoint, slidingPlaneNormal, slidingPlaneOrigin)));

		// Generate the slide vector, which is going to be the new
		// velocity vector for the next iteration
		Vector newVelocity = Vector.sub(newDestinationPoint, intersectionPoint);

		// Don't recurse if the new velocity is too small
		if (newVelocity.length() < veryCloseDistance) {
			return newBasePoint;
		}

		// Recurse:
		collisionRecursionDepth++;
		basePoint = newBasePoint;
		velocity = newVelocity;
		collisionFound = false;
		return collideWithMesh(mesh);
	}

	/**
	 * Checks if the object will collide with the given triangle. The results
	 * are stored in local variables for easier access later.
	 * 
	 * @param checkTriangle
	 *            the triangle that is tested for a collision
	 */
	public void checkTriangleForCollision(Triangle checkTriangle) {

		// Apply the eSpace to the triangle
		checkTriangle.applyESpace(changeOfBasis);

		// Calculate the signed distance from the sphere to the triangle
		float signedDistance = checkTriangle.signedDistanceTo(basePoint);

		if (Math.abs(signedDistance) <= 1.0f) {
			// Calculate the plane intersection point
			Vector planeIntersectionPoint = Vector.sub(basePoint,
					checkTriangle.eSpaceFaceNormal);

			if (checkTriangle.checkPointInTriangle(planeIntersectionPoint)) {
				embedded = true;
			}
		}

		// Check if triangle is front-facing to the velocity vector
		// If it is not, do not check it
		if (!checkTriangle.isFrontFacingTo(velocity)) {
			return;
		}

		// Get the interval of plane intersection
		float t0, t1;

		// Store the triangle normal dot velocity to use later
		float normalDotVelocity = Vector.dotProduct(
				checkTriangle.eSpaceFaceNormal, velocity);

		// If the object is travelling parallel to the plane, there is no
		// collision possible
		if (normalDotVelocity == 0.0f) {
			return;
		}

		// Calculate intersection interval
		t0 = (-1.0f - signedDistance) / normalDotVelocity;
		t1 = (1.0f - signedDistance) / normalDotVelocity;

		// Make sure t0 is less than t1
		if (t0 > t1) {
			float temp = t1;
			t1 = t0;
			t0 = temp;
		}

		// If both results are out of range, there is no collision
		if (t0 > 1.0f || t1 < 0.0f) {
			return;
		}

		// At this point, there are two values t0 and t1,
		// between which the collision must occur
		boolean foundCollision = false;
		float time = 1.0f;

		// Calculate the plane intersection point
		Vector planeIntersectionPoint = Vector
				.add(Vector.sub(basePoint, checkTriangle.eSpaceFaceNormal),
						velocity);

		// Check if the point is in the triangle
		if (checkTriangle.checkPointInTriangle(planeIntersectionPoint)) {
			foundCollision = true;
			time = t0;
		}

		// Apply the results
		if (foundCollision) {
			float distanceToCollision = time * velocity.length();

			// Check if this is the closest hit
			if (collisionFound == false
					|| distanceToCollision < nearestDistance) {
				nearestDistance = distanceToCollision;
				intersectionPoint = planeIntersectionPoint;
				collisionFound = true;
				collisionTriangle = checkTriangle;
			}
		}
	}

	/**
	 * Returns if a collision was found
	 * 
	 * @return if a collision was found
	 */
	public boolean foundCollision() {
		return collisionFound;
	}

	/**
	 * Returns if gravity collision was found (collision using gravity vector)
	 * 
	 * @return if a gravity collision was found
	 */
	public boolean foundGravityCollision() {
		return collisionWithGravity;
	}

	/**
	 * Returns the final position of the object after sliding
	 * 
	 * @return the final position
	 */
	public Vector getMoveTo() {
		return new Vector(finalPosition);
	}

	/**
	 * Returns the normal vector of the collision
	 * 
	 * @return the normal of the collision
	 */
	public Vector getCollisionNormal() {
		return new Vector(collisionTriangle.faceNormal);
	}

	/**
	 * Returns if the object is embedded in the mesh that it collided with
	 * 
	 * @return if the object is embedded
	 */
	public boolean isEmbedded() {
		return embedded;
	}

	/**
	 * Returns the triangles that the object collided with, making sure the
	 * correct triangle is given
	 * 
	 * @return the triangle that the object collided with
	 */
	public Triangle getCollisionTriangle() {
		if (collisionTriangle.point1 != null) {
			return collisionTriangle;
		} else {
			return null;
		}
	}
}
