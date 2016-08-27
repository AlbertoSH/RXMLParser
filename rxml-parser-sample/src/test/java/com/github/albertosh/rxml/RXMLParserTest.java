package com.github.albertosh.rxml;

import com.github.albertosh.rxml.sample.CD;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import rx.Observable;
import rx.observables.GroupedObservable;
import rx.observables.MathObservable;
import rx.observers.TestSubscriber;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;

public class RXMLParserTest {

    private static Document loadDocument(String fileName) throws Exception {
        URL fileUrl = RXMLParserTest.class.getClassLoader().getResource("xml/" + fileName);
        File file = new File(fileUrl.toURI());
        DocumentBuilderFactory dbFactory
                = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return dBuilder.parse(file);
    }

    private static Float round(Float f) {
        float tmp = f * 100;
        tmp = Math.round(tmp);
        return tmp / 100;
    }

    @Test
    public void simpleNote() throws Exception {
        Document document = loadDocument("note.xml");
        Observable<Node> obsNodes = RXMLParser.getNodes(document, "note", RXMLParser.WILD_CARD);
        TestSubscriber<Node> testSubscriber = TestSubscriber.create();
        obsNodes.subscribe(testSubscriber);
        testSubscriber.assertValueCount(4);
        List<Node> nodes = testSubscriber.getOnNextEvents();
        for (Node n : nodes) {
            if (n.getNodeName().equals("to") && (n.getTextContent().equals("Tove")))
                continue;
            if (n.getNodeName().equals("from") && (n.getTextContent().equals("Jani")))
                continue;
            if (n.getNodeName().equals("heading") && (n.getTextContent().equals("Reminder")))
                continue;
            if (n.getNodeName().equals("body") && (n.getTextContent().equals("Don't forget me this weekend!")))
                continue;
            throw new IllegalStateException("Unexpected node " + n.getNodeName());
        }
        testSubscriber.assertCompleted();
    }

    @Test
    public void parseCD() throws Exception {
        Document document = loadDocument("cd_catalog.xml");
        TestSubscriber<CD> testSubscriber = TestSubscriber.create();
        RXMLParser.getNodes(document, "CATALOG", "CD")
                .map(CD::new)
                .subscribe(testSubscriber);
        testSubscriber.assertValueCount(26);

        // Just some random checks...
        List<CD> cds = testSubscriber.getOnNextEvents();
        CD stillGotTheBlues = new CD.Builder()
                .title("Still got the blues")
                .artist("Gary More")
                .country("UK")
                .company("Virgin records")
                .price(10.20f)
                .year(1990)
                .build();
        assertThat(cds, hasItem(stillGotTheBlues));

        CD pavarottiGalaConcert = new CD.Builder()
                .title("Pavarotti Gala Concert")
                .artist("Luciano Pavarotti")
                .country("UK")
                .company("DECCA")
                .price(9.90f)
                .year(1991)
                .build();
        assertThat(cds, hasItem(pavarottiGalaConcert));

        testSubscriber.assertCompleted();
    }

    @Test
    public void simpleQuery() throws Exception {
        Document document = loadDocument("cd_catalog.xml");
        TestSubscriber<CD> testSubscriber = TestSubscriber.create();
        RXMLParser.getNodes(document, "CATALOG", "CD")
                .filter(cdNode -> RXMLParser.getNodes(cdNode, "COUNTRY")
                        .map(Node::getTextContent)
                        .map(country -> country.equals("EU"))
                        .toBlocking()
                        .first())
                .map(CD::new)
                .subscribe(testSubscriber);

        testSubscriber.assertValueCount(5);
        testSubscriber.getOnNextEvents()
                .forEach(cd -> assertThat(cd.getCountry(), is(equalTo("EU"))));

        testSubscriber.assertCompleted();
    }

    @Test
    public void averagePricePerCountry() throws Exception {
        Document document = loadDocument("cd_catalog.xml");

        Observable<GroupedObservable<String, CD>> parsedCDs = RXMLParser.getNodes(document, "CATALOG", "CD")
                .map(CD::new)
                .groupBy(CD::getCountry)
                .share();

        Observable<String> parsedCountries =
                parsedCDs
                        .map(GroupedObservable::getKey);

        Observable<Float> parsedAverage =
                parsedCDs
                        .concatMap(grouped ->
                                MathObservable.averageFloat(
                                        grouped.map(CD::getPrice)
                                ))
                        .map(RXMLParserTest::round);

        Observable<Pair<String, Float>> parsedAveragePerCountry =
                Observable.zip(parsedCountries, parsedAverage, Pair::new);
        TestSubscriber<Pair<String, Float>> parsedSubscriber = TestSubscriber.create();
        parsedAveragePerCountry.subscribe(parsedSubscriber);


        Observable<GroupedObservable<String, Node>> xmlCDs = RXMLParser.getNodes(document, "CATALOG", "CD")
                .groupBy(node -> RXMLParser.getNodes(node, "COUNTRY")
                        .map(Node::getTextContent)
                        .toBlocking()
                        .first())
                .share();

        Observable<String> xmlCountries =
                xmlCDs
                        .map(GroupedObservable::getKey);

        Observable<Float> xmlAverage =
                xmlCDs
                        .concatMap(grouped ->
                                MathObservable.averageFloat(
                                        grouped
                                                .flatMap(node -> RXMLParser.getNodes(node, "PRICE"))
                                                .map(Node::getTextContent)
                                                .map(Float::valueOf)))
                        .map(RXMLParserTest::round);

        Observable<Pair<String, Float>> xmlAveragePerCountry =
                Observable.zip(xmlCountries, xmlAverage, Pair::new);
        TestSubscriber<Pair<String, Float>> xmlSubscriber = TestSubscriber.create();
        xmlAveragePerCountry.subscribe(xmlSubscriber);

        parsedSubscriber.assertCompleted();
        xmlSubscriber.assertCompleted();
        assertThat(parsedSubscriber.getOnNextEvents(), is(equalTo(xmlSubscriber.getOnNextEvents())));
        assertThat(parsedSubscriber.getOnErrorEvents(), is(equalTo(xmlSubscriber.getOnErrorEvents())));
        assertThat(parsedSubscriber.getCompletions(), is(equalTo(xmlSubscriber.getCompletions())));

        List<Pair<String, Float>> averages = parsedSubscriber.getOnNextEvents();
        assertThat(averages, hasSize(4));
        assertThat(averages.get(0).getKey(), is(equalTo("USA")));
        assertThat(averages.get(0).getValue(), is(9.39f));
        assertThat(averages.get(1).getKey(), is(equalTo("UK")));
        assertThat(averages.get(1).getValue(), is(8.98f));
        assertThat(averages.get(2).getKey(), is(equalTo("EU")));
        assertThat(averages.get(2).getValue(), is(9.32f));
        assertThat(averages.get(3).getKey(), is(equalTo("Norway")));
        assertThat(averages.get(3).getValue(), is(7.9f));
    }

    private final static class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair<?, ?> pair = (Pair<?, ?>) o;

            if (key != null ? !key.equals(pair.key) : pair.key != null) return false;
            return value != null ? value.equals(pair.value) : pair.value == null;

        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return key + " = " + value;
        }
    }
}
