package com.example.flightmobileapp
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Hosts")
data class Host(
    @PrimaryKey
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "connectionTime") val time: Long = System.currentTimeMillis()
)