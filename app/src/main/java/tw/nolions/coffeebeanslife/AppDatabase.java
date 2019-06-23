package tw.nolions.coffeebeanslife;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import tw.nolions.coffeebeanslife.model.recordDao;
import tw.nolions.coffeebeanslife.model.entity.RecordEntity;

@Database(entities = {RecordEntity.class}, version = 1, exportSchema=false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract recordDao  getRecordDao();
}