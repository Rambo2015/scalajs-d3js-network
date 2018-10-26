# io.jorand.scalajs-d3js-network
This library wrap the org.singlespaced.scalajs-d3 facade to quickly create a network graph.

# Getting started

```

    val graph = D3Graph("svgDiv", 1000, 900, Tooltip("svg-tooltip", "auto"))

    graph.addNode(NodeD3("root","Root",10,nodeText = "JVM"))
    graph.addNode(NodeD3("scala","Scala",7, nodeText = "Scala"))
    graph.addNode(NodeD3("java","Java",1, nodeText = "Java"))
    graph.addLink(LinkD3Json("scala", "root"))
    graph.addLink(LinkD3Json("java", "root"))
    
```


will produce

![img](/doc/simpleGraph.png)