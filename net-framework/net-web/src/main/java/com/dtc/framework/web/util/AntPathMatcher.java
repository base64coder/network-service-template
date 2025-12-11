package com.dtc.framework.web.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntPathMatcher {

    public boolean match(String pattern, String path) {
        return getPattern(pattern).matcher(path).matches();
    }

    public Map<String, String> extractUriTemplateVariables(String pattern, String path) {
        Pattern p = getPattern(pattern);
        Matcher m = p.matcher(path);
        if (!m.matches()) {
            return Collections.emptyMap();
        }
        
        return extractVariables(pattern, m);
    }
    
    private Pattern getPattern(String pattern) {
        StringBuilder regex = new StringBuilder("^");
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '{') {
                int end = pattern.indexOf('}', i);
                if (end != -1) {
                    regex.append("([^/]+)");
                    i = end;
                } else {
                    regex.append("\\{");
                }
            } else if (c == '*') {
                regex.append("[^/]*"); // Simple * support
            } else if (c == '?') {
                regex.append(".");
            } else if ("()[]{}^$|?*+.".indexOf(c) != -1) {
                regex.append("\\").append(c);
            } else {
                regex.append(c);
            }
        }
        regex.append("$");
        return Pattern.compile(regex.toString());
    }
    
    private Map<String, String> extractVariables(String pattern, Matcher matcher) {
        Map<String, String> vars = new HashMap<>();
        int groupIndex = 1;
        for (int i = 0; i < pattern.length(); i++) {
             if (pattern.charAt(i) == '{') {
                 int end = pattern.indexOf('}', i);
                 if (end != -1) {
                     String varName = pattern.substring(i + 1, end);
                     if (groupIndex <= matcher.groupCount()) {
                         vars.put(varName, matcher.group(groupIndex++));
                     }
                     i = end;
                 }
             }
        }
        return vars;
    }
}

