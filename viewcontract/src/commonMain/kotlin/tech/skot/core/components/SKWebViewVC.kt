package tech.skot.core.components

import tech.skot.core.SKUri

@SKLayoutIsSimpleView
interface SKWebViewVC : SKComponentVC {
    val config: Config
    var launch: Launch?
    val openUrl: Launch.OpenUrl?
        get() = launch as? Launch.OpenUrl?

    var goBack: BackRequest?
    fun requestGoForward()
    fun requestReload()
    fun evaluateJavascript(js : String, onResult :(String)-> Unit)

    data class Config(
        val userAgent: String?,
        val javascriptEnabled: Boolean = true,
        val domStorageEnabled: Boolean = true,
        val javascriptOnFinished: (()-> String?)? = null,
        val javascriptOnStart : (()-> String?)? = null,
        val shouldOverrideUrlLoading: ((skUri: SKUri) -> Boolean)? = null,
        val onHttpError: ((url : SKUri, statusCode : Int) -> Unit)? = null,
        val onRequest: ((skUri: SKUri) -> Unit)? = null,
        val onHttpAuthRequest :((host : String?, realm : String?, onProceed : (login : String?, password : String?) -> Unit ) -> Unit)? = null
    )


    sealed class Launch(){
        abstract val onFinished: ((title : String?) -> Unit)?
        abstract val javascriptOnFinished: String?
        abstract val removeCookies:Boolean
        abstract val cookie:Pair<String,String>?
        abstract val url: String?

        data class OpenUrl(
            override val url: String,
            override val onFinished: ((title : String?) -> Unit)? = null,
            override val javascriptOnFinished: String? = null,
            val onError: (() -> Unit)? = null,
            override val removeCookies:Boolean = false,
            override val cookie:Pair<String,String>? = null
        ) : Launch()

        /**
         * launch url with headers
         */
        data class OpenUrlWithHeader(
            override val url: String,
            override val onFinished: ((title : String?) -> Unit)? = null,
            override val javascriptOnFinished: String? = null,
            val onError: (() -> Unit)? = null,
            val headers: Map<String,String> = emptyMap(),
            override val removeCookies:Boolean = false,
            override val cookie:Pair<String,String>? = null
        ) : Launch()

        /**
         * launch url with post parameters
         */
        data class OpenPostUrl(
            override val url: String,
            override val onFinished: ((title : String?) -> Unit)? = null,
            override val javascriptOnFinished: String? = null,
            val onError: (() -> Unit)? = null,
            val post: Map<String, String> = emptyMap(),
            override val removeCookies:Boolean = false,
            override val cookie:Pair<String,String>? = null
        ) : Launch()


        /**
         * load data with url
         * @param data, source code of the page
         * @param url, the baseUrl
         */
        data class LoadData(
            val data : String,
            override val url: String?,
            override val onFinished: ((title : String?) -> Unit)? = null,
            override val javascriptOnFinished: String? = null,
            override val removeCookies:Boolean = false,
            override val cookie:Pair<String,String>? = null
        ) : Launch()
    }

    data class BackRequest(val onCantBack: (() -> Unit)? = null)
}