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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import junit.framework.TestCase;
import lia.common.TestUtil;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

// From chapter 2
public class LockTest extends TestCase {

  private Directory dir;
  private File indexDir;

  protected void setUp() throws IOException {
    indexDir = new File("indexes/Indexing");
    dir = FSDirectory.open( FileSystems.getDefault().getPath(indexDir.getAbsolutePath()));
  }

  public void testWriteLock() throws IOException {
	
	IndexWriterConfig iwconfig1=  new IndexWriterConfig(new SimpleAnalyzer()) ;
	iwconfig1.setInfoStream(System.out);
	IndexWriterConfig iwconfig2=  new IndexWriterConfig(new SimpleAnalyzer()) ;
	iwconfig2.setInfoStream(System.out);

    IndexWriter writer1 = new IndexWriter(dir,iwconfig1);
    boolean  islocked = writer1.isLocked(dir);
    //writer1.close();
    //islocked = writer1.isLocked(dir);
    IndexWriter writer2 = null;
    try {
      writer2 = new IndexWriter(dir, iwconfig2);//fallisce qui!!
                               
      fail("We should never reach this point");
    }
    catch (LockObtainFailedException e) {
       e.printStackTrace();  // #A
    }
    finally {
      writer1.close();
      assertNull(writer2);
      TestUtil.rmDir(indexDir);
    }
  }
}

/*
#A Expected exception: only one IndexWriter allowed at once
*/
