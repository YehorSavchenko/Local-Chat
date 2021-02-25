package yehor.localchat.presentation.presentations_interface;

public interface MainPresenter {

    void sendMessage(String receiveRedress, String message);

    void startReceive();

    void destroy();

    void insertContact(String name, String ip);

    void getAllContacts();
}
