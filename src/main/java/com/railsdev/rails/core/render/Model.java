package com.railsdev.rails.core.render;

import com.railsdev.rails.core.render.shaders.Shader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.IOException;
import java.io.Serializable;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.assimp.Assimp.*;

/**
 * A 3D model formed from a collection of {@link Mesh}. Each mesh has it's own texture/shader properties.<br>
 * To load a mesh from any standard file format use {@link Model#fromFile(String)}.
 * @author Hamish Weir 2021
 * @see java.io.Serializable
 */
public class Model implements Serializable {

    private static final Logger LOGGER = LogManager.getLogger(Model.class);

    // List of supported texture types and their internal name - tbh this shouldn't be defined in this class
    private static final Object[][] TEXTURE_TYPES = {
        {aiTextureType_DIFFUSE,"diffuse"},
        {aiTextureType_BASE_COLOR,"base_color"},
        {aiTextureType_NORMALS,"normal"},
        {aiTextureType_METALNESS,"metalness"},
        {aiTextureType_DIFFUSE_ROUGHNESS,"roughness"},
        {aiTextureType_AMBIENT_OCCLUSION,"ao"}

    };

    //----------------------------------------------------------//
    //                  MEMBERS                                 //
    //----------------------------------------------------------//

    public Mesh[] meshes;

    //----------------------------------------------------------//
    //                  OBJECT INTERFACE                        //
    //----------------------------------------------------------//


    /**
     * Create a new model from an existing set of meshes.
     * @param meshes
     */
    public Model(Mesh... meshes) {
        this.meshes = meshes;
    }

    /**
     * Prepare model for rendering.
     * @return Self reference for method chaining.
     * @throws IOException If external resources could not be prepared.
     */
    public Model create() throws IOException{
        for (Mesh m : meshes){
            m.create();
        }
        return this;
    }

    /**
     * Draws each {@link Mesh} of the Model. Default shaders are overridden.
     * @param encoder Encoder ID to use
     * @param shader Shader to use
     */
    public void draw(long encoder, Shader shader){
        for (Mesh m : meshes){
            m.draw(encoder,shader,1);//TODO fix view parameter
        }
    }

    //-----------------------------------------------------------//
    //                  MODEL LOADING                            //
    //-----------------------------------------------------------//

    /**
     * Load a model from file using assimp.
     * @implNote This operation is expensive due to complex nature of parsing external files. Do not use in hot code (favour serialization) and consider delegating to worker thread.
     * @param path location of 3D model file relative to executable. Obj/dae/gltf.
     * @return An initialised (for now, this will change) instance of the model available for rendering inside the engine.
     * @throws IOException if the file is not found or some other non-recoverable issue occurs while loading.
     */
    public static Model fromFile(String path) throws IOException {
        AIScene scene = aiImportFile(path,aiProcess_JoinIdenticalVertices | aiProcess_Triangulate | aiProcess_OptimizeMeshes | aiProcess_MakeLeftHanded);

        if (scene == null || scene.mRootNode() == null){
            throw new IOException("Did not load '" + path + "' : Scene is empty");
        }

        int meshCount = scene.mNumMeshes();
        Mesh[] meshes = new Mesh[meshCount];
        processNode(scene.mRootNode(),scene, meshes,0);

        return new Model(meshes);
    }

    private static int processNode(AINode node, AIScene scene, Mesh[] meshes, int meshPos) throws IOException {

        // Load this nodes meshes
        int numMeshes = node.mNumMeshes();
        //Mesh[] meshes = new Mesh[numMeshes];

        IntBuffer nodeMeshes = node.mMeshes();          //Get indexes of this nodes meshes within the overall scene
        PointerBuffer sceneMeshes = scene.mMeshes();    //Get a pointer to meshes within scene
        for (int i = 0; i < numMeshes; i++) {

            if (meshPos >= meshes.length){
                LOGGER.warn("More meshes than expected. Some geometry has been skipped.");
                break;
            }

            AIMesh aiMesh =  AIMesh.create(sceneMeshes.get(nodeMeshes.get(i))); //wtf.. Find the mesh data within the scene for each mesh on this node
            meshes[meshPos] = processMesh(aiMesh, scene);                       //Process meshes and store

            meshPos++;

        }

        // Repeat for each child node
        PointerBuffer aiNodes = node.mChildren();
        for (int i = 0; i < node.mNumChildren(); i++){
            meshPos = processNode(AINode.create(aiNodes.get(i)),scene, meshes, meshPos);
            //arraycopy(processNode(subMeshes,0,meshes,meshPos,meshes.length - meshPos);

        }

        return meshPos;
    }

