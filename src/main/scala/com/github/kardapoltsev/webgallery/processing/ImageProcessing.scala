package com.github.kardapoltsev.webgallery.processing

import java.awt.image.BufferedImage
import java.awt.RenderingHints._
import javax.imageio.{IIOImage, ImageWriteParam, ImageIO}
import java.io.{FileInputStream, BufferedInputStream, IOException, File}
import javax.imageio.stream.{FileImageInputStream, FileImageOutputStream}
import java.awt.{Rectangle, AlphaComposite, Graphics2D, Transparency}
import com.mortennobel.imagescaling._
import java.util.{Date, Objects}
import org.apache.commons.io.FilenameUtils

sealed trait Dimensions

object RemainingSize extends Dimensions

class OptionalSize(val optWidth: Option[Int], val optHeight: Option[Int],
    val scaleType: ScaleType.Value = ScaleType.FitSource) extends Dimensions {

  def filledBySource: OptionalSize =
    OptionalSize(optWidth, optHeight, ScaleType.FillDest)

  def fitSource: OptionalSize =
    OptionalSize(optWidth, optHeight, ScaleType.FitSource)

  def predictScalingFrom(srcDimensions: SpecificSize): SpecificSize = {
    scaleType match {
      case ScaleType.FillDest if optWidth.isDefined && optHeight.isDefined =>
        SpecificSize(optWidth.get, optHeight.get, scaleType)
      case _ =>
        this withAspectRatioOf srcDimensions
    }
  }

  def withAspectRatioOf(src: SpecificSize): SpecificSize =
    scaleType match {
      case ScaleType.FitSource =>
        optWidth -> optHeight match {
          case (Some(w), Some(h)) =>
            src.aspectRatio.compareTo(w.toDouble / h) match {
              case 0 =>
                SpecificSize(w, h, scaleType)
              case 1 =>
                val newHeight = (w / src.aspectRatio).toInt
                SpecificSize(w, newHeight, scaleType)
              case -1 =>
                val newWidth = (h * src.aspectRatio).toInt
                SpecificSize(newWidth, h, scaleType)
            }
          case (Some(w), None) =>
            SpecificSize(w, (w / src.aspectRatio).toInt, scaleType)
          case (None, Some(h)) =>
            SpecificSize((h * src.aspectRatio).toInt, h, scaleType)
          case _ =>
            throw new IllegalArgumentException("(None by None).withAspectRatio")
        }

      case ScaleType.FillDest =>
        optWidth -> optHeight match {
          case (Some(w), Some(h)) =>
            SpecificSize(w, h, scaleType)
          case (Some(w), None) =>
            SpecificSize(w, (w / src.aspectRatio).toInt, scaleType)
          case (None, Some(h)) =>
            SpecificSize((h * src.aspectRatio).toInt, h, scaleType)
          case _ =>
            throw new IllegalArgumentException("(None by None).withAspectRatio")
        }
    }


  def limitedToSize(s: Int): OptionalSize = this


  override def hashCode(): Int =
    Objects.hash(optWidth, optHeight, scaleType)

  override def equals(obj: Any): Boolean = {
    obj match {
      case that: OptionalSize =>
        that.optHeight.getOrElse(-1) == optHeight.getOrElse(-1) &&
            that.optWidth.getOrElse(-1) == optWidth.getOrElse(-1)
      case _ =>
        false
    }
  }

  override def toString: String =
    optWidth.toString + " by " + optHeight.toString + ", " + scaleType.toString
}

object OptionalSize {
  def apply(width: Option[Int], height: Option[Int], scaleType: ScaleType.Value = ScaleType.FitSource): OptionalSize =
    if (width.isDefined && height.isDefined)
      SpecificSize(width.get, height.get, scaleType)
    else
      new OptionalSize(width, height, scaleType)
}

case class SpecificSize(width: Int, height: Int, override val scaleType: ScaleType.Value = ScaleType.FitSource)
    extends OptionalSize(Some(width), Some(height), scaleType) {

  val aspectRatio: Double = width.toDouble / height

  override def filledBySource: SpecificSize =
    SpecificSize(width, height, ScaleType.FillDest)

  override def fitSource: SpecificSize =
    SpecificSize(width, height, ScaleType.FitSource)

  override def limitedToSize(s: Int): SpecificSize =
    if (width * height < s)
      this
    else {
      val newHeight = Math.sqrt(s / aspectRatio).toInt
      val newWidth = (newHeight * aspectRatio).toInt
      SpecificSize(newWidth, newHeight, scaleType)
    }
}

