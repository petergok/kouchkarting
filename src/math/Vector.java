package math;

/**
 * A 3D vector, with functions to perform common vector math operations.
 * <P>
 * Note: Most of class by napier @ potatoland.org (a couple original methods)
 * This class also contains a lot of complicated math, and I thank napier for
 * coding this vector class that let me do many 3D operations in my program
 */
public class Vector
{
    private static final float PIOVER180 = 0.0174532925f;
    private static final float PIUNDER180 = 57.2957795130f;
	
	public float x = 0;
	public float y = 0;
	public float z = 0;
	
	
	/**
	 * Create a 0,0,0 vector 
	 */
	public Vector()
	{
	}
	
	/**
	 * Create a vector with the given xyz values
	 */
	public Vector(float xpos, float ypos, float zpos)
	{
		x = xpos;
		y = ypos;
		z = zpos;
	}
	
	/**
	 * Create a vector from the given float[3] xyz values
	 */
	public Vector (float[] float3)
	{
		x = float3[0];
		y = float3[1];
		z = float3[2];
	}
	
	/**
	 * Create a vector that duplicates the given vector
	 */
	public Vector(Vector v)
	{
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	/**
	 * Create a vector from point1 to point2
	 */
	public Vector(Vector point1, Vector point2)
	{
		x = point1.x - point2.x;
		y = point1.y - point2.y;
		z = point1.z - point2.z;
	}
	
	//========================================================================
	// Functions that operate on the vector (change the value of this vector)
	// These functions return "this", so can be chained together:
	//        GL_Vector a = new GLVector(b).mult(c).normalize()
	//========================================================================
	
	/**
	 * Add a vector to this vector
	 */
	public Vector add(Vector v)
	{
		x += v.x;
		y += v.y;
		z += v.z;
		return this;
	}
	
	/**
	 * Subtract vector from this vector
	 */
	public Vector sub(Vector v)
	{
		x -= v.x;
		y -= v.y;
		z -= v.z;
		return this;
	}
	
	/**
	 * Multiply this vector by another vector
	 */
	public Vector mult(Vector v)
	{
		x *= v.x;
		y *= v.y;
		z *= v.z;
		return this;
	}
	
	/**
	 * Divide this vector by another vector
	 */
	public Vector div(Vector v)
	{
		x /= v.x;
		y /= v.y;
		z /= v.z;
		return this;
	}
	
	/**
	 * Add a value to this vector
	 */
	public Vector add(float n)
	{
		x += n;
		y += n;
		z += n;
		return this;
	}
	
	/**
	 * Subtract a value from this vector
	 */
	public Vector sub(float n)
	{
		x -= n;
		y -= n;
		z -= n;
		return this;
	}
	
	/**
	 * Multiply vector by a value
	 */
	public Vector mult(float n)
	{
		x *= n;
		y *= n;
		z *= n;
		return this;
	}
	
	/**
	 * Divide vector by a value
	 */
	public Vector div(float n)
	{
		x /= n;
		y /= n;
		z /= n;
		return this;
	}
	
	/**
	 * Normalize the vector (make its length 1).
	 */
	public Vector normalize()
	{
		float len = length();
		if (len==0) return this;
		float invlen = 1f/len;
		x *= invlen;
		y *= invlen;
		z *= invlen;
		return this;
	}
	
	/**
	 * Reverse the vector
	 */
	public Vector reverse()
	{
		x = -x;
		y = -y;
		z = -z;
		return this;
	}
	
	/**
	 * Return the length of the vector.
	 */
	public float length()
	{
		return (float)Math.sqrt(x*x+y*y+z*z);
	}
	
	/**
	 * Return a string representation of the vector
	 */
	public String toString()
	{
		return new String ("<vector x="+x+" y="+y+" z="+z+">");
	}
	
	/**
	 * Return a copy of the vector
	 */
	public Vector getClone()
	{
		return new Vector(x,y,z);
	}
	
	/**
	 * Return true if this vector has the same xyz values as the argument vector
	 */
	public boolean equals(Vector v)
	{
		return (v.x==x && v.y==y && v.z==z);
	}
	
	/**public Vector rotateAround(Vector v, float angle) {
		float ux = x*v.x;
		float uy = x*v.y;
		float uz = x*v.z;
		float vx = y*v.x;
		float vy = y*v.y;
		float vz = y*v.z;
		float wx = z*v.x;
		float xy = z*v.y;
		float wz = z*v.z;
		float sa = (float) Math.sin(angle);
		float ca = (float) Math.cos(angle);
		x = v.x * (ux + vy + wz) + (x * (x))
	}*/
	
	//==================================================================
	// Functions that perform binary operations (add, subtract, multiply
	// two vectors and return answer in a new vector)
	//==================================================================
	
	/**
	 * Return a+b as a new vector
	 */
	public static Vector add(Vector a, Vector b)
	{
		return new Vector(a.x+b.x, a.y+b.y, a.z+b.z);
	}
	
	/**
	 * Return a-b as a new vector
	 */
	public static Vector sub(Vector a, Vector b)
	{
		return new Vector(a.x-b.x, a.y-b.y, a.z-b.z);
	}
	
	/**
	 * Return a*b as a new vector
	 */
	public static Vector mult(Vector a, Vector b)
	{
		return new Vector(a.x*b.x, a.y*b.y, a.z*b.z);
	}
	
	/**
	 * Return a/b as a new vector
	 */
	public static Vector div(Vector a, Vector b)
	{
		return new Vector(a.x/b.x, a.y/b.y, a.z/b.z);
	}
	
	/**
	 * Return the given vector multiplied by the given numeric value, as a new vector
	 */
	public static Vector multiply(Vector v, float r) {
		return new Vector(v.x*r, v.y*r, v.z*r);
	}
	
	/**
	 * Return a new vector scaled by the given factor
	 */
	public static Vector scale(Vector a, float f)
	{
		return new Vector(f*a.x,f*a.y,f*a.z);
	}
	
	/**
	 * Return the length of the given vector
	 */
	public static float length(Vector a)
	{
		return (float)Math.sqrt(a.x*a.x+a.y*a.y+a.z*a.z);
	}
	
	/**
	 *  return the dot product of two vectors
	 */
	public static float dotProduct(Vector u, Vector v)
	{
		return u.x * v.x + u.y * v.y + u.z * v.z;
	}
	
	/**
	 * Return the normalized vector as a new vector
	 */
	public static Vector normalize(Vector v) {
		float len = v.length();
		if (len==0) return v;
		float invlen = 1f/len;
		return new Vector(v.x*invlen, v.y*invlen, v.z*invlen);
	}
	
	/**
	 * Return the cross product of the two vectors, as a new vector.  The returned vector is 
	 * perpendicular to the plane created by a and b.
	 */
	public static Vector crossProduct(Vector a, Vector b)
	{
		return vectorProduct(a,b).normalize();
	}
	
	/**
	 * returns the normal vector of the plane defined by the a and b vectors
	 */
	public static Vector getNormal(Vector a, Vector b)
	{
		return vectorProduct(a,b).normalize();
	}
	
	/**
	 * returns the normal vector from the three vectors
	 */ 
	public static Vector getNormal(Vector a, Vector b, Vector c)
	{
		return vectorProduct(a,b,c).normalize();
	}
	
	/**
	 * returns a x b
	 */
	public static Vector vectorProduct(Vector a, Vector b)
	{
		return new Vector(a.y*b.z-b.y*a.z, a.z*b.x-b.z*a.x, a.x*b.y-b.x*a.y);
	}
	
	/**
	 * returns (b-a) x (c-a)
	 */ 
	public static Vector vectorProduct(Vector a, Vector b, Vector c)
	{
		return vectorProduct(sub(b,a),sub(c,a));
	}
	
	/**
	 * returns the angle between 2 vectors
	 */
	public static float angle(Vector a, Vector b)
	{
		a.normalize();
		b.normalize();
		return (a.x*b.x+a.y*b.y+a.z*b.z);
	}
	
	/**
	 *  returns the angle between 2 vectors on the XZ plane.
	 *  angle is 0-360 where the 0/360 divide is directly in front of the A vector
	 *  Ie. when A is pointing directly at B, angle will be 0.
	 *  If B moves one degree to the right, angle will be 1,
	 *  If B moves one degree to the left, angle will be 360.
	 *
	 *  right side is 0-180, left side is 360-180
	 */
	public static float angleXZ(Vector a, Vector b)
	{
		a.normalize();
		b.normalize();
		return (float)((Math.atan2(a.x*b.z-b.x*a.z, a.x*b.x+a.z*b.z) + Math.PI) * PIOVER180);
	}
	
	/**
	 *  returns the angle between 2 vectors on the XY plane.
	 *  angle is 0-360 where the 0/360 divide is directly in front of the A vector
	 *  Ie. when A is pointing directly at B, angle will be 0.
	 *  If B moves one degree to the right, angle will be 1,
	 *  If B moves one degree to the left, angle will be 360.
	 *
	 *  right side is 0-180, left side is 360-180
	 */
	public static float angleXY(Vector a, Vector b)
	{
		a.normalize();
		b.normalize();
		return (float)((Math.atan2(a.x*b.y-b.x*a.y, a.x*b.x+a.y*b.y) + Math.PI) * PIUNDER180);
	}
	
	/**
	 * return a vector rotated the given number of degrees around the Y axis  
	 */
	public static Vector rotationVector(float degrees) {
		return new Vector(
				(float)Math.sin(degrees * PIOVER180),
				0,
				(float)Math.cos(degrees * PIOVER180) );
	}
	
	public static boolean opposites (Vector a, Vector b) {
		float dotProduct = Vector.dotProduct(a, b);
		return (dotProduct <= 0);
	}
	
	public static Vector clone (Vector a) {
		return new Vector (a.x, a.y, a.z);
	}
}