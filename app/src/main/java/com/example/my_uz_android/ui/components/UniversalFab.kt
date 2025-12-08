package com.example.my_uz_android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_uz_android.R
import com.example.my_uz_android.ui.theme.InterFontFamily

data class FabOption(
    val label: String,
    val iconResId: Int,
    val onClick: () -> Unit
)

@Composable
fun UniversalFab(
    mainIconResId: Int = R.drawable.ic_plus,
    isExpandable: Boolean = false,
    isExpanded: Boolean = false,
    onMainFabClick: () -> Unit,
    options: List<FabOption> = emptyList(),
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(targetValue = if (isExpandable && isExpanded) 45f else 0f, label = "fab_rotation")
    val mainFabColor = Color(0xFF6750A4) // Schemes-Primary
    val optionsContainerColor = Color(0xFFEADDFF) // Schemes-Primary-Container
    val optionsTextColor = Color(0xFF4F378A) // Schemes-On-Primary-Container

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sekcja rozwijana (Opcje)
        AnimatedVisibility(
            visible = isExpandable && isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                options.forEach { option ->
                    FabOptionItem(option = option, backgroundColor = optionsContainerColor, textColor = optionsTextColor)
                }
            }
        }

        // Główny przycisk FAB (zgodny z dostarczonym snippetem Figmy, ale w kolorze Primary)
        FloatingActionButton(
            onClick = onMainFabClick,
            containerColor = mainFabColor, // Główny kolor
            contentColor = Color.White,
            // Kształt 16dp i cienie jak w Figmie:
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .size(56.dp)
                // Implementacja złożonego cienia Figmy:
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
        ) {
            Icon(
                painter = painterResource(id = mainIconResId),
                contentDescription = "FAB",
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
fun FabOptionItem(option: FabOption, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(28.dp) // Kształt pigułki (28dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { option.onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = option.iconResId),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = option.label,
                style = TextStyle(
                    color = textColor,
                    fontSize = 16.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.W500,
                    letterSpacing = 0.15.sp
                )
            )
        }
    }
}