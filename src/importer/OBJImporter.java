package importer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import material.MaterialLibrary;
import math.Vector;
import mesh.Mesh;
import mesh.Triangle;

import application.KouchKarting;

/**
 * The "OBJImporter" Class.
 * Purpose: Imports all the data from a .obj file
 * 
 * @author Peter G.
 * @version Jan. 24, 2012
 */
public class OBJImporter {

	// The vertex data loaded
	private ArrayList vertexData = new ArrayList ();
	private ArrayList normalData = new ArrayList ();
	private ArrayList textureData = new ArrayList ();
	
	// The groups loaded
	private ArrayList groups = new ArrayList ();
	
	// The faces loaded
	private ArrayList faces = new ArrayList ();
	
	// The mesh for this object
	private Mesh mesh;
	
	// The matrialLibrary name and object
	private String materialLibraryName = null;
	private MaterialLibrary materialLibrary;
	
	// The path and name of the .obj file
	private String filepath = "";
	private String filename = "";
	private String fullFilename = "";   
	
	/**
	 * Creates an empty OBJImporter for loading later
	 */
	public OBJImporter () {
	}
	
	/**
	 * Loads an object from the .obj file specified,
	 * and makes a mesh for that object
	 * 
	 * @param filename the name of the .obj file containing the object
	 * @return the mesh created
	 */
	public Mesh load (String fullFilename) {
		this.fullFilename = fullFilename;
		// Split up the filename into the path and the real filename
		String[] pathParts = KouchKarting.getPathAndFile(fullFilename);
        filepath = pathParts[0];
        filename = pathParts[1];
        System.out.println("The filepath is: " + filepath);
        System.out.println("The filename is: " + filename);
        // Show debugging messages and load the object
		System.out.println("OBJImporter.import(): Loading object from " + filename);
		loadObject();
		System.out.println("OBJImporter.importFromStream(): model has " + faces.size() + 
						   " faces and " + vertexData.size() + " vertices.  Mtl file is " + 
						   materialLibraryName);
		return makeMeshObject ();
	}
	
	/**
	 * Loads an object from the file the OBJImporter has.
	 */
	public void loadObject(){
		try {
			// Create a new buffered reader to read the file from
			BufferedReader OBJFile = new BufferedReader (new FileReader (fullFilename));
			String line = "";
			String materialName = "";
			int materialID = -1;
			
			// Create a default group in case the obj file doesn't have groups
			Group group = new Group ("default");
			groups.add(group);
			
			// While you haven't read the entire file
			while ((line = OBJFile.readLine()) != null) {
				
				// Remove all the extra spaces from the next line
				line = line.trim();
				line = line.replaceAll("  ", " ");
				
				// If the line contains data
				if (line.length() > 0) {
					
					// If the line has vertex data (In the form: v xCoord yCoord zCoord)
					if (line.startsWith("v ")) {
						vertexData.add(readFloats(line));
					}
					
					// If the line has texture data (In the form: vt uCoord vCoord wCoord-(usually 0))
					else if (line.startsWith("vt")) {
						textureData.add(readFloats(line));
					}
					
					// If the line has normal data (In the form: vn xCoord yCoord zCoord)
					else if (line.startsWith("vn")) {
						normalData.add(readFloats(line));
					}
					
					// If the line has face data (In the form: v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3)
					else if (line.startsWith("f ")) {
						Face newFace = readFace(line);
						
						// Assign the material ID for that group
						newFace.assignMaterial(materialID);
						faces.add(newFace);           					// Add face to complete face list
						group.addFace(newFace);     					// Add face to current group
						group.addTriangles(newFace.getNoOfTriangles()); // Track the number of triangles in the group
					}
					
					// If the line has group data (In the form: g groupName)
                    else if (line.startsWith("g ")) {
                        String groupname = (line.length() > 1) ? line.substring(2).trim() : "";
                        
                        // "Select" the given group
                        group = findGroup(groupname);
                        
                        // If group not found: start a new group
                        if (group == null) {
                            group = new Group(groupname);
                            group.assignMaterial(materialName, materialID);  // Assign current material to new group
                            groups.add(group);
                        }
                    }
					
					//If the line contains material data (In the form: usemtl materialName)
                    else if (line.startsWith("usemtl")) {
                        materialName = line.substring(7).trim();
                        
                        // Look for the material name in the library if there is a library
                        // and assign that material to the current group
                        materialID = (materialLibrary == null) ? -1 : materialLibrary.findID(materialName);
                        group.assignMaterial(materialName, materialID);
                    }
					
					// If the line contains material library data (In the form: mtllib materialLibraryFile.mtl)
                    else if (line.startsWith("mtllib")) {
                        // Load the material library
                        materialLibraryName = line.substring(7).trim();
                        if (materialLibraryName.startsWith("./")) {
                            materialLibraryName = materialLibraryName.substring(2);
                        }
                        materialLibrary = new MaterialLibrary(filepath + materialLibraryName);
                    }
				}
			}
		} 
		catch (Exception exception) {
			System.out.println("OBJImporter.loadObject() failed to load file: " + filename);
		}
		// Remove any empty groups
        for (int group = groups.size() - 1; group >= 0; group--) {
            if (((Group)groups.get(group)).faces.size() <= 0) {
                System.out.println("REMOVED EMPTY GROUP: " + ((Group) groups.get(group)).getName());
                groups.remove(group);
            }
        }

		// For debugging purposes
		System.out.println("OBJImporter: imported " + getNoOfPolygons()
						   + " faces in " + groups.size() + " groups");
		// For debugging purposes
        for (int group = 0; group < groups.size(); group++) {
            System.out.println("Group " + group + " " + ((Group)groups.get(group)).getName() + 
            				   " has " + ((Group)groups.get(group)).faces.size() + " faces, material is " 
            				   + ((Group)groups.get(group)).getMaterialName());
        }
	}
	
