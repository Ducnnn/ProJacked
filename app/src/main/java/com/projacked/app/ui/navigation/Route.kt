package com.projacked.app.ui.navigation

import kotlinx.serialization.Serializable

/** Type-safe navigation destinations, one per screen of the old app. */
sealed interface Route {
    @Serializable data object Welcome : Route
    @Serializable data object SignIn : Route
    @Serializable data object SignUp : Route
    @Serializable data object Home : Route
    @Serializable data object TrainingPlan : Route
    @Serializable data object DayConstructor : Route
    @Serializable data object CurrentDay : Route
    @Serializable data object Meals : Route
    @Serializable data object MealConstructor : Route
    @Serializable data object Parameters : Route
    @Serializable data object Profile : Route
}
