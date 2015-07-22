package lia.common;

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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class CreateTestIndex {
	
	public static final FieldType TYPE_WITH_TERM_VECT = new FieldType();
	  
	  static {
		  TYPE_WITH_TERM_VECT.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		  TYPE_WITH_TERM_VECT.setStored(true);
		  TYPE_WITH_TERM_VECT.setTokenized(true);
		  TYPE_WITH_TERM_VECT.setStoreTermVectors(true);
		  TYPE_WITH_TERM_VECT.freeze();
		  }
	  
	  
  public static Document getDocument(String rootDir, File file) throws IOException {
    Properties props = new Properties();
    props.load(new FileInputStream(file));

    Document doc = new Document();

    // category comes from relative path below the base directory
    String category = file.getParent().substring(rootDir.length());    //1
    category = category.replace(File.separatorChar, '/');              //1

    String isbn = props.getProperty("isbn");         //2
    String title = props.getProperty("title");       //2
    String author = props.getProperty("author");     //2
    String url = props.getProperty("url");           //2
    String subject = props.getProperty("subject");   //2
    String pubmonth = props.getProperty("pubmonth"); //2

    System.out.println(file.getName()+ "   ->  " + title + "\n" + author + "\n" + subject + "\n" + pubmonth + "\n" + category + "\n---------");

    doc.add(new StringField("isbn",                     // 3
                      isbn,                       // 3
                      Field.Store.YES)); // 3
    doc.add(new StringField("category",                 // 3
                      category,                   // 3
                      Field.Store.YES)); // 3
    
    //aggiunge docvalues per poter sortare per "category" i risultati di queries.
    //Vedi SortingExample.java
    doc.add(new SortedDocValuesField("category", new BytesRef(category)));
    
    
    
    doc.add(new TextField("title",                    // 3
                      title,                      // 3
                      Field.Store.YES));   // 3
    doc.add(new TextField("title2",                   // 3
                      title.toLowerCase(),        // 3
                      Field.Store.YES));  // 3

    // split multiple authors into unique field instances
    String[] authors = author.split(",");            // 3
    for (String a : authors) {                       // 3
      doc.add(new StringField("author",                    // 3
                        a,                           // 3
                        Field.Store.YES));   // 3
    }

    doc.add(new StringField("url",                        // 3
                      url,                           // 3
                      Field.Store.YES));   // 3
    doc.add(new Field("subject",                     // 3  //4
                      subject,                       // 3  //4
                      TYPE_WITH_TERM_VECT)); // 3  //4

    
    doc.add( new IntField("pubmonth",Integer.parseInt(pubmonth),Store.YES));
    doc.add(new NumericDocValuesField("pubmonth", Integer.parseInt(pubmonth)));
    
    
    
   //doc.add(new NumericField("pubmonth",          // 3
    //                         Field.Store.YES,     // 3
   //                          true).setIntValue(Integer.parseInt(pubmonth)));   // 3

    Date d; // 3
    try { // 3
      d = DateTools.stringToDate(pubmonth); // 3
    } catch (ParseException pe) { // 3
      throw new RuntimeException(pe); // 3
    }          
    
    // 3
    
    doc.add( new LongField("pubmonthAsDay",d.getTime()/(1000*3600*24),Store.YES));
   // doc.add(new NumericField("pubmonthAsDay")      // 3
    //             .setIntValue((int) (d.getTime()/(1000*3600*24))));   // 3

    for(String text : new String[] {title, subject, author, category}) {       
      doc.add(new TextField("contents", text,Field.Store.NO));    
    }

    return doc;
  }

  private static String aggregate(String[] strings) {
    StringBuilder buffer = new StringBuilder();

    for (int i = 0; i < strings.length; i++) {
      buffer.append(strings[i]);
      buffer.append(" ");
    }

    return buffer.toString();
  }

  private static void findFiles(List<File> result, File dir) {
    for(File file : dir.listFiles()) {
      if (file.getName().endsWith(".properties")) {
        result.add(file);
      } else if (file.isDirectory()) {
        findFiles(result, file);
      }
    }
  }

 /* private static class MyStandardAnalyzer extends StandardAnalyzer {  // 6
    public MyStandardAnalyzer( ) {                 // 6
      super();                                            // 6
    }                                                                 // 6
    public int getPositionIncrementGap(String field) {                // 6
      if (field.equals("contents")) {                                 // 6
        return 100;                                                   // 6
      } else {                                                        // 6
        return 0;                                                     // 6
      }
    }
  }*/

  public static void main(String[] args) throws IOException {
    String dataDir = args[0];
    String indexDir = args[1];
    List<File> results = new ArrayList<File>();
    findFiles(results, new File(dataDir));
    System.out.println(results.size() + " books to index");
    Directory dir = FSDirectory.open(  FileSystems.getDefault().getPath(indexDir));
    //Directory dir = FSDirectory.open(new File(indexDir));
    
   
   // Analyzer analyzer = new LimitTokenCountAnalyzer(new WhitespaceAnalyzer(),6);
    Analyzer analyzer = new SimpleAnalyzer();
    analyzer.setVersion(Version.LUCENE_5_2_1); 
    IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
    iwConfig.setOpenMode(OpenMode.CREATE);
    //iwConfig.setInfoStream(System.err);
    IndexWriter w = new IndexWriter(dir, iwConfig);           //3
    
   // IndexWriter w = new IndexWriter(dir,
    //                                new MyStandardAnalyzer(Version.LUCENE_30),
    //                                true,
    //                                IndexWriter.MaxFieldLength.UNLIMITED);
    for(File file : results) {
      Document doc = getDocument(dataDir, file);
      w.addDocument(doc);
    }
    w.close();
    dir.close();
  }
}

/*
  #1 Get category
  #2 Pull fields
  #3 Add fields to Document instance
  #4 Flag subject field
  #5 Add catch-all contents field
  #6 Custom analyzer to override multi-valued position increment
*/
