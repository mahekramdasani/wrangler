/*
 *  Copyright © 2021 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

 package io.cdap.wrangler.dq;

 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Utility class for string conversions.
  */
 public class ConvertString {
   private final String repeatedChar;
 
   public static final String[] WHITESPACE_CHARS = {
     " ",    // space
     "\t",   // character tabulation
     "\n",   // line feed
     "\u000B", // vertical tab
     "\f",   // form feed
     "\r",   // carriage return
     "\u0085", // next line
     "\u00A0", // non-breaking space
     "\u1680", "\u180E", "\u2000", "\u2001", "\u2002", "\u2003",
     "\u2004", "\u2005", "\u2006", "\u2007", "\u2008", "\u2009",
     "\u200A", "\u2028", "\u2029", "\u202F", "\u205F", "\u3000"
   };
 
   public ConvertString() {
     this.repeatedChar = null;
   }
 
   public ConvertString(String repeatedChar) {
     this.repeatedChar = repeatedChar;
   }
 
   public String removeTrailingAndLeading(String input) {
     if (input == null) {
       return null;
     }
     return input.trim();
   }
 
   public String removeTrailingAndLeading(String input, String trimChar) {
     if (input == null || trimChar == null || trimChar.isEmpty()) {
       return input;
     }
     return input.replaceAll("^(" + trimChar + ")+|(" + trimChar + ")+$", "");
   }
 
   public String removeTrailingAndLeadingWhitespaces(String input) {
     if (input == null || input.isEmpty()) {
       return input;
     }
     int start = 0, end = input.length();
 
     while (start < end && isWhitespaceChar(input.charAt(start))) {
       start++;
     }
 
     while (end > start && isWhitespaceChar(input.charAt(end - 1))) {
       end--;
     }
 
     return input.substring(start, end);
   }
 
   public String removeRepeatedChar(String input) {
     if (input == null || input.isEmpty() || repeatedChar == null || repeatedChar.isEmpty()) {
       return input;
     }
 
     StringBuilder sb = new StringBuilder();
     char[] chars = input.toCharArray();
     char prev = 0;
 
     for (char ch : chars) {
       if (repeatedChar.indexOf(ch) >= 0 && ch == prev) {
         continue;
       }
       sb.append(ch);
       prev = ch;
     }
 
     return sb.toString();
   }
 
   public String removeRepeatedWhitespaces(String input) {
     if (input == null || input.isEmpty()) {
       return input;
     }
 
     Set<Character> whitespaceSet = new HashSet<>();
     for (String ws : WHITESPACE_CHARS) {
       if (!ws.isEmpty()) {
         whitespaceSet.add(ws.charAt(0));
       }
     }
 
     StringBuilder result = new StringBuilder();
     char[] chars = input.toCharArray();
     char prev = 0;
 
     for (char ch : chars) {
       if (ch == prev && whitespaceSet.contains(ch)) {
         continue;
       }
       result.append(ch);
       prev = ch;
     }
 
     return result.toString();
   }
 
   private boolean isWhitespaceChar(char ch) {
     for (String ws : WHITESPACE_CHARS) {
       if (!ws.isEmpty() && ws.charAt(0) == ch) {
         return true;
       }
     }
     return false;
   }
 }

