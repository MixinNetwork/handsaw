package one.mixin.handsaw

sealed class Platform {
  sealed class Mobile : Platform() {
    object Android : Mobile()
    object IOS : Mobile()
  }

  sealed class Desktop : Platform() {
    object Mac : Desktop()
    object Windows : Desktop()
    object Linux : Desktop()
  }

  object Web : Platform()
}