package com.example.gogo.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gogo.Adapter.MapRoomAdapter;
import com.example.gogo.Adapter.MyPhotoAdapter;
import com.example.gogo.AddRoomMapsActivity;
import com.example.gogo.Model.MapRoom;
import com.example.gogo.Model.Post;
import com.example.gogo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GroupMapFragment extends Fragment {
    ImageView imgAddGroup,imgSearch;
    RecyclerView rvGroupMaps;
    ArrayList<MapRoom> mapRoomList;
    MapRoomAdapter mapRoomAdapter;
    String profileid;
    EditText search_bar;
    TextView tvMapsRoom;
    ImageView imgBack;
    List<String> userJoinedList;
    List<String> mapRoomidList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_map, container, false);
        imgAddGroup = view.findViewById(R.id.imgAddGroup);
        imgSearch = view.findViewById(R.id.imgSearch);
        search_bar = view.findViewById(R.id.search_bar);
        tvMapsRoom = view.findViewById(R.id.tvMapsRoom);
        imgBack = view.findViewById(R.id.imgBack);



//        SharedPreferences pref = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
//        profileid = pref.getString("profileid", "none");
        profileid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rvGroupMaps = view.findViewById(R.id.rvGroupMaps);
        rvGroupMaps.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 2);
        mapRoomList = new ArrayList<>();
        rvGroupMaps.setLayoutManager(linearLayoutManager);
        mapRoomList = new ArrayList<>();
        mapRoomAdapter = new MapRoomAdapter(getContext(), mapRoomList);
        rvGroupMaps.setAdapter(mapRoomAdapter);
        imgAddGroup.setOnClickListener(view1 -> {
            startActivity(new Intent(getContext(), AddRoomMapsActivity.class));
        });
        imgSearch.setOnClickListener(view1 -> {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new SearchGroupFragment()).commit();
        });
        checkJoined();

        return view;
    }
    private void checkJoined(){
        userJoinedList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Join").child(FirebaseAuth.getInstance().getUid()).child("joined");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userJoinedList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    userJoinedList.add(snapshot1.getKey());

                }
                readRoom();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


private void readRoom() {
    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("MapRooms");
    reference1.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            mapRoomList.clear();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                MapRoom mapRoom = dataSnapshot.getValue(MapRoom.class);

                for (String id : userJoinedList) {
                    if (mapRoom.getRoomid().equals(id)) {
                        mapRoomList.add(mapRoom);
                    }

                }
                if (mapRoom.getPublisher().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    mapRoomList.add(mapRoom);
                }
            }
            mapRoomAdapter.notifyDataSetChanged();
            // progressBar.setVisibility(View.GONE);

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
}
}