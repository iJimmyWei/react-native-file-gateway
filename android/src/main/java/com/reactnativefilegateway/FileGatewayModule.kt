package com.reactnativefilegateway

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.*
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.*

class FileGatewayModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  override fun getName(): String {
    return "FileGateway"
  }

  override fun getConstants(): Map<String, Any>? {
    val constants: MutableMap<String, Any> = HashMap()
    constants["Cache"] = reactApplicationContext.cacheDir.path
    constants["Application"] = reactApplicationContext.filesDir.path
    return constants
  }

  ///// ---- File methods
  private fun writeInternalFile(path: String, fileName: String, data: String, promise: Promise) {
    val out = FileWriter(File(path, fileName))
    out.write(data)
    out.close()

    promise.resolve("$path/$fileName")
  }

  private fun createAudioStore(fileName: String): Uri? {
    val audioCollection =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Media.getContentUri(
          MediaStore.VOLUME_EXTERNAL_PRIMARY
        )
      } else {
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
      }

    return reactApplicationContext.contentResolver.insert(
      audioCollection,
      ContentValues().apply {
        put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
      }
    )
  }

  /**
   * Writes a file given it's [fileName] and returning a path
   * The [intention] may be either application (data removed on uninstall), persistent (beyond application uninstall), or ephemeral (cache)
   * The [collection] may be either audio, image, video, document or download - if unspecified, it will be determined automatically
   */
  @ReactMethod
  fun writeFile(fileName: String, data: String, intention: String, collection: String, promise: Promise) {
    try {
      if (intention === "application") {
        return writeInternalFile(reactApplicationContext.filesDir.path, fileName, data, promise)
      }

      if (intention === "ephemeral") {
        return writeInternalFile(reactApplicationContext.cacheDir.path, fileName, data, promise)
      }

      if (intention === "persistent") {
        var store: Uri? = null;

        when(collection) {
          "audio" -> store = createAudioStore(fileName)
        }

        if (store == null) {
          throw Error("Unable to create store")
        }

        store.let {
          reactApplicationContext.contentResolver.openOutputStream(it)
        }?.use { output ->
          output.write(data.toByteArray())
          output.close()
        }

        promise.resolve(store.toString())
      }

      throw Error("The given intention is not a valid one")
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  /**
   * Reads a file given it's [path] and returning a string based on the [encoding] (base64, utf-8)
   */
  @ExperimentalStdlibApi
  @ReactMethod
  fun readFile(path: String, encoding: String, promise: Promise) {
    try {
      val data = File(path).inputStream().use { it.readBytes() }

      if (encoding === "base64") {
        val encodedB64String = Base64.encodeToString(data, Base64.DEFAULT)
        promise.resolve(encodedB64String)
      }

      promise.resolve(data.decodeToString())
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  /**
   * Deletes a file given it's [path]
   */
  @ReactMethod
  fun deleteFile(path: String, promise: Promise) {
    try {
      val success = File(path).delete()
      if (!success) {
        throw Error("Failed to delete file")
      }

      promise.resolve(true)
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  private fun File.getCreationTime(): String? {
    try {
      val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Paths.get(this.path)
      } else {
        return null
      }

      val creationTime = Files.getAttribute(path, "creationTime")
      return creationTime.toString()
    } catch (e: IOException) {
      return null
    }
  }

  private fun File.getLastAccessedTime(): String? {
    try {
      val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Paths.get(this.path)
      } else {
        return null
      }

      val attrs = Files.readAttributes(path, BasicFileAttributes::class.java)
      val time = attrs.lastAccessTime()
      return time.toString()
    } catch (e: IOException) {
      return null
    }
  }

  private fun File.getMimeType(): String? {
    if (this.isDirectory) {
      return null
    }

    fun fallbackMimeType(uri: Uri): String? {
      return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        reactApplicationContext.contentResolver.getType(uri)
      } else {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase(Locale.getDefault()))
      }
    }

    fun catchUrlMimeType(): String? {
      val uri = Uri.fromFile(this)

      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val path = Paths.get(uri.toString())
        try {
          Files.probeContentType(path) ?: fallbackMimeType(uri)
        } catch (ignored: IOException) {
          fallbackMimeType(uri)
        }
      } else {
        fallbackMimeType(uri)
      }
    }

    val stream = this.inputStream()
    return try {
      URLConnection.guessContentTypeFromStream(stream) ?: catchUrlMimeType()
    } catch (ignored: IOException) {
      catchUrlMimeType()
    } finally {
      stream.close()
    }
  }

  /**
   * Retrieves the status of a file given it's [path]
   */
  @ReactMethod
  fun status(path: String, promise: Promise) {
    try {
      val file = File(path);
      if (!file.exists()) {
        throw Error("File does not exist")
      }

      val statusMap = Arguments.createMap()

      val bytes = file.length()
      statusMap.putInt("size", bytes.toInt())

      val mimeType = file.getMimeType()
      statusMap.putString("mime", mimeType)

      val nameWithoutExtension = file.nameWithoutExtension
      val extension = file.extension
      statusMap.putString("nameWithoutExtension", nameWithoutExtension)
      statusMap.putString("extension", extension)

      val lastModified = file.lastModified() // returns back as unix time
      val lastModifiedDate = Date(lastModified)
      val formattedLastModifiedDate = toISO8601UTC(lastModifiedDate)
      statusMap.putString("lastModified", formattedLastModifiedDate)

      val creationTime = file.getCreationTime()
      statusMap.putString("creationTime", creationTime.toString())

      val lastAccessedTime = file.getLastAccessedTime()
      statusMap.putString("lastAccessedTime", lastAccessedTime)

      promise.resolve(statusMap)
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  @SuppressLint("SimpleDateFormat")
  fun toISO8601UTC(date: Date?): String? {
    val tz = TimeZone.getTimeZone("UTC")

    // Use SimpleDateFormat for maximum backwards compatibility
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    df.timeZone = tz
    return df.format(date)
  }

  ///////////////////////////
  // Directory Methods
  ///////////////////////////

  /**
   * Creates the directory at the given [path].
   */
  @ReactMethod
  fun createDirectory(path: String, promise: Promise) {
    try {
      val success = File(path).mkdir()
      if (!success) {
        throw Error("Failed to create directory");
      }

      promise.resolve(true)
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  /**
   * Checks if the given [path] is a directory.
   */
  @ReactMethod
  fun isDirectory(path: String, promise: Promise) {
    try {
      val isDirectory = File(path).isDirectory

      promise.resolve(isDirectory)
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  /**
   * Deletes all files recursively within the directory located at [path].
   */
  @ReactMethod
  fun deleteDirectory(path: String, promise: Promise) {
    try {
      if (!File(path).isDirectory) {
        throw Error("Specified path is not a directory")
      }

      var deleteSuccess = true;

      val walkTopDown = File(path).walkTopDown()
      walkTopDown.forEach {
        val files = it.list()
        files?.forEach { file ->
          // perhaps return back a list of failed deleted files?
          var deleteSuccessful = File("$path/$file").delete()

          if (!deleteSuccessful) {
            deleteSuccess = false
          }
        }
      }

      promise.resolve(deleteSuccess)
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  /**
   * Returns a (optionally [recursive]) list of files for a given [path].
   */
  @ReactMethod
  fun listFiles(path: String, recursive: Boolean = false, promise: Promise) {
    try {
      val filesArray = Arguments.createArray()

      if (recursive) {
        val walkTopDown = File(path).walkTopDown()
        walkTopDown.forEach {
          val files = it.list()
          files?.forEach { file ->
            filesArray.pushString(file)
          }
        }
      } else {
        val files = File(path).list()
        files?.forEach { filesArray.pushString(it) }
      }

      promise.resolve(filesArray)
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  //// ---- MISC
  // spaceLeft - for a given path, external and system - TODO

  /**
   * Checks if the file or directory exists for a given [path].
   */
  @ReactMethod
  fun exists(path: String, promise: Promise) {
    try {
      val exists = File(path).exists()
      promise.resolve(exists)
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  /**
   * Moves a file or directory given its [path] to the [targetPath]
   * Will replace existing file(s) in the target path
   */
  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun move(path: String, targetPath: String, promise: Promise) {
    try {
      val isDirectory = File(path).isDirectory
      if (!isDirectory) {
        throw Error("Specified path is not a directory")
      }

      val sourcePath = Paths.get(path)
      val destinationPath = Paths.get(targetPath)

      val path = Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING)

      promise.resolve(path)
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  // TO:DO
  // - Cryptography
  // - Downloads/Uploads
}