	/**
	 * Read the values from the line given and create
	 * a new float array with those values
	 * 
	 * @param line the line to read the values from
	 * @return an array containing the float values
	 */
	private float[] readFloats(String line)
	{
		try
		{
			// Create a new StringTokenizer and move to the first value
			StringTokenizer st = new StringTokenizer(line, " ");
			st.nextToken();
			
			// Create and return a new float array with the values read
			// from the given line
			if (st.countTokens() == 2) {
				return new float[] {Float.parseFloat(st.nextToken()),
									Float.parseFloat(st.nextToken()),
									0};
			}
			else {
				return new float[] {Float.parseFloat(st.nextToken()),
									Float.parseFloat(st.nextToken()),
									Float.parseFloat(st.nextToken())};
			}
		}
		catch (Exception e)
		{
			System.out.println("OBJImporter.readFloats(): error on line '" + line + "', " + e);
			return null;
		}
	}
	
	/**
	 * Look through all the groups for the group with the given name
	 * 
	 * @param name the name of the group
	 * @return the group with that name or null if no group was found
	 */
	public Group findGroup(String name) {
        for (int group = 0; group < groups.size(); group++) {
            if (((Group)groups.get(group)).getName().equals(name)) {
                return (Group)groups.get(group);
            }
        }
        return null;
    }
	
	/**
	 * Read face data from the given line and return a Face object.
	 * Face line is in the form: f 1/3/1 13/20/13 16/29/16
     * Three or more sets of numbers, each set contains vertex/texture/normal
     * indices
     * 
	 * @param line   the string to read the data from
	 * @return       the Face object created
	 */
	private Face readFace(String line) {
        // Throw out the "f" at the start of the line, and then split
        String[] triplets = line.substring(2).split(" ");
        // Create new arrays for the vertices, textures and normals
        int[] vertices = new int[triplets.length];
        int[] textures = new int[triplets.length];
        int[] normals = new int[triplets.length];
        // For each triplet (data for one point)
        for (int triplet = 0; triplet < triplets.length; triplet++) {
            // If the texture coordinate was not assigned, make it 0
            String[] vertTxtrNorm = triplets[triplet].replaceAll("//", "/0/").split("/");
            // Convert the data into the arrays
            if (vertTxtrNorm.length > 0) {
            	vertices[triplet] = convertIndex(vertTxtrNorm[0],vertexData.size());
            }
            if (vertTxtrNorm.length > 1) {
                textures[triplet] = convertIndex(vertTxtrNorm[1],textureData.size());
            }
            if (vertTxtrNorm.length > 2) {
                normals[triplet] = convertIndex(vertTxtrNorm[2],normalData.size());
            }
        }
        
        // Create a new face and return it
        return new Face(vertices, textures, normals);
	}
	
