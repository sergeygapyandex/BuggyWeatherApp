package ru.yandex.buggyweatherapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

object ImageLoader {

    private lateinit var appContext: Context

    private const val MAX_CACHE_SIZE = 50

    private val imageCache = ConcurrentHashMap<String, Bitmap>()

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }


    suspend fun loadImage(url: String): Bitmap? {
        return if (imageCache.containsKey(url)) {
            imageCache[url]
        } else {
            try {

                val bitmap = withContext(Dispatchers.IO) {
                    val connection = URL(url).openConnection()
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.connect()

                    val inputStream = connection.getInputStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()

                    if (bitmap != null) {
                        if (imageCache.size >= MAX_CACHE_SIZE) {
                            val firstKey = imageCache.keys.firstOrNull()
                            firstKey?.let { imageCache.remove(it) }
                        }
                        imageCache[url] = bitmap
                    }
                    bitmap
                }
                bitmap
            } catch (e: Exception) {

                null
            }
        }
    }

    suspend fun loadImageSync(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            if (imageCache.containsKey(url)) {
                imageCache[url]
            } else {
                try {
                    val connection = URL(url).openConnection()
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.connect()

                    val inputStream = connection.getInputStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()

                    if (bitmap != null) {
                        if (imageCache.size >= MAX_CACHE_SIZE) {
                            val firstKey = imageCache.keys.firstOrNull()
                            firstKey?.let { imageCache.remove(it) }
                        }
                        imageCache[url] = bitmap
                    }
                    bitmap
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    fun loadInto(url: String, imageView: ImageView, scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
            if (imageCache.containsKey(url)) {
                imageView.setImageBitmap(imageCache[url])
            } else {
                val bitmap = loadImageSync(url)
                bitmap?.let {
                    imageView.setImageBitmap(it)
                }
            }
        }
    }


}
