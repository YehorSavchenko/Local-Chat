package yehor.localchat.data_base;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Single;

@Dao
public abstract class ContactDAO {


    @Query("SELECT * FROM contacts")
    public abstract Single<List<Contact>> getAllEntities();


    @Query("SELECT * FROM contacts WHERE id=:id")
    public abstract Contact getEntityByID(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract Long insertEntity(Contact contact);


}
