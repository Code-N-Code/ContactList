package com.codencode.contactlist.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codencode.contactlist.ContactDetailsActivity;
import com.codencode.contactlist.ContactInfo;
import com.codencode.contactlist.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactDisplayAdapter extends RecyclerView.Adapter<ContactDisplayAdapter.ViewHolder> implements Filterable {

    ArrayList<ContactInfo> _contacts;
    ArrayList<ContactInfo> _contactsOriginal;
    Context context;

    public ContactDisplayAdapter(Context context, ArrayList<ContactInfo> _contacts)
    {
        this.context = context;
        this._contacts = _contacts;
        _contactsOriginal = new ArrayList<>(_contacts);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.single_contact_layout ,viewGroup , false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        final ContactInfo info = _contacts.get(i);

        Picasso.get().load(new File(info.getImgURI())).into(viewHolder.contactImage);
        viewHolder.contactName.setText(info.getContactName());
        viewHolder.mainContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailsIntent = new Intent(context , ContactDetailsActivity.class);
                detailsIntent.putExtra("Name" , info.getContactName());
                detailsIntent.putExtra("Phone" , info.getPhoneNumber());
                detailsIntent.putExtra("Email" , info.getEmailAddress());
                detailsIntent.putExtra("URI" , info.getImgURI());

                context.startActivity(detailsIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return _contacts.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView contactName;
        CircleImageView contactImage;
        RelativeLayout mainContainerLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            contactImage = itemView.findViewById(R.id.contact_image);
            contactName = itemView.findViewById(R.id.contact_name);
            mainContainerLayout = itemView.findViewById(R.id.main_container);
        }
    }

    @Override
    public Filter getFilter() {
        return filteredList;
    }

    private Filter filteredList = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<ContactInfo> filterContacts = new ArrayList<>();

            if(constraint == null || constraint.length() == 0)
            {
                filterContacts.addAll(_contactsOriginal);
            }
            else
            {
                String filterPattern = constraint.toString().trim().toLowerCase();

                for(ContactInfo info : _contactsOriginal)
                {
                    if(info.getContactName().toLowerCase().contains(filterPattern))
                    {
                        filterContacts.add(info);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filterContacts;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            _contacts.clear();
            _contacts.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };
}
