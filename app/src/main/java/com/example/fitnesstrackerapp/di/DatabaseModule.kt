package com.example.fitnesstrackerapp.di

import android.content.Context
import androidx.room.Room
import com.example.fitnesstrackerapp.data.DietDao
import com.example.fitnesstrackerapp.data.FitnessDatabase
import com.example.fitnesstrackerapp.data.GoalDao
import com.example.fitnesstrackerapp.data.UserDao
import com.example.fitnesstrackerapp.data.WorkoutDao
import com.example.fitnesstrackerapp.data.converter.DateConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DatabaseModule
 *
 * Dagger-Hilt module responsible for providing singleton-scoped Room database
 * and DAO dependencies across the app.
 *
 * Installed in the SingletonComponent to ensure one instance exists app-wide.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides a singleton instance of [FitnessDatabase] using Room.
     *
     * @param context Application context for database creation.
     * @return Configured [FitnessDatabase] instance.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FitnessDatabase {
        return Room.databaseBuilder(
            context,
            FitnessDatabase::class.java,
            "fitness_db"
        )
            // Adds support for custom data types like LocalDate via DateConverter
            .addTypeConverter(DateConverter())

            // NOTE:
            // fallbackToDestructiveMigration() disables auto-destructive migration.
            // It's good for production safety, but requires you to manage version upgrades explicitly.
            // For development, consider fallbackToDestructiveMigration() to avoid crashes after schema changes.
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the [UserDao] used for managing user entities.
     */
    @Provides
    fun provideUserDao(db: FitnessDatabase): UserDao = db.userDao()

    /**
     * Provides the [WorkoutDao] used for managing workout entities.
     */
    @Provides
    fun provideWorkoutDao(db: FitnessDatabase): WorkoutDao = db.workoutDao()

    /**
     * Provides the [DietDao] used for managing diet tracking.
     */
    @Provides
    fun provideDietDao(db: FitnessDatabase): DietDao = db.dietDao()

    /**
     * Provides the [GoalDao] used for goal tracking and updates.
     */
    @Provides
    fun provideGoalDao(db: FitnessDatabase): GoalDao = db.goalDao()
}
