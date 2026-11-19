package com.example

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.ByteArrayInputStream
import java.io.IOException

class MainActivity : ComponentActivity() {
  private lateinit var webView: WebView

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.parseColor("#1D1626")),
      navigationBarStyle = SystemBarStyle.dark(Color.parseColor("#16101E"))
    )
    super.onCreate(savedInstanceState)

    val plumBg = Color.parseColor("#1D1626")
    webView = WebView(this).apply {
      setBackgroundColor(plumBg)
      isVerticalScrollBarEnabled = false
      isHorizontalScrollBarEnabled = false
      overScrollMode = WebView.OVER_SCROLL_NEVER

      settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        mediaPlaybackRequiresUserGesture = false
        allowFileAccess = false
        allowContentAccess = false
        useWideViewPort = true
        loadWithOverviewMode = true
        setSupportZoom(false)
        builtInZoomControls = false
        displayZoomControls = false
        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        cacheMode = WebSettings.LOAD_DEFAULT
      }

      webChromeClient = WebChromeClient()
      webViewClient = object : WebViewClient() {
        override fun shouldInterceptRequest(
          view: WebView?,
          request: WebResourceRequest?
        ): WebResourceResponse? {
          val url = request?.url ?: return null
          if (url.host != VIRTUAL_HOST) {
            return super.shouldInterceptRequest(view, request)
          }
          val rawPath = url.path ?: "/"
          val relativePath = when {
            rawPath == "/" || rawPath.isEmpty() -> "index.html"
            else -> rawPath.trimStart('/')
          }
          return loadAssetResponse(relativePath)
        }
      }
    }

    ViewCompat.setOnApplyWindowInsetsListener(webView) { view, windowInsets ->
      val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
      view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
      windowInsets
    }

    setContentView(webView)

    onBackPressedDispatcher.addCallback(
      this,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          if (webView.canGoBack()) {
            webView.goBack()
          } else {
            finish()
          }
        }
      }
    )

    if (savedInstanceState != null) {
      webView.restoreState(savedInstanceState)
    } else {
      webView.loadUrl("https://$VIRTUAL_HOST/")
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    webView.saveState(outState)
  }

  override fun onResume() {
    super.onResume()
    webView.onResume()
  }

  override fun onPause() {
    webView.onPause()
    super.onPause()
  }

  override fun onDestroy() {
    webView.destroy()
    super.onDestroy()
  }

  private fun loadAssetResponse(assetPath: String): WebResourceResponse {
    val cleanPath = assetPath.replace("..", "").trimStart('/')
    val candidates = listOf(cleanPath, "www/$cleanPath")
    for (candidate in candidates) {
      try {
        val stream = assets.open(candidate)
        val headers = mapOf("Access-Control-Allow-Origin" to "*")
        return WebResourceResponse(
          mimeTypeFor(candidate),
          "UTF-8",
          200,
          "OK",
          headers,
          stream
        )
      } catch (_: IOException) {
        // Try next candidate path
      }
    }
    return WebResourceResponse(
      "text/plain",
      "UTF-8",
      404,
      "Not Found",
      emptyMap(),
      ByteArrayInputStream(ByteArray(0))
    )
  }

  private fun mimeTypeFor(path: String): String {
    val lower = path.lowercase()
    return when {
      lower.endsWith(".html") -> "text/html"
      lower.endsWith(".js") || lower.endsWith(".mjs") -> "application/javascript"
      lower.endsWith(".css") -> "text/css"
      lower.endsWith(".json") -> "application/json"
      lower.endsWith(".png") -> "image/png"
      lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
      lower.endsWith(".svg") -> "image/svg+xml"
      lower.endsWith(".ttf") -> "font/ttf"
      lower.endsWith(".woff2") -> "font/woff2"
      else -> "application/octet-stream"
    }
  }

  companion object {
    private const val VIRTUAL_HOST = "appassets.androidplatform.net"
  }
}
