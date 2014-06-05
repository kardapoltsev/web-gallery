package com.github.kardapoltsev.webgallery

import java.io.File
import com.github.kardapoltsev.webgallery.db.{Image, Metadata}
import java.util.{UUID, Date}



/**
 * Created by alexey on 5/27/14.
 */
trait TestFiles {
  protected val dsc2845 = new File(getClass.getResource("/DSC_2845.jpg").toURI)
  protected val dsc2845Metadata = Metadata("NIKON D7000", new Date(1399710030000L))
  protected val dsc2845Image = Image("DSC_2845.jpg", Seq.empty, dsc2845Metadata, UUID.randomUUID().toString)
  
  protected val dummyTextFile = new File(getClass.getResource("/dummy_text.jpg").toURI)
}