	/**
	 * Create a Mesh object from the data read by the OBJImporter
	 * 
	 * @return the mesh object created
	 */
    public Mesh makeMeshObject() {

        // Make a new mesh
        mesh = new Mesh(filename, materialLibraryName);
        if (materialLibrary != null) {
        	mesh.importMaterials(materialLibrary.getMaterials());
        }

        // Add the vertices to the Mesh
        for (int vertex = 0; vertex < vertexData.size(); vertex++) {
            float[] coords = (float[]) vertexData.get(vertex);
			mesh.addVertex(coords[0], coords[1], coords[2]);
        }

        // Allocate space for groups
        mesh.makeGroups(groups.size());

        // For each group
        for (int group = 0; group < groups.size(); group++) {
        	// Import its data, allocating space for triangles
            mesh.importGroup(group,
                          ((Group)groups.get(group)).getName(),
                          ((Group)groups.get(group)).getMaterialName(),
                          ((Group)groups.get(group)).getNoOfTriangles());
        }

        // Import triangles to the Mesh. 
        // A Face may be a triangle, quad or polygon.  
        // Convert all faces to triangles.
        
        // For each group
        for (int group = 0; group < groups.size(); group++) {
            int noOfTriangles = 0;
            // Stores its faces
            faces = ((Group)groups.get(group)).getFaces();
            // For each face
            for (int face = 0; face < faces.size(); face++) {
                Face addFace = (Face) faces.get(face);
                // Put vertices, normals, and texture coordinates into the mesh
                // by converting any polygon to a triangle fan, with first vertex (0)
                // at centre:  0,1,2   0,2,3   0,3,4   0,4,5
                for (int triangle = 0; triangle < addFace.getNoOfTriangles(); triangle++) {
                    addTriangle(mesh, group, noOfTriangles, addFace, 0, triangle + 1, triangle + 2, addFace.getMaterialID());
                    noOfTriangles++;
                }
            }
        }

		// Optimise the Mesh
		mesh.optimise();

        // Calculate the dimensions for the mesh
        mesh.calculateDimensions();
        
		return mesh;
	}
    
    /**
     * Add a new triangle to the Mesh.  
     * This assumes that the vertices have already been added to the Mesh, 
     * in the same order that they were in the OBJ.  
     * Also that the mesh has groups allocated with triangle arrays.
     *
     * @param mesh the Mesh object
     * @param groupNum the group to add the triangle to
     * @param triangleNum the index of the triangle to add in the group
     * @param face the face from which the triangle is from
     * @param vertexOne the index of the first vertex of the triangle
     * @param vertexTwo the index of the second vertex of the triangle
     * @param vertexThree the index of the third vertex of the triangle
     * @param materialID the index of the material of the triangle
     */
    public void addTriangle(Mesh mesh, int groupNum, int triNum, Face face,
                                   int vertexOne, int vertexTwo, int vertexThree, int materialID) {
        // A face may have many vertices (can be a polygon).
        // Make a new triangle with the specified three vertices.
        Triangle triangle = new Triangle(
            mesh.getVertex(face.getVertexIDs () [vertexOne]),
            mesh.getVertex(face.getVertexIDs () [vertexTwo]),
            mesh.getVertex(face.getVertexIDs () [vertexThree]));

        // Import texture coordinates into triangle
        // if texture coordinates were loaded
        if (textureData.size() > 0) {
            float[] textureCoord;
            
            // Set the texture coordinate for vertex 1 
            textureCoord = (float[]) textureData.get(face.getTextureIDs () [vertexOne]); 
            triangle.texture1 = new Vector (textureCoord[0], textureCoord[1], textureCoord[2]);
            
            // Set the texture coordinate for vertex 2
            textureCoord = (float[]) textureData.get(face.getTextureIDs () [vertexTwo]); 
            triangle.texture2 = new Vector (textureCoord[0], textureCoord[1], textureCoord[2]);
             
            // Set the texture coordinate for vertex 3
            textureCoord = (float[]) textureData.get(face.getTextureIDs () [vertexThree]);
            triangle.texture3 = new Vector (textureCoord[0], textureCoord[1], textureCoord[2]);
        }

        // Import normals into triangle
        // if normal data was loaded
        if (normalData.size() > 0) {
            float[] normal;
            
            // Set the normal coordinate for vertex 1 
            normal = (float[]) normalData.get(face.getNormalIDs () [vertexOne]); 
            triangle.normal1 = new Vector (normal[0], normal[1], normal[2]);
            
            // Set the normal coordinate for vertex 2 
            normal = (float[]) normalData.get(face.getNormalIDs () [vertexTwo]); 
            triangle.normal2 = new Vector (normal[0], normal[1], normal[2]);
            
            // Set the normal coordinate for vertex 2 
            normal = (float[]) normalData.get(face.getNormalIDs () [vertexThree]); 
            triangle.normal3 = new Vector (normal[0], normal[1], normal[2]);
        }

        // Store the material index in the triangle
        triangle.materialID = materialID;

        // Add triangle to the given group in the Mesh
        mesh.addTriangle(triangle, groupNum, triNum);
    }
	
