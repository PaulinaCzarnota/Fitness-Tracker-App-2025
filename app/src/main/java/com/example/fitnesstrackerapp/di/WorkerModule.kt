package com.example.fitnesstrackerapp.di

import com.example.fitnesstrackerapp.worker.GoalReminderWorker
import com.example.fitnesstrackerapp.worker.StepTrackingWorker
import com.example.fitnesstrackerapp.worker.WorkoutReminderWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {
    // Goal reminder workers
    worker { GoalReminderWorker(get(), get()) }

    // Workout reminder workers
    worker { WorkoutReminderWorker(get(), get()) }

    // Step tracking workers
    worker { StepTrackingWorker(get(), get()) }
}
