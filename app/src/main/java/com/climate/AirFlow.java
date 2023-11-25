package com.climate;

import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;


public class AirFlow {
    private static final String TAG = "AirFlow";

    private static final int BYTES_PER_FLOAT = 4;

    private ObjReader mReader;

    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    private float[] mModelMatrix = new float[16];
    static final int COORDS_PER_VERTEX = 3;
    private final int vertexStride = COORDS_PER_VERTEX * BYTES_PER_FLOAT; // 4 bytes per vertex

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
            "uniform mat4 uMVPMatrix;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";


    float color[] = {1.0f, 1.0f, 0.0f, 1.0f};

    public AirFlow(Context context) {
        mReader = new ObjReader(context, R.raw.wind_1);
        mReader.createBuffer();
        Matrix.setIdentityM(mModelMatrix, 0);
    }

    public void initialize() {
        Log.d(TAG, "initialize: ");
        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderCode);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderCode);
        mProgram = ShaderHelper.linkProgram(vertexShader, fragmentShader);
        mPositionHandle = glGetAttribLocation(mProgram, "vPosition");
    }

    public void draw(float angle, float[] vMatrix, float[] pMatrix) {
        glUseProgram(mProgram);

        glEnableVertexAttribArray(mPositionHandle);
        glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, mReader.getVertexBuffer());

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] mvpMatrix = new float[16];
        // Apply rotation to the model matrix
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angle, 1.0f, 0.0f, 0f);

        // Combine the model matrix with the view and projection matrices
        Matrix.multiplyMM(mvpMatrix, 0, vMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvpMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mReader.getVertexCount());

        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
