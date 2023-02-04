package com.manuni.admingroceryapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.admingroceryapp.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private ArrayList<ModelShop> list;
    private AdapterShop adapterShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadAllShops();


    }
    private void loadAllShops(){


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.orderByChild("accountType").equalTo("Seller").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                list = new ArrayList<>();
                list.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    ModelShop data = dataSnapshot.getValue(ModelShop.class);

                    list.add(data);
                }
                adapterShop = new AdapterShop(MainActivity.this,list);
                binding.allShopSellerRV.setAdapter(adapterShop);
                adapterShop.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });


    }
}