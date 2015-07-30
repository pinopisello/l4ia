package lia.common;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;


//trasforma tutti i termini lowercase e replace .,_,/ cons spazi
public final class MyAnalyzer extends StopwordAnalyzerBase {
	  private int maxTokenLength = 255;
	  
	  
	  public MyAnalyzer() {
	    super( StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	  }


	  @Override
	  protected Reader initReader(String fieldName, Reader reader) {
	    NormalizeCharMap.Builder builder = new NormalizeCharMap.Builder();
	    builder.add(".", " ");
	    builder.add("_", " ");
	    builder.add("/", " ");
	    NormalizeCharMap normMap = builder.build();
	    return new MappingCharFilter(normMap, reader);
	  }

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		final StandardTokenizer src = new StandardTokenizer( );
	    src.setMaxTokenLength(maxTokenLength);
	    TokenStream tok = new StandardFilter( src);
	    tok = new LowerCaseFilter(tok);
	    tok = new StopFilter( tok, stopwords);
	    return new TokenStreamComponents(src, tok) {
	      @Override
	      protected void setReader(final Reader reader) throws IOException {
	        src.setMaxTokenLength(MyAnalyzer.this.maxTokenLength);
	        super.setReader(reader);
	      }
	    };
		
	}
	}