    private static Mesh processMesh(AIMesh mesh, AIScene scene) throws IOException {
        int vertexCount = mesh.mNumVertices();
        int faceCount = mesh.mNumFaces();
        String name = mesh.mName().dataString();

        AIVector3D.Buffer vertexBuf = mesh.mVertices();
        AIVector3D.Buffer normalBuf = mesh.mNormals();
        AIVector3D.Buffer texBuf0 = mesh.mTextureCoords(0); // We only support one texture uv map at the moment
        AIFace.Buffer faceBuf = mesh.mFaces();

        Serializable[][] vertices = new Serializable[vertexCount][3+3+2]; // vec3 pos, vec3 normal, vec2 tex0
        int[] indices = new int[faceCount * 3];

        int i = 0;
        while (vertexBuf.remaining() > 0 && i <= vertexCount) {
            AIVector3D aiVertex = vertexBuf.get();
            vertices[i][0] = aiVertex.x();
            vertices[i][1] = aiVertex.y();
            vertices[i][2] = aiVertex.z();

            AIVector3D aiNormal = normalBuf.get();
            vertices[i][3] = aiNormal.x();
            vertices[i][4] = aiNormal.y();
            vertices[i][5] = aiNormal.z();

            AIVector3D aiTex = texBuf0.get();
            vertices[i][6] = aiTex.x();
            vertices[i][7] = aiTex.y();

            i++;
        }

        int indexPos = 0;
        for (int ii = 0; ii < faceCount; ii++) {
            AIFace aiFace = faceBuf.get(ii);
            IntBuffer buffer = aiFace.mIndices();
            for(int j = 0; j <aiFace.mNumIndices(); j++){
                indices[indexPos++] = buffer.get(j);
            }
        }


        //mesh.mMaterialIndex();
        PointerBuffer aiMaterials = scene.mMaterials();
        AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(mesh.mMaterialIndex()));
        Map<String, String[]> textures = getTextures(aiMaterial);

        LOGGER.debug("Loaded '{}' verts: {}, tris: {}, mat: {}",name, vertexCount, faceCount, textures.keySet());

        return new Mesh(vertices,indices, textures.get("diffuse")) //TODO create system to pick required/available textures
                .setVertexLayout(Mesh.VertexType.POSITION,Mesh.VertexType.NORMAL,Mesh.VertexType.UV)
                .create();
    }


    /**
     * Get a list of all textures for each supported texture type.
     * @implNote Uses dynamic heap allocation - don't use in hot code.
     * @param aiMaterial The material to fetch textures for
     * @return List of texture paths per texture type
     */
    private static Map<String, String[]> getTextures(AIMaterial aiMaterial){

        Map<String, String[]> textureMap = new HashMap<>();
        AIString pathBuf = AIString.calloc();

        for (var type : TEXTURE_TYPES){

            // Create an array to fit each texture
            int count = Assimp.aiGetMaterialTextureCount(aiMaterial, (int) type[0]);

            if (count == 0) continue;

            String[] texturePaths = new String[count];

            // Get each textures path from assimp
            for (int i = 0; i < count; i++){
                Assimp.aiGetMaterialTexture(aiMaterial,(int)type[0],0,pathBuf,(IntBuffer) null,null,null,null,null,null);
                texturePaths[i] = pathBuf.dataString();
            }

            textureMap.put((String) type[1], texturePaths);
        }

        return textureMap;
    }
}
