package com.example.runpal.room

import androidx.room.Entity


/**
 * A table for keeping track of run updates not synchronized with the server.
 */
@Entity(tableName = "sync", primaryKeys = ["user", "runId"])
class Sync(
    var user: String = "",
    var runId: Long = 0L,
    /**
     * A null value means that the run was not created.
     * A nonnull value means that the updates until  and including 'since' have been synchronized,
     * while updates after 'since' have not.
     */
    var since: Long? = null
) {

}