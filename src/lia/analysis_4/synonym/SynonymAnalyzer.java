package lia.analysis_4.synonym;

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

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

// From chapter 4
public class SynonymAnalyzer extends Analyzer {
  private SynonymEngine engine;

  public SynonymAnalyzer(SynonymEngine engine) {
    this.engine = engine;
  }

  /*
  public TokenStream tokenStream(String fieldName, Reader reader) {
	  TokenStream result = null;
	  
   TokenStream result = new SynonymFilter(
                          new StopFilter(true,
                            new LowerCaseFilter(
                              new StandardFilter(
                                new StandardTokenizer(
                                 Version.LUCENE_30, reader))),
                            StopAnalyzer.ENGLISH_STOP_WORDS_SET),
                          engine
                         );
    return result;
  }*/

@Override
protected TokenStreamComponents createComponents(String fieldName) {
	// TODO Auto-generated method stub
	return null;
}
}
