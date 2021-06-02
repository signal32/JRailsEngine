package com.railsdev.rails.core.render;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.IOException;
import java.nio.IntBuffer;

import static org.lwjgl.assimp.Assimp.*;

public class Model {

    public Mesh[] meshes;

    public Model(Mesh... meshes) {
        this.meshes = meshes;
    }

    public static Model fromFile(String path) throws IOException {
        AIScene scene = aiImportFile(path,aiProcess_JoinIdenticalVertices | aiProcess_Triangulate | aiProcess_FixInfacingNormals);

        if (scene == null || scene.mRootNode() == null){
            throw new RuntimeException("Error loading model");
        }


        int meshCount = scene.mNumMeshes();
        Mesh[] meshes = processNode(scene.mRootNode(),scene, new Mesh[meshCount],0);

        return new Model(meshes);
    }

    public static Mesh[] processNode(AINode node, AIScene scene, Mesh[] meshes, int meshPos) throws IOException {

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
            processNode(AINode.create(aiNodes.get(i)),scene, meshes, meshPos);
        }

        return meshes;
    }

    public static Mesh processMesh(AIMesh mesh, AIScene scene) throws IOException {
        int vertexCount = mesh.mNumVertices();
        int faceCount = mesh.mNumFaces();

        AIVector3D.Buffer vertexBuf = mesh.mVertices();
        AIVector3D.Buffer normalBuf = mesh.mNormals();
        AIVector3D.Buffer texBuf0 = mesh.mTextureCoords(0); // We only support one texture uv map at the moment
        AIFace.Buffer faceBuf = mesh.mFaces();

        int testIndexCount = faceBuf.mIndices().remaining();

        Object[][] vertices = new Object[vertexCount][3+3+2]; // vec3 pos, vec3 normal, vec2 tex0
        int[] indices = new int[faceCount * testIndexCount];

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
            while (buffer.remaining() > 0) {
                //indices.add(buffer.get());
                indices[buffer.position() + indexPos]=buffer.get();
            }
            indexPos+=testIndexCount;
        }

        return new Mesh(vertices,indices)
                //.setVertexLayout(Mesh.VertexType.POSITION,Mesh.VertexType.NORMAL,Mesh.VertexType.UV)
                .setVertexLayout(Mesh.VertexType.POSITION)
                .create();
    }
    public static Texture[] loadTextures(){return null;}
}
