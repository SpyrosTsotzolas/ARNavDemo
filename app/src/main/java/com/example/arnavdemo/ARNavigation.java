package com.example.arnavdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;

public class ARNavigation extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;


    private ArFragment mARFragment;
    private ModelRenderable mObjRenderable;
    private Anchor mAnchor = null;
    private TransformableNode mARObject = null;
    private AnchorNode mAnchorNode = null;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        String from = getIntent().getExtras().getString(DestinationActivity.FROM);
        ArrayList<Integer> coordsOfCurrentPos = getIntent().getIntegerArrayListExtra(DestinationActivity.COORDS_OF_CURRENT_POS);
        ArrayList<Integer> coordsOfDestinationId = getIntent().getIntegerArrayListExtra(DestinationActivity.COORDS_OF_DESTINATION_ID);
        ArrayList<Integer> coordsOfEntrance = getIntent().getIntegerArrayListExtra(DestinationActivity.COORDS_OF_ENTRANCE);

        setContentView(R.layout.activity_arnavigation);
        setARFragment();
    }

    private void setARFragment() {
        mARFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().

        ModelRenderable.builder()
                .setSource(this, Uri.parse("model.sfb"))
                .build()
                .thenAccept(renderable -> mObjRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this, "Unable to load obj renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
        mARFragment.getArSceneView().getScene().addOnUpdateListener(this::onSceneUpdate);

        /*
        Node node = new Node();
        node.setParent(mARFragment.getArSceneView().getScene());
        node.setRenderable(mObjRenderable);

        mARFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (mObjRenderable == null) {
                        return;
                    }

                    // Create the Anchor.
                    mAnchor = hitResult.createAnchor();
                    mAnchorNode = new AnchorNode(mAnchor);
                    mAnchorNode.setParent(mARFragment.getArSceneView().getScene());

                    // Create the transformable object and add it to the anchor.
                    mARObject = new TransformableNode(mARFragment.getTransformationSystem());

                    // Set the min and max scales of the ScaleController.
                    // Default min is 0.75, default max is 1.75.
                    mARObject.getScaleController().setMinScale(0.1f);
                    mARObject.getScaleController().setMaxScale(2.0f);

                    // Set the local scale of the node BEFORE setting its parent
                    mARObject.setLocalScale(new Vector3(0.2f, 0.2f, 0.2f));

                    mARObject.setParent(mAnchorNode);
                    mARObject.setRenderable(mObjRenderable);
                    mARObject.select();
                });
                */
    }
    
    private void onSceneUpdate(FrameTime frameTime) {
        // Let the fragment update its state first.
        mARFragment.onUpdate(frameTime);

        // If there is no frame then don't process anything.
        if (mARFragment.getArSceneView().getArFrame() == null) {
            return;
        }

        // If ARCore is not tracking yet, then don't process anything.
        if (mARFragment.getArSceneView().getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        // Place the anchor 1m in front of the camera if anchorNode is null.
        if (this.mAnchorNode == null) {
            Session session = mARFragment.getArSceneView().getSession();
            float[] pos = {0, 0, -10};
            float[] rotation = {90, -180, 120, 90};
            Anchor anchor = session.createAnchor(new Pose(pos, rotation));
            mAnchorNode = new AnchorNode(anchor);
            mAnchorNode.setRenderable(mObjRenderable);
            mAnchorNode.setParent(mARFragment.getArSceneView().getScene());
        }
    }
    

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager)activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        return true;
    }

}
