package lia.extsearch_6.perf;

import java.util.Date;

import junit.framework.TestCase;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

/**
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */
public class SearchTest extends TestCase {

  private static String today = Search.today();
  private static Date janOneTimestamp = Search.janOneTimestamp();
  private static Date todayTimestamp = Search.todayTimestamp();

  public SearchTest(String name) {
    super(name);
  }
  
  public SearchTest() {
	    super();
  }

  public void testSearchByTimestamp() throws Exception {
    Search s = new Search();
    ScoreDoc[] hits = s.searchByTimestamp(janOneTimestamp,todayTimestamp);
    assertEquals(1000, hits.length);
  }

  public void testSearchByDay() throws Exception {
    Search s = new Search();
    ScoreDoc[] hits = s.searchByDay(Search.getDate(2004,4,1), Search.todayTimestamp());
    assertEquals(1000, hits.length);
  }

  public void testQueryParsing() throws Exception {
    SmartDayQueryParser parser =new SmartDayQueryParser("contents", new StandardAnalyzer());
    //parser.setLocale(Locale.US);

    Query query = parser.parse("last-modified:[1/1/04 TO 2/29/04]");

    assertEquals("last-modified:[1/1/04 TO 2/29/04]",
        query.toString("contents"));
  }

}