class DimensionOptionInt(width: Option[Int]) {
  def by(height: Option[Int]): OptionalSize =
    OptionalSize(width, height)

  def by(height: Int): OptionalSize =
    OptionalSize(width, Some(height))
}

class DimensionInt(width: Int) {
  def by(height: Option[Int]): OptionalSize =
    OptionalSize(Some(width), height)

  def by(height: Int): SpecificSize =
    SpecificSize(width, height)
}


abstract class ImageImplicits {

  import scala.language.implicitConversions

  implicit def int2DimensionInt(that: Int): DimensionInt = new DimensionInt(that)

  implicit def optionInt2DimensionInt(that: Option[Int]): DimensionOptionInt = new DimensionOptionInt(that)


  def extractDimensions(path: String): Option[(SpecificSize, Int)] = extractDimensions(new File(path))


  def extractDimensions(file: File): Option[(SpecificSize, Int)] = {
    val extension = FilenameUtils.getExtension(file.getName)
    val readers = ImageIO.getImageReadersBySuffix(extension)
    if (readers.hasNext) {
      val reader = readers.next
      try {
        val stream = new FileImageInputStream(file)
        reader.setInput(stream)
        val width = reader.getWidth(reader.getMinIndex)
        val height = reader.getHeight(reader.getMinIndex)
        Some(SpecificSize(width, height) -> 1)
      } finally {
        reader.dispose()
      }
    } else None
  }
}


object Java2DImageImplicits extends ImageImplicits {
  import scala.language.implicitConversions

  implicit def bufferedImage2Java2DImage(image: BufferedImage): Java2DImage = new Java2DImage(image)
  implicit def java2DImage2BufferedImage(image: TransformableImage[BufferedImage]): BufferedImage =
    image match {
      case j2di: Java2DImage => j2di.underlying
    }

  def imageFrom(path: String): TransformableImage[BufferedImage] = imageFrom(new File(path))
  def imageFrom(file: File): TransformableImage[BufferedImage] = {
    new Java2DImage(ImageIO.read(file))
  }
}


object ScaleType extends Enumeration {

  /**
   * Scale exactly to specified size, fit source to destination bounds.
   * In case of aspect difference between source and destination, black bounds would appear.
   * Old semantics for this operation is crop = false.
   */
  val FitSource = Value("FitSource")

  /**
   * Scale exactly to specified size, fill destination by source.
   * In case of aspect difference between source and destination, source center would be cropped.
   * Old semantics for this operation is crop = true.
   */
  val FillDest = Value("FillDest")
}


trait TransformableImage[T] {

  def dimensions: SpecificSize

  def rotatedBy(angle: Int): TransformableImage[T] =
    apply(rotate(angle))

  def scaledTo(dimen: OptionalSize): TransformableImage[T] =
    apply(scale(dimen))

  def croppedFrom(point: (Int, Int)): TransformableImage[T] =
    apply(crop(point._1, point._2, RemainingSize))

  def croppedToSize(dimen: SpecificSize): TransformableImage[T] =
    apply(crop(0, 0, dimen))

  def croppedToRect(point: (Int, Int), dimen: SpecificSize): TransformableImage[T] = {
    val x = (point._1 max 0) min (dimensions.width - 1)
    val y = (point._2 max 0) min (dimensions.height - 1)
    val w = (dimensions.width - x) min dimen.width
    val h = (dimensions.height - y) min dimen.height
    apply(crop(x, y, SpecificSize(w, h, dimen.scaleType)))
  }

  def withOverlay(image: T): TransformableImage[T] =
    apply(alphaBlend(image))

  def withOverlayAndKey(image: T, key: Int = 0xF9F9F9): TransformableImage[T] =
    apply(alphaKeyMaskBlend(image, key))

  def writeTo(path: String)

  def writeTo(file: File)

  protected def rotate(angle: Int)(src: T): T
  protected def scale(dimen: Dimensions)(src: T): T
  protected def crop(x: Int, y: Int, dimen: Dimensions)(src: T): T
  protected def alphaBlend(image: T)(src: T): T
  protected def alphaKeyMaskBlend(image: T, key: Int)(src: T): T

  protected def apply(op: T => T): TransformableImage[T]
}


