import io.jorand.scalajs.Tooltip
import io.jorand.scalajs.d3graph.{D3Graph, LinkD3Json, NodeD3}

import scala.scalajs.js

object Main {

  def main(args: Array[String]): Unit = {
    // Define a SVG canvas to host the graph
    val graph = D3Graph("svgDiv", 1000, 900, Tooltip("svg-tooltip", "auto"))

    // Create an iterator with a delay growing by increments on each iteration
    val initialTimer = 3000
    val increments = 2000
    val it = (0 to 20).map(initialTimer + _ * increments).toIterator

    // Create and show the initial graph
    createGrape("", _ => "online")

    // Create the same grape but with different status, should update the node
    js.timers.setTimeout(it.next()) {
      createGrape("", i => if (i % 2 == 0) "online" else "offline")
    }

    // Create a new grape with another name
    js.timers.setTimeout(it.next()) {
      createGrape(",1", i => if (i % 3 == 0) "online" else "offline")
    }

    // Link the two root
    js.timers.setTimeout(it.next()) {
      graph.addLink(LinkD3Json("newroot,1", "newroot"))
    }

    // Remove some nodes
    js.timers.setTimeout(it.next()) {
      graph.removeNode("new-1")
      graph.removeNode("new-2")
      graph.removeNode("new-3")
      graph.removeNode("new-4")
      graph.removeNode("new-5")
    }

    // Remove all links
    js.timers.setTimeout(it.next()) {
      graph.removeAllLinks()
    }

    // Remove all the nodes
    js.timers.setTimeout(it.next()) {
      graph.removeAllNodes()
    }

    // Recreate a grape to inspect the structure in the browser.
    js.timers.setTimeout(it.next()) {
      createGrape("--", i => if (i % 4 == 0) "online" else "offline")
    }


    def shape(i: Int): D3Graph.Shape = i match {
      case t if t % 3 == 0 => D3Graph.Rect
      case _ => D3Graph.Circle
    }


    def createGrape(prefix: String, status: Int => String): Unit = {
      graph.addNode(NodeD3(s"newroot$prefix", "the new Node", 23, "<p>tooltip</p>", "root", "rootNode"))
      for (i <- 1 to 10) {
        graph.addNode(NodeD3(s"new-$i$prefix", s"the new Node $i$prefix", i, s"<p>tooltip $i$prefix</p>", s"node-$i$prefix", status(i), shape(i)))
        graph.addLink(LinkD3Json(s"new-$i$prefix", s"newroot$prefix"))
      }
    }

  }
}
