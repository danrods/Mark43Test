package com.api;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by drodrigues on 4/6/16.
 */
@Controller
public class APIController {


    private static final String AVG_WORD_LEN = "/words/avg_len";
    private static final String COMMON_WORDS = "/words/most_com";
    private static final String MED_FREQ = "/words/median";
    private static final String AVG_SENTENCE_LEN = "/sentences/avg_len";
    private static final String PHONE_NUM = "/phones";


    private static final String SPACE_DELIM = " "; //Find space separated words
    private static final String SENTENCE_DELIM = "(\\.|!|\\?)"; //Match a Period, Question Mark or Exclamation Point

    //A period, or Exclamation point, or word composed of at least one letter with possible - or ' or "
    private static final String WORD_REGEX = "\\.|!$|[a-zA-z]+(-|\'|\")*[a-zA-z]*(!|\\.)*";

    //A sentence which contains letters, dashes, quotes, ticks, Parens, semicolons, colons, etc.
    private static final String SENTENCE_REGEX = "[a-zA-z\\s,\\-\'\"()@;:]+\\s*";

    //Match a phone number with optional Area code
    private static final String PHONE_REGEX = "(\\([0-9]{3}\\)\\-?|[0-9]{3}\\-?)?[0-9]{3}\\-?[0-9]{4}";
    /**
     * Convenience Method that will parse a string containing JSON into a JSON Object
     *  and then fetch the 'text' that was passed in if the JSON was well-formed and according to
     *  the input specification
     * @param json The JSON Object as a String
     * @return Returns the text we will be parsing if it is of the form --> {"text":"Hello World!"}
     * @throws JSONException Throws exception if the String json was not well formed
     */
    private String parseJSON(String json) throws JSONException{
        JSONObject object = new JSONObject(json); //Lets let Java do the parsing for us
        String text = object.getString("text");
        System.out.println("TEXT : " + text);

        if(text == null || text.equals("")) throw new JSONException("Value of text cannot be NULL");
        return text;
    }


    /**
     * Convenience Method to parse text into tokens based on the given Regex.
     * @param text The text we are delimiting
     * @param splitRegex The Regex we are using to split up into tokens.
     * @param matchRegex The Regex we are using to verify token is valid.
     * @return A list of tokens that match the matchRegex
     */
    private List<String> matchTokens(String text, String splitRegex, String matchRegex){

        List<String> tokenList = new LinkedList<>();

        if(text == null && splitRegex == null && matchRegex == null){
            System.out.println("Text, SplitRegex and MatchRegex must be provided");
            return tokenList;
        }

        String[] tokens = text.split(splitRegex);

        for(String token : tokens){

            if(token.matches(matchRegex)){
                tokenList.add(token);
            }
            else System.out.println("Invalid Token : "+ token);
        }

        return tokenList;
    }


