package yehor.localchat.ui.views_interfaces;

import java.util.List;

import yehor.localchat.data_base.Contact;

public interface MainView {
    void appendMessage(String s);

    void initContactAdapter(List<Contact> contactsList);

    void addSpinnerItem(Contact contact);
}
