package com.manuni.admingroceryapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.manuni.admingroceryapp.databinding.ActivityShopDetailsBinding;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ShopDetailsActivity extends AppCompatActivity {
    ActivityShopDetailsBinding binding;
    private String shopUid,latitude,longitude;
    private String deliFee;
    private String shopName;
    private String city,fullAddress;

    private long inProgressOrders;

    private double totalComFeeAsDouble;

    private  long  cancelledOrders;
    private long completedOrder;
    private  long totalOrders;
    private long totalCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        shopUid = getIntent().getStringExtra("shopUid");
        latitude = getIntent().getStringExtra("latitude");
        longitude = getIntent().getStringExtra("longitude");

        loadThisShopDeliveryFee();

        loadAllShopsInfo();
        loadAllCompletedOrders();
        loadAllCancelledOrders();
        loadAllInProgressOrder();



        loadShopAddress();

        loadAccountStatus();


        binding.blockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String,Object> hashMap = new HashMap<>();
                hashMap.put("accountStatus","blocked");

                DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("Users");
                dRef.child(shopUid).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ShopDetailsActivity.this, ""+shopName+" is in blocked state", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.unblockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,Object> hashMapUnblock = new HashMap<>();
                hashMapUnblock.put("accountStatus","unblocked");
                DatabaseReference dRefUnblock = FirebaseDatabase.getInstance().getReference().child("Users");
                dRefUnblock.child(shopUid).updateChildren(hashMapUnblock).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ShopDetailsActivity.this, ""+shopName+" is in unblocked state", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.okCheckedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateTodayInfo();
            }
        });

        binding.showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShopDetailsActivity.this,AllCalculationActivity.class);
                intent.putExtra("shopId",shopUid);
                startActivity(intent);
            }
        });

