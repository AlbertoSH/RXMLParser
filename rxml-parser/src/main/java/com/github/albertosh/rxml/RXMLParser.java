package com.github.albertosh.rxml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rx.Observable;
import rx.schedulers.Schedulers;

public class RXMLParser {

    public static final String WILD_CARD = "*";

    private RXMLParser() {}

    private static Observable<Node> nodeToChildrenObservable(Node n) {
        return Observable.create(subscriber -> {
            NodeList children = n.getChildNodes();
            int length = children.getLength();
            for (int i = 0; i < length; i++) {
                if (subscriber.isUnsubscribed())
                    return;
                Node node = children.item(i);
                subscriber.onNext(node);
            }
            subscriber.onCompleted();
        });
    }

    public static Observable<Node> getNodes(Node node, String... nodeNames) {
        return getNodes(node, 0, nodeNames);
    }

    private static Observable<Node> getNodes(Node node, int index, String... nodeNames) {
        return nodeToChildrenObservable(node)
                .filter(childNode -> {
                    String currentName = nodeNames[index];

                    return !childNode.getNodeName().startsWith("#")
                            && (currentName.equals(WILD_CARD)
                            || nodeNames[index].equals(childNode.getNodeName()));

                })
                .concatMap(childNode -> {
                    if (index < nodeNames.length - 1) {
                        return getNodes(childNode, index + 1, nodeNames);
                    } else {
                        return Observable.just(childNode);
                    }
                });
    }
}
