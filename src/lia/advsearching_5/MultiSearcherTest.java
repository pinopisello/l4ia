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

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

// From chapter 5
public class MultiSearcherTest extends TestCase {
  private IndexSearcher searcher;
  private MultiReader multireader;

  public void setUp() throws Exception {
    String[] animals = { "aardvark", "beaver", "coati",
                       "dog", "elephant", "frog", "gila monster",
                       "horse", "iguana", "javelina", "kangaroo",
                       "lemur", "moose", "nematode", "orca",
                       "python", "quokka", "rat", "scorpion",
                       "tarantula", "uromastyx", "vicuna",
                       "walrus", "xiphias", "yak", "zebra"};

    Analyzer analyzer = new WhitespaceAnalyzer();
 
    Directory aTOmDirectory = new RAMDirectory();     // #1 Create two directories
    Directory nTOzDirectory = new RAMDirectory();     // #1 Create two directories

    IndexWriter aTOmWriter = new IndexWriter(aTOmDirectory,new IndexWriterConfig(analyzer));
    IndexWriter nTOzWriter = new IndexWriter(nTOzDirectory,new IndexWriterConfig(analyzer));
    
    for (int i=animals.length - 1; i >= 0; i--) {
      Document doc = new Document();
      String animal = animals[i];
      doc.add(new StringField("animal", animal, Field.Store.YES));
      if (animal.charAt(0) < 'n') {
        aTOmWriter.addDocument(doc);                 //#2 Index halves of the alphabet <n => aTOmWriter
      } else {                                       
        nTOzWriter.addDocument(doc);                 //#2 Index halves of the alphabet >n => nTOzDirectory
      }
    }

    aTOmWriter.close();
    nTOzWriter.close();

    IndexReader ireader1 = DirectoryReader.open(aTOmDirectory);
    IndexReader ireader2 = DirectoryReader.open(nTOzDirectory);
    multireader = new MultiReader(ireader1,ireader2);
    searcher = new IndexSearcher(multireader);

  }

  public void testMulti() throws Exception {
    TermRangeQuery query = TermRangeQuery.newStringRange("animal", //ritorna tutti i doc con field   animal che inizia con lettere tha 'h' e 't'
                                              "h",        // #3
                                              "t",        // #3
                                              true, true);// #3

    TopDocs hits = searcher.search(query, 10); //searcher cerca in ambo le dirctory!!
    for (ScoreDoc sd : hits.scoreDocs) {
        Document doc = searcher.doc(sd.doc);
        System.out.println(sd.score + ": " + doc.get("animal"));
      }
    assertEquals("tarantula not included", 12, hits.totalHits);
  }

}
