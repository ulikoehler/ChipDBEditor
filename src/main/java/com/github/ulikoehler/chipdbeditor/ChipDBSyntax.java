/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ulikoehler.chipdbeditor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides methods for creating and parsing the ChipDB syntax
 * @author uli
 */
public class ChipDBSyntax {
    
    private static Pattern subscriptPattern = Pattern.compile("__([\\S]+)"); //Group 1: The subscript
    private static Pattern overlinePattern = Pattern.compile("~([^\\s~]+)(~?)"); //Group 1: The overline pattern

    /**
     * Converts a ChipDB YAML syntax pattern like __ into a HTML like "\<sub\>" 
     */
    public static String chipDBSyntaxToHTML(String syntax) {
        if (syntax == null) {
            return "";
        }
        syntax = subscriptPattern.matcher(syntax).replaceAll("<sub>$1</sub>");
        //It's a hassle to replace it this way but text-decoration: overline
        //doesn't work...
        //NOTE THAT THIS ALGORITHM DOESNT NECCCESSARILY WORK WHEN USING
        //NON-CAPTURING GROUPS
        Matcher m = overlinePattern.matcher(syntax);
        Map<String, String> toBeReplaced = new HashMap<String, String>(); //Not instantly replacing avoids matcher issues
        while(m.find()) {
            String group1 = m.group(1);
            //Build a String visualizing the overline
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < group1.length(); i++) {
                builder.append(group1.charAt(i));
                builder.append('\u0305');
            }
            toBeReplaced.put(m.group(0), builder.toString());
        }
        //Replace the stuff
        for(Map.Entry<String, String> entry : toBeReplaced.entrySet()) {
            syntax = syntax.replace(entry.getKey(), entry.getValue());
        }
        return "<html>" + syntax + "</html>";
    }
    
//        public static String htmlToChipDB(String html) {
//        html = html.replace("<sub>", "__");
//        html = html.replace("</sub>", "");
//        html = html.replace("<overline>", "~");
//        html = html.replace("</overline>", "~");
//        html = html.replace("<html>", "");
//        html = html.replace("</html>", "");
//        return html;
//    }

}
