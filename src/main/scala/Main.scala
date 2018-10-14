import io.jorand.scalajs.Tooltip
import io.jorand.scalajs.d3graph.{D3Graph, LinkD3Json, NodeD3}

import scala.scalajs.js

object Main {

  def main(args: Array[String]): Unit = {
    // Define a SVG canvas to host the graph
    val graph = D3Graph("svgDiv", 1000, 900, Tooltip("svg-tooltip", "auto"))

    // Some data representing the graph
    val json: String =
      """{
        |  "nodes": [
        |    {
        |      "name": "smartLab box!",
        |      "id": "smartLab_box",
        |      "value": 20
        |    },
        |    {
        |      "name": "Temperature 1",
        |      "id": "temp1",
        |      "value": 16
        |    },
        |    {
        |      "name": "Temperature 2",
        |      "id": "temp2",
        |      "value": 14
        |    },
        |    {
        |      "name": "Temperature 3",
        |      "id": "temp3",
        |      "value": 23
        |    },
        |    {
        |      "name": "Temperature 4",
        |      "id": "temp4",
        |      "value": 26
        |    },
        |    {
        |      "name": "Temperature 5",
        |      "id": "temp5",
        |      "value": 30
        |    },
        |    {
        |      "name": "Temperature 6",
        |      "id": "temp6",
        |      "value": 10
        |    }
        |    ],
        |    "links": [
        |    {
        |      "source": "smartLab_box",
        |      "target": "temp1"
        |    },
        |    {
        |      "source": "smartLab_box",
        |      "target": "temp2"
        |    },
        |    {
        |      "source": "smartLab_box",
        |      "target": "temp3"
        |    },
        |    {
        |      "source": "smartLab_box",
        |      "target": "temp4"
        |    },
        |    {
        |      "source": "smartLab_box",
        |      "target": "temp5"
        |    },
        |    {
        |      "source": "smartLab_box",
        |      "target": "temp6"
        |    }
        |    ]
        |}""".stripMargin

    val json2Level: String =
      """{
        |  "nodes": [
        |    {
        |      "name": "smartLab box",
        |      "id": "smartLab_box",
        |      "value": 21,
        |      "tooltip": "<p>smartLab</p>",
        |      "nodeText": "smartLab",
        |      "circleClass": "rootNode"
        |    }
        |    ],
        |    "links": [
        |    {
        |      "source": "smartLab_box",
        |      "target": "temp1"
        |    },
        |    {
        |      "source": "smartLab_box",
        |      "target": "temp2"
        |    },
        |    {
        |      "source": "smartLab_box",
        |      "target": "temp3"
        |    },
        |    {
        |      "source": "smartLab_box",
        |      "target": "temp4"
        |    },
        |    {
        |      "source": "smartLab_box",
        |      "target": "temp5"
        |    },
        |    {
        |      "source": "smartLab_box",
        |      "target": "temp6"
        |    }
        |    ,
        |    {
        |      "source": "temp6",
        |      "target": "state_temp6"
        |    }
        |    ]
        |}""".stripMargin


    // Create an iterator with a delay growing by increments on each iteration
    val initialTimer = 3000
    val increments = 2000
    val it = (0 to 20).map(initialTimer + _ * increments).toIterator

    // Create and show the initial graph
    createGrappe("", (_) => "online")

    // Create a new grape with another name
    js.timers.setTimeout(it.next()) {
      createGrappe(",1", (i) => if (i % 3 == 0) "online" else "offline")
    }

    // Link the two root
    js.timers.setTimeout(it.next()) {
      graph.addLink(LinkD3Json("newroot,1", "newroot"))
    }

    // Remove the node new1
    js.timers.setTimeout(it.next()) {
      graph.removeNode("new-1")
    }
//
//    js.timers.setTimeout(it.next()) {
//      graph.removeAllLinks()
//    }
//
//    js.timers.setTimeout(it.next()) {
//      graph.removeAllNodes()
//    }
//
//    js.timers.setTimeout(it.next()) {
//      createGrappe("--", (i) => if (i % 4 == 0) "online" else "offline")
//    }


    def shape(i: Int): String = i match {
      case t if t % 3 == 0 => "rect"
      case _ => "circle"
    }

    def createGrappe(prefix: String, status: (Int) => String): Unit = {
      graph.addNode(NodeD3(s"newroot$prefix", "the new Node", 23, "<p>tooltip</p>", "root", "rootNode"))
      for (i <- 1 to 10) {
        graph.addNode(NodeD3(s"new-$i$prefix", s"the new Node $i$prefix", i, s"<p>tooltip $i$prefix</p>", s"node-$i$prefix", status(i), shape(i)))
        graph.addLink(LinkD3Json(s"new-$i$prefix", s"newroot$prefix"))
      }
    }

  }
}
