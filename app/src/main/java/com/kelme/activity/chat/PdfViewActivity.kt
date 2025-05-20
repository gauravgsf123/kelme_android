package com.kelme.activity.chat

import android.graphics.Bitmap
import android.net.wifi.WifiConfiguration.AuthAlgorithm.strings
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.kelme.R
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class PdfViewActivity : AppCompatActivity() {

    var reportLoadError = false
    lateinit var pdfView:WebView
    var stringUrl:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)

        val urls = getIntent().getStringExtra("url");

        //pdfView = findViewById(R.id.idPDFView)
       // RetrivePdfStream().execute(urls)
    }

    fun initWebView() {
        pdfView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(webview: WebView, url: String, favicon: Bitmap?) {
                webview.visibility = View.INVISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {

                //handles load error
                if (reportLoadError) {
//                        loader.visibility = View.GONE
//                        view.visibility = View.GONE
//                        noData.visibility = View.VISIBLE
//                        iconShare.visibility = View.GONE
//                        btnTryAgain.setOnClickListener {
//                            initWebView()
//                        }

                    //handles successfull pdf load
                } else {
                    view.visibility = View.VISIBLE
//                        noData.visibility = View.GONE
//                        iconShare.visibility = View.VISIBLE
//                        loader.visibility = View.GONE
                }
                super.onPageFinished(view, url)

            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                reportLoadError = true

            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view!!.loadUrl(request!!.url.toString())
                return false
            }

        }
        pdfView.settings.javaScriptEnabled = true
        pdfView.settings.domStorageEnabled = true
        pdfView.overScrollMode = WebView.OVER_SCROLL_NEVER

        pdfView.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=$stringUrl")
    }
}

open class RetrivePdfStream : AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void?): String? {
        var inputStream: InputStream? = null
        try {

            // adding url
            val url = URL(strings[0])
            val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection

            // if url connection response code is 200 means ok the execute
            if (urlConnection.getResponseCode() === 200) {
                inputStream = BufferedInputStream(urlConnection.inputStream)
            }
        } // if error return null
        catch (e: IOException) {
            return null
        }
        return inputStream.toString()
    }

    // Here load the pdf and dismiss the dialog box
    protected fun onPostExecute(inputStream: InputStream?) {
//        pdfView.fromStream(inputStream).load()
//        dialog.dismiss()
    }

}