trait Java2DImageOps {

  def dimensions: SpecificSize

  private def createDestImage(src: BufferedImage, width: Int, height: Int): BufferedImage = {
    new BufferedImage(width, height,
      if (src.getTransparency == Transparency.OPAQUE) BufferedImage.TYPE_INT_RGB
      else BufferedImage.TYPE_INT_ARGB
    )
  }


  protected def rotate(angle: Int)(src: BufferedImage): BufferedImage = {
    val modAngle = angle % 360
    if (modAngle == 0)
      src
    else {
      val w = src.getWidth
      val h = src.getHeight
      val dest = if (angle == 180) createDestImage(src, w, h) else createDestImage(src, h, w)

      if (angle == 270)
        for (x <- 0 until src.getWidth; y <- 0 until src.getHeight)
          dest.setRGB(h - y - 1, x, src.getRGB(x, y))
      else if (angle == 90)
        for (x <- 0 until src.getWidth; y <- 0 until src.getHeight)
          dest.setRGB(y, w - x - 1, src.getRGB(x, y))
      else if (angle == 180)
        for (x <- 0 until src.getWidth; y <- 0 until src.getHeight)
          dest.setRGB(w - x - 1, h - y - 1, src.getRGB(x, y))
      else
        throw new IllegalArgumentException
      dest
    }
  }


  protected def crop(x: Int, y: Int, dimen: Dimensions)(src: BufferedImage): BufferedImage = {
    val (width, height) = dimen match {
      case RemainingSize => (src.getWidth - x, src.getHeight - y)
      case SpecificSize(w, h, _) => (w, h)
      case os: OptionalSize => {
        os.optWidth -> os.optHeight match {
          case (Some(w), Some(h)) => (w, h)
          case (Some(w), None) => (w, src.getHeight - y)
          case (None, Some(h)) => (src.getWidth - x, h)
          case (None, None) => (src.getWidth - x, src.getHeight - y)
        }
      }
    }

    require(x >= 0, "x < 0")
    require(y >= 0, "y < 0")
    require(x + width <= src.getWidth, "x + width > src.getWidth")
    require(y + height <= src.getHeight, "y + height > src.getHeight")

    val dest = createDestImage(src, width, height)
    dest.getGraphics.drawImage(src, 0, 0, width, height, x, y, x + width, y + height, null)
    dest
  }


