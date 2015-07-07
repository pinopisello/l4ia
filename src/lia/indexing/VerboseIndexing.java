package lia.indexing;

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
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

// From chapter 2
public class VerboseIndexing {

  private void index() throws IOException {
    IndexWriterConfig iwconfig=  new IndexWriterConfig(new WhitespaceAnalyzer()) ;
	iwconfig.setInfoStream(System.out);  //stampa in system.out un botto di roba
	double maxbuffersize = iwconfig.getRAMBufferSizeMB();
	int maxbuffereddocs = iwconfig.getMaxBufferedDocs();
	int maxdeleteddocs = iwconfig.getMaxBufferedDocs();
	iwconfig.setMaxBufferedDocs(2);
	//Directory dir = new RAMDirectory();
	Path pth = FileSystems.getDefault().getPath( "indexes/Indexing");
	Directory dir =FSDirectory.open(pth);
	
    IndexWriter writer = new IndexWriter(dir, iwconfig);


    for (int i = 0; i < 100; i++) {
      Document doc = new Document();
      doc.add(new Field("keyword", "goober", Field.Store.YES, Field.Index.NOT_ANALYZED));
      writer.addDocument(doc);
    }
    
    //writer.deleteAll();
 
    writer.prepareCommit();
    writer.commit();
  
    writer.close();
  }

  public static void main(String[] args) throws IOException {
    VerboseIndexing vi = new VerboseIndexing();
    vi.index();
  }
}
