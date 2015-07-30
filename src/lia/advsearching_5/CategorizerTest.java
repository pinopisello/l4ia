package lia.advsearching_5;

/**
 * Copyright Manning Publications Co.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific lan      
*/

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;
import lia.common.TestUtil;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

// From chapter 5
public class CategorizerTest extends TestCase {
  Map<String,Map<String,Integer>> categoryMap;

  protected void setUp() throws Exception {
    categoryMap = new TreeMap<String, Map<String,Integer> >();

    buildCategoryVectors();
//    dumpCategoryVectors();
  }

  public void testCategorization() throws Exception {
    assertEquals("/technology/computers/programming/methodology",
        getCategory("extreme agile methodology"));
    assertEquals("/education/pedagogy",
        getCategory("montessori education philosophy"));
  }

  private void dumpCategoryVectors() {
    Iterator categoryIterator = categoryMap.keySet().iterator();
    while (categoryIterator.hasNext()) {
      String category = (String) categoryIterator.next();
      System.out.println("Category " + category);

      Map vectorMap = (Map) categoryMap.get(category);
      Iterator vectorIterator = vectorMap.keySet().iterator();
      while (vectorIterator.hasNext()) {
        String term = (String) vectorIterator.next();
        System.out.println("    " + term + " = " + vectorMap.get(term));
      }
    }
  }

  
  //Itera per ogni document e costruice this.categoryMapMap<String,Map<String,Integer>>
  //Ogni category e' key, value e' una Map <subject_token,incidenza token totale per field "subject" per tutti i docs nell index che hanno tale category>
  private void buildCategoryVectors() throws IOException {
    IndexReader reader = DirectoryReader.open(TestUtil.getBookIndexDirectory());
    int maxDoc = reader.maxDoc();

    for (int i = 0; i < maxDoc; i++) {
        Document doc = reader.document(i);
        String category = doc.get("category");

        Map<String,Integer> category_subjectWord_FreqMap = (Map<String,Integer>) categoryMap.get(category);
        if (category_subjectWord_FreqMap == null) {
        	category_subjectWord_FreqMap = new TreeMap<String,Integer>();
          categoryMap.put(category, category_subjectWord_FreqMap);
        }
        Terms currDocsubjectvector = reader.getTermVector(i, "subject");  
        updateSubjectFreqMap(category_subjectWord_FreqMap, currDocsubjectvector);
    }
  }
 

  private void updateSubjectFreqMap(Map<String,Integer> category_subjectWord_FreqMap,Terms currDocsubjectvector) throws IOException{
	  List<String> subjectterms=new ArrayList<String>();
	  List<Integer> subjectfreqs=new ArrayList<Integer>();
	  TermsEnum termsiterator = currDocsubjectvector.iterator();
	   BytesRef currsubjectTerm = null;
	    
	   while ( (currsubjectTerm = termsiterator.next()) != null) {                      
	    	String currsubjectTermStr = currsubjectTerm.utf8ToString();
	    	Integer currsubjectTermStrdocFreq=termsiterator.docFreq();
	    	subjectterms.add(currsubjectTermStr);
	    	subjectfreqs.add(currsubjectTermStrdocFreq);
	    }
	
	    for (int i = 0; i < subjectterms.size(); i++) {
	      String term = subjectterms.get(i);
	      if (category_subjectWord_FreqMap.containsKey(term)) {
	        Integer value = (Integer) category_subjectWord_FreqMap.get(term);
	        category_subjectWord_FreqMap.put(term,
	            new Integer(value.intValue() + subjectfreqs.get(i)));
	      } else {
	    	  category_subjectWord_FreqMap.put(term, new Integer(subjectfreqs.get(i)));
	      }
	    }
  }

  //per un dato subject cerca quale 
  private String getCategory(String subject) {
    String[] subject_words = subject.split(" ");
    Iterator categoryIterator = categoryMap.keySet().iterator();
    double bestAngle = Double.MAX_VALUE;
    String bestCategory = null;

    //Per ogni category esistente, misuro l angolo tra  subject_words 
    //e lo spazio delle words/freq per tale category calcolato in buildCategoryVectors()
    while (categoryIterator.hasNext()) {
      String category = (String) categoryIterator.next();
      System.out.println(category);

      double angle = computeAngle(subject_words, category);
      System.out.println(" -> angle = " + angle + " (" + Math.toDegrees(angle) + ")");
      if (angle < bestAngle) {
        bestAngle = angle;
        bestCategory = category;
      }
    }

    return bestCategory;
  }

  private double computeAngle(String[] subject_words, String category) {
    Map<String,Integer> category_subjectWord_FreqMap = (Map) categoryMap.get(category);

    int somma_incidenze = 0;  //incidenza totale subject_words
    int somma_incidenze_qudaratiche = 0;//somma (incidenza  subject_words)^2
    for (String curr_word : subject_words) {//per ogni word in subject_words calcolo la frequenza totale occorrenze [categoryWordFreq] nel category_subjectWord_FreqMap
      int categoryWordFreq = 0;
      if (category_subjectWord_FreqMap.containsKey(curr_word)) {
        categoryWordFreq =
            ((Integer) category_subjectWord_FreqMap.get(curr_word)).intValue();
      }

      somma_incidenze+=categoryWordFreq;  //#1
      somma_incidenze_qudaratiche += categoryWordFreq * categoryWordFreq;
    }


    double denominator;
    if (somma_incidenze_qudaratiche == subject_words.length) {
      denominator = somma_incidenze_qudaratiche; // #2
    } else {
      denominator = Math.sqrt(somma_incidenze_qudaratiche) *
                    Math.sqrt(subject_words.length);
    }

    double ratio = somma_incidenze/denominator;

    return Math.acos(ratio);
  }
  /*
    #1 Assume each word has frequency 1
    #2 Shortcut to prevent precision issue
  */
}


