package lia.extsearch_6.perf;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRef;

public class SmartDayQueryParser extends QueryParser {
  public static final DateFormat formatter =
      new SimpleDateFormat("yyyyMMdd");

  public SmartDayQueryParser(String field, Analyzer analyzer) {
    super(field, analyzer);
  }

  
  
  
  protected Query getRangeQuery(String field, Analyzer analyzer,
                                String part1, String part2,
                                boolean inclusive) throws ParseException {
    try {
      DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT,  getLocale());
      df.setLenient(true);
      Date d1 = df.parse(part1);
      Date d2 = df.parse(part2);
      part1 = formatter.format(d1);
      part2 = formatter.format(d2);
    } catch (Exception ignored) {
    	ignored.printStackTrace();
    }
    Query query = new TermRangeQuery("last-modified",  new BytesRef(part1 ), new BytesRef(part2 ), true, true);
    return query;
  }
}