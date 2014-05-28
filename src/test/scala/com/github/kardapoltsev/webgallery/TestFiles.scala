package com.github.kardapoltsev.webgallery

import java.io.File
import com.github.kardapoltsev.webgallery.db.Metadata
import java.util.Date



/**
 * Created by alexey on 5/27/14.
 */
trait TestFiles {
  protected val dsc2845 = new File(getClass.getResource("/DSC_2845.jpg").toURI)
  protected val dsc2845Metadata = Metadata("NIKON D7000", new Date(1399710030000L))
  
  protected val dummyTextFile = new File(getClass.getResource("/dummy_text.jpg").toURI)
}
