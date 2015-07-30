package lia.extsearch_6.filters;

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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BitsFilteredDocIdSet;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.BitDocIdSet;
import org.apache.lucene.util.Bits;

// From chapter 6
public class SpecialsFilter extends Filter {
  private SpecialsAccessor accessor;
  private LeafReader reader;
  
  
  public SpecialsFilter(SpecialsAccessor accessor) {
    this.accessor = accessor;
  }

 
  public DocIdSet getDocIdSet(LeafReaderContext context, Bits acceptDocs) throws IOException{
	reader = context.reader();
	BitDocIdSet.Builder res  = new BitDocIdSet.Builder(reader.maxDoc());	
    String[] isbns = accessor.isbns();                  // #1 Fetch ISBNs permessi
    for (String isbn : isbns) {
      if (isbn != null) {
    	PostingsEnum matching = reader.postings(new Term("isbn",  isbn));  //ritorna tutti i postings con il field "isbn" contenente  isbn
    	int currDocId = -1;
    	res.or(matching); 			                       // #3   Set corresponding bit
    	while((currDocId = matching.nextDoc())!= PostingsEnum.NO_MORE_DOCS){
    		Document currDoc = reader.document(currDocId);     //#2 Jump to term
    		String currIsbn = currDoc.get("isbn");
    		System.out.println("currIsbn: "+currIsbn);
        }                        
      }
    }
    DocIdSet out = BitsFilteredDocIdSet.wrap(res.build(), acceptDocs);
    return out;
  }
  /*
#1 Fetch ISBNs
#2 Jump to term
#3 Set corresponding bit
  */



@Override
public String toString(String field) {
	// TODO Auto-generated method stub
	return null;
}
}
