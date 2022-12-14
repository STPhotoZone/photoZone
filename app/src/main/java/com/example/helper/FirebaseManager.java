package com.example.helper;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.stphotozone.MyChr;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;


/** Helper class for Firebase storage of cloud anchor IDs. */
public class FirebaseManager {

    /** Listener for a new Cloud Anchor ID from the Firebase Database. */
    public interface CloudAnchorIdListener {
        void onCloudAnchorIdAvailable(String cloudAnchorId);
    }

    /** Listener for a new short code from the Firebase Database. */
    public interface ShortCodeListener {
        void onShortCodeAvailable(Integer shortCode);
    }

    public interface ModelListener {
        void onModelAvailable(int model);
    }


    private static final String TAG = FirebaseManager.class.getName();
    private static final String KEY_ROOT_DIR = "shared_anchor"; // 실시간 데이터베이스 가장 root
    private static final String KEY_NEXT_SHORT_CODE = "next_short";
    private static final String KEY_PREFIX = "anchor;";
    private static final int INITIAL_SHORT_CODE = 142; // 초기 ShortCode
    private final DatabaseReference rootRef; // 데이터 베이스 주소 저자
    int model;

    /** Constructor that initializes the Firebase connection. */
    public FirebaseManager() {
        //FirebaseApp firebaseApp = FirebaseApp.initializeApp(context);
        rootRef = FirebaseDatabase.getInstance("https://st-ar-photozone-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(KEY_ROOT_DIR);
        DatabaseReference.goOnline();
    }

    /** Gets a new short code that can be used to store the anchor ID. */
    public void nextShortCode(ShortCodeListener listener) {
        // Run a transaction on the node containing the next short code available. This increments the
        // value in the database and retrieves it in one atomic all-or-nothing operation.
        rootRef
                .child(KEY_NEXT_SHORT_CODE)
                .runTransaction(
                        new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData currentData) {
                                Integer shortCode = currentData.getValue(Integer.class);
                                if (shortCode == null) {
                                    // Set the initial short code if one did not exist before.
                                    shortCode = INITIAL_SHORT_CODE - 1; //
                                }
                                currentData.setValue(shortCode + 1);
                                return Transaction.success(currentData);
                            }

                            @Override
                            public void onComplete(
                                    DatabaseError error, boolean committed, DataSnapshot currentData) {
                                if (!committed) {
                                    Log.e(TAG, "Firebase Error", error.toException());
                                    listener.onShortCodeAvailable(null);
                                } else {
                                    listener.onShortCodeAvailable(currentData.getValue(Integer.class));
                                }
                            }
                        });
    }

    /** Stores the cloud anchor ID in the configured Firebase Database. */
    public void storeUsingShortCode(int shortCode, String cloudAnchorId, int model) {
        MyChr character = new MyChr(cloudAnchorId, model);
        rootRef.child(shortCode+"").setValue(character);
    }


    /**
     * Retrieves the cloud anchor ID using a short code. Returns an empty string if a cloud anchor ID
     * was not stored for this short code.
     */
    public void getCloudAnchorId(int shortCode, CloudAnchorIdListener listener, ModelListener modelListener) { // shortCode로 anchor로 보여주게 함!!
        rootRef.child(shortCode+"")
                .addListenerForSingleValueEvent( // 데이터가 변할 때 수신을 위함!!
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Listener invoked when the data is successfully read from Firebase.
                                listener.onCloudAnchorIdAvailable(String.valueOf(dataSnapshot.child("cloud").getValue()));
                                modelListener.onModelAvailable(Integer.parseInt(String.valueOf(dataSnapshot.child("model").getValue())));

                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Log.e(
                                        TAG,
                                        "The Firebase operation for getCloudAnchorId was cancelled.",
                                        error.toException());
                                listener.onCloudAnchorIdAvailable(null);
                            }
                        });
    }

}
