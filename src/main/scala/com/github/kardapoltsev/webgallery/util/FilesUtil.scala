package com.github.kardapoltsev.webgallery.util


import org.apache.commons.io.FilenameUtils
import java.util.UUID
import java.io.File
import java.nio.file.Path



/**
 * Created by alexey on 6/8/14.
 */
object FilesUtil {
  def newFilename(old: String): String = {
    val ext = FilenameUtils.getExtension(old)
    UUID.randomUUID().toString + "." + ext
  }


  def moveFile(source: File, destination: String): Path = {
    import java.nio.file.{StandardCopyOption, Files}
    Files.move(source.toPath, new File(destination).toPath, StandardCopyOption.REPLACE_EXISTING)
  }

}