    /**
     * Method to Find the average length of a single word in a list of text.
     * @param json The JSON element that was passed to the HTTP Post Request.
     * @param req The Request object, unused.
     * @return Returns a ResponseEntity with the average word length (0 if none matched) or an error message if failed.
     */
    @RequestMapping(value=AVG_WORD_LEN, method=RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> averageWord(@RequestBody String json, HttpServletRequest req){

        try{
            String text = parseJSON(json);

            List<String> tokens = matchTokens(text, SPACE_DELIM, WORD_REGEX);

            int average = 0, totalLength = 0;
            for(String s : tokens){
                totalLength += s.length();
            }

            if(tokens.size() > 0) average = totalLength / tokens.size(); //Let's not divide by zero

            return new ResponseEntity<String>("" + average, HttpStatus.OK);
        }
        catch (JSONException e) {
            return new ResponseEntity<String>("Error, Malformed JSON : " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch(Exception e){
            return new ResponseEntity<String>("Unexpected Error : " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }

    }


    /**
     * Method to Find the common words in a list of text.
     * @param json The JSON element that was passed to the HTTP Post Request.
     * @param req The Request object, unused.
     * @return Returns a ResponseEntity with the most common word (null if none) upon request or error message if failed.
     */
    @RequestMapping(value=COMMON_WORDS, method=RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> commonWord(@RequestBody String json, HttpServletRequest req){

        Map<String, Integer> dictionary = new HashMap<>(); //Used to store word frequency

        try {
            String text = parseJSON(json);
            String[] words = text.split(" ");

            Integer val;
            for(String word : words){
                if((val = dictionary.get(word)) == null){ //If we haven't found this word yet
                    val = 0; //Java will auto-cast to Integer object
                }
                dictionary.put(word, ++val);
            }

            int occurrences = 0;
            Integer value;

            LinkedList<String> returnList = new LinkedList<>(); //The list of words with highest frequency
            for(String key : dictionary.keySet()){
                if((value = dictionary.get(key))!= null  //Just in case a key is mapped to null. Should never happen
                        && value > occurrences){ //If the current max frequency is less than the one we just found
                    returnList.clear(); //Clear the previous max frequency list
                    returnList.add(key); //Add the new one
                    occurrences = value; //Set the new max frequency
                }
                else if(value == occurrences){ //In case multiple matches
                    returnList.add(key);
                }
            }

            Collections.sort(returnList); //Sort the List Lexicographically

            return new ResponseEntity<String>(returnList.poll(), HttpStatus.OK); //Return the head of the list
        }
        catch (JSONException e) {
            return new ResponseEntity<String>("Error, Malformed JSON : " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch(Exception e){
            return new ResponseEntity<String>("Unexpected Error : " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Inner class to make it easy to parse the median after the fact.
     */
    public class FreqNode {

        String key;
        int freq;

        public FreqNode(String key, int val){this.key = key; freq=val;}

        public void incFrequency(){
            freq++;
        }

        public void decFrequency(){
            freq--;
        }

        public String toString(){
            return "Key: " + key + " Freq: " + freq;
        }
    }

    /**
     * Comparator for a FreqNode that first compares the frequency, \
     * then lexicographically by the key.
     */
    public class MinComparator implements Comparator<FreqNode> {

        @Override
        public int compare(FreqNode left, FreqNode right) {
            // 2 - 3
            int num;
            if((num = left.freq - right.freq) != 0) return num; //If not equal just return the int

            return left.key.compareTo(right.key); //else use String compare method to compare lexicographically
        }
    }


    /**
     * Method to Find the word(s) with Median Frequency in a list of text.
     * @param json The JSON element that was passed to the HTTP Post Request.
     * @param req The Request object, unused.
     * @return Returns a ResponseEntity with the median words upon request (null if none) or error message if failed.
     */
    @RequestMapping(value=MED_FREQ, method=RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> medianFreq(@RequestBody String json, HttpServletRequest req){

        Map<String, FreqNode> dictionary = new HashMap<>();
        ArrayList<FreqNode> nodeList = new ArrayList<>();

        try {
            String text = parseJSON(json);
            String[] words = text.split(" ");

            FreqNode val;
            for(String word : words){
                if((val = dictionary.get(word)) == null){
                    val = new FreqNode(word, 0);
                    nodeList.add(val);
                }
                val.incFrequency();
                dictionary.put(word, val);
            }

            Collections.sort(nodeList, new MinComparator()); //Sort the list ascending using our comparator
            System.out.println(nodeList);


            // [ 1, 2, 3] mid = 3/2 = 1  or [1, 2, 3, 4] mid = 4/2 = 2
            List<String> returnList = new ArrayList<>(2);

            int mid = nodeList.size()/2;
            returnList.add(nodeList.get(mid).key); //Add the median of the list (Integer division takes floor)

            if((nodeList.size() & 1) == 0){ //If we have an even number of elements, add mid - 1 as well
                returnList.add(nodeList.get(mid - 1).key);
            }

            return new ResponseEntity<String>("" + returnList, HttpStatus.OK);
        }
        catch (JSONException e) {
            return new ResponseEntity<String>("Error, Malformed JSON : " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch(Exception e){
            return new ResponseEntity<String>("Unexpected Error : " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Method to Find the average sentence length in a list of text.
     * @param json The JSON element that was passed to the HTTP Post Request.
     * @param req The Request object, unused.
     * @return Returns a ResponseEntity with the average sentence length (0 if no sentences) upon request or error message if failed.
     */
    @RequestMapping(value=AVG_SENTENCE_LEN, method=RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> avgSentence(@RequestBody String json, HttpServletRequest req){

        try {
            String text = parseJSON(json);

            List<String> tokens = matchTokens(text, SENTENCE_DELIM, SENTENCE_REGEX);

            int average = 0, totalLength = 0;
            for(String s : tokens){
                //Trim the sentence to get rid of any leading/trailing white space before we get the length
                //We need to add 1 for ?,! or . that we removed when splitting
                totalLength += s.trim().length() + 1;
            }

            if(tokens.size() > 0) average = totalLength / tokens.size(); //Let's not divide by zero

            return new ResponseEntity<String>("" + average, HttpStatus.OK);
        }
        catch (JSONException e) {
            return new ResponseEntity<String>("Error, Malformed JSON : " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch(Exception e){
            return new ResponseEntity<String>("Unexpected Error : " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Method to Find the phone numbers in a list of text.
     * @param json The JSON element that was passed to the HTTP Post Request.
     * @param req The Request object, unused.
     * @return Returns a ResponseEntity with a list of valid phone numbers upon request (empty list if none) or error message if failed.
     */
    @RequestMapping(value=PHONE_NUM, method=RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> phoneNumbers(@RequestBody String json, HttpServletRequest req){

        try {
            String text = parseJSON(json);

            List<String> tokens = matchTokens(text, SPACE_DELIM, PHONE_REGEX);

            return new ResponseEntity<String>(tokens+"" , HttpStatus.OK);
        }
        catch (JSONException e) {
            return new ResponseEntity<String>("Error, Malformed JSON : " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch(Exception e){
            return new ResponseEntity<String>("Unexpected Error : " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

}
