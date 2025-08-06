package com.example.fitnesstrackerapp.util

import android.database.sqlite.SQLiteException
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Utility class for handling database operations and errors.
 *
 * Features:
 * - Standardized error handling
 * - Result wrapping for database operations
 * - Error logging and recovery
 * - Transaction management
 */
sealed class DatabaseResult<out T> {
    data class Success<T>(val data: T) : DatabaseResult<T>()
    data class Error(val exception: Exception) : DatabaseResult<Nothing>()
}

object DatabaseUtils {
    /**
     * Wraps a database operation in a try-catch block.
     */
    suspend inline fun <T> dbOperation(
        crossinline operation: suspend () -> T
    ): DatabaseResult<T> {
        return try {
            DatabaseResult.Success(operation())
        } catch (e: SQLiteException) {
            DatabaseResult.Error(e)
        } catch (e: IOException) {
            DatabaseResult.Error(e)
        } catch (e: Exception) {
            DatabaseResult.Error(e)
        }
    }

    /**
     * Wraps a Flow operation with error handling.
     */
    fun <T> Flow<T>.handleDatabaseError(): Flow<DatabaseResult<T>> {
        return this
            .map { DatabaseResult.Success(it) as DatabaseResult<T> }
            .catch { emit(DatabaseResult.Error(it as Exception)) }
    }

    /**
     * Executes a database transaction safely.
     */
    suspend inline fun <T> RoomDatabase.safeTransaction(
        crossinline transaction: suspend () -> T
    ): DatabaseResult<T> {
        return try {
            if (!isOpen) {
                return DatabaseResult.Error(IllegalStateException("Database is closed"))
            }
            
            beginTransaction()
            try {
                val result = transaction()
                setTransactionSuccessful()
                DatabaseResult.Success(result)
            } finally {
                endTransaction()
            }
        } catch (e: Exception) {
            DatabaseResult.Error(e)
        }
    }

    /**
     * Maps error messages to user-friendly strings.
     */
    fun getErrorMessage(error: Exception): String {
        return when (error) {
            is SQLiteException -> "Database error: ${error.localizedMessage}"
            is IOException -> "Storage error: ${error.localizedMessage}"
            else -> "An unexpected error occurred: ${error.localizedMessage}"
        }
    }

    /**
     * Checks if an error is recoverable.
     */
    fun isRecoverableError(error: Exception): Boolean {
        return when (error) {
            is SQLiteException -> true
            is IOException -> true
            else -> false
        }
    }

    /**
     * Validates database integrity.
     */
    suspend fun validateDatabaseIntegrity(db: RoomDatabase): Boolean {
        return dbOperation {
            db.query("PRAGMA integrity_check", null).use { cursor ->
                cursor.moveToFirst()
                cursor.getString(0) == "ok"
            }
        } is DatabaseResult.Success
    }
}
