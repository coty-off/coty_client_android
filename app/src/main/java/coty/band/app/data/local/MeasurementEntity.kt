package coty.band.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: String = "",
    val chestCm: Float,
    val waistCm: Float,
    val hipCm: Float,
    val heightCm: Float,
    val bodyType: String,
    val notes: String?,
    val createdAt: String
)