	/**
     * Converts the index given inside the token into the proper array index.
     * Can be in two forms:
     * If positive, then it is the index starting from 1.
     * Therefore the method converts it to the index starting from 0.
     * If negative, then it is the index subtracted from the back of the array.
     * 
     * @param token a token from the OBJ file containing a numeric value or blank
     * @param noOfElements the number of elements in the array
     * @return index the proper index in the array 
     */
    public int convertIndex(String token, int noOfElements) {
        int index = Integer.valueOf(token).intValue(); // OBJ file index starts at 1
        // Convert index to start at 0
        if (index < 0) {
        	index += noOfElements;  
        }
        else {
        	index--;
        }
        return index;
    }
    
    /**
     * Returns the total number of polygons imported
     * 
     * @return the total number of polygons imported
     */
    public int getNoOfPolygons() {
    	// For each group, add the number of polygons in that group to the total
        int total = 0;
        for (int group = 0; group < groups.size(); group++) {
            total += ((Group)groups.get(group)).faces.size();
        }
        return total;
    }
    
    //========================================================================
    // Group class holds a group of faces with a name and material
    //========================================================================

    @SuppressWarnings("unused")
	public class Group {
    	
    	// The variables for the name, material name and ID, 
    	// total number of triangles and an array for all the faces in that group
        private String name;
        private String materialName;
		private int materialID;
        private int noOfTriangles;
        private ArrayList faces;

        /**
         * Create a new group with the given name
         * 
         * @param name the name of the group
         */
        public Group(String name) {
            this.name = name;
            this.materialName = "";
            this.faces = new ArrayList();
        }

        /**
         * Add a new face to the group
         * 
         * @param f the face to add
         */
		public void addFace (Face f) {
        	this.faces.add(f);
        }
        
        /**
         * Add a given number of triangles to the group
         * 
         * @param noOfTriangles the number of triangles to add
         */
        public void addTriangles (int noOfTriangles) {
        	this.noOfTriangles += noOfTriangles;
        }
        
        /**
         * Assign the given material name and ID to the group
         * 
         * @param materialName
         * @param materialID
         */
        public void assignMaterial (String materialName, int materialID) {
        	this.materialName = materialName;
        	this.materialID = materialID;
        }
        
        /**
         * Returns the name of the group
         * 
         * @return the name of the group
         */
        public String getName () {
        	return this.name;
        }
        
        /**
         * Returns the name of the material of this group
         * 
         * @return the name of the material
         */
        public String getMaterialName() {
        	return this.materialName;
        }
        
        /**
         * Returns the number of triangles in the group
         * 
         * @return the number of triangles
         */
        public int getNoOfTriangles() {
        	return this.noOfTriangles;
        }
        
        /**
         * Returns the faces in the group
         * 
         * @return the array list containing the faces
         */
        public ArrayList getFaces() {
        	return this.faces;
        }
    }
}
