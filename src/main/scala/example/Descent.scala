/*
 * Copyright (c) 2020 by Andrew Charneski.
 *
 * The author licenses this file to you under the
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package example

import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.HTMLImageElement

import scala.concurrent.Promise
import scala.concurrent.duration._
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Descent")
class Descent {

  @JSExport val stepZoom = 0.5
  @JSExport val width = 800
  @JSExport val height = 840
  @JSExport val innerBorder = 1.0 / 8
  @JSExport val preMagnify = 10.0 / 8.0
  @JSExport val frameDuration = (1 second).toMillis

  @JSExport
  def animate(canvas: html.Canvas, urls: String*): Unit = {
    val ctx = canvas.getContext("2d")
      .asInstanceOf[dom.CanvasRenderingContext2D]

    val frames = urls.map(src => {
      val image = dom.document.createElement(s"img").asInstanceOf[HTMLImageElement]
      image.src = src
      println(s"Loading $src")
      val promisedElement = Promise[HTMLImageElement]()
      image.onabort = (e: dom.UIEvent) => {
        println(s"Error Loading $src - ${e.`type`}: ${e.toString}")
        promisedElement.failure(new RuntimeException(e.`type`))
      }
      image.onload = (e: dom.Event) => {
        println("Loaded " + src)
        promisedElement.success(image)
      }
      promisedElement.future
    })

    val start = System.currentTimeMillis()
    val cycleDuration = frameDuration * frames.size
    val zoomFactor = 1.0 / (1.0 + stepZoom)

    def run() = {
      val animatedTime = System.currentTimeMillis() - start
      val cycleTime = (animatedTime % cycleDuration).floor.toLong
      val frame = (cycleTime / frameDuration).floor.toInt
      val frameTime = (cycleTime % frameDuration)
      val outerImage = frames(frame)
      var frameZoom = Math.exp(-(frameTime.toDouble / frameDuration) * Math.log(zoomFactor))
      frameZoom = frameZoom * preMagnify
      if (outerImage.isCompleted) {
        ctx.drawImage(outerImage.value.get.get,
          (width - width / frameZoom) / 2, (height - height / frameZoom) / 2,
          width / frameZoom, height / frameZoom,
          0, 0,
          width, height)
      }
      frameZoom = frameZoom * zoomFactor
      val innerIndex = (frame + 1) % frames.size
      val innerImage = frames(innerIndex)
      if (innerImage.isCompleted) {
        val innerSize = 1 - (2 * innerBorder)
        ctx.drawImage(innerImage.value.get.get,
          width * innerBorder, height * innerBorder,
          width * innerSize, height * innerSize,
          width / 2 - width * innerSize * frameZoom / 2, height / 2 - height * innerSize * frameZoom / 2,
          width * frameZoom * innerSize, height * frameZoom * innerSize)
      }
    }

    dom.window.setInterval(() => run, 50)
  }
}
