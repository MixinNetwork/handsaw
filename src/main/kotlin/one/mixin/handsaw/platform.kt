package one.mixin.handsaw

sealed class Platform {
    object Android : Platform()
    object IOS : Platform()
    object Desktop: Platform()

    override fun toString(): String =
        when(this) {
            Android -> "Android"
            IOS -> "iOS"
            Desktop -> "Desktop"
        }
}

fun Collection<String>.containsIgnoreCase(text: String) = any { it.trim().equals(text, true) }