/**
 * Task Scheduler for Background Operations
 *
 * This class has been superseded by WorkManagerScheduler.
 * Kept for backward compatibility.
 *
 * @deprecated Use WorkManagerScheduler instead
 */
package com.example.fitnesstrackerapp.scheduler

import android.content.Context

/**
 * Legacy task scheduler - delegates to WorkManagerScheduler
 * @deprecated Use WorkManagerScheduler directly
 */
@Deprecated(
    "Use WorkManagerScheduler instead",
    ReplaceWith("WorkManagerScheduler")
)
class TaskScheduler(context: Context) {
    private val workManagerScheduler = WorkManagerScheduler.getInstance(context)

    /**
     * Schedules all periodic background tasks
     * @deprecated Use WorkManagerScheduler.initializeAllWork() instead
     */
    @Deprecated("Use WorkManagerScheduler.initializeAllWork() instead")
    fun schedulePeriodicTasks() {
        // Delegate to WorkManagerScheduler with a default user ID
        workManagerScheduler.initializeAllWork(1L) // Default user ID for compatibility
    }

    /**
     * Cancels all scheduled tasks
     * @deprecated Use WorkManagerScheduler.cancelAllWork() instead
     */
    @Deprecated("Use WorkManagerScheduler.cancelAllWork() instead")
    fun cancelAllTasks() {
        workManagerScheduler.cancelAllWork()
    }
}
