package io.jorand.scalajs

import org.querki.jquery.$
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, window}

/*
  UI Class
 */
case class Tooltip(tooltipId: String, width: String) {
  $("body").append(s"<div class='tooltip' id='$tooltipId'></div>")

  $(s"#$tooltipId").css("width", "auto")

  hideTooltip()

  def hideTooltip() {
    $(s"#$tooltipId").hide()
  }

  def showTooltip(content: String, event: dom.Event) {
    $("#" + tooltipId).html(content)
    $("#" + tooltipId).show()

    updatePosition(event)
  }

  def updatePosition(event: dom.Event) {
    val ttid = "#" + tooltipId
    val xOffset = 20
    val yOffset = 10

    val toolTipW = $(ttid).width()
    val toolTipeH = $(ttid).height()
    val windowY = $(window).scrollTop()
    val windowX = $(window).scrollLeft()
    val curX = event.asInstanceOf[MouseEvent].pageX
    val curY = event.asInstanceOf[MouseEvent].pageY
    var ttleft = if (curX < $(window).width() / 2) curX - toolTipW - xOffset * 2 else curX + xOffset
    if (ttleft < windowX + xOffset) {
      ttleft = windowX + xOffset
    }
    var tttop = if ((curY - windowY + yOffset * 2 + toolTipeH) > $(window).height()) curY - toolTipeH - yOffset * 2 else curY + yOffset
    if (tttop < windowY + yOffset) {
      tttop = curY + yOffset
    }
    $(ttid).css("top", tttop + "px").css("left", ttleft + "px")
  }
}
