package yehor.localchat.model;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import yehor.localchat.data_base.Contact;
import yehor.localchat.data_base.DBProvider;
import yehor.localchat.model.models_interface.MainModel;

import static yehor.localchat.tags.Tags.SEND;


public class MainInteractorRealize extends BaseModel implements MainModel {


    private ExecutorService executor;
    private ClientManager clientManager;
    private DBProvider database;


    public MainInteractorRealize(DBProvider database) {
        this.database = database;
    }

    @Override
    public void sendMessage(String receiveRedress, String message) {
        Log.i(SEND, "BEFORE");
        clientManager.sendMessage(receiveRedress, message);
        Log.i(SEND, "AFTER");

    }

    @Override
    public Completable addContact(Contact contact) {
        return multithread(Completable.fromAction(
                () -> database.contactDAO().insertEntity(contact)
        ));
    }

    @Override
    public Single<List<Contact>> getAllContacts() {
        return database.contactDAO().getAllEntities();
    }


    @Override
    public void createServerSocket(ClientHandlerCallBack callBack) {
        clientManager = new ClientManager(callBack);
        clientManager.createServerSocket(callBack);
        executor = Executors.newSingleThreadExecutor();
        executor.execute(clientManager);
    }


    @Override
    public void destroy() {
        shutDownExecute();
        clientManager.closeServerConnection();
        clientManager.closeClients();
    }

    @Override
    public void shutDownExecute() {
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(100, TimeUnit.MICROSECONDS)) {
                System.exit(0);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private Completable multithread(Completable completable) {
        return completable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
