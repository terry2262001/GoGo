package com.example.gogo.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gogo.CommentActivity;
import com.example.gogo.FollowerActivity;
import com.example.gogo.Fragment.PostDetailFragment;
import com.example.gogo.Fragment.ProfileFragment;
import com.example.gogo.Model.Post;
import com.example.gogo.Model.User;
import com.example.gogo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostVH> {
    public List<Post> mPosts;
    public Context mContext;

    private FirebaseUser firebaseUser;

    public PostAdapter( Context mContext,List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;

    }

    @NonNull
    @Override
    public PostVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item,parent,false);

        return new PostAdapter.PostVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostVH holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mPosts.get(position);



        Glide.with(mContext).load(post.getPostImage())
                .apply(new RequestOptions().placeholder(R.drawable.nopicture))
                .into(holder.imgPost);
        if(post.getDescription().equals("")){
            holder.tvDescription.setVisibility(View.GONE);
        }else{
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(post.getDescription());
        }
        publisherInfo(holder.imgProfile,holder.tvUsername,holder.tvPublisher,post.getPublisher());

        isLiked(post.getPostId(), holder.imgLike);
        nrLike(holder.tvLikes,post.getPostId());
        getComments(post.getPostId(),holder.tvComments);
        isSaved(post.getPostId(),holder.imgSave);


        holder.tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("profileid",post.getPublisher());
                editor.apply();
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                        ,new ProfileFragment()).commit();
            }
        });
        holder.imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("profileid",post.getPublisher());
                editor.apply();
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                        ,new ProfileFragment()).commit();
            }
        });
        holder.tvPublisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("profileid",post.getPublisher());
                editor.apply();
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                        ,new ProfileFragment()).commit();
            }
        });
        holder.imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("postid",post.getPostId());
                editor.apply();
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                        ,new PostDetailFragment()).commit();

            }
        });

        holder.imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if(holder.imgLike.getTag().equals("like")){
                        FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                                .child(firebaseUser.getUid()).setValue(true);
                        addNotifications(post.getPublisher(),post.getPostId());
                    }else {
                        FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                                .child(firebaseUser.getUid()).removeValue();

                    }

            }
        });
        holder.imgPost.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("postid",post.getPostId());
                editor.apply();
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                        ,new PostDetailFragment()).commit();

            }

            @Override
            public void onDoubleClick(View v) {
                if(holder.imgLike.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                            .child(firebaseUser.getUid()).setValue(true);
                    addNotifications(post.getPublisher(),post.getPostId());
                }else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                            .child(firebaseUser.getUid()).removeValue();

                }

            }
        });
        holder.imgComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postid",post.getPostId());
                intent.putExtra("publisherid",post.getPublisher());

                mContext.startActivity(intent);
            }
        });
        holder.tvComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postid",post.getPostId());
                intent.putExtra("publisherid",post.getPublisher());
                mContext.startActivity(intent);
            }
        });
        holder.imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.imgSave.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostId()).setValue(true);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostId()).removeValue();
                }

            }
        });
        holder.tvLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FollowerActivity.class);
                intent.putExtra("id",post.getPostId());
                intent.putExtra("title","likes");
                mContext.startActivity(intent);
            }
        });




    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class PostVH extends RecyclerView.ViewHolder {

        TextView tvUsername,tvPublisher,tvDescription,tvComments,tvLikes;
        ImageView imgProfile, imgPost,imgLike,imgComment,imgSave;

        public PostVH(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            imgPost = itemView.findViewById(R.id.imgPost);
            imgLike = itemView.findViewById(R.id.imgLike);
            imgComment = itemView.findViewById(R.id.imgComment);
            imgSave = itemView.findViewById(R.id.imgSave);
            tvPublisher = itemView.findViewById(R.id.tvPublisher);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvComments = itemView.findViewById(R.id.tvComments);
            tvLikes = itemView.findViewById(R.id.tvLikes);


        }
    }

    private void getComments(String postid,TextView tvComments){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvComments.setText("View All " + snapshot.getChildrenCount()+" Comments" );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isLiked(String postId, ImageView imageView){
        FirebaseUser firebaseUser =  FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference =  FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                }else{
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addNotifications(String userid,String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("text","like your post");
        hashMap.put("postid",postid);
        hashMap.put("ispost",true);

        reference.push().setValue(hashMap);
    }
    private void nrLike(TextView likes , String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likes.setText(snapshot.getChildrenCount() +" likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public abstract class DoubleClickListener implements View.OnClickListener {

        private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

        long lastClickTime = 0;

        @Override
        public void onClick(View v) {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
                onDoubleClick(v);
                lastClickTime = 0;
            } else {
                onSingleClick(v);
            }
            lastClickTime = clickTime;
        }

        public abstract void onSingleClick(View v);
        public abstract void onDoubleClick(View v);
    }


    private void publisherInfo(final ImageView imgProfile,final TextView tvUsername,final TextView tvPublisher,final String userid){
        DatabaseReference reference  = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageURL()).into(imgProfile);
                tvUsername.setText(user.getUsername());
                tvPublisher.setText(user.getUsername());



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void isSaved(final String postid,ImageView imgSave){
        DatabaseReference  reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postid).exists() ){
                    imgSave.setImageResource(R.drawable.ic_save);
                    imgSave.setTag("saved");
                }else {
                    imgSave.setImageResource(R.drawable.ic_save_black);
                    imgSave.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
