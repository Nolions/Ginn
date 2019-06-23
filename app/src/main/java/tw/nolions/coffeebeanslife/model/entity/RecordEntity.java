package tw.nolions.coffeebeanslife.model.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "records")
public class RecordEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "record")
    public String record;

    @ColumnInfo(name = "runTime")
    public int runTime;

    @ColumnInfo(name = "inBean")
    public int inBeanIndex;

    @ColumnInfo(name = "firstCrackIndex")
    public int firstCrackIndex;

    @ColumnInfo(name = "secondCrackIndex")
    public int secondCrackIndex;

    @ColumnInfo(name = "create_at")
    public Long create_at;
}
