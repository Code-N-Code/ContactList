package com.codencode.contactlist;

import android.content.Intent;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactDetailsActivity extends AppCompatActivity {

    ContactInfo contactInfo;
    CircleImageView mProfileImage;
    ImageView mCall , mMail , mText;
    TextView mPhoneNumber , mEmailAdd ,mName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contactInfo = new ContactInfo();
        contactInfo.setContactName(getIntent().getStringExtra("Name"));
        contactInfo.setPhoneNumber(getIntent().getStringExtra("Phone"));
        contactInfo.setEmailAddress(getIntent().getStringExtra("Email"));
        contactInfo.setImgURI(getIntent().getStringExtra("URI"));

        initDetails(contactInfo);
    }

    private void initDetails(final ContactInfo contactInfo) {
        mCall = findViewById(R.id.contact_details_call_button);
        mMail = findViewById(R.id.contact_details_email_button);
        mText = findViewById(R.id.contact_details_text_button);
        mName = findViewById(R.id.contact_details_contact_name);

        mPhoneNumber = findViewById(R.id.contact_details_phone);
        mEmailAdd = findViewById(R.id.contact_details_email);
        mProfileImage = findViewById(R.id.contact_details_profile_image);

        mPhoneNumber.setText(contactInfo.getPhoneNumber());
        mEmailAdd.setText(contactInfo.getEmailAddress());
        mName.setText(contactInfo.getContactName());
        Picasso.get().load(new File(contactInfo.getImgURI())).into(mProfileImage);

        //call Intent Handeling
        mCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", contactInfo.getPhoneNumber(), null));
                startActivity(intent);
            }
        });


        //Text Message handeling
        mText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", contactInfo.getPhoneNumber(), null)));
            }
        });


        //Mail Intent handeling
        mMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto",contactInfo.getEmailAddress(), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.contact_details_menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        if(item.getItemId() == R.id.action_delete)
        {
            MySQLiteHelper helper = new MySQLiteHelper(this);
            int deletedCoutn  = helper.delete(contactInfo);

            if(deletedCoutn > 0)
            {
                File file = new File(contactInfo.getImgURI());
                file.delete();
                Toast.makeText(this, "Deleted Succesfully", Toast.LENGTH_SHORT).show();
                Intent mainActivity = new Intent(this , MainActivity.class);
                mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainActivity);
            }
            else
            Toast.makeText(this, "Something gone wrong , please try again", Toast.LENGTH_SHORT).show();

            return false;
        }
        if(item.getItemId() == R.id.action_edit)
        {
            Intent editActivityIntent = new Intent(this , EditContactActivity.class);
            editActivityIntent.putExtra("Name" , contactInfo.getContactName());
            editActivityIntent.putExtra("Phone" , contactInfo.getPhoneNumber());
            editActivityIntent.putExtra("Email" , contactInfo.getEmailAddress());
            editActivityIntent.putExtra("URI" , contactInfo.getImgURI());
            startActivity(editActivityIntent);

            return false;
        }

        Intent mainIntent = new Intent(this , MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent mainIntent = new Intent(this , MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
    }
}
