package tech.skot.core.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import tech.skot.core.SKLog
import tech.skot.core.SKUri
import tech.skot.core.toSKUri
import java.net.URLEncoder

class SKWebViewView(
    override val proxy: SKWebViewViewProxy,
    activity: SKActivity,
    fragment: Fragment?,
    private var webView: WebView,
) : SKComponentView<WebView>(proxy, activity, fragment, webView) {


    init {
        binding.webChromeClient = SKWebChromeClient(activity, proxy)
    }

    fun onConfig(config: SKWebViewVC.Config) {
        webView.settings.apply {
            userAgentString = config.userAgent
            javaScriptEnabled = config.javascriptEnabled
            domStorageEnabled = config.domStorageEnabled
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            webView.webViewClient = object : WebViewClient() {
                override fun onRenderProcessGone(
                    view: WebView?,
                    detail: RenderProcessGoneDetail?
                ): Boolean {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (view == webView && detail?.didCrash() == true) {
                            recreateWebView()
                            return true
                        }
                    }
                    return super.onRenderProcessGone(view, detail)
                }


                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    request?.url?.toSKUri()?.let { skUri ->
                        try {
                            if (config.shouldOverrideUrlLoading?.invoke(skUri) == true) {
                                return true
                            }
                        } catch (ex: Exception) {
                            SKLog.e(
                                ex,
                                "Erreur dans l'invocation de shouldOverrideUrlLoading depuis SKWebViewView"
                            )
                        }

                        try {
                            if (activity.featureInitializer.onDeepLink?.invoke(skUri, true) == true
                            ) {
                                return true
                            }
                        } catch (ex: Exception) {
                            SKLog.e(
                                ex,
                                "Erreur dans l'invocation de onDeepLink depuis SKWebViewView"
                            )
                        }
                    }
                    request?.let {
                        if (it.isForMainFrame && it.isRedirect) {
                            oneRedirectionAskedForCurrentOpenUrl = true
                        }
                    }
                    return super.shouldOverrideUrlLoading(view, request)
                }


                override fun onReceivedHttpError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    errorResponse: WebResourceResponse?
                ) {
                    request?.url?.toSKUri()?.let { url ->
                        errorResponse?.statusCode?.let { statusCode ->
                            config.httpError(url, statusCode)
                        }
                    }
                    super.onReceivedHttpError(view, request, errorResponse)
                }

                override fun onReceivedHttpAuthRequest(
                    view: WebView?,
                    handler: HttpAuthHandler?,
                    host: String?,
                    realm: String?
                ) {
                    config.onHttpAuthRequest?.invoke(host, realm) { login, password ->
                        if (login == null || password == null) {
                            super.onReceivedHttpAuthRequest(view, handler, host, realm)
                        } else {
                            handler?.proceed(login, password)
                        }
                    } ?: super.onReceivedHttpAuthRequest(view, handler, host, realm)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    config.javascriptOnStart?.invoke()?.let {
                        onEvaluateJavascript(it) {

                        }
                    }
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    openingUrl?.finished(url)
                    config.javascriptOnFinished?.invoke()?.let {
                        onEvaluateJavascript(it) {

                        }
                    }
                    super.onPageFinished(view, url)
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    openingUrl?.error(request?.url, error?.errorCode)
                    super.onReceivedError(view, request, error)
                }

                @Deprecated("Deprecated in Java")
                @Suppress("deprecation")
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    openingUrl?.error(failingUrl?.toUri(), errorCode)
                    super.onReceivedError(view, errorCode, description, failingUrl)
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    request?.url?.toSKUri()?.let { skUri ->
                        try {
                            config.onRequest?.invoke(skUri)
                        } catch (ex: Exception) {
                            SKLog.e(
                                ex,
                                "Erreur dans l'invocation de shouldInterceptRequest depuis SKWebViewView"
                            )
                        }
                    }
                    return super.shouldInterceptRequest(view, request)
                }
            }
        } else {
            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedHttpError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    errorResponse: WebResourceResponse?
                ) {
                    request?.url?.toSKUri()?.let { url ->
                        errorResponse?.statusCode?.let { statusCode ->
                            config.httpError(url, statusCode)
                        }
                    }
                    super.onReceivedHttpError(view, request, errorResponse)
                }

                @Suppress("DEPRECATION")
                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {

                    url?.let { Uri.parse(url).toSKUri() }?.let { skUri ->
                        try {
                            if (config.shouldOverrideUrlLoading?.invoke(skUri) == true) {
                                return true
                            }
                        } catch (ex: Exception) {
                            SKLog.e(
                                ex,
                                "Erreur dans l'invocation de shouldOverrideUrlLoading depuis SKWebViewView"
                            )
                        }

                        try {
                            if (activity.featureInitializer.onDeepLink?.invoke(
                                    skUri,
                                    true
                                ) == true
                            ) {
                                return true
                            }
                        } catch (ex: Exception) {
                            SKLog.e(
                                ex,
                                "Erreur dans l'invocation de onDeepLink depuis SKWebViewView"
                            )
                        }
                    }

                    oneRedirectionAskedForCurrentOpenUrl = true

                    return super.shouldOverrideUrlLoading(view, url)
                }

                override fun onReceivedHttpAuthRequest(
                    view: WebView?,
                    handler: HttpAuthHandler?,
                    host: String?,
                    realm: String?
                ) {
                    config.onHttpAuthRequest?.invoke(host, realm) { login, password ->
                        handler?.proceed(login, password)
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    config.javascriptOnStart?.invoke()?.let {
                        webView.evaluateJavascript(it, null)
                    }
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    openingUrl?.finished(url)
                    config.javascriptOnFinished?.invoke()?.let {
                        webView.evaluateJavascript(it, null)
                    }
                    super.onPageFinished(view, url)

                }


                @RequiresApi(Build.VERSION_CODES.M)
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    SKLog.d(
                        "WebView onReceivedError ${error?.errorCode}"
                    )
                    openingUrl?.error(request?.url, error?.errorCode)
                    super.onReceivedError(view, request, error)
                }

                @Deprecated("Deprecated in Java")
                @Suppress("DEPRECATION")
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    openingUrl?.error(failingUrl?.toUri(), errorCode)
                    super.onReceivedError(view, errorCode, description, failingUrl)
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    request?.url?.toSKUri()?.let { skUri ->
                        try {
                            config.onRequest?.invoke(skUri)
                        } catch (ex: Exception) {
                            SKLog.e(
                                ex,
                                "Erreur dans l'invocation de shouldInterceptRequest depuis SKWebViewView"
                            )
                        }
                    }
                    return super.shouldInterceptRequest(view, request)
                }


            }
        }
    }

    private fun recreateWebView() {
        try {
            val parent = webView.parent as? ViewGroup
            val params = webView.layoutParams
            val id = webView.id
            parent?.removeView(webView)
            val newWv = WebView(context)
            parent?.addView(newWv.apply {
                setId(id)
                layoutParams = params
            })
            webView = newWv
            onConfig(proxy.config)
            proxy.config.onWebViewCrash?.invoke()
        } catch (e: Throwable) {
            SKLog.e(e, "Webview Crash")
        }
    }

    private var openingUrl: SKWebViewVC.Launch? = null

    private var oneRedirectionAskedForCurrentOpenUrl = false

    private fun SKWebViewVC.Launch.finished(finishedUrl: String?) {
        val escapedUrl = if (this is SKWebViewVC.Launch.LoadData) {
            "data:text/html,$data"
        } else {
            url?.replace(" ", "%20")
        }

        if (finishedUrl == escapedUrl || finishedUrl == "$escapedUrl/" || oneRedirectionAskedForCurrentOpenUrl) {
            openingUrl = null
            onFinished?.invoke(webView.title)
            javascriptOnFinished?.let {
                webView.evaluateJavascript(it, null)
            }
        }
    }

    private fun SKWebViewVC.Launch.error(requestedUri: Uri?, errorCode: Int?) {
        SKLog.d("error ${requestedUri?.toString()} $errorCode")
        when (this) {
            is SKWebViewVC.Launch.LoadData -> {}
            is SKWebViewVC.Launch.OpenPostUrl -> launchError(requestedUri, this.url, this.onError)
            is SKWebViewVC.Launch.OpenUrl -> launchError(requestedUri, this.url, this.onError)
            is SKWebViewVC.Launch.OpenUrlWithHeader -> launchError(
                requestedUri,
                this.url,
                this.onError
            )
        }
    }

    private fun SKWebViewVC.Config.httpError(requestedUri: SKUri, errorCode: Int) {
        launchHttpError(requestedUri, errorCode, this.onHttpError)
    }

    private fun launchHttpError(
        requestedUri: SKUri,
        statusCode: Int,
        onError: ((url: SKUri, statusCode: Int) -> Unit)?
    ) {
        onError?.invoke(requestedUri, statusCode)
    }

    private fun launchError(requestedUri: Uri?, url: String?, onError: (() -> Unit)?) {
        requestedUri?.toString()?.let { requestUrl ->
            if (url == requestUrl) {
                onError?.invoke()
                openingUrl = null
            }
        }
    }

    fun onLaunch(launch: SKWebViewVC.Launch?) {
        oneRedirectionAskedForCurrentOpenUrl = false
        openingUrl = launch
        if (launch != null) {
            if (launch.removeCookies) {
                CookieManager.getInstance().removeAllCookies {
                    launch.cookie?.let {
                        CookieManager.getInstance().setCookie(it.first, it.second)
                    }
                    launchNow(launch)
                }
            } else {
                launch.cookie?.let {
                    CookieManager.getInstance().setCookie(it.first, it.second)
                }
                launchNow(launch)
            }
        }
    }

    private fun launchNow(launch: SKWebViewVC.Launch) {
        when (launch) {
            is SKWebViewVC.Launch.LoadData -> {
                if (launch.url != null) {
                    webView.loadDataWithBaseURL(launch.url, launch.data, null, null, null)
                } else {
                    webView.loadData(launch.data, null, null)
                }
            }

            is SKWebViewVC.Launch.OpenPostUrl -> {
                val params = launch.post.map {
                    "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
                }
                    .joinToString(separator = "&")
                webView.postUrl(launch.url, params.toByteArray())
            }

            is SKWebViewVC.Launch.OpenUrl -> {
                webView.loadUrl(launch.url)
            }

            is SKWebViewVC.Launch.OpenUrlWithHeader -> {
                webView.loadUrl(launch.url, launch.headers)
            }
        }
    }

    fun onGoBackLD(goBack: SKWebViewVC.BackRequest?) {
        goBack?.let {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                it.onCantBack?.invoke()
            }
            proxy.goBack = null
        }
    }

    fun onRequestGoForward() {
        webView.goForward()
    }

    fun onRequestReload() {
        webView.reload()
    }

    fun onEvaluateJavascript(js: String, onResult: (String) -> Unit) {
        SKLog.d("evaluateJs $js")
        webView.evaluateJavascript(js, onResult)
    }

    fun Uri.getMapQueryParameters(): Map<String, String> =
        try {
            queryParameterNames.associateWith { getQueryParameter(it)!! }
        } catch (ex: Exception) {
            SKLog.e(ex, "Pb au parse des paramètres d'une url de redirection")
            emptyMap()
        }


    open class SKWebChromeClient(private val activity: SKActivity, val proxy : SKWebViewViewProxy) : WebChromeClient() {
        var currentRequest : PermissionRequest? = null

        fun String.toSKWebViewPermissionType() : SKWebViewVC.SKWebViewPermissionType?{
            return when(this) {
                PermissionRequest.RESOURCE_MIDI_SYSEX -> SKWebViewVC.SKWebViewPermissionType.MIDI
                PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID -> SKWebViewVC.SKWebViewPermissionType.MEDIA
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> SKWebViewVC.SKWebViewPermissionType.CAMERA
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> SKWebViewVC.SKWebViewPermissionType.MICROPHONE
                else -> null
            }
        }

        fun SKWebViewVC.SKWebViewPermissionType.toWebkitString() : String{
            return when(this) {
                SKWebViewVC.SKWebViewPermissionType.CAMERA ->  PermissionRequest.RESOURCE_VIDEO_CAPTURE
                SKWebViewVC.SKWebViewPermissionType.MICROPHONE ->  PermissionRequest.RESOURCE_AUDIO_CAPTURE
                SKWebViewVC.SKWebViewPermissionType.MEDIA -> PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID
                SKWebViewVC.SKWebViewPermissionType.MIDI -> PermissionRequest.RESOURCE_MIDI_SYSEX
            }
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            activity.fileCallback = filePathCallback
            fileChooserParams?.createIntent()?.let { activity.fileSelectorCallback.launch(it) }
            return true
        }

        override fun getDefaultVideoPoster(): Bitmap? {
            return Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
        }

        override fun onPermissionRequest(request: PermissionRequest?) {
            request?.let {
                currentRequest = it
                proxy.config.onPermissionRequested?.invoke(it.resources.mapNotNull {
                    it.toSKWebViewPermissionType()
                }){

                    val permissions = it.map {
                        it.toWebkitString()
                    }

                    if(permissions.isEmpty()){
                        currentRequest?.deny()
                    }else {
                        currentRequest?.grant(permissions.toTypedArray())
                    }
                    currentRequest = null
                }
            }
        }

    }


}