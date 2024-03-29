package com.example.gogo;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {
    private Uri mImageUri;
    String myUrl = "";
    private StorageTask storageTask;
    StorageReference storageReference ;
    private final int CODE_IMG_GALLERY= 1;
    private String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);
        storageReference  = FirebaseStorage.getInstance().getReference("story");
          openImage1();

    }
    private String getFileExtentison(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }
    private void publishStory(){
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.show();
        if (mImageUri != null){
            StorageReference imageReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtentison(mImageUri));
            storageTask =  imageReference.putFile(mImageUri);
            storageTask.continueWithTask(new Continuation() {
                @Override
                public Task<Uri> then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                                .child(myid);

                        String storyid = reference.push().getKey();
                        long timeend = System.currentTimeMillis()+86400000;// 1day86400000

                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("imageurl",myUrl);
                        hashMap.put("timestart", ServerValue.TIMESTAMP);
                        hashMap.put("timeend",timeend);
                        hashMap.put("storyid",storyid);
                        hashMap.put("userid",myid);

                        reference.child(storyid).setValue(hashMap);
                        pd.dismiss();
                        finish();

                    }else {
                        Toast.makeText(AddStoryActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddStoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(this, "No image selected !", Toast.LENGTH_SHORT).show();
        }

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK){
//           // mImageUri = data.get
//
//            publishStory();
//        }else {
//            Toast.makeText(this, "Something gone", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(AddStoryActivity.this,MainActivity.class));
//            finish();
//        }
//    }
    private void openImage1(){
        startActivityForResult(new Intent()
                .setAction(Intent.ACTION_GET_CONTENT)
                .setType("image/*"),CODE_IMG_GALLERY);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CODE_IMG_GALLERY  && resultCode == RESULT_OK) {
            Uri imageUri1  = data.getData();
            if (imageUri1 != null){
                startCrop(imageUri1);
            }

        }else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK){
            mImageUri = UCrop.getOutput(data);
            if (mImageUri != null){
                publishStory();
                //mImageUri.setImageURI(mImageUri);
            }

        }else {
            Toast.makeText(this, "Something not gone", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryActivity.this, MainActivity.class));
            finish();
        }
    }
    private void  startCrop(@NonNull Uri uri){
        String destinationFileName =SAMPLE_CROPPED_IMG_NAME ;
        destinationFileName += ".pnj";
        UCrop uCrop = UCrop.of(uri,Uri.fromFile(new File(getCacheDir(),destinationFileName)));
        uCrop.withAspectRatio(16, 9);
        uCrop.withAspectRatio(1,1);
        uCrop.withMaxResultSize(450,600);
        uCrop.withOptions(getOptions());
        uCrop.start(AddStoryActivity.this);

    }

    private UCrop.Options getOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(70);
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(true);

        // options.setStatusBarColor();
        options.setToolbarTitle("Edit Image");
        return options;
    }
}