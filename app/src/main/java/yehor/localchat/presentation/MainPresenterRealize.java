package yehor.localchat.presentation;


import android.os.Handler;
import android.util.Log;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import yehor.localchat.data_base.Contact;
import yehor.localchat.model.ClientHandlerCallBack;
import yehor.localchat.model.MainInteractorRealize;
import yehor.localchat.presentation.presentations_interface.MainPresenter;
import yehor.localchat.ui.views_interfaces.MainView;


public class MainPresenterRealize extends BasePresenter implements MainPresenter, ClientHandlerCallBack {
    private MainView view;
    private MainInteractorRealize interactor;
    private final CompositeDisposable disposable = new CompositeDisposable();
    private Handler handler = new Handler();


    public MainPresenterRealize(MainInteractorRealize interactor, MainView view) {
        this.interactor = interactor;
        this.view = view;
    }

    @Override
    public void sendMessage(String receiveRedress, String message) {
        Log.i("DEB", receiveRedress + " : " + message);
        interactor.sendMessage(receiveRedress, message);
        handleMessage(message, "me", receiveRedress);
    }

    @Override
    public void startReceive() {
        interactor.createServerSocket(this);
    }

    @Override
    public void destroy() {
        interactor.destroy();
    }

    @Override
    public void handleMessage(String message, String from, String to) {
        handler.post(() -> view.appendMessage(String.format("[%s->%s]: %s" + System.getProperty("line.separator"), from, to, message)));
    }

    @Override
    public void insertContact(String name, String ip) {
        Contact c = new Contact(ip, name);
        disposable.add(interactor.addContact(c).subscribe(() -> {
        }, Throwable::printStackTrace));
        view.addSpinnerItem(c);
    }

    @Override
    public void getAllContacts() {
        disposable.add(interactor.getAllContacts().subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(
                (contactsList) -> {
                    view.initContactAdapter(contactsList);
                },
                Throwable::printStackTrace
        ));
    }


}
