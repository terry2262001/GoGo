package com.example.gogo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class AddRoomMapsActivity extends AppCompatActivity {
    TextView tvAdd;
    MaterialEditText  etNameRoom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room_maps);
        tvAdd = findViewById(R.id.tvAdd);
        etNameRoom = findViewById(R.id.etNameRoom);
        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateRoom();
            }
        });
    }

    private void updateRoom() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("MapRooms");
        String roomid = reference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("roomid", roomid);
        hashMap.put("imageURL","https://firebasestorage.googleapis.com/v0/b/gogo-3b657.appspot.com/o/imgMap.PNG?alt=media&token=b2686092-71b5-4f5f-9141-b29433f92d51");
        hashMap.put("roomname", etNameRoom.getText().toString());
        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap.put("rating", 0);

        reference.child(roomid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(AddRoomMapsActivity.this, "Add room success", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    finish();
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddRoomMapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}