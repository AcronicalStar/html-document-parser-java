package main.java;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This program loads documents from urls passed through the command line. It fetches links from the HTMl documents
 * prints the number of external urls referenced in the documents. This program does not parse document types other
 * than HTML documents.
 */
public class Main {
    public static void main(String[] args) {
        Validate.isTrue(args.length >= 1, "usage: supply urls to fetch");

        for (int i = 0; i < args.length; i++) {
            String url = args[i];

            Document doc = null;

            // Loads a document from a url
            try {
                doc = Jsoup.connect(url).get();
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid URL");
                System.exit(1);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.out.println("Unable to connect to host");
                System.exit(1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Finds links using CSS selectors
            Elements links = doc.select("a[href]");
            Elements media = doc.select("[src]");
            Elements imports = doc.select("link[href]");

            Set<String> externalLinks = new HashSet<>();

            // Adds links to the set
            getExternalLinks(url, links, externalLinks, "href");

            // Adds media links to the set
            getExternalLinks(url, media, externalLinks, "src");

            // Adds import links to the set
            getExternalLinks(url, imports, externalLinks, "href");

            System.out.println(args[i] + " " + externalLinks.size());
        }
    }

    /**
     *
     * @param url - The base uri
     * @param links - A list of link elements
     * @param externalLinks - A set of external links. Links will be added to this set if they are external
     * @param attribute - Used to get the links with the specified attribute
     */
    private static void getExternalLinks(String url, Elements links, Set<String> externalLinks, String attribute) {
        externalLinks.addAll(
                links
                        .stream()
                        // Checks to see if the link is external. If it is, it adds it to the HashSet.
                        .filter(src -> isExternalLink(src.attr(attribute), url))
                        .map(e -> e.attr(attribute))
                        .collect(Collectors.toSet()));
    }

    /**
     * This method checks to see if given link is an external link
     * @param url - The method determines whether this url is an external link
     * @param baseURI - The base URI of the HTML document
     * @return returns true if the link is external or false if it is not
     */
    private static boolean isExternalLink(String url, String baseURI) {
        return !(url.startsWith(baseURI) || url.startsWith("/") || url.startsWith("./") || url.startsWith("../") || url.startsWith("#"));
    }
}