  protected def scale(dimen: Dimensions)(src: BufferedImage): BufferedImage = {
    //    val DOWNSCALE_STEP_COUNT = 5

    dimen match {
      case requestedDimen @ SpecificSize(width, height, scaleType) =>

        val newDimen = requestedDimen withAspectRatioOf dimensions

        scaleType match {
          case ScaleType.FitSource =>
            val resampleOp = new ResampleOp(newDimen.width, newDimen.height)
            resampleOp.setFilter(ResampleFilters.getLanczos3Filter)
            resampleOp.filter(src, null)


          case ScaleType.FillDest =>
            if ((dimensions.aspectRatio / newDimen.aspectRatio max
                newDimen.aspectRatio / dimensions.aspectRatio) < 1.02) {
              val resampleOp = new ResampleOp(newDimen.width, newDimen.height)
              resampleOp.setFilter(ResampleFilters.getLanczos3Filter)
              resampleOp.filter(src, null)
            } else {
              val newDimen2 = dimensions withAspectRatioOf requestedDimen
              val dest = createDestImage(src, newDimen.width, newDimen.height)
              val destGraphics = dest.createGraphics
              destGraphics.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC)
              destGraphics.drawImage(src, 0, 0, width, height,
                (src.getWidth - newDimen2.width) / 2, (src.getHeight - newDimen2.height) / 2,
                (src.getWidth + newDimen2.width) / 2, (src.getHeight + newDimen2.height) / 2, null)
              destGraphics.dispose()
              dest
            }
        }


      case os: OptionalSize =>
        if (os.optWidth.isDefined && os.optHeight.isDefined)
          scale(SpecificSize(os.optWidth.get, os.optHeight.get, os.scaleType))(src)
        else {
          val newDimen = os withAspectRatioOf SpecificSize(src.getWidth, src.getHeight)
          scale(SpecificSize(newDimen.optWidth.get, newDimen.optHeight.get, newDimen.scaleType))(src)
        }

      case _ =>
        src

    }
  }


  protected def alphaBlend(overlay: BufferedImage)(src: BufferedImage): BufferedImage = {
    if (overlay.getAlphaRaster != null) {
      val base =
        if (src.getWidth == overlay.getWidth && src.getHeight == overlay.getHeight)
          src
        else {
          scale(SpecificSize(overlay.getWidth, overlay.getHeight) filledBySource)(src)
        }

      val dest = createDestImage(overlay, base.getWidth, base.getHeight)
      val graphics = dest.getGraphics.asInstanceOf[Graphics2D]

      graphics.drawImage(base, 0, 0, null)
      graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER))
      graphics.drawImage(overlay, 0, 0, null)

      dest
    } else
      src
  }


  protected def alphaKeyMaskBlend(overlay: BufferedImage, key: Int)(src: BufferedImage): BufferedImage = {
    if (overlay.getAlphaRaster != null) {
      val width = overlay.getWidth
      val height = overlay.getHeight

      val base =
        if (src.getWidth == width && src.getHeight == height)
          src
        else {
          scale(SpecificSize(width, height) filledBySource)(src)
        }

      val dest = overlay //new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
      val destPixels = new Array[Int](width)

      for (y <- 0 until base.getHeight) {
        val overlayPixels = overlay.getRGB(0, y, width, 1, new Array[Int](width), 0, width)
        val alphas = overlay.getAlphaRaster.getPixels(0, y, width, 1, new Array[Int](width))

        for (x <- 0 until base.getWidth) {
          val baseRgb = base.getRGB(x, y) & 0xFFFFFF
          val overlayRgb = overlayPixels(x) & 0xFFFFFF
          val overlayAlpha = alphas(x)
          val baseAlpha = 255 - overlayAlpha

          if ((0xFFFFFF & overlayRgb) == key) {
            destPixels(x) = baseAlpha << 24 | baseRgb
          } else {
            val newR = ((baseRgb & 0x0000FF) * baseAlpha + (overlayRgb & 0x0000FF) * overlayAlpha) / 255
            val newG = ((baseRgb & 0x00FF00) * baseAlpha + (overlayRgb & 0x00FF00) * overlayAlpha) / 255
            val newB = ((baseRgb & 0xFF0000) * baseAlpha + (overlayRgb & 0xFF0000) * overlayAlpha) / 255
            destPixels(x) = 0xFF000000 | 0xFF0000 & newB | 0x00FF00 & newG | newR
          }
        }
        dest.setRGB(0, y, width, 1, destPixels, 0, width)
      }

      dest
    } else
      src
  }
}


/**
 * Wrapper class for BufferedImage providing DSL-like syntax for image transformations.
 * @param underlying wrapped image
 */
class Java2DImage(val underlying: BufferedImage) extends TransformableImage[BufferedImage] with Java2DImageOps {

  val dimensions: SpecificSize = SpecificSize(underlying.getWidth, underlying.getHeight)

  protected def apply(op: BufferedImage => BufferedImage): Java2DImage =
    new Java2DImage(op(underlying))


  def writeTo(path: String) {
    writeTo(new File(path))
  }


  // TODO workaround for bug with writing 4-channel images to jpeg
  def writeTo(file: File) {
    // Possible workaround for possible ImageIO bug with stream handling {
    val extension = FilenameUtils.getExtension(file.getAbsolutePath)
    val writers = ImageIO.getImageWritersByFormatName(extension)
    val imageWriter = writers.next
    val iwp = imageWriter.getDefaultWriteParam
    if (extension.toLowerCase == "jpg") {
      iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
      iwp.setCompressionQuality(0.9f)
    }
    val out = new FileImageOutputStream(file)
    try {
      imageWriter.setOutput(out)
      imageWriter.write(null, new IIOImage(underlying, null, null), iwp)
      out.flush()
    } catch {
      case e: IOException =>
        imageWriter.abort()
        throw e
    } finally {
      out.close()
      imageWriter.dispose()
    }
  }


  override def equals(obj: Any): Boolean = obj match {
    case that: Java2DImage =>
      this.dimensions == that.dimensions && (
          for {
            x <- 0 until this.underlying.getWidth
            y <- 0 until this.underlying.getHeight
          } yield (x, y)
          ).forall {
        case (x, y) => (this.underlying.getRGB(x, y) & 0xFFFFFF) == (that.underlying.getRGB(x, y) & 0xFFFFFF)
      }
    case _ =>
      false
  }
}
