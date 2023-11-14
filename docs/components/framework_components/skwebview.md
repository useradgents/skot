# SKWebView

Show a WebView in your application.

## Usage

In your screenVC, declare a SKWebView : 

```kotlin
interface ChatbotWebVC : SKScreenVC {
    val webView: SKWebViewVC
}
```


Launch the generation with the SKGenerate gradle task.

In your xml layout file,  add a WebView widget with id `webView` :

```xml
<WebView
    android:id="@+id/webView"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_weight="1" />
```

In your screen, you have to instantiate your component, for example with an url : 

```kotlin
override val webView: SKWebView = SKWebView(url = "https://www.useradgents.com/")
```

You can also instantiate your component with a specific configuration and launch option : 

```kotlin
public override val webView: SKWebView = SKWebView(
    config = SKWebViewVC.Config("AndroidApp"),
    launch = SKWebViewVC.Launch.OpenUrl(url = "https://www.useradgents.com/")
)
```

## Properties

| name | type | description |  
|--|--|--|
| config | SKWebViewVC.Config | Configuration for the component |
| launch | SKWebViewVC.Launch? | Launch option | 


### SKWebViewVC.Config

| name | type | description |
|--|--|--|
| userAgent | String? | Sets the WebView's user-agent string |
| javascriptEnabled | Boolean | Tells the WebView to enable JavaScript execution. Default value is true
| domStorageEnabled | Boolean | Sets whether the DOM storage API is enabled. Default value is true |
| javascriptOnStart | (()-> String?)? | Javascript code to execute when a web page start loaded. Default value is null |
| javascriptOnFinished | (()-> String?)? | Javascript code to execute when a web page finish loaded. Default value is null |
| shouldOverrideUrlLoading: ((skUri: SKUri) -> Boolean)? | Give the component a chance to take control when a URL is about to be loaded. Default value is null |
| onRequest | ((skUri: SKUri) -> Unit)? | Function called when a resource request is received by the component. Default value is null |
| onHttpAuthRequest | ((host : String?, realm : String?, onProceed : (login : String?, password : String?) -> Unit ) -> Unit)? | Function called when a auth request is received by the component. Default value is null |

Whereas `Launch.javascriptOnFinished` is a static string, `Config.javascriptOnFinished` is a function called in viewmodel. It allows javascript code to be updated by viewmodel. 

### SKWebViewVC.Launch
| name | type | description |
|--|--|--|
| onFinished | ((title : String?) -> Unit)? | Function called when web page is loaded |
| javascriptOnFinished |  String? | Javascript code to execute when the page is loaded |
| removeCookies | Boolean | Remove existing cookies on component start |
| cookie | Pair<String,String>? | Set cookies to apply to component | 
| url | String? | URL to load | 

#### SKWebViewVC.OpenUrl
`OpenUrl` inherit from Launch, overriding some parameters : 
| parameters | value | 
|--|--|
| onFinished | null |
| javascriptOnFinished |  null |
| removeCookies | false |
| cookie | Pair<String,String>? | null |

Specific parameters : 

| name | type | description |
|--|--|--|
| onError | (() -> Unit)? | Function called when webView component receive an error. Default value is null| 

#### SKWebViewVC.OpenUrlWithHeader
`OpenUrlWithHeader` inherit from Launch, overriding some parameters :
| parameters | value |
|--|--|
| onFinished | null |
| javascriptOnFinished |  null |
| removeCookies | false |
| cookie | Pair<String,String>? | null |

Specific parameters :

| name | type | description |
|--|--|--|
| onError | (() -> Unit)? | Function called when webView component receive an error. Default value is null |
| headers | Map<String,String> | Headers to set in request. Default value is emptyMap() |

#### SKWebViewVC.OpenPostUrl
`OpenPostUrl` inherit from Launch, overriding some parameters :
| parameters | value |
|--|--|
| onFinished | null |
| javascriptOnFinished |  null |
| removeCookies | false |
| cookie | Pair<String,String>? | null |

Specific parameters :

| name | type | description |
|--|--|--|
| onError | (() -> Unit)? | Function called when webView component receive an error. Default value is null |
| post | Map<String,String> | Post parameters to send. Default value is emptyMap() |


#### SKWebViewVC.LoadData 
`LoadData` inherit from Launch, overriding some parameters :
| parameters | value |
|--|--|
| onFinished | null |
| javascriptOnFinished |  null |
| removeCookies | false |
| cookie | Pair<String,String>? | null |

Specific parameters :

| name | type | description |
|--|--|--|
| data | String | Data to load in page |

This class can be used for loading local html file for example.