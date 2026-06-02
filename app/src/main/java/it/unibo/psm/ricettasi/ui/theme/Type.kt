package it.unibo.psm.ricettasi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import it.unibo.psm.ricettasi.R

val FrauncesFamily = FontFamily(
    Font(R.font.fraunces_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.fraunces_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.fraunces_black, FontWeight.Black, FontStyle.Normal),
    Font(R.font.fraunces_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.fraunces_bold_italic, FontWeight.Bold, FontStyle.Italic),
)

val ManropeFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.manrope_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.manrope_bold, FontWeight.Bold, FontStyle.Normal),
)

val Typography = Typography(
    // Fraunces 700 40sp - hero titles
    displayLarge = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp,
    ),
    // Fraunces 700 32sp - page titles
    displayMedium = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.3).sp,
    ),
    // Fraunces 700 24sp - section titles
    headlineMedium = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.2).sp,
    ),
    // type-heading-sm: Manrope 700 18sp - card titles, ingredient names
    headlineSmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.1).sp,
    ),
    // Manrope 600 16sp - primary button, active tab
    labelLarge = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    // Manrope 400 15sp - body text
    bodyMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    // Manrope 400 13sp - metadata
    bodySmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),
    // Manrope 500 11sp - tab labels, badges
    labelSmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.2.sp,
    ),
    // Manrope 600 10sp - section labels uppercase
    labelMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp,
    ),
)

// Italic variant for hero title
val DisplayXlItalic = TextStyle(
    fontFamily = FrauncesFamily,
    fontWeight = FontWeight.Bold,
    fontStyle = FontStyle.Italic,
    fontSize = 40.sp,
    lineHeight = 44.sp,
    letterSpacing = (-0.5).sp,
)
