package cz.cuni.mff.xrg.odalic.util.parsing;

import uk.ac.shef.dcs.util.StringUtils;

public class UriParsingUtil {

    public static String parseLabelFromResourceUri(String resourceURI, boolean applyCustomHeuristics) {
        if (!resourceURI.startsWith("http")) {
            return resourceURI;
        }

        // URI like https://www.w3.org/1999/02/22-rdf-syntax-ns#type
        int trimPosition = resourceURI.lastIndexOf("#");

        // URI like http://dbpedia.org/property/name
        if (trimPosition == -1) {
            trimPosition = resourceURI.lastIndexOf("/");
        }

        if (trimPosition != -1) {
            // Remove anything that is not a character or digit
            // TODO: For a future improvement, take into account the "_" character.
            String stringValue = resourceURI.substring(trimPosition + 1).replaceAll("[^a-zA-Z0-9]", "").trim();

            // Derived KBs can have custom URI conventions.
            stringValue = applyCustomUriHeuristics(resourceURI, stringValue, applyCustomHeuristics);
            stringValue = StringUtils.splitCamelCase(stringValue);

            return stringValue;
        }

        return resourceURI;
    }

    public static String applyCustomUriHeuristics(String resourceURI, String label, boolean applyCustomHeuristics) {
        if (!applyCustomHeuristics) {
            return label;
        }

        // This is an yago resource, which may have numbered ids as suffix
        // e.g., City015467.
        if (resourceURI.contains("yago")) {
            int end = 0;
            for (int i = 0; i < label.length(); i++) {
                if (Character.isDigit(label.charAt(i))) {
                    end = i;
                    break;
                }
            }

            if (end > 0) {
                label = label.substring(0, end);
            }
        } else {
            return label;
        }

        return label;
    }
}
