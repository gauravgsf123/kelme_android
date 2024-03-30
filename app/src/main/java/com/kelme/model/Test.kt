package com.kelme.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Amit Gupta on 28-06-2021.
 */

@Entity(
    tableName = "test"
)

data class Test(
    @PrimaryKey(autoGenerate = false)
    var i: Int
)