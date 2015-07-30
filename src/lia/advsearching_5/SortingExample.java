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

import java.io.PrintStream;
import java.text.DecimalFormat;

import lia.common.TestUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;

// From chapter 5
public class SortingExample {
  private Directory directory;

  public SortingExample(Directory directory) {
    this.directory = directory;
  }
  


  public static void main(String[] args) throws Exception {
    Query allBooks = new MatchAllDocsQuery();
    Directory directory = TestUtil.getBookIndexDirectory();
    QueryParser parser = new QueryParser( "contents", new StandardAnalyzer(  ));             
    Query javaORcontents =parser.parse("java OR action");
    
    BooleanQuery query = new BooleanQuery();          
    //Si fa l OR delle due per dare uno score diverso ai docs.
    //Se metto solo allBooks tutti hanno score = 1
    //Se metto solo javaORcontents solo 2 dei 13 docs appaione nel resulteset.
    
    query.add(allBooks, BooleanClause.Occur.SHOULD);                             
    query.add(javaORcontents, BooleanClause.Occur.SHOULD);       
    SortingExample example = new SortingExample(directory);                     

    example.displayResults(query, Sort.RELEVANCE);

    example.displayResults(query, Sort.INDEXORDER);

    example.displayResults(query, new Sort(new SortField("category", Type.STRING)));

    example.displayResults(query, new Sort(new SortField("pubmonth", Type.INT, true))); //true significa inverse:dal piu alto al piu basso.

    example.displayResults(query,
        new Sort(new SortField("category", Type.STRING),
                     SortField.FIELD_SCORE,
                 new SortField("pubmonth", Type.INT, true)
                 ));

    example.displayResults(query, new Sort(new SortField[] {SortField.FIELD_SCORE,
    									   new SortField("category", Type.STRING)}));
    directory.close();
  }

  public void displayResults(Query query, Sort sort) throws Exception {          // #1  throws IOException {
	IndexReader ireader = DirectoryReader.open(directory);
	IndexSearcher searcher = new IndexSearcher(ireader);
 
	
	//Quando si impone un sort order , Lucene NON assegna uno score agli ScoreDocs.
	//Per questo bisogna chiedere al searcher di assegnre uno score comunque.
	//Vedi  searcher.search soto con doDocScores,  doMaxScore = true
    //searcher.setDefaultFieldSortScoring(true, false);              // #2

	//Sort per campi che non siano id o necessitano che l'indice abbia un SortedDocValuesField aggiunto per ogni doc per il field che andra' sortato.
	//Vedi CreateTestIndex.java doc.add(new SortedDocValuesField("category", new BytesRef(category)));
	
    TopDocs results = searcher.search(query, null,20, sort,true,true );           // #3

    System.out.println("\nResults for: " + query.toString() + " sorted by " + sort);

    System.out.println(StringUtils.rightPad("Title", 30) +
      StringUtils.rightPad("pubmonth", 10) +
      StringUtils.center("id", 4) +
      StringUtils.center("score", 12)+ 
      StringUtils.rightPad("category", 40));

    PrintStream out = new PrintStream(System.out, true, "UTF-8");    // #5

    DecimalFormat scoreFormatter = new DecimalFormat("0.######");
    for (ScoreDoc sd : results.scoreDocs) {
      int docID = sd.doc;
      float score = sd.score;
      Document doc = searcher.doc(docID);
      out.println(
          StringUtils.rightPad( StringUtils.abbreviate(doc.get("title"), 29), 30) + // #6
          StringUtils.rightPad(doc.get("pubmonth"), 10) +                        // #6
          StringUtils.center("" + docID, 4) +                                    // #6
          StringUtils.rightPad( scoreFormatter.format(score), 12)+
          StringUtils.rightPad(doc.get("category"), 40));
         
      //out.println(searcher.explain(query, docID));   // #7
    }

    //searcher.close();
  }


}

/*
#1 Create test query
#2 Create example running
*/