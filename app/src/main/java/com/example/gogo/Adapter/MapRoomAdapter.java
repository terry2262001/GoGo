package com.example.gogo.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gogo.Fragment.MapsFragment;
import com.example.gogo.Model.MapRoom;
import com.example.gogo.R;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MapRoomAdapter extends RecyclerView.Adapter<MapRoomAdapter.MapRoomVH> {
    private Context mContext;
    private ArrayList<MapRoom> mapRoomList;

    public MapRoomAdapter(Context mContext, ArrayList<MapRoom> mapRoomList) {
        this.mContext = mContext;
        this.mapRoomList = mapRoomList;
    }

    @NonNull
    @Override
    public MapRoomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.room_item,parent,false);
        return new  MapRoomAdapter.MapRoomVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MapRoomVH holder, int position) {
        MapRoom mapRoom = mapRoomList.get(position);
        Glide.with(mContext).load(mapRoom.getImageURL()).into(holder.imgRoom);
        holder.tvNameRoom.setText(mapRoom.getRoomname());

        holder.imgRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("roomid",mapRoom.getRoomid());
                editor.apply();
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                        ,new MapsFragment()).commit();
            }
        });


    }

    @Override
    public int getItemCount() {
        return mapRoomList.size();
    }

    public class MapRoomVH extends RecyclerView.ViewHolder {
     TextView tvNameRoom;
     ImageView imgRoom;

     public MapRoomVH(@NonNull View itemView) {
         super(itemView);
         tvNameRoom =itemView.findViewById(R.id.tvNameRoom);
         imgRoom = itemView.findViewById(R.id.imgRoom);

     }
 }


}
