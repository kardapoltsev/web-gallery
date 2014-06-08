package com.github.kardapoltsev.webgallery.util


import org.scalatest.{Matchers, FlatSpec}
import java.io.File



/**
 * Created by alexey on 6/8/14.
 */
class FilesUtilSpec extends FlatSpec with Matchers {

  behavior of "FilesUtil"

  it should "create new filename" in {
    val name = "test.jpg"
    val newName = FilesUtil.newFilename(name)
    newName should not be(name)
    newName.endsWith(".jpg") should be(true)
  }

  it should "move files" in {
    val file = File.createTempFile("file", "ext")
    val dest = file.getAbsolutePath + "2"
    FilesUtil.moveFile(file, dest)
    val movedFile = new File(dest)
    movedFile.exists() should be(true)
  }

}
