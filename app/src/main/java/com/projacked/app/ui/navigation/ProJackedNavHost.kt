package com.projacked.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.projacked.app.ui.screens.auth.SignInScreen
import com.projacked.app.ui.screens.auth.SignUpScreen
import com.projacked.app.ui.screens.auth.WelcomeScreen
import com.projacked.app.ui.screens.currentday.CurrentDayScreen
import com.projacked.app.ui.screens.dayconstructor.DayConstructorScreen
import com.projacked.app.ui.screens.home.HomeScreen
import com.projacked.app.ui.screens.mealconstructor.MealConstructorScreen
import com.projacked.app.ui.screens.meals.MealsScreen
import com.projacked.app.ui.screens.parameters.ParametersScreen
import com.projacked.app.ui.screens.plan.TrainingPlanScreen
import com.projacked.app.ui.screens.profile.ProfileScreen

/**
 * The app's navigation graph. Back always pops the stack (no forward-navigating back handlers, unlike the old app).
 * Signing in makes Home the root, so Back on Home leaves the app; logging out clears the stack.
 * The start destination becomes auth-dependent in Phase 3.
 */
@Composable
fun ProJackedNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Route.Welcome,
        modifier = modifier,
    ) {
        composable<Route.Welcome> {
            WelcomeScreen(
                onSignIn = { navController.navigate(Route.SignIn) },
                onSignUp = { navController.navigate(Route.SignUp) },
            )
        }
        composable<Route.SignIn> {
            SignInScreen(onSignedIn = navController::navigateToHomeAsRoot)
        }
        composable<Route.SignUp> {
            SignUpScreen(onSignedUp = navController::navigateToHomeAsRoot)
        }
        composable<Route.Home> {
            HomeScreen(
                onConstructPlan = { navController.navigate(Route.TrainingPlan) },
                onCurrentDay = { navController.navigate(Route.CurrentDay) },
                onMeals = { navController.navigate(Route.Meals) },
                onProfile = { navController.navigate(Route.Profile) },
            )
        }
        composable<Route.TrainingPlan> {
            TrainingPlanScreen(onAddWorkout = { navController.navigate(Route.DayConstructor) })
        }
        composable<Route.DayConstructor> {
            DayConstructorScreen(onSaved = navController::popBackStack)
        }
        composable<Route.CurrentDay> {
            CurrentDayScreen()
        }
        composable<Route.Meals> {
            MealsScreen(
                onAddMeal = { navController.navigate(Route.MealConstructor) },
                onSuggestedNutrition = { navController.navigate(Route.Parameters) },
            )
        }
        composable<Route.MealConstructor> {
            MealConstructorScreen(onSaved = navController::popBackStack)
        }
        composable<Route.Parameters> {
            ParametersScreen(onSubmitted = navController::popBackStack)
        }
        composable<Route.Profile> {
            ProfileScreen(onLoggedOut = navController::navigateToWelcomeClearingStack)
        }
    }
}

/** After sign-in or sign-up: Home becomes the root, so Back on Home exits instead of returning to login. */
private fun NavHostController.navigateToHomeAsRoot() {
    navigate(Route.Home) {
        popUpTo<Route.Welcome> { inclusive = true }
        launchSingleTop = true
    }
}

/** After logout: nothing signed-in stays on the back stack. */
private fun NavHostController.navigateToWelcomeClearingStack() {
    navigate(Route.Welcome) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
