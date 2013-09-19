package mesh;

import java.util.ArrayList;

import math.Vector;

/**
 * The "Triangle" Class. 
 * Purpose: describes a triangular face. Holds references
 * to three vertices, their normals and texture coodinates. Vertex normals are
 * stored here and not in the Vertex object because a vertex may be shared
 * between two or more faces, and the faces may have very different normals (ie.
 * if the faces are at a 90 degree angle and make a sharp edge).
 * <P>
 * Note: This method contains a lot of public data fields. This was done mainly
 * for efficiency purposes to speed up the program when drawing
 * 
 * @author Peter G.
 * @version Jan 24, 2012
 */
public class Triangle {
	// The three vertices
	public Vertex point1;
	public Vertex point2;
	public Vertex point3;

	// The three normals
	public Vector normal1;
	public Vector normal2;
	public Vector normal3;

	// The three texture coordinates
	public Vector texture1 = new Vector();
	public Vector texture2 = new Vector();
	public Vector texture3 = new Vector();

	// The eSpacePlaneCosntant eSpace mathematics position constant for
	// subsequent scaled transformation vector quantities
	public float eSpacePlaneConstant;

	// The face normal and eSpace face normal for the triangle
	public Vector faceNormal = new Vector();
	public Vector eSpaceFaceNormal = new Vector();

	// ID's of the triangle to parent arrays
	public int ID = 0;
	public int groupID = 0;
	public int materialID;

	/**
	 * Creates a new triangle with the three vertices given
	 * 
	 * @param point1
	 *            the first vertex
	 * @param point2
	 *            the second vertex
	 * @param point3
	 *            the third vertex
	 */
	public Triangle(Vertex point1, Vertex point2, Vertex point3) {
		this.point1 = point1;
		this.point2 = point2;
		this.point3 = point3;
	}

	/**
	 * Creates an empty triangle
	 */
	public Triangle() {
	}

	/**
	 * Returns the vertex specified
	 * 
	 * @param whichPoint
	 *            which vertex to return
	 * @return the vertex specified
	 */
	public Vertex getPoint(int whichPoint) {
		if (whichPoint == 1) {
			return point1;
		} else if (whichPoint == 2) {
			return point2;
		} else if (whichPoint == 3) {
			return point3;
		}
		return null;
	}

	/**
	 * Applies eSpace transformations to the triangle for further calculations
	 * 
	 * @param changeOfBasis
	 *            the scaling of the eSpace
	 */
	public void applyESpace(Vector changeOfBasis) {
		// Apply the eSpace to each of the vertices
		point1.applyESpace(changeOfBasis);
		point2.applyESpace(changeOfBasis);
		point3.applyESpace(changeOfBasis);

		// Calculate the eSpace face normal and plane constant
		eSpaceFaceNormal = Vector.getNormal(point1.posESpace, point2.posESpace,
				point3.posESpace);
		eSpacePlaneConstant = -(point1.posESpace.x * eSpaceFaceNormal.x
				+ point1.posESpace.y * eSpaceFaceNormal.y + point1.posESpace.z
				* eSpaceFaceNormal.z);

		// Calculate the original face normal
		faceNormal = new Vector(eSpaceFaceNormal).mult(changeOfBasis);
	}

	/**
	 * Checks if the triangle is facing the vector
	 * 
	 * @param vector
	 *            the vector that is being checked
	 * @return if the triangle is facing the vector
	 */
	public boolean isFrontFacingTo(Vector vector) {
		float dotProduct = Vector.dotProduct(eSpaceFaceNormal, vector);
		return (dotProduct <= 0);
	}

	/**
	 * Returns the signed distance from the triangle to the point
	 * 
	 * @param point
	 *            the point from which the signed distance is measured
	 * @return the signed distance
	 */
	public float signedDistanceTo(Vector point) {
		return Vector.dotProduct(eSpaceFaceNormal, point) + eSpacePlaneConstant;
	}

	/**
	 * Checks if the point is in the triangle
	 * 
	 * @param the
	 *            point being checked
	 * @return if the point is in the triangle
	 */
	public boolean checkPointInTriangle(Vector point) {
		float totalAngles = 0.0f;

		// Calculate the vectors between the point and each of the vertices
		Vector v1 = Vector.sub(point, point1.posESpace);
		Vector v2 = Vector.sub(point, point2.posESpace);
		Vector v3 = Vector.sub(point, point3.posESpace);

		// Normalize those vectors
		v1.normalize();
		v2.normalize();
		v3.normalize();

		// Calculate the sum of teh angle from each vector to the next
		totalAngles += Math.acos(Vector.dotProduct(v1, v2));
		totalAngles += Math.acos(Vector.dotProduct(v2, v3));
		totalAngles += Math.acos(Vector.dotProduct(v3, v1));

		// If the total of the angles is within 0.005 of 2PI, then it is in the
		// triangle
		if (Math.abs(totalAngles - 2 * Math.PI) <= 0.005) {
			return true;
		}

		// Otherwise, return that the point isn't in the triangle
		return false;
	}

	/**
	 * Calculates the signed distance from the point to the triangle with the
	 * given origin and face normal
	 * 
	 * @param point
	 *            the point from which the signed distance is being calculated
	 * @param faceNormal
	 *            the normal of the triangle that is being used
	 * @param origin
	 *            the origin of the triangle that is being used
	 * @return the signed distance to the point
	 */
	public static float signedDistanceTo(Vector point, Vector faceNormal,
			Vector origin) {
		float planeConstant = -(origin.x * faceNormal.x + origin.y
				* faceNormal.y + origin.z * faceNormal.z);
		return Vector.dotProduct(point, faceNormal) + planeConstant;
	}
}