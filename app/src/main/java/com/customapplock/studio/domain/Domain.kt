package com.customapplock.studio.domain

/** Pseudo package names used for the in-app master lock and the home screen lock. */
const val MASTER_PKG = "__master__"
const val HOME_PKG = "__home__"

enum class LockType(val id: String, val title: String, val hint: String, val level: String) {
    NUMBER_PATTERN("number_pattern", "Number Pattern", "Connect at least 5 numbered dots in your secret order", "Strong"),
    JIGSAW("jigsaw", "Jigsaw Puzzle", "Drag the missing piece into 4 secret spots", "Very strong"),
    SCROLL_WHEEL("scroll_wheel", "Vault Wheel", "Spin 6 wheels to your secret code", "Very strong"),
    COLOR_SEQUENCE("color_sequence", "Color Sequence", "Tap 5 glowing orbs in your secret order", "Strong"),
    SHAPE_MATCH("shape_match", "Shape Match", "Drop each of the 5 shapes into its secret slot", "Strong"),
    CLOCK_HAND("clock_hand", "Clock Hands", "Set the clock hands to your secret time", "Good"),
    EMOJI_MATRIX("emoji_matrix", "Emoji Matrix", "Tap 5 emojis in your secret order", "Very strong"),
    CALCULATOR("calculator", "Calculator", "Type a secret formula and press =", "Very strong"),
    BUBBLE_POP("bubble_pop", "Bubble Pop", "Pop 5 floating bubbles in your secret order", "Strong"),
    SWIPE("swipe", "Direction Swipe", "Swipe 6 secret directions", "Strong");

    companion object {
        fun fromId(id: String): LockType = entries.firstOrNull { it.id == id } ?: NUMBER_PATTERN
    }
}

enum class RelockMode(val title: String, val desc: String) {
    IMMEDIATE("Immediately on exit", "Relock the moment you leave the app"),
    SCREEN_OFF("After screen turns off", "Stay unlocked until the screen switches off"),
    GRACE("Custom grace period", "Relock after a delay you choose")
}
