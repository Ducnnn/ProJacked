package com.projacked.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// The old app rounded every card and tile by 10dp and gave cards a 3dp white border.
val Shapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(10.dp),
)

/** Border width of the old app's cards; pair it with `ProJackedTheme.extendedColors.cardBorder`. */
val CardBorderWidth = 3.dp