//        binding.deleteBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });





    }

    private void calculateTodayInfo() {

      totalCount = completedOrder+cancelledOrders;

      totalOrders = totalOrders -totalCount;

      saveTodaysData();


      //deleteCancelledChild();



    }

    private void saveTodaysData() {



        String timestamp = ""+System.currentTimeMillis();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("completed",""+completedOrder);
        hashMap.put("cancelled",""+cancelledOrders);
        hashMap.put("deliveryFeeFullDay",""+totalComFeeAsDouble);
        hashMap.put("shopName",""+shopName);
        hashMap.put("fullAddress",""+fullAddress);
        hashMap.put("inProgress",""+inProgressOrders);
        hashMap.put("totalOfCompleted",""+totalOrderCostForCompleted);
        hashMap.put("totalOfCancelled",""+totalOrderCostForCancelled);
        hashMap.put("totalOfCost",""+totalOrderCostForAll);
        hashMap.put("totalOfInProgress",""+totalOrderCost);
        hashMap.put("shopDeliveryFee",""+deliFee);
        hashMap.put("shopUid",""+shopUid);


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("OrderInfo");
        reference.child(shopUid).push().setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ShopDetailsActivity.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();

                deleteCompletedChild();



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCancelledChild() {
        DatabaseReference dbCompleted = FirebaseDatabase.getInstance().getReference().child("Users");
        dbCompleted.child(shopUid).child("Orders").orderByChild("orderStatus").equalTo("Cancelled").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String orderId =  dataSnapshot.getRef().getKey();

                        assert orderId != null;
                        dbCompleted.child(shopUid).child("Orders").child(orderId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                Toast.makeText(ShopDetailsActivity.this, "Deleted all cancelled orders.", Toast.LENGTH_SHORT).show();



                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                }
                System.exit(0);


            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void deleteCompletedChild(){
        DatabaseReference dbCompleted = FirebaseDatabase.getInstance().getReference().child("Users");
        dbCompleted.child(shopUid).child("Orders").orderByChild("orderStatus").equalTo("Completed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        String orderId =  dataSnapshot.getRef().getKey();

                        assert orderId != null;
                        dbCompleted.child(shopUid).child("Orders").child(orderId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(ShopDetailsActivity.this, "Deleted all completed orders.", Toast.LENGTH_SHORT).show();
                                deleteCancelledChild();


                            }

                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }



                }


            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }

    private void loadAccountStatus(){
        DatabaseReference dref = FirebaseDatabase.getInstance().getReference().child("Users");
        dref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String accountStatus = ""+snapshot.child("accountStatus").getValue();
                    if (accountStatus.equals("blocked")){
                        binding.accountStatusTxt.setTextColor(getResources().getColor(R.color.colorRed));
                        binding.accountStatusTxt.setText("Account Status: "+accountStatus);
                    }else {
                        binding.accountStatusTxt.setTextColor(getResources().getColor(R.color.colorGreen));
                        binding.accountStatusTxt.setText("Account Status: "+accountStatus);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void loadShopAddress(){
        double doubleLatitude = Double.parseDouble(latitude);
        double doubleLongitude = Double.parseDouble(longitude);

        Geocoder geocoder;
        List<Address> addressList;
        geocoder = new Geocoder(ShopDetailsActivity.this, Locale.getDefault());
        try {
            addressList = geocoder.getFromLocation(doubleLatitude,doubleLongitude,1);
            fullAddress = addressList.get(0).getAddressLine(0);
            city = addressList.get(0).getLocality();
            String state = addressList.get(0).getAdminArea();
            String countryName = addressList.get(0).getCountryName();

            binding.fullAddressTxt.setText("Full Address: "+fullAddress);
            binding.cityTxt.setText("City: "+city);
            binding.stateTxt.setText("State: "+state);
            binding.countryTxt.setText("Country: "+countryName);



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadThisShopDeliveryFee() {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dRef.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()){
                    deliFee = "" + snapshot.child("deliveryFee").getValue();
                    shopName = ""+snapshot.child("shopName").getValue();

                    binding.shopNameTV.setText("Shop Name: "+shopName);
                    binding.deliveryFee.setText("Delivery Fee: "+deliFee+" Taka");
                }

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
                if (snapshot.exists()){
                    inProgressOrders = snapshot.getChildrenCount();

                    binding.inProgressOrders.setText("" + inProgressOrders);


                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String orderCost = "" + dataSnapshot.child("orderCost").getValue();

                        double orderCostInDouble = Double.parseDouble(orderCost);
                        totalOrderCost = totalOrderCost + orderCostInDouble;

                    }

                    binding.totalCostInProgress.setText(String.format("%.2f", totalOrderCost));
                }


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
                if (snapshot.exists()){
                    cancelledOrders = snapshot.getChildrenCount();

                    binding.cancelledOrders.setText("" + cancelledOrders);

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String orderCostCancelled = "" + dataSnapshot.child("orderCost").getValue();

                        double orderCostInDouble = Double.parseDouble(orderCostCancelled);
                        totalOrderCostForCancelled = totalOrderCostForCancelled + orderCostInDouble;
                    }

                    binding.totalCostCancelled.setText(String.format("%.2f", totalOrderCostForCancelled));
                }

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
                if (snapshot.exists()){
                    completedOrder = snapshot.getChildrenCount();

                    String completedOrderASString = String.valueOf(completedOrder);
                    double completedOrderAsDouble = Double.parseDouble(completedOrderASString);

                    //long delFeeAsLong = Long.parseLong(deliFee);
                    double delFeeAsDouble = Double.parseDouble(deliFee);

                    totalComFeeAsDouble = completedOrderAsDouble*delFeeAsDouble;



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
                if (snapshot.exists()){
                    totalOrders = snapshot.getChildrenCount();

                    binding.totalOrders.setText("" + totalOrders);

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String totalCostForAllType = "" + dataSnapshot.child("orderCost").getValue();

                        double orderCostInDouble = Double.parseDouble(totalCostForAllType);
                        totalOrderCostForAll = totalOrderCostForAll + orderCostInDouble;
                    }

                    binding.totalCost.setText(String.format("%.2f", totalOrderCostForAll));
                }


            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
}