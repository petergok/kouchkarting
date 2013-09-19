package camera;


import org.lwjgl.util.glu.*;

import math.Matrix;
import math.Vector;

/**
 * The "Camera" Class.
 * Purpose: controls the user's view of the scene.
 * Uses gluLookAt() to render the scene
 * 
 * @author Peter G.
 * @version Jan. 24, 2012
 */
public class Camera
{
	// A final value of PI divided by 180 to convert to radians easily
    final float PIdiv180 = 0.0174532925f;
    
    // The camera's view direction, right vector, up vector and postion
    public Vector viewDirection;
    public Vector upVector;
    public Vector position;
    
    // The rotation values of the camera on each of the axis
    public float RotatedX, RotatedY, RotatedZ;

   /**
    * Creates a new camera with the given position, up vector and view direction.
    * 
    * @param position the position of the camera
    * @param upVector the direction straight up
    * @param viewDirection the direction the camera is facing
    */
    public Camera (Vector position, Vector viewDirection, Vector upVector)
    {
    	this.position = position;
    	this.viewDirection = viewDirection;
    	this.upVector = upVector;
    }

    /**
     * Change the camera's view direction.
     */
    public void viewDir (Vector viewDirection)
    {
    	this.viewDirection = viewDirection;
    }
    
    /**
     * Move camera to the given position.
     */
    public void MoveTo (Vector position)
    {
    	this.position = position;
    }


    /**
     * Call GLU.gluLookAt() to set view position, direction and orientation.
     * Modelview matrix must be selected in openGL.
     */
    public void Render ()
    {
    	// Calculate the point at which the camera looks at
    	Vector ViewPoint = Vector.add (position, viewDirection);

    	// Call gluLookAt using the vectors
    	GLU.gluLookAt (position.x, position.y, position.z,
    			ViewPoint.x, ViewPoint.y, ViewPoint.z,
    			upVector.x, upVector.y, upVector.z);
    }
}
