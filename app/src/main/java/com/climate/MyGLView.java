package com.climate;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import org.w3c.dom.Text;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLView extends GLSurfaceView {
    private static final String TAG = "MyGLView";
    private float previousX;
    private float previousY;
    private AirVentRenderer mRenderer;

    public MyGLView(Context context) {
        super(context);
        init(context);
    }

    public MyGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);
        mRenderer = new AirVentRenderer(context);
        setRenderer(mRenderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - previousX;
                float dy = y - previousY;

                mRenderer.setAngle(mRenderer.getAngle() + ((dx + dy) * 0.5f));

                requestRender();
                break;
        }

        previousX = x;
        previousY = y;
        return true;
    }

    private static class AirVentRenderer implements Renderer {
        private AirFlow mAirFlow;
        private float mAngle;
        private float[] mViewMatrix = new float[16];
        private float[] mProjectionMatrix = new float[16];

        public AirVentRenderer(Context context) {
            mAirFlow = new AirFlow(context);
        }

        public float getAngle() {
            return mAngle;
        }

        public void setAngle(float angle) {
            mAngle = angle;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(0f, 0f, 0f, 1f);
            mAirFlow.initialize();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            glViewport(0, 0, width, height);
            float ratio = (float) width / height;
            float left = -ratio;
            float right = ratio;
            float bottom = -1.0f;
            float top = 1.0f;
            float near = 3.0f;
            float far = 7.0f;

            Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GL_COLOR_BUFFER_BIT);
            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
//
//            float[] mvpMatrix = new float[16];
//            Matrix.multiplyMM(mvpMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
            mAirFlow.draw(mAngle, mViewMatrix, mProjectionMatrix);
        }
    }
}
