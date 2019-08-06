package com.example.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;

public class TravelDealsAdapter extends RecyclerView.Adapter<TravelDealsAdapter.DealViewHolder>{
    ArrayList<TravellingDeals> dealings;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ImageView imageDeal;
    private ChildEventListener mChildListener;

    public TravelDealsAdapter() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("deals");
        this.dealings = FirebaseUtil.mDeals;
        mChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                TravellingDeals td = dataSnapshot.getValue(TravellingDeals.class);
                Log.d("Deal: ", td.getTitle());
                td.setId(dataSnapshot.getKey());
                dealings.add(td);
                notifyItemInserted(dealings.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

    };
        mDatabaseReference.addChildEventListener(mChildListener);
}
@Override
public TravelDealsAdapter.DealViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    View itemView = LayoutInflater.from(context)
            .inflate(R.layout.recycle_view_rows, parent, false);
    return new DealViewHolder(itemView);

}

    @Override
    public void onBindViewHolder(TravelDealsAdapter.DealViewHolder holder, int position) {
        TravellingDeals deals = dealings.get(position);
        holder.bind(deals);
    }

    @Override
    public int getItemCount() {
        return dealings.size();
    }

public class DealViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener{
    TextView myTitle;
    TextView myDescription;
    TextView myPrice;

    public DealViewHolder(View itemView) {
        super(itemView);
        myTitle = (TextView) itemView.findViewById(R.id.title);
        myDescription = (TextView) itemView.findViewById(R.id.description);
        myPrice = (TextView) itemView.findViewById(R.id.price);
        imageDeal = (ImageView) itemView.findViewById(R.id.images);
        itemView.setOnClickListener(this);

    }
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                Log.d("Click", String.valueOf(position));
                TravellingDeals selectedDeal = dealings.get(position);
                Intent intent = new Intent(view.getContext(), InsertDataActivity.class);
                intent.putExtra("Deal", selectedDeal);
                view.getContext().startActivity(intent);
            }


    public void bind(TravellingDeals deals) {
        myTitle.setText(deals.getTitle());
        myDescription.setText(deals.getDescription());
        myPrice.setText(deals.getPrice());
        showImage(deals.getImageUrl());
    }
 private void showImage(String url){
        if (url != null && !url.isEmpty()) {
            Picasso.get()
                    .load(url)
                    .resize(160, 160)
                    .centerCrop()
                    .into(imageDeal);
        }
 }

}
}
