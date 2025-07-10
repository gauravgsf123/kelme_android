package com.kelme.activity.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide

import com.kelme.R
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityImagePreviewBinding
import com.kelme.model.response.DocumentData
import com.kelme.utils.Constants
import com.kelme.utils.ProgressDialog

class ImagePreviewActivity : BaseActivity() {

    private lateinit var binding: ActivityImagePreviewBinding
    private lateinit var documentData: DocumentData

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_preview)

        //documentData = intent.extras?.getParcelable<DocumentData>(Constants.DATA)!!
        val documentType = intent.getStringExtra(Constants.DOCUMENT_TYPE)!!
        val documentUrl = intent.getStringExtra(Constants.DOCUMENT_URL)!!
        if(documentType == "pdf"){
            binding.imageView.visibility = View.GONE
            with(binding.webView) {
                visibility = View.VISIBLE
                settings.javaScriptEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        ProgressDialog.showProgressBar(this@ImagePreviewActivity)
                        visibility = View.INVISIBLE
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        ProgressDialog.hideProgressBar()
                        visibility = View.VISIBLE
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        ProgressDialog.hideProgressBar()
                        Toast.makeText(
                            this@ImagePreviewActivity,
                            "Failed to load PDF",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=$documentUrl")
            }
            //RetrievePDFFromURL(binding.idPDFView).execute(documentUrl)
        }else{
            binding.webView.visibility = View.GONE
            binding.imageView.visibility = View.VISIBLE
            Glide.with(this).load(documentUrl).into(binding.imageView)
        }

        binding.backArrow.setOnClickListener { onBackPressed() }

        //
    }


    override fun initializerControl() {

    }

    /*@SuppressLint("StaticFieldLeak")
    class RetrievePDFFromURL(pdfView: PDFView) : AsyncTask<String, Void, InputStream>() {

        val mypdfView: PDFView = pdfView

        // on below line we are calling our do in background method.
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: String?): InputStream? {
            // on below line we are creating a variable for our input stream.
            var inputStream: InputStream? = null
            try {
                val url = URL(params.get(0))
                val urlConnection: HttpURLConnection = url.openConnection() as HttpsURLConnection
                if (urlConnection.responseCode == 200) {
                    inputStream = BufferedInputStream(urlConnection.inputStream)
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                return null;
            }
            // on below line we are returning input stream.
            return inputStream;
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: InputStream?) {
            mypdfView.fromStream(result).load()

        }
    }*/
}