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

import lia.common.TestUtil;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;

// From chapter 5
public class BooksLikeThis {
	  
	  
	  
  public static void main(String[] args) throws IOException {
    Directory dir = TestUtil.getBookIndexDirectory();
    IndexReader reader = DirectoryReader.open(dir);
    int numDocs = reader.maxDoc();

    BooksLikeThis blt = new BooksLikeThis(reader);
    for (int i = 0; i < numDocs; i++) {                    // #1
      System.out.println();
      Document doc = reader.document(i);
      System.out.println(doc.get("title"));

      Document[] docs = blt.docsLike(i, 10);               // #2
      if (docs.length == 0) {
        System.out.println("  None like this");
      }
      for (Document likeThisDoc : docs) {
        System.out.println("  -> " + likeThisDoc.get("title"));
      }
    }
    reader.close();
    dir.close();
  }

  private IndexReader reader;
  private IndexSearcher searcher;

  public BooksLikeThis(IndexReader reader) {
    this.reader = reader;
    searcher = new IndexSearcher(reader);
  }

  
  
  
  //trova tutti i docs simili al doc con lo specifico id.
  //Simili vuol dire con author 
  public Document[] docsLike(int id, int max) throws IOException {
    Document doc = reader.document(id);

    String[] authors = doc.getValues("author");
    BooleanQuery authorQuery = new BooleanQuery();                 // #3  Boosts books by same author Rudolf Steiner
    for (String author : authors) {                                // #3
      authorQuery.add(new TermQuery(new Term("author", author)),   // #3
          BooleanClause.Occur.SHOULD);                             // #3
    }
    authorQuery.setBoost(2.0f);

    //Ritorna tutti i Terms per il campo "subject" del doc "id"
    Terms vector = reader.getTermVector(id, "subject");                   // #4 Use terms from "subject" term vectors 
    																//Subject e' indicizzatp con TermVector.Altrimenti vector sarebbe null!!CreateTestIndex
    BooleanQuery subjectQuery = new BooleanQuery();                // #4
    
    TermsEnum termsiterator = vector.iterator();
    BytesRef currTerm = null;
    
    while ( (currTerm = termsiterator.next()) != null) {                      // #4
    	String vecTermStr = currTerm.utf8ToString();
    	TermQuery tq = new TermQuery(  new Term("subject", vecTermStr));              // #4
      subjectQuery.add(tq, BooleanClause.Occur.SHOULD);            // #4
    }

    BooleanQuery likeThisQuery = new BooleanQuery();               // #5
    likeThisQuery.add(authorQuery, BooleanClause.Occur.SHOULD);    // #5
    likeThisQuery.add(subjectQuery, BooleanClause.Occur.SHOULD);   // #5

    likeThisQuery.add(new TermQuery(                                       // #6
        new Term("isbn", doc.get("isbn"))), BooleanClause.Occur.MUST_NOT); // #6

    // System.out.println("  Query: " +
    //    likeThisQuery.toString("contents"));
    TopDocs hits = searcher.search(likeThisQuery, 10);
    int size = max;
    if (max > hits.scoreDocs.length) size = hits.scoreDocs.length;

    Document[] docs = new Document[size];
    for (int i = 0; i < size; i++) {
      docs[i] = reader.document(hits.scoreDocs[i].doc);
    }

    return docs;
  }
}
/*
#1 Iterate over every book
#2 Look up books like this
#3 Boosts books by same author
#4 Use terms from "subject" term vectors 
#5 Create final query
#6 Exclude current book
*/
