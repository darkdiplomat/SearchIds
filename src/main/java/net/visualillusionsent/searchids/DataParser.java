/*
 * This file is part of SearchIds.
 *
 * Copyright © 2012-2013 Visual Illusions Entertainment
 *
 * SearchIds is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * SearchIds is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SearchIds.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.searchids;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Parser class
 *
 * @author croemmich
 * @author Jason (darkdiplomat)
 */
public final class DataParser {

    private final SearchIds searchids;
    private final SAXParser saxParser;

    public DataParser(SearchIds searchids) throws ParserConfigurationException, SAXException {
        this.searchids = searchids;
        this.saxParser = SAXParserFactory.newInstance().newSAXParser();
    }

    public final ArrayList<Result> search(String query) {
        return search(query, "decimal");
    }

    public final ArrayList<Result> search(String query, String base) {
        try {
            DataHandler handler = new DataHandler(query);
            saxParser.parse(searchids.properties.dataXml(), handler);
            return handler.getResults();
        }
        catch (Exception e) {
            searchids.warning("An error occurred while getting Results (Corrupt XML File?)");
        }
        return null;
    }

    final class DataHandler extends DefaultHandler {

        boolean data = false;
        boolean blocks = false;
        boolean items = false;
        boolean item = false;
        private final Pattern pattern;
        private ArrayList<Result> results;

        DataHandler(String query) {
            this.pattern = Pattern.compile(".*?" + Pattern.quote(query) + ".*", Pattern.CASE_INSENSITIVE);
            this.results = new ArrayList<Result>();
        }

        final ArrayList<Result> getResults() {
            return results;
        }

        @Override
        public final void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equalsIgnoreCase("DATA")) {
                data = true;
            }

            if (qName.equalsIgnoreCase("BLOCKS")) {
                blocks = true;
            }

            if (qName.equalsIgnoreCase("ITEMS")) {
                items = true;
            }

            if (qName.equalsIgnoreCase("ITEM")) {
                item = true;

                if (searchids.properties.searchType().equalsIgnoreCase("all") ||
                        (searchids.properties.searchType().equalsIgnoreCase("blocks") && blocks == true) ||
                        (searchids.properties.searchType().equalsIgnoreCase("items") && items == true)) {

                    String name = attributes.getValue("name");
                    String value = attributes.getValue("dec");
                    String id = attributes.getValue("id");

                    if (name != null && value != null) {
                        if (pattern.matcher(name).matches()) {
                            if (id != null) {
                                results.add(new Result(Integer.valueOf(value), Integer.valueOf(id), name, searchids.properties));
                            }
                            else {
                                results.add(new Result(Integer.valueOf(value), name, searchids.properties));
                            }
                        }
                    }
                    else {
                        searchids.warning("Name or value is null on an item");
                    }
                }
            }
        }

        @Override
        public final void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equalsIgnoreCase("DATA")) {
                data = false;
            }

            if (qName.equalsIgnoreCase("BLOCKS")) {
                blocks = false;
            }

            if (qName.equalsIgnoreCase("ITEMS")) {
                items = false;
            }

            if (qName.equalsIgnoreCase("ITEM")) {
                item = false;
            }
        }
    }
}
