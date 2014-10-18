package com.github.kardapoltsev.webgallery.util


import org.apache.commons.io.FilenameUtils
import java.util.UUID
import java.io.File
import java.nio.file.Path



/**
 * Created by alexey on 6/8/14.
 */
object FilesUtil {
  import java.nio.file.{StandardCopyOption, Files}

  def newFilename(old: String): String = {
    val ext = FilenameUtils.getExtension(old)
    UUID.randomUUID().toString + "." + ext
  }


  def moveFile(source: File, destination: String): Path = {
    Files.move(source.toPath, new File(destination).toPath, StandardCopyOption.REPLACE_EXISTING)
  }


  def exists(filename: String): Boolean = {
    Files.exists(new File(filename).toPath)
  }


  def rm(filename: String): Unit = {
    Files.delete(new File(filename).toPath)
  }

}
