package cv.toolkit.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson

data class IpHistoryEntry(val ipInfo: IpInfo, val timestamp: Long = System.currentTimeMillis())

class IpHistoryManager(context: Context) : SQLiteOpenHelper(context, "ip_history.db", null, 1) {
    private val gson = Gson()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE history (ip TEXT PRIMARY KEY, data TEXT, timestamp INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS history")
        onCreate(db)
    }

    fun getHistory(): List<IpHistoryEntry> {
        val list = mutableListOf<IpHistoryEntry>()
        readableDatabase.rawQuery("SELECT data, timestamp FROM history ORDER BY timestamp DESC", null).use { cursor ->
            while (cursor.moveToNext()) {
                try {
                    val ipInfo = gson.fromJson(cursor.getString(0), IpInfo::class.java)
                    list.add(IpHistoryEntry(ipInfo, cursor.getLong(1)))
                } catch (_: Exception) {}
            }
        }
        return list
    }

    fun addEntry(ipInfo: IpInfo) {
        val values = ContentValues().apply {
            put("ip", ipInfo.ip)
            put("data", gson.toJson(ipInfo))
            put("timestamp", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("history", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}
