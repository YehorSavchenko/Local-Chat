package yehor.localchat.ui.mainActivity;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import yehor.localchat.data_base.Contact;

public class ContactsSpinnerAdapter extends ArrayAdapter<Contact> {

    public ContactsSpinnerAdapter(@NonNull Context context, int resource, @NonNull Contact[] objects) {
        super(context, resource, objects);
    }


}

