package com.github.kardapoltsev.webgallery.http.marshalling

import com.github.kardapoltsev.webgallery.StatsManager.GetStats

/**
 * Created by alexey on 8/26/14.
 */
trait StatsMarshalling { this: WebGalleryMarshalling =>

  implicit val getStatsUM = unmarshallerFrom {
    () => GetStats()
  }

}
