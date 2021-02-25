package yehor.localchat.data_base;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import androidx.room.Database;

@Database(entities = {Contact.class},
        version = 1, exportSchema = false)
public abstract class DBProvider extends RoomDatabase implements DBModel {

    public abstract ContactDAO contactDAO();

    private static DBProvider database;

    public static DBProvider getDatabase(Context context) {
        synchronized (DBProvider.class) {
            if (database == null) {
                database = Room.databaseBuilder(context, DBProvider.class, "LOCAL_CHAT_DB").build();
            }
        }
        return database;
    }


}
