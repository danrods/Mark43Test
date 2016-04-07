package com.api;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

    private String parseJSON(String json) throws JSONException{
        JSONObject object = new JSONObject(json);
        String text = object.getString("text");
        System.out.println("TEXT : " + text);

        if(text == null || text.equals("")) throw new JSONException("Value of text cannot be NULL");
        return text;
    }

    @RequestMapping(value=AVG_WORD_LEN, method= {RequestMethod.GET, RequestMethod.POST}, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> averageWord(@RequestBody String json, HttpServletRequest req){

        try{
            String text = parseJSON(json);
            String[] words = text.split(" ");

            int average = 0, totalWords = 0, totalLength = 0;
            for(String word : words){
                //A period, or Exclamation point, or word composed of at least one letter with possible - or ' or "
                if(word.matches("\\.|!$|[a-zA-z]+(-|\'|\")*[a-zA-z]*(!|\\.)*")){
                    totalWords++;
                    totalLength += word.length();
                }
                else System.out.println("Invalid word : "+ word);
            }

            if(totalWords > 0) average = totalLength / totalWords;

            return new ResponseEntity<String>("" + average, HttpStatus.OK);
        }
        catch (JSONException e) {
            return new ResponseEntity<String>("Error, Malformed JSON : " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }


    }



    @RequestMapping(value=COMMON_WORDS, method= {RequestMethod.GET, RequestMethod.POST}, consumes = "application/json")
    @ResponseBody
    public ResponseEntity<String> commonWord(@RequestBody String json, HttpServletRequest req){

        Map<String, Integer> dictionary = new HashMap<>();

        try {
            String text = parseJSON(json);
            String[] words = text.split(" ");

            Integer val;
            for(String word : words){
                if((val = dictionary.get(word)) == null){
                    val = 0;
                }
                dictionary.put(word, ++val);
            }

            int occurrences = 0;
            Integer value;
            LinkedList<String> returnList = new LinkedList<>();
            for(String key : dictionary.keySet()){
                if((value = dictionary.get(key))!= null && value > occurrences){ //Just in case a key is mapped to null
                    returnList.clear();
                    returnList.add(key);
                    occurrences = value;
                }
                else if(value == occurrences){ //In case multiple matches
                    returnList.add(key);
                }
            }

            Collections.sort(returnList);

            return new ResponseEntity<String>(returnList.poll(), HttpStatus.OK);
        }
        catch (JSONException e) {
            return new ResponseEntity<String>("Error, Malformed JSON : " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }


}
