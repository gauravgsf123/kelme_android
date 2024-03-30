package com.kelme.utils

import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs


/**
 * Created by Gaurav Kumar on 14/07/21.
 */
class DownloadAndSaveImageTask(var context: Context) : AsyncTask<String, String, String>() {
    var filePath: File? = null
    override fun doInBackground(vararg strings: String?): String? {
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strings.get(0))
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.setRequestMethod("GET")
            urlConnection.setDoOutput(true)
            urlConnection.connect()
            //File mydir = context.getDir("mydirectory", Context.MODE_PRIVATE)
            filePath = File(
                context.getDir(Environment.getExternalStorageState(), Context.MODE_PRIVATE),
                "Kelme"
            )
            if (!filePath!!.exists()) {
                filePath!!.mkdirs()
            }
            Log.e("check_path", "" + filePath!!.absolutePath)

            val fileName = strings[0]!!
                .substring(
                    strings[0]!!.lastIndexOf('/') + 1, strings[0]!!
                        .length
                )

            val imgFile: File = File(filePath, fileName)
            if (!filePath!!.exists()) {
                imgFile.createNewFile()
            }
            val inputStream: InputStream = urlConnection.getInputStream()
            val totalSize: Int = urlConnection.getContentLength()
            val outPut = FileOutputStream(imgFile)
            var downloadedSize = 0
            val buffer = ByteArray(2024)
            var bufferLength = 0
            while (inputStream.read(buffer).also { bufferLength = it } > 0) {
                outPut.write(buffer, 0, bufferLength)
                downloadedSize += bufferLength
                Log.e("Progress:", "downloadedSize:" + abs(downloadedSize * 100 / totalSize))
            }
            Log.e("Progress:", "imgFile.getAbsolutePath():" + imgFile.absolutePath)
            Log.e("File_path", "check image path 2" + imgFile.absolutePath)
            //mImageArray.add(imgFile.absolutePath)
            outPut.close()

        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("checkException:-", "" + e)
        }
        return null
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
//        Toast.makeText(context,"file download success",Toast.LENGTH_LONG).show()
//    }
    }
}




