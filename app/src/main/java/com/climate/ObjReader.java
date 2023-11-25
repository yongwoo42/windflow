package com.climate;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;


public class ObjReader {
    private List<float[]> mVertices = new ArrayList<>();
    private List<float[]> mNormals = new ArrayList<>();
    private List<float[]> mTexCoords = new ArrayList<>();
    private List<int[]> mFaces = new ArrayList<>();
    private int mVertexCount = 0;
    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;

    public ObjReader(Context context, int resourceId) {
        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                processLine(nextLine);
            }
            bufferedReader.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not open resource: " + resourceId, e);
        } catch (Resources.NotFoundException nfe) {
            throw new RuntimeException("Resource not found: " + resourceId, nfe);
        }
    }

    private void processLine(String line) {
        String[] parts = line.trim().split("\\s+");
        switch (parts[0]) {
            case "v":
                mVertices.add(parseVertex(parts));
                break;
            case "vn":
                mNormals.add(parseNormal(parts));
                break;
            case "f":
                mFaces.add(parseFace(parts));
                break;
            case "vt":  // vertex texture
                mTexCoords.add(parseTexCoord(parts));
                break;
        }
    }

    public FloatBuffer getVertexBuffer() {
        return mVertexBuffer;
    }

    public ShortBuffer getDrawListBuffer() {
        return mDrawListBuffer;
    }

    public int getVertexCount() { return mVertexCount; }

    private float[] parseVertex(String[] parts) {
        return new float[]{Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])};
    }

    private float[] parseNormal(String[] parts) {
        return new float[]{Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])};
    }

    private float[] parseTexCoord(String[] parts) {
        return new float[]{Float.parseFloat(parts[1]), Float.parseFloat(parts[2])};
    }

    private int[] parseFace(String[] parts) {
        int[] face = new int[parts.length - 1];

        for (int i = 1; i < parts.length; i++) {
            String[] indices = parts[i].split("/");
            face[i - 1] = Integer.parseInt(indices[0]) - 1;  // OBJ indices start from 1
        }
        return face;
    }

    public void createBuffer() {
        int verticesCount = mFaces.size() * 3; // Each face has three vertices

        float[] combinedVertices = new float[verticesCount * 3]; // 3 for position

        int vertexIndex = 0;

        for (int[] face : mFaces) {
            for (int i : face) {
                float[] vertex = mVertices.get(i);
                // Combine position and normal into a single vertex
                System.arraycopy(vertex, 0, combinedVertices, vertexIndex * 3, 3);
                vertexIndex++;
            }
        }
        mVertexBuffer = ByteBuffer.allocateDirect(combinedVertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(combinedVertices);
        mVertexBuffer.position(0);
        mVertexCount = combinedVertices.length;
    }

    public void createBuffers() {
        int verticesCount = mFaces.size() * 3; // Each face has three vertices

        float[] combinedVertices = new float[verticesCount * 6]; // 3 for position, 3 for normal
        short[] drawOrder = new short[verticesCount];

        int vertexIndex = 0;

        for (int[] face : mFaces) {
            for (int i : face) {
                float[] vertex = mVertices.get(i);
                float[] normal = mNormals.get(i);

                // Combine position and normal into a single vertex
                System.arraycopy(vertex, 0, combinedVertices, vertexIndex * 6, 3);
                System.arraycopy(normal, 0, combinedVertices, vertexIndex * 6 + 3, 3);

                drawOrder[vertexIndex] = (short) vertexIndex;
                vertexIndex++;
            }
        }

        mVertexBuffer = ByteBuffer.allocateDirect(combinedVertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(combinedVertices);
        mVertexBuffer.position(0);
        mVertexCount = combinedVertices.length;

        mDrawListBuffer = ByteBuffer.allocateDirect(drawOrder.length * 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        mDrawListBuffer.put(drawOrder);
        mDrawListBuffer.position(0);
    }
}
