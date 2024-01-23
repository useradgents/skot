# SkScreen

With Skot, it is no longer necessary to think of Activity or Fragment. 
All navigable views become SKScreens, whether they are used in a [SKDialog](framework_components/skdialog.md), a [SkBottomSheet](framework_components/skbottomsheet.md), or a [SKStack](framework_components/skstack.md).
If there are parameters to pass, they can simply be passed in the constructor, no need to create Bundles.


To put it simply SKScreen inherits from SKComponent but adds navigation features :
- push (add screen on top in stack)
- finish (close screen)
- replaceWith (replace this screen by an other in the stack)
- dismiss (dismiss if screen is in a [SkBottomSheet](framework_components/skbottomsheet.md) or a [SkDialog](framework_components/skdialog.md), throw an Exception if not dismissable)
- dismissIfPresented (dismiss if screen is in a [SkBottomSheet](framework_components/skbottomsheet.md) or a [SkDialog](framework_components/skdialog.md))

## create a SkScreen

It's as simple as creating a [SKComponent](./readme.md), nothing else needs to be done.

See [create My First Screen](../start/createscreen.md)

## SKScreenView

View give access to fives * :
- fullScreen: Boolean ->
   let drawing in the statusBar

- lightStatusBar: Boolean ->
    let change text or icon color 

- secured:Boolean = false ->
- withWindowsInsetsPaddingTop: Boolean ->
- onWindowInset: ((windowInsets: WindowInsetsCompat) -> Unit) ->



