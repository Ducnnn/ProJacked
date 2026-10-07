package com.projacked.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.projacked.app.ui.screens.profile.ProfileViewModel

/**
 * The app's navigation graph. Back always pops the stack (no forward-navigating back handlers, unlike the old app).
 * Signing in makes Home the root, so Back on Home leaves the app; logging out clears the stack.
 * The app starts on Home when someone is already signed in, otherwise on Welcome. Any change from signed in to
 * signed out (Log out, or Firebase ending the session) returns to Welcome with the back stack cleared.
 */
@Composable
fun ProJackedNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val isSignedIn by sessionViewModel.isSignedIn.collectAsStateWithLifecycle()
    var wasSignedIn by rememberSaveable { mutableStateOf(isSignedIn) }
    LaunchedEffect(isSignedIn) {
        if (wasSignedIn && !isSignedIn) navController.navigateToWelcomeClearingStack()
        wasSignedIn = isSignedIn
    }

    NavHost(
        navController = navController,
        startDestination = sessionViewModel.startRoute,
        modifier = modifier,
    ) {
        composable<Route.Welcome> {
            WelcomeScreen(
                onSignIn = { navController.navigate(Route.SignIn) },
                onSignUp = { navController.navigate(Route.SignUp) },
            )
        }
        composable<Route.SignIn> {
            SignInScreen(onSignedIn = navController::navigateToHomeAsRoot, viewModel = hiltViewModel())
        }
        composable<Route.SignUp> {
            SignUpScreen(onSignedUp = navController::navigateToHomeAsRoot, viewModel = hiltViewModel())
        }
        composable<Route.Home> {
            HomeScreen(
                onConstructPlan = { navController.navigate(Route.TrainingPlan) },
                onCurrentDay = { navController.navigate(Route.CurrentDay) },
                onMeals = { navController.navigate(Route.Meals) },
                onProfile = { navController.navigate(Route.Profile) },
                viewModel = hiltViewModel(),
            )
        }
        composable<Route.TrainingPlan> {
            TrainingPlanScreen(
                onAddWorkout = { navController.navigate(Route.DayConstructor) },
                viewModel = hiltViewModel(),
            )
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
            val viewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(onLogOut = viewModel::logOut)
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
