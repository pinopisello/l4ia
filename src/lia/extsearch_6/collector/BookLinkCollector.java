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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.SimpleCollector;

// From chapter 6
public class BookLinkCollector extends SimpleCollector {
	private Map<String, String> documents = new HashMap<String, String>();
	private Scorer scorer;
	private LeafReader reader;

	@Override
	protected void doSetNextReader(LeafReaderContext context)
			throws IOException {
		reader = context.reader();
	}

	public void collect(int docID) {
		try {
			Document doc = reader.document(docID);
			String url = doc.get("url");		 // #C
			String title = doc.get("title"); 	 // #C
			documents.put(url, title); 			 // #C
			System.out.println(title + ":" + scorer.score());
		} catch (IOException e) {
			// ignore
		}
	}

	public Map<String, String> getLinks() {
		return Collections.unmodifiableMap(documents);
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}

	@Override
	public boolean needsScores() {
		return true;
	}
}

/*
 * #A Accept docIDs out of order 
 * #C Store details for the match
 */
