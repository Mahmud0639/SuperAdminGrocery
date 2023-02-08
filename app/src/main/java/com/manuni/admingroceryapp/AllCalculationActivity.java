package com.manuni.admingroceryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.admingroceryapp.databinding.ActivityAllCalculationBinding;

import java.util.ArrayList;

public class AllCalculationActivity extends AppCompatActivity {
    ActivityAllCalculationBinding binding;
    private AdapterCalculation adapterCalculation;
    private ArrayList<ModelCalculation> list;
    private String shopUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllCalculationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.totalLinear.setVisibility(View.GONE);

        shopUid = getIntent().getStringExtra("shopId");

        loadTotalTaka();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("OrderInfo");
        dbRef.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    list = new ArrayList<>();
                    list.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        ModelCalculation data = dataSnapshot.getValue(ModelCalculation.class);
                        list.add(0,data);
                    }

                    adapterCalculation = new AdapterCalculation(AllCalculationActivity.this,list);

                    binding.calculationRV.setAdapter(adapterCalculation);
                    adapterCalculation.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        binding.totalTakaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.totalLinear.setVisibility(View.VISIBLE);

            }
        });

        binding.gotItBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.totalLinear.setVisibility(View.GONE);
            }
        });

        binding.searchCalculation.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterCalculation.getFilter().filter(charSequence);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });




    }
    private double allTotalCompleted=0.0, allTotalDelivery=0.0, allTotalFullDayDelivery=0.0;

    private void loadTotalTaka() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("OrderInfo");
        databaseReference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String totalCompleted = ""+dataSnapshot.child("totalOfCompleted").getValue();
                        String totalDelivery = ""+dataSnapshot.child("completed").getValue();
                        String shopDeliveryFee = ""+dataSnapshot.child("deliveryFeeFullDay").getValue();


                        double totalCom = Double.parseDouble(totalCompleted);
                        allTotalCompleted = allTotalCompleted + totalCom;

                        double totalDel = Double.parseDouble(totalDelivery);
                        allTotalDelivery = allTotalDelivery+totalDel;

                        double totalShopDel = Double.parseDouble(shopDeliveryFee);
                        allTotalFullDayDelivery = allTotalFullDayDelivery+totalShopDel;

                    }

                    binding.totalEarnedTV.setText("Total Income: "+String.format("%.2f",allTotalCompleted)+"Tk");
                    binding.totalDeliveryTV.setText("Total Delivery: "+allTotalDelivery);
                    binding.totalFullDayDelTV.setText("Total Delivery Charge: "+String.format("%.2f",allTotalFullDayDelivery)+"Tk");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
}