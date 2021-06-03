package com.railsdev.rails.core.render;

import com.railsdev.rails.core.render.shaders.Shader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.IOException;
import java.io.Serializable;
import java.nio.IntBuffer;

import static org.lwjgl.assimp.Assimp.*;
import static java.lang.System.arraycopy;

/**
 * A 3D model formed from a collection of {@link Mesh}. Each mesh has it's own texture/shader properties.<br>
 * To load a mesh from any standard file format use {@link Model#fromFile(String)}.
 * @author Hamish Weir 2021
 * @see java.io.Serializable
 */
public class Model implements Serializable {

    private static final Logger LOGGER = LogManager.getLogger(Model.class);

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
            m.draw(encoder,shader);
        }
    }

    //-----------------------------------------------------------//
    //                  MODEL LOADING                            //
    //-----------------------------------------------------------//

    /**
     * Load a model from file using assimp.
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
        String[] textures = getTextures(aiMaterial);

        LOGGER.debug("Loaded '{}' verts: {}, tris: {}",name, vertexCount, faceCount);

        return new Mesh(vertices,indices,textures)
                .setVertexLayout(Mesh.VertexType.POSITION,Mesh.VertexType.NORMAL,Mesh.VertexType.UV)
                .create();
    }


    private static String[] getTextures(AIMaterial aiMaterial){

        Assimp.aiGetMaterialTextureCount(aiMaterial, aiTextureType_NONE);
        String[] textures = new String[5];

        AIString path = AIString.calloc();

        Assimp.aiGetMaterialTexture(aiMaterial,aiTextureType_BASE_COLOR,0,path,(IntBuffer) null,null,null,null,null,null);
        textures[0] = path.dataString();

        Assimp.aiGetMaterialTexture(aiMaterial,aiTextureType_NORMALS,0,path,(IntBuffer) null,null,null,null,null,null);
        textures[1] = path.dataString();

        Assimp.aiGetMaterialTexture(aiMaterial,aiTextureType_METALNESS,0,path,(IntBuffer) null,null,null,null,null,null);
        textures[2] = path.dataString();

        Assimp.aiGetMaterialTexture(aiMaterial,aiTextureType_DIFFUSE_ROUGHNESS,0,path,(IntBuffer) null,null,null,null,null,null);
        textures[3] = path.dataString();

        Assimp.aiGetMaterialTexture(aiMaterial,aiTextureType_AMBIENT_OCCLUSION,0,path,(IntBuffer) null,null,null,null,null,null);
        textures[4] = path.dataString();

        return textures;
    }


}
