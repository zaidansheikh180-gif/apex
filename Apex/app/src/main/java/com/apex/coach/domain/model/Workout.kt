package com.apex.coach.domain.model

data class Workout(
    val id: String,
    val name: String,
    val description: String,
    val durationMinutes: Int,
    val exercises: List<Exercise> = emptyList(),
    val targetMode: WorkoutMode = WorkoutMode.FULL
)

data class Exercise(
    val id: String,
    val name: String,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int = 60,
    val weightKg: Double = 0.0
)
