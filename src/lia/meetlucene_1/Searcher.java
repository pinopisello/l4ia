package lia.meetlucene_1;

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

import java.nio.file.FileSystems;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

// From chapter 1

/**
 * This code was originally written for
 * Erik's Lucene intro java.net article
 */
public class Searcher {

  public static void main(String[] args) throws  Exception {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: java " + Searcher.class.getName()
        + " <index dir> <query>");
    }

    String indexDir = args[0];               //1 
    String q = args[1];                      //2   

    search(indexDir, q);
  }

  public static void search(String indexDir, String q)  throws Exception {

	Directory dir = FSDirectory.open(  FileSystems.getDefault().getPath( indexDir)); //3
	IndexReader ireader = DirectoryReader.open(dir);
    IndexSearcher is = new IndexSearcher(ireader);   //3   
    Analyzer analyzer = new StandardAnalyzer();
    analyzer.setVersion(Version.LUCENE_5_2_1);
    
    
    QueryParser parser = new QueryParser( "contents",analyzer );  //4
    //Query query = parser.parse("Recipient AND understands AND that AND although");   //per cercare co espressioni logiche complesse  
    Query query = new TermQuery(new Term("filename", "apache1.0.txt"));                   //per cercare una singola paroa 
    long start = System.currentTimeMillis();
    TopDocs hits = is.search(query, 10); //5
    long end = System.currentTimeMillis();

    System.err.println("Found " + hits.totalHits +   // 6  
      " document(s) (in " + (end - start) +          // 6
      " milliseconds) that matched query '" +        // 6
      q + "':");                                     // 6

    for(ScoreDoc scoreDoc : hits.scoreDocs) {
      Document doc = is.doc(scoreDoc.doc);               //7      
      System.out.println(doc.get("fullpath"));  //8  
    }
  }
}

/*
#1 Parse provided index directory
#2 Parse provided query string
#3 Open index
#4 Parse query
#5 Search index
#6 Write search stats
#7 Retrieve matching document
#8 Display filename
#9 Close IndexSearcher
*/
