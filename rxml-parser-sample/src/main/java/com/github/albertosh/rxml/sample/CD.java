package com.github.albertosh.rxml.sample;

import com.github.albertosh.rxml.RXMLParser;

import org.w3c.dom.Node;

public class CD {

    private final String title;
    private final String artist;
    private final String country;
    private final String company;
    private final Float price;
    private final Integer year;

    private CD(Builder builder) {
        this.title = builder.title;
        this.artist = builder.artist;
        this.country = builder.country;
        this.company = builder.company;
        this.price = builder.price;
        this.year = builder.year;
    }

    public CD(Node node) {
        this.title = RXMLParser.getNodes(node, "TITLE")
                .map(Node::getTextContent)
                .toBlocking()
                .first();
        this.artist = RXMLParser.getNodes(node, "ARTIST")
                .map(Node::getTextContent)
                .toBlocking()
                .first();
        this.country = RXMLParser.getNodes(node, "COUNTRY")
                .map(Node::getTextContent)
                .toBlocking()
                .first();
        this.company = RXMLParser.getNodes(node, "COMPANY")
                .map(Node::getTextContent)
                .toBlocking()
                .first();
        this.price = RXMLParser.getNodes(node, "PRICE")
                .map(Node::getTextContent)
                .map(Float::parseFloat)
                .toBlocking()
                .first();
        this.year = RXMLParser.getNodes(node, "YEAR")
                .map(Node::getTextContent)
                .map(Integer::parseInt)
                .toBlocking()
                .first();
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getCountry() {
        return country;
    }

    public String getCompany() {
        return company;
    }

    public Float getPrice() {
        return price;
    }

    public Integer getYear() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CD cd = (CD) o;

        if (!title.equals(cd.title)) return false;
        if (!artist.equals(cd.artist)) return false;
        if (!country.equals(cd.country)) return false;
        if (!company.equals(cd.company)) return false;
        if (!price.equals(cd.price)) return false;
        return year.equals(cd.year);

    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + artist.hashCode();
        result = 31 * result + country.hashCode();
        result = 31 * result + company.hashCode();
        result = 31 * result + price.hashCode();
        result = 31 * result + year.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CD{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", country='" + country + '\'' +
                ", company='" + company + '\'' +
                ", price=" + price +
                ", year=" + year +
                '}';
    }

    public static class Builder {
        private String title;
        private String artist;
        private String country;
        private String company;
        private Float price;
        private Integer year;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder artist(String artist) {
            this.artist = artist;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder company(String company) {
            this.company = company;
            return this;
        }

        public Builder price(Float price) {
            this.price = price;
            return this;
        }

        public Builder year(Integer year) {
            this.year = year;
            return this;
        }

        public Builder fromPrototype(CD prototype) {
            title = prototype.title;
            artist = prototype.artist;
            country = prototype.country;
            company = prototype.company;
            price = prototype.price;
            year = prototype.year;
            return this;
        }

        public CD build() {
            return new CD(this);
        }
    }
}
