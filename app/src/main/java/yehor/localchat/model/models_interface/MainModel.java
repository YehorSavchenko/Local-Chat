package yehor.localchat.model.models_interface;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import yehor.localchat.data_base.Contact;
import yehor.localchat.model.ClientHandlerCallBack;

public interface MainModel {
    void sendMessage(String receiveRedress, String message);

    void createServerSocket(ClientHandlerCallBack callBack);

    void destroy();

    void shutDownExecute();

    Completable addContact(Contact contact);

    Single<List<Contact>> getAllContacts();
}
