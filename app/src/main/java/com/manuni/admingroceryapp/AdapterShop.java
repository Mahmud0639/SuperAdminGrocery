package com.manuni.admingroceryapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.admingroceryapp.databinding.RowShopBinding;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.AdapterShopViewHolder>{

    long completedOrder;
    long  cancelledOrders;
    long allOrdersCount;


    private Context context;
    public ArrayList<ModelShop> list;

    public AdapterShop(Context context, ArrayList<ModelShop> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public AdapterShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop,parent,false);
        return new AdapterShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterShopViewHolder holder, int position) {
        ModelShop data = list.get(position);

        String accountType = data.getAccountType();
        String address = data.getAddress();
        String city = data.getCity();
        String country = data.getCountryName();
        String deliveryFee = data.getDeliveryFee();
        String email = data.getEmail();
        String latitude = data.getLatitude();
        String longitude = data.getLongitude();
        String online = data.getOnline();
        String name = data.getFullName();
        String phone = data.getPhoneNumber();
        String uid = data.getUid();
        String timestamp = data.getTimestamp();
        String shopOpen = data.getShopOpen();
        String state = data.getState();
        String profileImage = data.getProfileImage();
        String shopName = data.getShopName();



        loadShopTotalOrders(uid,holder);
        loadAllCompletedOrders(uid);
        loadAllCancelledOrders(uid);


        loadRatings(data,holder);

        holder.binding.shopNameTV.setText(data.getShopName());
        holder.binding.phoneTV.setText(data.getPhoneNumber());
        holder.binding.addressTV.setText(data.getAddress());




        if (shopOpen.equals("true")){
            holder.binding.closedTV.setVisibility(View.GONE);
        }else {
            holder.binding.closedTV.setVisibility(View.VISIBLE);
        }

        try {
            Glide.with(context).load(profileImage).placeholder(R.drawable.impl1).into(holder.binding.shopIV);
        }catch (Exception e){
            holder.binding.shopIV.setImageResource(R.drawable.impl1);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,ShopDetailsActivity.class);
                intent.putExtra("shopUid",uid);
                intent.putExtra("latitude",latitude);
                intent.putExtra("longitude",longitude);
                context.startActivity(intent);
            }
        });

    }

    private void loadShopTotalOrders(String shopUid,AdapterShopViewHolder myHolder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(shopUid).child("Orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                    allOrdersCount =  snapshot.getChildrenCount();
                    myHolder.binding.orderCount.setText(""+allOrdersCount);

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });


    }

    private float ratingSum = 0;
    private void loadRatings(ModelShop data, AdapterShopViewHolder holder) {

        String shopUid = data.getUid();


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(shopUid).child("Ratings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ratingSum = 0;
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    float rating = Float.parseFloat(""+dataSnapshot.child("ratings").getValue());//e.g 4.5
                    ratingSum = ratingSum+rating;


                }


                long numberOfReviews = snapshot.getChildrenCount();
                float avgOfReviews = ratingSum/numberOfReviews;

                holder.binding.ratingBar.setRating(avgOfReviews);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class AdapterShopViewHolder extends RecyclerView.ViewHolder{
        RowShopBinding binding;

        public AdapterShopViewHolder(View itemView) {
            super(itemView);

            binding = RowShopBinding.bind(itemView);
        }
    }
    private void loadAllCompletedOrders(String shopId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(shopId).child("Orders").orderByChild("orderStatus").equalTo("Completed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                completedOrder = snapshot.getChildrenCount();


            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void loadAllCancelledOrders(String shopId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbRef.child(shopId).child("Orders").orderByChild("orderStatus").equalTo("Cancelled").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                cancelledOrders = snapshot.getChildrenCount();

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
}
