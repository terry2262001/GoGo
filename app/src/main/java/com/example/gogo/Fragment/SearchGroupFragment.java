package com.example.gogo.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.gogo.Adapter.SearchMapsRoomAdapter;
import com.example.gogo.Adapter.UserAdapter;
import com.example.gogo.Model.MapRoom;
import com.example.gogo.Model.User;
import com.example.gogo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class SearchGroupFragment extends Fragment {
    private RecyclerView rvSearchGroupMaps;
    private SearchMapsRoomAdapter searchMapsRoomAdapter;
    private List<MapRoom> mapRoomList;

    EditText search_bar;
    ImageView imgBack;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_group, container, false);
        rvSearchGroupMaps = view.findViewById(R.id.rvSearchGroupMaps);
        search_bar = view.findViewById(R.id.search_bar);
        imgBack = view.findViewById(R.id.imgBack);


        readMapsRoom();
        rvSearchGroupMaps.setLayoutManager(new LinearLayoutManager(getContext()));

        mapRoomList = new ArrayList<>();
        searchMapsRoomAdapter = new SearchMapsRoomAdapter(getContext(),mapRoomList);
        rvSearchGroupMaps.setAdapter(searchMapsRoomAdapter);
        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUsersJoin(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        imgBack.setOnClickListener(view1 -> {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new GroupMapFragment()).commit();
        });




        return view;
    }
    private void searchUsersJoin(String s){
        Query query = FirebaseDatabase.getInstance().getReference("MapRooms").orderByChild("roomname")
                .startAt(s)
                .endAt(s +"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mapRoomList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    MapRoom mapRoom = dataSnapshot.getValue(MapRoom.class);
                    mapRoomList.add(mapRoom);
                }
                searchMapsRoomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void readMapsRoom(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("MapRooms");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!search_bar.getText().toString().equals("")){
                    mapRoomList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                       MapRoom mapRoom = dataSnapshot.getValue(MapRoom.class);
                        mapRoomList.add(mapRoom);
                    }
                    searchMapsRoomAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}