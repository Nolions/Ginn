package tw.nolions.coffeebeanslife.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import tw.nolions.coffeebeanslife.model.entity.RecordEntity;

@Dao
public interface recordDao {

    @Query("SELECT * FROM records ORDER BY id DESC")
    List<RecordEntity> getAll();

    @Query("SELECT * FROM records WHERE id = :id")
    RecordEntity find(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RecordEntity recordEntity);

    @Delete
    void delete(RecordEntity recordEntity);
}
