package com.example.travelmantics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class InsertDataActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private static final int PICTURE_RESULT = 42; //the answer to everything
    private EditText mTitle;
    private EditText mPrice;
    private EditText mDescription;
    private ImageView imageView;
    TravellingDeals deal;
    private Button mButton;
    private StorageReference photoStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_data);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
     mDatabaseReference = FirebaseUtil.mDatabaseReference;
     mTitle = (EditText) findViewById(R.id.Title);
     mPrice = (EditText) findViewById(R.id.Price);
     mDescription =(EditText) findViewById(R.id.Description);
     imageView = (ImageView) findViewById(R.id.imageView);

        Intent intent = getIntent();
        TravellingDeals deal = (TravellingDeals) intent.getSerializableExtra("Deal");
        if (deal==null) {
            deal = new TravellingDeals();
        }
        this.deal = deal;
        mTitle.setText(deal.getTitle());
        mDescription.setText(deal.getDescription());
        mPrice.setText(deal.getPrice());
        showImage(deal.getImageUrl());

        Button mAbout = findViewById(R.id.button);
        mAbout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent,
                        "Insert Picture"), PICTURE_RESULT);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.savings_menu, menu);

        if (FirebaseUtil.isAdmin) {
            menu.findItem(R.id.delete_my_menu).setVisible(true);
            menu.findItem(R.id.save_my_menu).setVisible(true);
            enableEditTexts(true);
        } else {
            menu.findItem(R.id.delete_my_menu).setVisible(false);
            menu.findItem(R.id.save_my_menu).setVisible(false);
            enableEditTexts(false);

        }
        return true;
    }
    private void enableEditTexts(boolean isEnabled) {
        mTitle.setEnabled(isEnabled);
        mDescription.setEnabled(isEnabled);
        mPrice.setEnabled(isEnabled);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            photoStorageReference = FirebaseUtil.mStorageReference.child(Objects.requireNonNull(imageUri.getLastPathSegment()));

            final UploadTask uploadTask = photoStorageReference.putFile(imageUri);

            uploadTask.continueWithTask(task -> {
                //There is an error.
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Otherwise continue with the task to get the download URL
                return photoStorageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String downloadUrl = Objects.requireNonNull(task.getResult()).toString();
                    String pictureName = uploadTask.getSnapshot().getStorage().getPath();

                    deal.setImageUrl(downloadUrl);
                    deal.setImageName(pictureName);
                    Log.d("Uri: ", downloadUrl);
                    Log.d("Name: ", pictureName);

                    showImage(downloadUrl);
                } else {
                    Toast.makeText(InsertDataActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });



                }
            }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_my_menu:
                savingdeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.delete_my_menu:
                deleteDeal();
                Toast.makeText(this, "Deal deleted!", Toast.LENGTH_LONG).show();
                backToList();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void savingdeal(){
        deal.setTitle(mTitle.getText().toString());
       deal.setDescription(mDescription.getText().toString());
      deal.setPrice(mPrice.getText().toString());
   if(deal.getId() == null){
       mDatabaseReference.push().setValue(deal);
   }else {
       mDatabaseReference.child(deal.getId()).setValue(deal);
   }

}
   private void clean(){
        mTitle.setText("");
        mDescription.setText("");
        mPrice.setText("");
        mTitle.requestFocus();
}
    private void deleteDeal() {
        if (deal == null) {
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();
        Log.d("image name", deal.getImageName());
        if(deal.getImageName() != null && deal.getImageName().isEmpty() == false) {
            StorageReference picRef = FirebaseUtil.mFirebaseStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image", "Image Successfully Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete Image", e.getMessage());
                }
            });
        }

    }
    private void backToList() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }


        private void showImage (String url){
            if (url != null && !url.isEmpty()) {
                int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                Picasso.get()
                        .load(url)
                        .resize(width, width * 2 / 3)
                        .centerCrop()
                        .into(imageView);
            }
        }
}
