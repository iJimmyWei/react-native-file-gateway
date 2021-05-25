package com.reactnativefilegateway

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.reactnativefilegateway.FileGatewayModule.Errors.Companion.ERROR_CREATE_DIRECTORY_FAILED
import com.reactnativefilegateway.exceptions.CreateDirectoryException
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
  }

  @Test
  fun createDirectory_ReturnsError() {
    reactModule?.createDirectory("", mockPromise)

    val exception = ArgumentCaptor.forClass(Exception::class.java)

    verify(mockPromise, times(1)).reject(eq(ERROR_CREATE_DIRECTORY_FAILED), exception.capture())
    assertThat(exception.value, `is`(instanceOf(CreateDirectoryException::class.java)))
    assertThat(exception.value.message, `is`("Unable to create directory at the given path"))
  }
}
