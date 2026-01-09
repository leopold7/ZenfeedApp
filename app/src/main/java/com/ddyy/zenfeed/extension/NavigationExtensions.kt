package com.ddyy.zenfeed.extension

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

object NavigationDefaults {
    const val SLIDE_DURATION = 350
    const val FADE_DURATION = 200
    const val FADE_DELAY = 0
    const val EXIT_FADE_DURATION = 200
    const val EXIT_FADE_DELAY = 0
}

fun AnimatedContentTransitionScope<*>.defaultEnterTransition() = slideIntoContainer(
    towards = AnimatedContentTransitionScope.SlideDirection.Left,
    animationSpec = tween(
        durationMillis = NavigationDefaults.SLIDE_DURATION,
        easing = FastOutSlowInEasing
    )
) + fadeIn(
    animationSpec = tween(
        durationMillis = NavigationDefaults.FADE_DURATION,
        delayMillis = NavigationDefaults.FADE_DELAY,
        easing = FastOutSlowInEasing
    )
)

fun AnimatedContentTransitionScope<*>.defaultExitTransition() = slideOutOfContainer(
    towards = AnimatedContentTransitionScope.SlideDirection.Right,
    animationSpec = tween(
        durationMillis = NavigationDefaults.SLIDE_DURATION,
        easing = FastOutSlowInEasing
    )
) + fadeOut(
    animationSpec = tween(
        durationMillis = NavigationDefaults.EXIT_FADE_DURATION,
        delayMillis = NavigationDefaults.EXIT_FADE_DELAY,
        easing = FastOutSlowInEasing
    )
)

fun AnimatedContentTransitionScope<*>.defaultPopEnterTransition() = fadeIn(
    animationSpec = tween(
        durationMillis = NavigationDefaults.FADE_DURATION,
        easing = FastOutSlowInEasing
    )
)

fun AnimatedContentTransitionScope<*>.defaultPopExitTransition() = fadeOut(
    animationSpec = tween(
        durationMillis = NavigationDefaults.EXIT_FADE_DURATION,
        easing = FastOutSlowInEasing
    )
) + slideOutOfContainer(
    towards = AnimatedContentTransitionScope.SlideDirection.Right,
    animationSpec = tween(
        durationMillis = NavigationDefaults.SLIDE_DURATION,
        delayMillis = NavigationDefaults.EXIT_FADE_DELAY,
        easing = FastOutSlowInEasing
    )
)

fun NavController.navigateToSettings(
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate("settings", builder)
}

fun NavController.navigateToFeedDetail(
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate("feedDetail", builder)
}

fun NavController.navigateToMultiServerConfig(
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate("multiServerConfig", builder)
}

fun NavController.navigateToHomeGroupingSettings(
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate("homeGroupingSettings", builder)
}

fun NavController.navigateToFeedFilterSettings(
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate("feedFilterSettings", builder)
}

fun NavController.navigateToStyleSettings(
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate("styleSettings", builder)
}

fun NavController.navigateToBlogSettings(
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate("blogSettings", builder)
}

fun NavController.navigateToLogging(
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate("logging", builder)
}

fun NavController.navigateToAbout(
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate("about", builder)
}

fun NavController.navigateToWebView(
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate("webview", builder)
}
