
package mesh;

import java.util.ArrayList;

import math.Vector;

/**
 * The "Vertex" Class.
 * Purpose: Holds data for one single vertex in a mesh
 * Note: The data fields for this class are public for efficiency when they are used for drawing
 * 
 * @author Peter G.
 * @version Jan. 24, 2012
 */
public class Vertex
{
	// The position and eSpace position of the vertex
    public Vector pos = new Vector();
    public Vector posESpace = new Vector ();
    
    // The ID of the vertex into the parent array of vertices
    public int ID;
    
    // The neighbour triangles of the vertex
    public ArrayList neighbourTriangles = new ArrayList();

    /**
     * Create a new empty vertex at the origin
     */
    public Vertex() {
        this (0f, 0f, 0f);
    }

    /**
     * Create a vertex from the given x, y and z coordinates
     * 
     * @param xPos the x position of the vertex
     * @param yPos the y position of the vertex
     * @param zPos the z position of the vertex
     */
    public Vertex(float xPos, float yPos, float zPos) {
        pos = new Vector(xPos, yPos, zPos);
    }
    
    /**
     * Apply the eSpace to the vertex
     * 
     * @param changeOfBasis the eSpace transformation value
     */
    public void applyESpace (Vector changeOfBasis) {
    	posESpace = Vector.div(pos, changeOfBasis);
    }

    /**
     * Adds a neighbour triangle to this vertex
     * 
     * @param triangle the triangle object to add
     */
    public void addNeighbourTriangle(Triangle triangle)
    {
        if (!neighbourTriangles.contains(triangle)) {
        	neighbourTriangles.add(triangle);
        }
    }

    /**
     * Clears the neighbour triangle list
     */
    public void resetNeighbours()
    {
    	neighbourTriangles.clear();
    }
}