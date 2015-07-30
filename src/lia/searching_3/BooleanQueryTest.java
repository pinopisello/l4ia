package lia.searching_3;

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
import lia.common.TestUtil;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.store.Directory;

// From chapter 3
public class BooleanQueryTest extends TestCase {
  public void testAnd() throws Exception {
	//Termquery subject = "search"
    TermQuery searchingBooks = new TermQuery(new Term("subject","search"));  //#1
    
    //NumericRangeQuery 201001  < pubmonth < 201012
    Query books2010 =  NumericRangeQuery.newIntRange("pubmonth", 201001,   201012, true, true);                //#2

    //BooleanQuery = TermQuery AND NumericRangeQuery
    BooleanQuery searchingBooks2010 = new BooleanQuery();              //#3
    searchingBooks2010.add(searchingBooks, BooleanClause.Occur.MUST);  //#3
    searchingBooks2010.add(books2010, BooleanClause.Occur.MUST);       //#3

    Directory dir = TestUtil.getBookIndexDirectory();
    IndexReader ireader = DirectoryReader.open(dir);
    IndexSearcher searcher = new IndexSearcher(ireader);
    TopDocs matches = searcher.search(searchingBooks2010, 10);
    //System.out.println("(subject = search)  AND (201001  < pubmonth < 201012)");
    System.out.println(searchingBooks2010);
    for(int i=0;i<matches.totalHits;i++) {
    	Document doc = searcher.doc(matches.scoreDocs[i].doc);
        System.out.println("match " + i + ": subject = " + doc.get("subject")+ " ; pubmonth = "+ doc.get("pubmonth"));
      }
    
    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches,
                                 "Lucene in Action, Second Edition"));
    //searcher.close();
    dir.close();
  }

/*
#1 Match books with subject “search”
#2 Match books in 2004
#3 Combines two queries
*/

  public void testOr() throws Exception {
	//TermQuery category = "/technology/computers/programming/methodology"
    TermQuery methodologyBooks = new TermQuery(                       // #1
               new Term("category",                                   // #1
                 "/technology/computers/programming/methodology"));   // #1

    //TermQuery category = "/philosophy/eastern"
    TermQuery easternPhilosophyBooks = new TermQuery(                 // #2
        new Term("category",                                          // #2
            "/philosophy/eastern"));                                  // #2

    
    //BooleanQuery = TermQuery1 should TermQuery2
    BooleanQuery enlightenmentBooks = new BooleanQuery();             // #3
    enlightenmentBooks.add(methodologyBooks,                          // #3
                           BooleanClause.Occur.SHOULD);               // #3
    enlightenmentBooks.add(easternPhilosophyBooks,                    // #3
                           BooleanClause.Occur.SHOULD);               // #3

    Directory dir = TestUtil.getBookIndexDirectory();
    IndexReader ireader = DirectoryReader.open(dir);
    IndexSearcher searcher = new IndexSearcher(ireader);
    TopDocs matches = searcher.search(enlightenmentBooks, 10);
    System.out.println("or = " + enlightenmentBooks);

    
    //System.out.println("category = /philosophy/eastern  OR /technology/computers/programming/methodology");
    for(int i=0;i<matches.totalHits;i++) {
    	Document doc = searcher.doc(matches.scoreDocs[i].doc);
        System.out.println("match " + i + ": title = " + doc.get("title")+ " ; category = "+ doc.get("category"));
      }
    
    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches,
                                         "Extreme Programming Explained"));
    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches,
                                         "Tao Te Ching \u9053\u5FB7\u7D93"));
   // searcher.close();
    dir.close();
  }

  /*
#1 Match 1st category
#2 Match 2nd category
#3 Combine
   */
}
