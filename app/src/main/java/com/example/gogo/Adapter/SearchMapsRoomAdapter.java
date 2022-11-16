package com.example.gogo.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gogo.Model.MapRoom;
import com.example.gogo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchMapsRoomAdapter extends RecyclerView.Adapter<SearchMapsRoomAdapter.SearchMapsRoomVH> {
    private Context mContext;
    private List<MapRoom> mapsRoomList;
    private FirebaseUser firebaseUser;

    public SearchMapsRoomAdapter(Context mContext, List<MapRoom> mapsRoomList) {
        this.mContext = mContext;
        this.mapsRoomList = mapsRoomList;
    }

    @NonNull
    @Override
    public SearchMapsRoomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_search_maps_room,parent,false);
       // SearchMapsRoomVH searchMapsRoomVH = new SearchMapsRoomVH(view);
        return new  SearchMapsRoomAdapter.SearchMapsRoomVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchMapsRoomVH holder, int position) {
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        MapRoom mapRoom = mapsRoomList.get(position);
        Glide.with(mContext).load(mapRoom.getImageURL()).into(holder.imgMapsRoom);
        holder.tvMapsRoomName.setText(mapRoom.getRoomname());
        isJoin(mapRoom,holder.btJoin);
        if(mapRoom.getPublisher().equals(firebaseUser.getUid())){
            holder.btJoin.setVisibility(View.GONE);
        }
        holder.btJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(holder.btJoin.getText().toString().equals("join")){
////                    FirebaseDatabase.getInstance().getReference().child("Join").child(firebaseUser.getUid())
////                            .child("joined").child(mapRoom.getPublisher()).setValue(true);
////                    FirebaseDatabase.getInstance().getReference().child("Join").child(mapRoom.getPublisher())
////                            .child("joiner").child(firebaseUser.getUid()).setValue(true);
////                  // addNotifications(user.getId());
////                }else {
////                    FirebaseDatabase.getInstance().getReference().child("Join").child(firebaseUser.getUid())
////                            .child("joined").child(mapRoom.getPublisher()).removeValue();
////                    FirebaseDatabase.getInstance().getReference().child("Join").child(mapRoom.getPublisher())
////                            .child("joiner").child(firebaseUser.getUid()).removeValue();
////                }

                if(holder.btJoin.getText().toString().equals("join")){
                    FirebaseDatabase.getInstance().getReference().child("Join").child(firebaseUser.getUid())
                            .child("joined").child(mapRoom.getRoomid()).setValue(true);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("Join").child(firebaseUser.getUid())
                            .child("joined").child(mapRoom.getRoomid()).removeValue();
                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return mapsRoomList.size();
    }

    public class SearchMapsRoomVH extends RecyclerView.ViewHolder {
        CircleImageView imgMapsRoom;
        TextView tvMapsRoomName;
        Button btJoin;
        public SearchMapsRoomVH(@NonNull View itemView) {
            super(itemView);
            imgMapsRoom = itemView.findViewById(R.id.imgMapsRoom);
            tvMapsRoomName = itemView.findViewById(R.id.tvMapsRoomName);
            btJoin = itemView.findViewById(R.id.btJoin);

        }
    }
    private void isJoin(MapRoom mapRoom, Button button){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Join").child(firebaseUser.getUid()).child("joined");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              if (snapshot.child(mapRoom.getRoomid()).exists()){
                  button.setText("joined");
              }else
              {
                  button.setText("join");
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
