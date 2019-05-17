package com.wpam.kupmi.firebase.storage;

import android.net.Uri;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wpam.kupmi.firebase.storage.config.StorageConfiguration;

import java.io.File;

import static com.wpam.kupmi.utils.FirebaseUtils.createPath;

public class StorageManager
{
    // Private fields
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Constructors
    public StorageManager()
    {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    // Public methods
    public StorageManager getInstance()
    {
        return new StorageManager();
    }

    public void uploadUserAvatar(String userUID, String imagePath)
    {
        if (userUID != null && imagePath != null)
        {
            Uri file = Uri.fromFile(new File(imagePath));
            StorageReference riversRef = storageRef.child(createPath(StorageConfiguration.USERS_KEY,
                    file.getLastPathSegment()));
            UploadTask uploadTask = riversRef.putFile(file);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            });
        }
    }

    public void downloadUserAvatar(String userUID)
    {

    }

}
