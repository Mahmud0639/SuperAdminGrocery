package com.manuni.admingroceryapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.admingroceryapp.databinding.ActivityShopDetailsBinding;

public class ShopDetailsActivity extends AppCompatActivity {
    ActivityShopDetailsBinding binding;
    private String shopUid;
    private String deliFee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        shopUid = getIntent().getStringExtra("shopUid");

        loadThisShopDeliveryFee();

        loadAllShopsInfo();
        loadAllCompletedOrders();
        loadAllCancelledOrders();
        loadAllInProgressOrder();





    }

    private void loadThisShopDeliveryFee() {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dRef.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                deliFee = "" + snapshot.child("deliveryFee").getValue();
                String shopName = ""+snapshot.child("shopName").getValue();

                binding.shopNameTV.setText("Shop Name: "+shopName);
                binding.deliveryFee.setText("Delivery Fee: "+deliFee+" Taka");
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private double totalOrderCost = 0.0, totalOrderCostForCancelled = 0.0, totalOrderCostForCompleted = 0.0, totalOrderCostForAll = 0.0;

    private void loadAllInProgressOrder() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.child(shopUid).child("Orders").orderByChild("orderStatus").equalTo("In Progress").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long inProgressOrders = snapshot.getChildrenCount();

                binding.inProgressOrders.setText("" + inProgressOrders);


                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String orderCost = "" + dataSnapshot.child("orderCost").getValue();

                    double orderCostInDouble = Double.parseDouble(orderCost);
                    totalOrderCost = totalOrderCost + orderCostInDouble;

                }

                binding.totalCostInProgress.setText(String.format("%.2f", totalOrderCost));

                ;

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void loadAllCancelledOrders() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dbRef.child(shopUid).child("Orders").orderByChild("orderStatus").equalTo("Cancelled").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long cancelledOrders = snapshot.getChildrenCount();

                binding.cancelledOrders.setText("" + cancelledOrders);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String orderCostCancelled = "" + dataSnapshot.child("orderCost").getValue();

                    double orderCostInDouble = Double.parseDouble(orderCostCancelled);
                    totalOrderCostForCancelled = totalOrderCostForCancelled + orderCostInDouble;
                }

                binding.totalCostCancelled.setText(String.format("%.2f", totalOrderCostForCancelled));
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void loadAllCompletedOrders() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(shopUid).child("Orders").orderByChild("orderStatus").equalTo("Completed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long completedOrder = snapshot.getChildrenCount();

                String completedOrderASString = String.valueOf(completedOrder);
                double completedOrderAsDouble = Double.parseDouble(completedOrderASString);

                //long delFeeAsLong = Long.parseLong(deliFee);
                double delFeeAsDouble = Double.parseDouble(deliFee);

                double totalComFeeAsDouble = completedOrderAsDouble*delFeeAsDouble;



               // long totalCompletedDelFee = (long) (completedOrder*delFeeAsDouble);

                //binding.deliFeeForCompleted.setText(totalCompletedDelFee+" Taka");
                binding.deliFeeForCompleted.setText(String.format("%.2f",totalComFeeAsDouble)+" Taka");





                binding.completedOrders.setText("" + completedOrder);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String orderCostCompleted = "" + dataSnapshot.child("orderCost").getValue();

                    double orderCostInDouble = Double.parseDouble(orderCostCompleted);
                    totalOrderCostForCompleted = totalOrderCostForCompleted + orderCostInDouble;
                }

                binding.totalCompleted.setText(String.format("%.2f", totalOrderCostForCompleted));
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void loadAllShopsInfo() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users");
        db.child(shopUid).child("Orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long totalOrders = snapshot.getChildrenCount();

                binding.totalOrders.setText("" + totalOrders);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String totalCostForAllType = "" + dataSnapshot.child("orderCost").getValue();

                    double orderCostInDouble = Double.parseDouble(totalCostForAllType);
                    totalOrderCostForAll = totalOrderCostForAll + orderCostInDouble;
                }

                binding.totalCost.setText(String.format("%.2f", totalOrderCostForAll));

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
}