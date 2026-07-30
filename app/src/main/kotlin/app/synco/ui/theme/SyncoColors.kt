package app.synco.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal object SyncoColors {

    val light: ColorScheme = lightColorScheme(
        primary = Color(0xFF00658F),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFC7E7FF),
        onPrimaryContainer = Color(0xFF001E2E),
        secondary = Color(0xFF4E616C),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD1E5F3),
        onSecondaryContainer = Color(0xFF0A1E27),
        tertiary = Color(0xFF615A7C),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE7DEFF),
        onTertiaryContainer = Color(0xFF1D1735),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFBFCFE),
        onBackground = Color(0xFF191C1E),
        surface = Color(0xFFFBFCFE),
        onSurface = Color(0xFF191C1E),
        surfaceVariant = Color(0xFFDDE3EA),
        onSurfaceVariant = Color(0xFF41484D),
        outline = Color(0xFF71787E),
        outlineVariant = Color(0xFFC1C7CE),
        inverseSurface = Color(0xFF2E3133),
        inverseOnSurface = Color(0xFFF0F1F3),
    )

    val dark: ColorScheme = darkColorScheme(
        primary = Color(0xFF87CEFF),
        onPrimary = Color(0xFF00344C),
        primaryContainer = Color(0xFF004C6D),
        onPrimaryContainer = Color(0xFFC7E7FF),
        secondary = Color(0xFFB5C9D7),
        onSecondary = Color(0xFF20333D),
        secondaryContainer = Color(0xFF364954),
        onSecondaryContainer = Color(0xFFD1E5F3),
        tertiary = Color(0xFFCBC2E9),
        onTertiary = Color(0xFF332C4B),
        tertiaryContainer = Color(0xFF494263),
        onTertiaryContainer = Color(0xFFE7DEFF),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF111416),
        onBackground = Color(0xFFE1E2E5),
        surface = Color(0xFF111416),
        onSurface = Color(0xFFE1E2E5),
        surfaceVariant = Color(0xFF41484D),
        onSurfaceVariant = Color(0xFFC1C7CE),
        outline = Color(0xFF8B9198),
        outlineVariant = Color(0xFF41484D),
        inverseSurface = Color(0xFFE1E2E5),
        inverseOnSurface = Color(0xFF2E3133),
    )
}
