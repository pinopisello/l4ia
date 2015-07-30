package lia.indexing_2;

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
import java.util.Calendar;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

// From chapter 2

/** Just to test the code compiles. */
class Fragments {

  public static void indexNumbersMethod() {
    // START
    new Field("size", "4096", Field.Store.YES, Field.Index.NOT_ANALYZED);
    new Field("price", "10.99", Field.Store.YES, Field.Index.NOT_ANALYZED);
    new Field("author", "Arthur C. Clark", Field.Store.YES, Field.Index.NOT_ANALYZED);
    // END
  }

  public static final String COMPANY_DOMAIN = "example.com";
  public static final String BAD_DOMAIN = "yucky-domain.com";

  private String getSenderEmail() {
    return "bob@smith.com";
  }

  private String getSenderName() {
    return "Bob Smith";
  }

  private String getSenderDomain() {
    return COMPANY_DOMAIN;
  }

  private String getSubject() {
    return "Hi there Lisa";
  }

  private String getBody() {
    return "I don't have much to say";
  }

  private boolean isImportant(String lowerDomain) {
    return lowerDomain.endsWith(COMPANY_DOMAIN);
  }

  private boolean isUnimportant(String lowerDomain) {
    return lowerDomain.endsWith(BAD_DOMAIN);
  }

  public void ramDirExample() throws Exception {
    Analyzer analyzer = new WhitespaceAnalyzer();
    analyzer.setVersion(Version.LUCENE_5_2_1); 
    IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
    Directory ramDir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(ramDir, iwConfig);    
                                
    // END
  }

  public void dirCopy() throws Exception {
    Directory otherDir = null;

    // START
    Directory ramDir = new RAMDirectory(null);
    // END
  }

  public void addIndexes() throws Exception {
    IndexWriter writer = new IndexWriter(new RAMDirectory(),  new IndexWriterConfig(new WhitespaceAnalyzer()));    
   
    writer.addIndexes(new Directory[] {new RAMDirectory()});
    // END
  }

  public void docBoostMethod() throws IOException {

    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(new RAMDirectory(),  new IndexWriterConfig(new WhitespaceAnalyzer()));    
    
    // START
    Document doc = new Document();
    String senderEmail = getSenderEmail();
    String senderName = getSenderName();
    String subject = getSubject();
    String body = getBody();
    doc.add(new Field("senderEmail", senderEmail,
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED));
    doc.add(new Field("senderName", senderName,
                      Field.Store.YES,
                      Field.Index.ANALYZED));
    doc.add(new Field("subject", subject,
                      Field.Store.YES,
                      Field.Index.ANALYZED));
    doc.add(new Field("body", body,
                      Field.Store.NO,
                      Field.Index.ANALYZED));
    String lowerDomain = getSenderDomain().toLowerCase();
    if (isImportant(lowerDomain)) {
     // doc.setBoost(1.5F);     //1
    } else if (isUnimportant(lowerDomain)) {
    //  doc.setBoost(0.1F);    //2 
    }
    writer.addDocument(doc);
    // END
    writer.close();

    /*
      #1 Good domain boost factor: 1.5
      #2 Bad domain boost factor: 0.1
    */
  }

  public void fieldBoostMethod() throws IOException {

    String senderName = getSenderName();
    String subject = getSubject();

    // START
    Field subjectField = new Field("subject", subject,
                                   Field.Store.YES,
                                   Field.Index.ANALYZED);
    subjectField.setBoost(1.2F);
    // END
  }

  public void numberField() {
    Document doc = new Document();
    // START
    doc.add(  new FloatField("price",19.99f,Store.YES));
    // END
  }

  public void numberTimestamp() {
    Document doc = new Document();
    // START
    doc.add(new LongField("timestamp",new Date().getTime(),Store.YES));
    // END

    // START
    doc.add(new LongField("day",new Date().getTime()/24/3600,Store.YES));
           
    // END

    Date date = new Date();
    // START
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    doc.add(new IntField("dayOfMonth",cal.get(Calendar.DAY_OF_MONTH),Store.YES));
         
    // END
  }

  public void setInfoStream() throws Exception {
    Directory dir = null;
    Analyzer analyzer = null;
    // START
    IndexWriter writer = new IndexWriter(new RAMDirectory(),  new IndexWriterConfig(new WhitespaceAnalyzer()).setInfoStream(System.out)  );    
  
    // END
  }

  public void dateMethod() {
    Document doc = new Document();
    doc.add(new Field("indexDate",
                      DateTools.dateToString(new Date(), DateTools.Resolution.DAY),
                      Field.Store.YES,
                      Field.Index.NOT_ANALYZED));
  }

  public void numericField() throws Exception {
    Document doc = new Document();
    DoubleField price = new DoubleField("price",19.99d,Store.YES);
    doc.add(price);

    LongField timestamp = new LongField("timestamp",new Date().getTime(),Store.YES);
    doc.add(timestamp);

    Date b = new Date();
    String v = DateTools.dateToString(b, DateTools.Resolution.DAY);
    IntField birthday = new IntField("birthday",Integer.parseInt(v),Store.YES);

    doc.add(birthday);
  }

  public void indexAuthors() throws Exception {
    String[] authors = new String[] {"lisa", "tom"};
    // START
    Document doc = new Document();
    for (String author: authors) {
      doc.add(new Field("author", author,
                        Field.Store.YES,
                        Field.Index.ANALYZED));
    }
    // END
  }
}

