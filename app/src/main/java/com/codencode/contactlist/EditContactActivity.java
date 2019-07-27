package com.codencode.contactlist;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditContactActivity extends AppCompatActivity {

    Spinner editSpinner;
    EditText mNameEditText , mPhoneEditText , mEMailEditText;
    CircleImageView mProfileImage;
    ImageView mSelectNewImage;
    ContactInfo contactInfo;
    String imgPath = "-1";
    Bitmap imgBitmap;
    ProgressBar progressBar;
    MySQLiteHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Spinner handeling area
        editSpinner = findViewById(R.id.edit_contact_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this , R.array.spinner_content,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editSpinner.setAdapter(adapter);

        contactInfo = new ContactInfo();
        contactInfo.setContactName(getIntent().getStringExtra("Name"));
        contactInfo.setPhoneNumber(getIntent().getStringExtra("Phone"));
        contactInfo.setEmailAddress(getIntent().getStringExtra("Email"));
        contactInfo.setImgURI(getIntent().getStringExtra("URI"));
        imgPath = contactInfo.getImgURI();
        imgBitmap = null;

        initDetails();

    }

    private void initDetails() {
        progressBar = findViewById(R.id.edit_contact_progressbar);
        mNameEditText = findViewById(R.id.edit_contact_name_edittext);
        mPhoneEditText = findViewById(R.id.edit_contact_phone_edittext);
        mEMailEditText = findViewById(R.id.edit_contact_email_edittext);
        helper = new MySQLiteHelper(this);

        mProfileImage = findViewById(R.id.edit_contact_profile_image);
        mSelectNewImage = findViewById(R.id.edit_contact_new_profile_image);

        mNameEditText.setText(contactInfo.getContactName());
        mPhoneEditText.setText(contactInfo.getPhoneNumber());
        mEMailEditText.setText(contactInfo.getEmailAddress());

        Picasso.get().load(new File(contactInfo.getImgURI())).into(mProfileImage);


        mSelectNewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(EditContactActivity.this);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.edit_contact_menu , menu);
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
        if(item.getItemId() == R.id.action_done)
        {
            saveContactInfo();
            return false;
        }

        startParentActivity(contactInfo);
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                imgPath = result.toString();
                Picasso.get().load(resultUri).into(mProfileImage);

                try {
                    imgBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                } catch (IOException e) {
                    Toast.makeText(this, "cant load bitmap", Toast.LENGTH_SHORT).show();
                }
            }
            else
            if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage , String name){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("profileDir", Context.MODE_PRIVATE);

        File mypath=new File(directory,name+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);

            bitmapImage.compress(Bitmap.CompressFormat.PNG, 50, fos);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        imgPath = mypath.getAbsolutePath();
        return imgPath;
    }


    private void saveContactInfo() {

        String name = mNameEditText.getText().toString().trim();
        String phone = mPhoneEditText.getText().toString().trim();
        String mail = mEMailEditText.getText().toString().trim();

        if(isNameValid(name) == false)
        {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(isPhoneValid(phone) == false)
        {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            return;
        }
        if(isValidMail(mail) == false)
        {
            Toast.makeText(this, "Please enter a valid mail", Toast.LENGTH_SHORT).show();
            return;
        }

        if(isValidImage(imgPath) == false)
        {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        if(contactInfo.getImgURI().equals(imgPath) == false)
        {
            File file = new File(contactInfo.getImgURI());
            file.delete();
        }

        saveImageAyncTast task = new saveImageAyncTast(this , name , phone , imgPath , mail);
        if(imgBitmap == null)
        task.execute("-1");
        else task.execute(phone);
    }

    private void startParentActivity(ContactInfo info) {
        progressBar.setVisibility(View.INVISIBLE);
        Intent contactListActivity = new Intent(this , ContactDetailsActivity.class);

        contactListActivity.putExtra("Name" , info.getContactName());
        contactListActivity.putExtra("Phone" , info.getPhoneNumber());
        contactListActivity.putExtra("Email" , info.getEmailAddress());
        contactListActivity.putExtra("URI" , info.getImgURI());
        contactListActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(contactListActivity);
    }

    private boolean isValidImage(String imgPath) {
        if(imgPath.equals("-1"))
            return false;
        else
            return true;
    }

    private boolean isPhoneValid(String phone) {
        if(phone == null || phone.length()!= 10)
            return false;
        else
            return true;
    }

    private boolean isNameValid(String name) {
        if(name == null || name.length() == 0)
            return false;
        else
            return true;
    }

    private boolean isValidMail(String mail)
    {
        if(mail == null || mail.contains("@") == false || mail.contains("@.com"))
            return false;
        else
            return true;
    }


    private final class saveImageAyncTast extends AsyncTask<String , Void , String>
    {
        WeakReference<EditContactActivity> weakReference;
        String name , phone , imgUri , email;
        saveImageAyncTast(EditContactActivity editContactActivity , String name , String phone , String imgUri , String email)
        {
            weakReference = new WeakReference<>(editContactActivity);
            this.name = name;
            this.phone = phone;
            this.imgUri = imgUri;
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            EditContactActivity activity = weakReference.get();
            if(activity == null || activity.isFinishing())
                return;

            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(activity, "Saving Profie Photo", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String phone = strings[0];

            if(phone.equals("-1"))
                return imgPath;
            Random random = new Random();
            int num = random.nextInt(100000000);
            String result = saveToInternalStorage(imgBitmap , phone+num);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            EditContactActivity activity = weakReference.get();
            if(activity == null || activity.isFinishing())
                return;
            imgPath = s;

            ContactInfo info = new ContactInfo();
            info.setPhoneNumber(phone);
            info.setEmailAddress(email);
            info.setContactName(name);
            info.setImgURI(imgPath);

            long id = helper.update(contactInfo , info);
            if(id >= 0)
            {
                Toast.makeText(activity, "Contact Saved", Toast.LENGTH_SHORT).show();
                startParentActivity(info);
            }
            else Toast.makeText(activity, "Error: Couldn't save , please try again", Toast.LENGTH_SHORT).show();
        }
    }

}
