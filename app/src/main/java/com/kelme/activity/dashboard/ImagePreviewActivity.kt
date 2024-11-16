package com.kelme.activity.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.github.barteksc.pdfviewer.PDFView
import com.kelme.R
import com.kelme.app.BaseActivity
import com.kelme.databinding.ActivityImagePreviewBinding
import com.kelme.model.response.DocumentData
import com.kelme.utils.Constants
import com.kelme.utils.ProgressDialog
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

class ImagePreviewActivity : BaseActivity() {

    private lateinit var binding: ActivityImagePreviewBinding
    private lateinit var documentData: DocumentData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_preview)

        //documentData = intent.extras?.getParcelable<DocumentData>(Constants.DATA)!!
        val documentType = intent.getStringExtra(Constants.DOCUMENT_TYPE)!!
        val documentUrl = intent.getStringExtra(Constants.DOCUMENT_URL)!!
        if(documentType.equals("pdf")){
            binding.idPDFView.visibility = View.VISIBLE
            binding.imageView.visibility = View.GONE
            RetrievePDFFromURL(binding.idPDFView).execute(documentUrl)
        }else{
            binding.idPDFView.visibility = View.GONE
            binding.imageView.visibility = View.VISIBLE
            Glide.with(this).load(documentUrl).into(binding.imageView)
        }

        binding.backArrow.setOnClickListener { onBackPressed() }

        //
    }


    override fun initializerControl() {

    }

    @SuppressLint("StaticFieldLeak")
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
    }
}