package mesh;

import importer.OBJImporter.Group;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import material.Material;

import application.KouchKarting;

/**
 * The "Mesh" Class. 
 * Purpose: This object holds all the data loaded from the
 * .obj file that will be used for drawing later
 * 
 * @author Peter G.
 * @version Jan. 24, 2012
 */
public class Mesh {
	// Temporary lists for vertices and triangles while mesh is being loaded
	public ArrayList vertexData = new ArrayList();
	public ArrayList triangleData = new ArrayList();

	// Arrays hold vertices and triangles after mesh is done being loaded (see
	// optimise())
	public Vertex[] vertices;
	public Triangle[] triangles;

	// The total number of vertices and triangles in the mesh
	public int numVertices = 0;
	public int numTriangles = 0;

	// The object's name
	public String name = "";

	// Name of the material library from the .obj file, as well as all the
	// materials in the mesh (or null)
	public String materialLibraryName = null;
	public Material[] materials = null;

	// Store all the triangles for easy access, with one default group (See
	// makeGroups())
	Triangle[][] groupTriangles = new Triangle[1][];
	String[] groupNames = { "default" };
	String[] groupMaterialNames = { null };

	// The group currently "selected" in the mesh
	int currentGroup = 0;

	// The outside points for the mesh
	public float leftmostPoint = 0;
	public float rightmostPoint = 0;
	public float lowestPoint = 0;
	public float highestPoint = 0;
	public float farthestPoint = 0;
	public float nearestPoint = 0;

	/**
	 * Create a new default empty mesh with the given name and material library
	 * name
	 * 
	 * @param name
	 *            the name of the mesh
	 */
	public Mesh(String name, String materialLibraryName) {
		this.name = name;
		this.materialLibraryName = materialLibraryName;
	}

	/**
	 * Imports the materials into the mesh
	 */
	public void importMaterials(Material[] materials) {
		this.materials = materials;
	}

	/**
	 * Adds the new given vertex to the mesh vertex data
	 * 
	 * @param newVertex
	 *            the new vertex to be added
	 */
	public void addVertex(Vertex newVertex) {
		newVertex.ID = vertexData.size();
		vertexData.add(newVertex);
	}

	/**
	 * Creates a new Vertex from the coordinates and adds them to the mesh
	 * 
	 * @param xPos
	 *            the x position of the vertex
	 * @param yPos
	 *            the y position of the vertex
	 * @param zPos
	 *            the z position of the vertex
	 */
	public void addVertex(float xPos, float yPos, float zPos) {
		addVertex(new Vertex(xPos, yPos, zPos));
	}

	/**
	 * add triangle to given group. used by Importer to load mesh groups. Groups
	 * had to be allocated first by makeGroups() and inited with initGroup().
	 * 
	 * @param newTriangle
	 * @param groupNum
	 * @param triangleNum
	 * @see makeGroups()
	 * @see setGroup()
	 */
	public void addTriangle(Triangle newTriangle, int groupNum, int triangleNum) {
		// set IDs into the triangle
		newTriangle.ID = triangleData.size();
		newTriangle.groupID = groupNum;
		// store the triangle
		triangleData.add(newTriangle);
		groupTriangles[groupNum][triangleNum] = newTriangle;
	}

	/**
	 * Allocate space for the number of groups needed
	 * 
	 * @param noOfGroups
	 *            the number of groups needed
	 */
	public void makeGroups(int noOfGroups) {
		groupTriangles = new Triangle[noOfGroups][];
		groupNames = new String[noOfGroups];
		groupMaterialNames = new String[noOfGroups];
	}

	/**
	 * Allocate a triangle array for a group. Also set the name and material
	 * name of the group.
	 * 
	 * @param groupNum
	 *            the group number
	 * @param name
	 *            the name of the group
	 * @param materialName
	 *            the name of the group's material
	 * @param noOfTriangles
	 *            the number of triangles in the group
	 */
	public void importGroup(int groupNum, String name, String materialName,
			int noOfTriangles) {
		groupTriangles[groupNum] = new Triangle[noOfTriangles];
		groupNames[groupNum] = name;
		groupMaterialNames[groupNum] = materialName;
		currentGroup = groupNum;
	}

	public Vertex getVertex(int index) {
		if (vertexData != null) {
			return (Vertex) vertexData.get(index);
		} else {
			return vertices[index];
		}
	}

	/**
	 * Optimises the mesh for faster performance. The method does this by
	 * converting the array lists to arrays, making performance faster. It also
	 * stores all the neighbour triangles of each vertex for faster performance
	 * later.
	 */
	public void optimise() {
		if (vertexData == null || triangleData == null) {
			System.out
					.println("Mesh.optimise(): cannot optimise after finalised");
		}

		// Create a new array with the same size as the triangle array list
		numVertices = vertexData.size();
		vertices = new Vertex[numVertices];

		// For each vertex,
		for (int vertex = 0; vertex < numVertices; vertex++) {
			// Get the vertex, set its new ID and reset its neighbour triangles
			vertices[vertex] = getVertex(vertex);
			vertices[vertex].ID = vertex;
			vertices[vertex].resetNeighbours();
		}

		// Create a new array with the same size as the triangle array list
		Triangle newTriangle;
		numTriangles = triangleData.size();
		triangles = new Triangle[numTriangles];

		// For each triangle,
		for (int triangle = 0; triangle < numTriangles; triangle++) {
			// Get the triangle and set its new ID
			triangles[triangle] = newTriangle = (Triangle) triangleData
					.get(triangle);
			newTriangle.ID = triangle;

			// Register the triangle as a "neighbour" of its vertices
			newTriangle.point1.addNeighbourTriangle(newTriangle);
			newTriangle.point2.addNeighbourTriangle(newTriangle);
			newTriangle.point3.addNeighbourTriangle(newTriangle);
		}
	}

	/**
	 * Find and store the outside points of the mesh (the rightmost, leftmost,
	 * highest, lowest, etc.)
	 */
	public void calculateDimensions() {
		// Reset all the outside points to 0
		leftmostPoint = rightmostPoint = 0;
		lowestPoint = highestPoint = 0;
		farthestPoint = nearestPoint = 0;
		// For each triangle,
		for (int triangle = 0; triangle < triangles.length; triangle++) {
			// Get the triangle
			Triangle checkTriangle = (Triangle) triangles[triangle];
			// For each vertex in that triangle
			for (int vertex = 1; vertex <= 3; vertex++) {
				Vertex checkVertex = checkTriangle.getPoint(vertex);
				if (checkVertex.pos.x < leftmostPoint) {
					leftmostPoint = checkVertex.pos.x;
				} else if (checkVertex.pos.x > rightmostPoint) {
					rightmostPoint = checkVertex.pos.x;
				}
				if (checkVertex.pos.y < lowestPoint) {
					lowestPoint = checkVertex.pos.y;
				} else if (checkVertex.pos.y > highestPoint) {
					highestPoint = checkVertex.pos.y;
				}
				if (checkVertex.pos.z < nearestPoint) {
					nearestPoint = checkVertex.pos.z;
				} else if (checkVertex.pos.z > farthestPoint) {
					farthestPoint = checkVertex.pos.z;
				}
			}
		}
	}
}
