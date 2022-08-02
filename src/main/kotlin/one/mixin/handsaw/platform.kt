package one.mixin.handsaw

sealed class Platform {
    object Android : Platform()
    object IOS : Platform()
    object Desktop: Platform()
    object IOSAuthorization: Platform()
    object AppStore: Platform()

    override fun toString(): String =
        when(this) {
            Android -> "Android"
            IOS -> "iOS"
            Desktop -> "Desktop"
            IOSAuthorization -> "iOSAuthorization"
            AppStore -> "AppStore"
        }
}

fun Collection<String>.containsIgnoreCase(text: String) = any { it.trim().equals(text, true) }