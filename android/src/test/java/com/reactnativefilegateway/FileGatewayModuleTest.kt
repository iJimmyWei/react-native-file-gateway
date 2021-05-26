package com.reactnativefilegateway

import android.app.Application

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.facebook.react.bridge.*
import com.reactnativefilegateway.FileGatewayModule.Errors.Companion.ERROR_CREATE_DIRECTORY_FAILED
import com.reactnativefilegateway.FileGatewayModule.Errors.Companion.ERROR_DELETE_DIRECTORY_FAILED
import com.reactnativefilegateway.exceptions.CreateDirectoryException
import com.reactnativefilegateway.exceptions.DeleteDirectoryException
import com.reactnativefilegateway.exceptions.ListDirectoryException

import org.assertj.core.api.Assertions.assertThat

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.eq
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileWriter


/**
 * Unit tests for the FileGatewayModule.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P], application = Application::class, manifest = Config.NONE)
class FileGatewayModuleTest {
  private var reactModule: FileGatewayModule? = null

  private val mockPromise: Promise = mock(Promise::class.java)
  private var tempPath: String? = null

  private var mockedArguments: MockedStatic<Arguments>? = null;

  @Before
  fun beforeAll() {
    val applicationContext = ApplicationProvider.getApplicationContext<android.content.Context>()
    val reactApplicationContext = ReactApplicationContext(applicationContext)

    reactModule = FileGatewayModule(reactApplicationContext)

    tempPath = folder.newFolder("temp").absolutePath

    this.mockedArguments = Mockito.mockStatic(Arguments::class.java)
    `when`(Arguments.createArray())
      .thenAnswer {
        JavaOnlyArray()
      }
  }

  @After
  fun after() {
    reset(mockPromise)

    this.mockedArguments?.close()
  }

  @get:Rule
  val folder = TemporaryFolder()

  @Test
  fun createDirectory_ReturnsPath() {
    val targetPath = "$tempPath/test"
    reactModule?.createDirectory(targetPath, mockPromise)

    verify(mockPromise, times(1)).resolve(targetPath)

    assertThat(File(targetPath).exists()).isEqualTo(true)
  }

  @Test
  fun createDirectory_ReturnsError() {
    reactModule?.createDirectory("", mockPromise)

    val exception = ArgumentCaptor.forClass(Exception::class.java)

    verify(mockPromise, times(1)).reject(eq(ERROR_CREATE_DIRECTORY_FAILED), exception.capture())
    assertThat(exception.value).isInstanceOf(CreateDirectoryException::class.java)
    assertThat(exception.value.message).isEqualTo("Unable to create directory at the given path")
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
    assertThat(File(tempFolder).exists()).isEqualTo(false)
  }

  @Test
  fun deleteDirectory_NotExists_ReturnsFalse() {
    reactModule?.deleteDirectory("/not/a/directory", mockPromise)

    val exception = ArgumentCaptor.forClass(Exception::class.java)

    verify(mockPromise, times(1)).reject(eq(ERROR_DELETE_DIRECTORY_FAILED), exception.capture())
    assertThat(exception.value).isInstanceOf(DeleteDirectoryException::class.java)
    assertThat(exception.value.message).isEqualTo("Not a directory")
  }

  @Test
  fun deleteDirectory_File_ReturnsFile() {
    val tempFile = folder.newFile().absolutePath

    reactModule?.deleteDirectory(tempFile, mockPromise)

    val exception = ArgumentCaptor.forClass(Exception::class.java)

    verify(mockPromise, times(1)).reject(eq(ERROR_DELETE_DIRECTORY_FAILED), exception.capture())
    assertThat(exception.value).isInstanceOf(DeleteDirectoryException::class.java)
    assertThat(exception.value.message).isEqualTo("Not a directory")

    assertThat(File(tempFile).exists()).isEqualTo(true)
  }

  @Test
  fun listFiles_ReturnsFiles() {
    val tempFolder = folder.newFolder().absolutePath

    FileWriter(File(tempFolder, "test1"))
    FileWriter(File(tempFolder, "test2"))

    reactModule?.listFiles(tempFolder, false, mockPromise)

    val resolvedArray = ArgumentCaptor.forClass(JavaOnlyArray::class.java)
    verify(mockPromise, times(1)).resolve(resolvedArray.capture())

    assertThat(resolvedArray.value).isInstanceOf(JavaOnlyArray::class.java)
    assertThat(resolvedArray.value.size()).isEqualTo(2)
    assertThat(resolvedArray.value.toArrayList()).containsExactlyInAnyOrder("test1", "test2")
  }

  @Test
  fun listFiles_ReturnsNoRecursiveFiles() {
    val tempFolder = folder.newFolder().absolutePath

    FileWriter(File(tempFolder, "test1"))
    FileWriter(File(tempFolder, "test2"))

    val childTempPath = "${tempFolder}/testdir";
    File(childTempPath).mkdir()

    FileWriter(File(childTempPath, "test3"))
    FileWriter(File(childTempPath, "test4"))

    reactModule?.listFiles(tempFolder, false, mockPromise)

    val resolvedArray = ArgumentCaptor.forClass(JavaOnlyArray::class.java)
    verify(mockPromise, times(1)).resolve(resolvedArray.capture())

    assertThat(resolvedArray.value).isInstanceOf(JavaOnlyArray::class.java)
    assertThat(resolvedArray.value.size()).isEqualTo(2)
    assertThat(resolvedArray.value.toArrayList()).containsExactlyInAnyOrder("test1", "test2")
  }

  @Test
  fun listFiles_NotDirectory_ReturnsError() {
    reactModule?.listFiles("no_directory", false, mockPromise)

    val exception = ArgumentCaptor.forClass(Exception::class.java)

    verify(mockPromise, times(1)).reject(eq(FileGatewayModule.Errors.ERROR_LIST_DIRECTORY_FAILED), exception.capture())
    assertThat(exception.value).isInstanceOf(ListDirectoryException::class.java)
    assertThat(exception.value.message).isEqualTo("Not a directory")
  }

  @Test
  fun listFiles_ReturnsNoFiles() {
    val tempFolder = folder.newFolder().absolutePath

    reactModule?.listFiles(tempFolder, false, mockPromise)

    val resolvedArray = ArgumentCaptor.forClass(JavaOnlyArray::class.java)
    verify(mockPromise, times(1)).resolve(resolvedArray.capture())

    assertThat(resolvedArray.value).isInstanceOf(JavaOnlyArray::class.java)
    assertThat(resolvedArray.value.size()).isEqualTo(0)
  }

  @Test
  fun listFiles_Recursive_ReturnsFiles() {
    val tempFolder = folder.newFolder().absolutePath

    FileWriter(File(tempFolder, "test1"))
    FileWriter(File(tempFolder, "test2"))

    val childTempPath = "${tempFolder}/testdir";
    File(childTempPath).mkdir()

    FileWriter(File(childTempPath, "test3"))
    FileWriter(File(childTempPath, "test4"))

    reactModule?.listFiles(tempFolder, true, mockPromise)

    val resolvedArray = ArgumentCaptor.forClass(JavaOnlyArray::class.java)
    verify(mockPromise, times(1)).resolve(resolvedArray.capture())

    assertThat(resolvedArray.value).isInstanceOf(JavaOnlyArray::class.java)
    assertThat(resolvedArray.value.size()).isEqualTo(4)
    assertThat(resolvedArray.value.toArrayList()).containsExactlyInAnyOrder("test1", "test2", "test3", "test4")
  }
}
