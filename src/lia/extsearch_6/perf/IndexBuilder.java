package lia.extsearch_6.perf;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import lia.common.MyAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */
public class IndexBuilder {
  private Path timestamp_path;
  private Path day_path;


  public IndexBuilder() throws Exception {
	timestamp_path =  FileSystems.getDefault().getPath("indexes/Perf/timestamp");
	day_path =  FileSystems.getDefault().getPath("indexes/Perf/day");
  }

  public Path byTimestampIndexDirName() {
    return timestamp_path;
  }

  public Path byDayIndexDirName() {
    return day_path;
  }

  //Costruisce due indici in:
  //Perf/day:           1000 docs  con field last-modified con valori ascendenti da now di un secondo [20150729211912]
  //Perf/timesamp:      1000 docs  con field last-modified con stesso valore oggi [20150730]
  
  public void buildIndex(int size) throws Exception {
    buildIndexByTimestamp(byTimestampIndexDirName(), size);
    buildIndexByDay(byDayIndexDirName(), size);
  }

  public void buildIndexByTimestamp(Path dirName, int size)throws Exception {
    IndexWriter writer = newIndexWriter(dirName);
    Calendar timestamp = GregorianCalendar.getInstance();
    timestamp.set(Calendar.DATE,  timestamp.get(Calendar.DATE) - 1);
    for (int i = 0; i < size; i++) {
      timestamp.set(Calendar.SECOND, timestamp.get(Calendar.SECOND) + 1);
      Date now = timestamp.getTime();
      String now_string = DateTools.dateToString(now,Resolution.SECOND);
      Document document = new Document();
      document.add( new StringField("last-modified", now_string,Field.Store.YES));
      writer.addDocument(document);
    }
    writer.close();
  }

  public void buildIndexByDay(Path dirName, int size) throws Exception {
    String today = Search.today();
    IndexWriter writer = newIndexWriter(dirName);
    for (int i = 0; i < size; i++) {
      Document document = new Document();
      document.add( new StringField("last-modified",   today, Field.Store.YES)); 
      writer.addDocument(document);
    }
    writer.close();
  }

  private IndexWriter newIndexWriter(Path dirName) throws IOException {
    Directory indexDirectory =FSDirectory.open(dirName );
    Analyzer analyzer = new MyAnalyzer();
    IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
    iwConfig.setOpenMode(OpenMode.CREATE);
    IndexWriter w = new IndexWriter(indexDirectory, iwConfig);  
    return w;
  }

  public static void main(String args[]) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: IndexBuilder <size>");
      System.exit(0);
    }
    IndexBuilder builder = new IndexBuilder();
    builder.buildIndex(Integer.parseInt(args[0]));
  }
}