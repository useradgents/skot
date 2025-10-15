package tech.skot.core.components

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.HttpAuthHandler
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                val bundle = Bundle()
                webView.saveState(bundle)
                proxy.setState(bundle)
                lifecycleOwner.lifecycle.removeObserver(this)
                super.onDestroy(owner)
            }
        }
        )
    }

    private fun SKWebViewVC.Config.configureWebViewSettings() {
        webView.settings.apply {
            userAgentString = userAgent
            javaScriptEnabled = javascriptEnabled
            domStorageEnabled = domStorageEnabled
        }
    }

    private fun SKWebViewVC.Config.createWebViewClient(): WebViewClient {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ModernWebViewClient(this)
        } else {
            LegacyWebViewClient(this)
        }
    }

    fun onConfig(config: SKWebViewVC.Config) {
        with(config) {
            configureWebViewSettings()
            webView.webViewClient = createWebViewClient()
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
        val bundle = proxy.state?.takeIf { it.launch == launch }
        if (bundle != null) {
            webView.restoreState(bundle.bundle)
            proxy.setState(null)
        } else {
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


    open class SKWebChromeClient(private val activity: SKActivity, val proxy: SKWebViewViewProxy) :
        WebChromeClient() {
        var currentRequest: PermissionRequest? = null

        fun String.toSKWebViewPermissionType(): SKWebViewVC.SKWebViewPermissionType? {
            return when (this) {
                PermissionRequest.RESOURCE_MIDI_SYSEX -> SKWebViewVC.SKWebViewPermissionType.MIDI
                PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID -> SKWebViewVC.SKWebViewPermissionType.MEDIA
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> SKWebViewVC.SKWebViewPermissionType.CAMERA
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> SKWebViewVC.SKWebViewPermissionType.MICROPHONE
                else -> null
            }
        }

        fun SKWebViewVC.SKWebViewPermissionType.toWebkitString(): String {
            return when (this) {
                SKWebViewVC.SKWebViewPermissionType.CAMERA -> PermissionRequest.RESOURCE_VIDEO_CAPTURE
                SKWebViewVC.SKWebViewPermissionType.MICROPHONE -> PermissionRequest.RESOURCE_AUDIO_CAPTURE
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
            return createBitmap(50, 50)
        }

        override fun onPermissionRequest(request: PermissionRequest?) {
            request?.let {
                currentRequest = it
                proxy.config.onPermissionRequested?.invoke(it.resources.mapNotNull {
                    it.toSKWebViewPermissionType()
                }) {

                    val permissions = it.map {
                        it.toWebkitString()
                    }

                    if (permissions.isEmpty()) {
                        currentRequest?.deny()
                    } else {
                        currentRequest?.grant(permissions.toTypedArray())
                    }
                    currentRequest = null
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private inner class ModernWebViewClient(private val config: SKWebViewVC.Config) :
        WebViewClient() {

        override fun onRenderProcessGone(
            view: WebView?,
            detail: RenderProcessGoneDetail?
        ): Boolean {
            return handleRenderProcessGone(view, detail).takeIf { it } ?: super.onRenderProcessGone(
                view,
                detail
            )
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return handleUrlLoading(request, config)
        }

        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            handleHttpError(request, errorResponse, config)
            super.onReceivedHttpError(view, request, errorResponse)
        }

        override fun onReceivedHttpAuthRequest(
            view: WebView?,
            handler: HttpAuthHandler?,
            host: String?,
            realm: String?
        ) {
            handleHttpAuthRequest(handler, host, realm, config)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            executeJavaScriptOnStart(config)
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            handlePageFinished(url, config)
            super.onPageFinished(view, url)
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            handleReceivedError(request, error)
            super.onReceivedError(view, request, error)
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            handleInterceptRequest(request, config)
            return super.shouldInterceptRequest(view, request)
        }
    }


    private inner class LegacyWebViewClient(private val config: SKWebViewVC.Config) :
        WebViewClient() {
        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            handleHttpError(request, errorResponse, config)
            super.onReceivedHttpError(view, request, errorResponse)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            oneRedirectionAskedForCurrentOpenUrl = true
            return handleUrlLoading(request, config)
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

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            SKLog.d("WebView onReceivedError ${error?.errorCode}")
            handleReceivedError(request, error)
            super.onReceivedError(view, request, error)
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            handleInterceptRequest(request, config)
            return super.shouldInterceptRequest(view, request)
        }
    }


    private fun handleRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (view == webView && detail?.didCrash() == true) {
                recreateWebView()
                return true
            }
        }
        return false
    }

    private fun handleUrlLoading(
        request: WebResourceRequest?,
        config: SKWebViewVC.Config
    ): Boolean {
        request?.url?.toSKUri()?.let { skUri ->
            if (shouldOverrideUrl(skUri, config) || shouldHandleDeepLink(skUri)) {
                return true
            }
        }

        request?.let {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    it.isForMainFrame && it.isRedirect
                } else {
                    true
                }
            ) {
                oneRedirectionAskedForCurrentOpenUrl = true
            }
        }
        return false
    }

    private fun shouldOverrideUrl(skUri: SKUri, config: SKWebViewVC.Config): Boolean {
        return try {
            config.shouldOverrideUrlLoading?.invoke(skUri) == true
        } catch (ex: Exception) {
            SKLog.e(ex, "Erreur dans l'invocation de shouldOverrideUrlLoading depuis SKWebViewView")
            false
        }
    }

    private fun shouldHandleDeepLink(skUri: SKUri): Boolean {
        return try {
            activity.featureInitializer.onDeepLink?.invoke(skUri, true) == true
        } catch (ex: Exception) {
            SKLog.e(ex, "Erreur dans l'invocation de onDeepLink depuis SKWebViewView")
            false
        }
    }

    private fun handleHttpError(
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?,
        config: SKWebViewVC.Config
    ) {
        request?.url?.toSKUri()?.let { url ->
            errorResponse?.statusCode?.let { statusCode ->
                config.httpError(url, statusCode)
            }
        }
    }

    private fun handleHttpAuthRequest(
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?,
        config: SKWebViewVC.Config
    ) {
        config.onHttpAuthRequest?.invoke(host, realm) { login, password ->
            if (login == null || password == null) {
                handler?.cancel()
            } else {
                handler?.proceed(login, password)
            }
        } ?: handler?.cancel()
    }

    private fun executeJavaScriptOnStart(config: SKWebViewVC.Config) {
        config.javascriptOnStart?.invoke()?.let {
            onEvaluateJavascript(it) {}
        }
    }

    private fun handlePageFinished(url: String?, config: SKWebViewVC.Config) {
        openingUrl?.finished(url)
        config.javascriptOnFinished?.invoke()?.let {
            onEvaluateJavascript(it) {}
        }
    }

    private fun handleReceivedError(request: WebResourceRequest?, error: WebResourceError?) {
        openingUrl?.error(request?.url, error?.errorCode)
    }

    private fun handleInterceptRequest(request: WebResourceRequest?, config: SKWebViewVC.Config) {
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
    }


}