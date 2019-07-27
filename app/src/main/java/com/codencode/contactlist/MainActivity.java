package com.codencode.contactlist;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.codencode.contactlist.Adapters.ContactDisplayAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton addContactFAB;
    RecyclerView contactRecyclerView;
    ContactDisplayAdapter adapter;
    ArrayList<ContactInfo> _contactsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addContactFAB = findViewById(R.id.add_contact_fab);
        contactRecyclerView = findViewById(R.id.contact_recyclerview);

        addContactFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.add_contact_fab)
                    addContact();
            }
        });

        loadData();
    }

    private void loadData() {
        _contactsList = new ArrayList<>();
        MySQLiteHelper helper = new MySQLiteHelper(this);
        _contactsList = helper.loadData();

        adapter = new ContactDisplayAdapter(this , _contactsList);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactRecyclerView.setAdapter(adapter);
    }

    private void addContact() {
        Intent i = new Intent(this , AddContactActivity.class);
        startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contacts_menu , menu);

        MenuItem serachItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) serachItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }
}
