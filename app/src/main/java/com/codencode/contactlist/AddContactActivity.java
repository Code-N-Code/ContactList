package com.codencode.contactlist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class AddContactActivity extends AppCompatActivity {

    EditText mName , mPhone , mEMail;
    Spinner mSpinner;
    ImageView mChooseImage;
    CircleImageView mProfileImage;
    String imgPath = "-1";
    MySQLiteHelper helper;
    Bitmap imgBitmap;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       mName = findViewById(R.id.add_contact_name_edittext);
       mPhone = findViewById(R.id.add_contact_phone_edittext);
       mEMail = findViewById(R.id.add_contact_email_edittext);
       mChooseImage = findViewById(R.id.add_contact_new_profile_image);
       mProfileImage = findViewById(R.id.add_contact_profile_image);
       progressBar = findViewById(R.id.add_contact_progressbar);
       progressBar.setVisibility(View.INVISIBLE);

       //Spinner handeling area
       mSpinner = findViewById(R.id.add_contact_spinner);
       ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this , R.array.spinner_content,android.R.layout.simple_spinner_item);
       adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       mSpinner.setAdapter(adapter);



       //chosing new image area
       mChooseImage.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               CropImage.activity()
                       .setGuidelines(CropImageView.Guidelines.ON)
                       .setAspectRatio(1,1)
                       .start(AddContactActivity.this);
           }
       });

    }

    //onCreate ends here

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

    //onActivityResult ends here

    private String saveToInternalStorage(Bitmap bitmapImage , String name){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("profileDir", Context.MODE_PRIVATE);

        File mypath=new File(directory,name+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);

            bitmapImage.compress(Bitmap.CompressFormat.PNG, 40, fos);

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

    //saveToInternalStorage Ends Here


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_contact_menu , menu);

        MenuItem saveItem = menu.findItem(R.id.action_save);
        saveItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.action_save)
                {
                    item.setEnabled(false);
                    saveContactInfo();
                    item.setEnabled(true);
                }
                return false;
            }
        });

        return true;
    }

    private void saveContactInfo() {
        String name = mName.getText().toString().trim();
        String phone = mPhone.getText().toString().trim();
        String mail = mEMail.getText().toString().trim();
        
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
        saveImageAyncTast task = new saveImageAyncTast(this , name , phone , imgPath ,mail);
        task.execute(phone);

    }

    private void startParentActivity() {
        progressBar.setVisibility(View.INVISIBLE);
        Intent contactListActivity = new Intent(this , MainActivity.class);
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
        {
            return true;
        }
    }

    private boolean isNameValid(String name) {
        if(name == null || name.length() == 0)
            return false;
        else
            return true;
    }
    
    private boolean isValidMail(String mail)
    {
        if(mail == null || mail.contains("@") == false || mail.contains(".com") == false || mail.contains("@.com"))
            return false;
        else
            return true;
    }


    private final class saveImageAyncTast extends AsyncTask<String , Void , String>
    {
        WeakReference<AddContactActivity> weakReference;
        String name , phone , imgUri , email;
        saveImageAyncTast(AddContactActivity addContactActivity , String name , String phone , String imgUri , String email)
        {
            weakReference = new WeakReference<>(addContactActivity);
            this.name = name;
            this.phone = phone;
            this.imgUri = imgUri;
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AddContactActivity activity = weakReference.get();
            if(activity == null || activity.isFinishing())
                return;

            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(activity, "Saving Profie Photo", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String phone = strings[0];
            Random random = new Random();
            int num = random.nextInt(100000000);
            String result = saveToInternalStorage(imgBitmap , phone+num);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            AddContactActivity activity = weakReference.get();
            if(activity == null || activity.isFinishing())
                return;
            imgPath = s;

            helper = new MySQLiteHelper(activity);

            long id = helper.insertData(name , phone , email , imgPath);
            if(id > 0)
            {
                Toast.makeText(activity, "Contact Saved", Toast.LENGTH_SHORT).show();
                startParentActivity();
            }
        }
    }
}
