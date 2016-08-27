# RXML-Parser

 [ ![Download](https://api.bintray.com/packages/albertosh/maven/com.github.albertosh.rxml-parser/images/download.svg) ](https://bintray.com/albertosh/maven/com.github.albertosh.rxml-parser/_latestVersion)
 
Parsing XML gracefully with RxJava!

## Overview

We all hate XML parsing (at least I do)

We all love RxJava (at least I do)

**RXML-Parser** provides an easy way of parsing XML files through an Rx API

---

## Features

* Parse of Java built-in [Document](https://docs.oracle.com/javase/7/docs/api/org/w3c/dom/Document.html) and [Node](https://docs.oracle.com/javase/7/docs/api/org/w3c/dom/Node.html) interface
* Easy API
* No need of instantiating new objects

--- 

## How to start

You'll need some XML content. You can find a couple of them in the sample project (under `src/test/resources/xml`)

When you have your file you just have to convert it to a `Document`. You can use the following snippet for that:

    
```java
private static Document loadDocument(String path) throws Exception {
    File file = new File(path);
    DocumentBuilderFactory dbFactory
            = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    return dBuilder.parse(file);
}
```

Once you have your `Document` decide which nodes you want to access. For example in the `simpleNote` test I want to get all nodes (using `RXMLParser.WILD_CARD`) that are under `note` node so I will use:

```java
Observable<Node> obsNodes = RXMLParser.getNodes(document, "note", RXMLParser.WILD_CARD)
```

and that will give me an `Observable` that emits the requested nodes. At that point you have a regular `Observable` so you can use all `RxJava` stuff! For example, if I wanted to get all nodes that fulfills a condition I could use

```java
Observable<Node> euNodes = RXMLParser.getNodes(document, "CATALOG", "CD")
        .filter(cdNode -> RXMLParser.getNodes(cdNode, "COUNTRY")
                .map(Node::getTextContent)
                .map(country -> country.equals("EU"))
                .toBlocking()
                .first())
```

You can find more examples under sample project

---

##License
 
    The MIT License (MIT)
    Copyright (c) 2016 Alberto Sanz

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
