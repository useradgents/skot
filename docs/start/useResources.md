# Using Android Resources in the ViewModel

It is possible to use Android resources in the ViewModel. To do this, the resources must be in the `res_referenced` directory of the view, rather than in the `res` directory.

The `res_referenced` directory uses the classic structure of an Android resource directory.

Once the file is placed in one of the subdirectories (drawable, values...) of `res_referenced`, you need to run the Gradle task `skGenerate` to generate the resource files.

All resources are accessible via global variables.

## Icons

Access to icons is done through the global variable `icons`. For example, if you have a drawable `my_icon.xml` in the `drawable` directory of `res_referenced`, you will have a `Icon` class `my_icon` in `icons`.

The `Icons` interface also contains a method to retrieve an `Icon` from its name. This can be used if the name of the icon is generated based on a WS response or a state.

There are helpers in the view to use an `Icon`:

* From an `ImageView`: `setIcon()`
* From a `View`: `setBackground()`

## Colors

Access to colors is done through the global variable `colors`. For example, if you have a color `my_color` in a `values` file in the `values` directory of `res_referenced`, you will have a `Color` class `my_color` in `colors`.

This will be a `ColorRef` that inherits from `Color`.

The `Icons` interface also contains a method to retrieve a color from its name. This can be used if the name of the color is generated based on a WS response or a state. This avoids having to use a `when` statement.

It is also possible to create a color by using the `ColorHex` implementation of `Color`. In this case, the color is not linked to a resource, and is instantiated with the hexadecimal code of a color (e.g.: `#FFFFFF`).


There are helpers in the view to use a color:

* From a `View`: `setBackground()`, `setBackgroundTint()`
* To retrieve the value of the color from the color: `getColor()`
* From an `ImageView`: `setImageTint()`
* From a `Drawable`: `setTint()`

## Dimensions

Access to dimensions is done through the global variable `dimens`. For example, if you have a dimension `my_dimen` in a `values` file in the `values` directory of `res_referenced`, you will have a `Dimen` class `my_dimen` in `dimens`.

This will be a `DimenRef` that inherits from `Dimen`.

It is also possible to create a dimension by using the `DimenDP` implementation of `Dimen`. In this case, the Dimension is not linked to a resource, and is instantiated with the value of the dimension in dp (e.g.: `8`).


There is a helper in the view to use a dimension:

* To retrieve the pixel value of the dimension: `toPixelSize()`

## Strings

Access to strings is done through the global variable `strings`. For example, if you have a string `my_string` in a `values` file in the `values` directory of `res_referenced`, you will have a `String` class `my_string` in `strings`.

The `Strings` interface also contains a method to retrieve a string from its name. This can be used if the name of the string is generated based on a WS response or a state. This avoids having to use a `when` statement.