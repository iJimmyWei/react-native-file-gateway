package com.reactnativefilegateway

import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

enum class DirectoryType {
  Application,
  Cache,
  External
}

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

  //https://stackoverflow.com/questions/60798804/store-image-via-android-media-store-in-new-folder
  // intention should be application (default), persistent (external), emphermeral (cache)
  // file type - audio, image, video
  @ReactMethod
  fun writeFile(fileName: String, data: String, intention: String, promise: Promise) {
    try {
      // TO:DO

      promise.resolve(true)
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

  // fileExists(path: string, promise: Promise)

  // stat(path: string) - TO:DO

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
