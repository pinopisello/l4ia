package lia.extsearch_6.perf;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import com.clarkware.profiler.Profiler;

/**
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */
public class Search {

  private IndexBuilder index;

  public Search() throws Exception {
    index = new IndexBuilder();
  }

  public ScoreDoc[]  searchByTimestamp(Date begin, Date end) throws Exception {
	BytesRef beginTerm = new BytesRef( DateTools.dateToString(begin, Resolution.SECOND));
	BytesRef endTerm = new BytesRef( DateTools.dateToString(end, Resolution.SECOND));
    Query query = new TermRangeQuery("last-modified", beginTerm, endTerm, true, true);
    return newSearcher(index.byTimestampIndexDirName()).search(query,1000000).scoreDocs;
  }

  public ScoreDoc[]  searchByDay(Date begin, Date end) throws Exception {
    BytesRef beginTerm = new BytesRef( DateTools.dateToString(begin, Resolution.DAY));
	BytesRef endTerm = new BytesRef( DateTools.dateToString(end, Resolution.DAY));
    Query query = new TermRangeQuery("last-modified", beginTerm, endTerm, true, true);
    return newSearcher(index.byDayIndexDirName()).search(query,1000000).scoreDocs;
  }

  public static Date janOneTimestamp() {
    Calendar firstDay = GregorianCalendar.getInstance();
    firstDay.set(2004, 0, 01); // Jan = 0
    return firstDay.getTime();
  }
  
  
  public static Date getDate(int year,int month,int day) {
	    Calendar firstDay = GregorianCalendar.getInstance();
	    firstDay.set(year, month, day); // Jan = 0
	    return firstDay.getTime();
	  }
  

  public static Date todayTimestamp() {
    return GregorianCalendar.getInstance().getTime();
  }

  public static String today() {
    SimpleDateFormat dateFormat =(SimpleDateFormat) SimpleDateFormat.getDateInstance();
    dateFormat.applyPattern("yyyyMMdd");
    return dateFormat.format(todayTimestamp());
  }

  private IndexSearcher newSearcher(Path indexDirName) throws IOException {
    Directory indexDirectory =  FSDirectory.open(indexDirName);  
    IndexReader reader = DirectoryReader.open(indexDirectory);  
    return new IndexSearcher(reader);
  }

  
  
  public static void main(String args[]) throws Exception {
    Search s = new Search();
    ScoreDoc[] result=null;
    
    
    //
    // Cache because it makes Lucene feel good
    //
    Profiler.begin("searchByTimestamp: 1");
    result = s.searchByTimestamp(Search.janOneTimestamp(),Search.todayTimestamp());
    Profiler.end("searchByTimestamp: 1");
    Profiler.begin("searchByDay: 1");
    result = s.searchByDay(getDate(2004,4,1), Search.todayTimestamp());
    Profiler.end("searchByDay: 1");

    //
    // Search by timestamp
    //
    Profiler.begin("searchByTimestamp: 2");
    ScoreDoc[] hits = s.searchByTimestamp(Search.janOneTimestamp(),Search.todayTimestamp());
    System.out.println(hits.length + " hits by timestamp");
    Profiler.end("searchByTimestamp: 2");

    //
    // Searby by day
    //
    Profiler.begin("searchByDay: 2");
    hits = s.searchByDay(getDate(2004,4,1),Search.todayTimestamp());
    System.out.println(hits.length + " hits by day");
    Profiler.end("searchByDay: 2");

    System.out.println("");
    Profiler.print();
  }
  
}