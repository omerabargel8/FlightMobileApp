package com.example.flightmobileapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
@Dao
public abstract interface HostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHost(host: Host)

    @Query("SELECT * From Hosts ORDER BY connectionTime DESC LIMIT 5")
    fun getAll(): List<Host>?

    @Query("DELETE FROM Hosts")
    fun clearDB()
}