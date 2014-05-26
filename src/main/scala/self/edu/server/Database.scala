package self.edu.server

import akka.actor.{Actor, ActorLogging}
import java.io.File
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import java.util.Date
import java.text.SimpleDateFormat
import spray.json.DefaultJsonProtocol

/**
 * Created by alexey on 5/26/14.
 */
class Database extends Actor with ActorLogging {
  import Database._

  val files = readFiles()

  override def preStart(): Unit = {
//    log.debug(files.toString())
  }

  private def readFiles(): Seq[Image] = {
    val dir = new File(Configs.OriginalsDir)
    dir.listFiles().filter(_.isFile).map{ f =>
      val meta = ImageMetadataReader.readMetadata(f)
      val directory = meta.getDirectory(classOf[ExifSubIFDDirectory])
      val date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
      val album = new SimpleDateFormat("yyyy-MM-dd").format(date)
      Image(f.getName, album)
    }.toSeq
  }


  def receive: Receive = {
    case GetByAlbum(album) => sender() ! files.filter(_.album == album)
    case GetAlbums => sender() ! files.map(_.album).distinct
  }
}


case class Image(filename: String, album: String)
object Image extends  DefaultJsonProtocol {
  implicit val _ = jsonFormat(Image.apply, "filename", "album")
}


object Database {
  case class GetByAlbum(album: String)
  case object GetAlbums
}