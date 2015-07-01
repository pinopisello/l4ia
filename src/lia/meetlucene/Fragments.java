package lia.meetlucene;

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

import org.apache.lucene.store.*;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

// From chapter 1

/** Just contains any code fragments from chapter 1 */

public class Fragments {
	
	
	  public static void main(String[] args) throws  Exception {
		  simpleSearch();
	  }
	  
	  
  public static void simpleSearch() throws IOException {
	Directory dir = FSDirectory.open(  FileSystems.getDefault().getPath( "indexes/MeetLucene")); //3
	IndexReader ireader = DirectoryReader.open(dir);
    IndexSearcher is = new IndexSearcher(ireader);   //3   
    Query q = new TermQuery(new Term("filename", "apache1.0.txt"));
    TopDocs hits = is.search(q, 10);
  System.out.println("trovati " +hits.totalHits+" matching docs.");
  }
}