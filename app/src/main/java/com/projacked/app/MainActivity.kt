package com.projacked.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.projacked.app.ui.navigation.ProJackedNavHost
import com.projacked.app.ui.theme.ProJackedTheme
import dagger.hilt.android.AndroidEntryPoint

/** The app's only activity: hosts the Compose navigation graph. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProJackedTheme {
                ProJackedNavHost()
            }
        }
    }
}
