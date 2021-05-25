package com.reactnativefilegateway

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.reactnativefilegateway.FileGatewayModule.Errors.Companion.ERROR_CREATE_DIRECTORY_FAILED
import com.reactnativefilegateway.FileGatewayModule.Errors.Companion.ERROR_DELETE_DIRECTORY_FAILED
import com.reactnativefilegateway.exceptions.CreateDirectoryException
import com.reactnativefilegateway.exceptions.DeleteDirectoryException
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.mockito.ArgumentCaptor
import java.lang.Exception
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.`is`
import org.mockito.Mockito.reset
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.mockito.Mockito.eq
import java.io.File

/**
 * Unit tests for the FileGatewayModule.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class FileGatewayModuleTest {
  private var reactModule: FileGatewayModule? = null

  private val mockPromise: Promise = mock(Promise::class.java)
  private var tempPath: String? = null

  @Before
  fun before() {
    val applicationContext = ApplicationProvider.getApplicationContext<android.content.Context>()
    val reactApplicationContext = ReactApplicationContext(applicationContext)

    reactModule = FileGatewayModule(reactApplicationContext)

    tempPath = folder.newFolder("temp").absolutePath
  }

  @After
  fun after() {
    reset(mockPromise)
  }

  @get:Rule
  var folder = TemporaryFolder()

  @Test
  fun createDirectory_ReturnsPath() {
    val targetPath = "$tempPath/test"
    reactModule?.createDirectory(targetPath, mockPromise)

    verify(mockPromise, times(1)).resolve(targetPath)

    assertThat(File(targetPath).exists(), `is`(true))
  }

  @Test
  fun createDirectory_ReturnsError() {
    reactModule?.createDirectory("", mockPromise)

    val exception = ArgumentCaptor.forClass(Exception::class.java)

    verify(mockPromise, times(1)).reject(eq(ERROR_CREATE_DIRECTORY_FAILED), exception.capture())
    assertThat(exception.value, `is`(instanceOf(CreateDirectoryException::class.java)))
    assertThat(exception.value.message, `is`("Unable to create directory at the given path"))
  }

  @Test
  fun isDirectory_ReturnsTrue() {
    tempPath?.let { reactModule?.isDirectory(it, mockPromise) }

    verify(mockPromise, times(1)).resolve(true)
  }

  @Test
  fun isDirectory_NotExists_ReturnsFalse() {
    reactModule?.isDirectory("/not/a/directory", mockPromise)

    verify(mockPromise, times(1)).resolve(false)
  }

  @Test
  fun isDirectory_File_ReturnsFalse() {
    val tempFile = folder.newFile()

    reactModule?.isDirectory(tempFile.absolutePath, mockPromise)

    verify(mockPromise, times(1)).resolve(false)
  }

  @Test
  fun deleteDirectory_ReturnsTrue() {
    val tempFolder = folder.newFolder().absolutePath

    reactModule?.deleteDirectory(tempFolder, mockPromise)

    verify(mockPromise, times(1)).resolve(tempFolder)
    assertThat(File(tempFolder).exists(), `is`(false))
  }

  @Test
  fun deleteDirectory_NotExists_ReturnsFalse() {
    reactModule?.deleteDirectory("/not/a/directory", mockPromise)

    val exception = ArgumentCaptor.forClass(Exception::class.java)

    verify(mockPromise, times(1)).reject(eq(ERROR_DELETE_DIRECTORY_FAILED), exception.capture())
    assertThat(exception.value, `is`(instanceOf(DeleteDirectoryException::class.java)))
    assertThat(exception.value.message, `is`("Not a directory"))
  }

  @Test
  fun deleteDirectory_File_ReturnsFile() {
    val tempFile = folder.newFile().absolutePath

    reactModule?.deleteDirectory(tempFile, mockPromise)

    val exception = ArgumentCaptor.forClass(Exception::class.java)

    verify(mockPromise, times(1)).reject(eq(ERROR_DELETE_DIRECTORY_FAILED), exception.capture())
    assertThat(exception.value, `is`(instanceOf(DeleteDirectoryException::class.java)))
    assertThat(exception.value.message, `is`("Not a directory"))

    assertThat(File(tempFile).exists(), `is`(true))
  }
}
