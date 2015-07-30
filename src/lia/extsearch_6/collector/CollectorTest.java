package lia.extsearch_6.collector;

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

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import java.util.Map;

// From chapter 6
public class CollectorTest extends TestCase {

  public void testCollecting() throws Exception {
	Directory dir = TestUtil.getBookIndexDirectory();
	IndexReader reader = DirectoryReader.open(dir);  
    IndexSearcher searcher = new IndexSearcher(reader);
    TermQuery query = new TermQuery(new Term("contents", "junit"));
    BookLinkCollector collector = new BookLinkCollector();
    searcher.search(query, collector);//collector.collect(int docID) viene chiamato ogni match.
    								  //collector deve salvare tutti i results !!

    Map<String,String> linkMap = collector.getLinks();
    assertEquals("Ant in Action",linkMap.get("http://www.manning.com/loughran"));

    TopDocs hits = searcher.search(query, 10);
    TestUtil.dumpHits(searcher, hits);

    //searcher.close();
    dir.close();
  }
}
