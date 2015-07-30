package lia.extsearch_6.sorting;

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
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SimpleFieldComparator;
import org.apache.lucene.util.Bits;

import sun.awt.SunHints.Value;
 
public class DistanceComparatorSource  extends FieldComparatorSource { // #1 Extend FieldComparatorSource
  private int x;
  private int y;

  public DistanceComparatorSource(int x, int y) { // #2  Give constructor base location
    this.x = x;
    this.y = y;
  }

  public FieldComparator newComparator(String fieldName,   		// #3 Create comparator
                                       int numHits, int sortPos,   			// #3
                                       boolean reversed)   			 throws IOException {       // #3

    return new MioComparator( fieldName,1000000);
  }

  
  public  class MioComparator extends SimpleFieldComparator<Float>{
	    //Campi relativi a NumericComparator
	    protected final Integer missingValue;
	    protected final String field;  
	    protected NumericDocValues currentReaderXValues;
	    protected NumericDocValues currentReaderYValues;
	    
	    
	    //Campi reltivi a IntegerComparator
	    private List<long[]> xypositions=new ArrayList<long[]>();
	    private List<Float> values = new ArrayList<Float>();
	    private float bottom;
	    private float top;
	    
	    public MioComparator( String field, Integer missingValue) {
	    	 this.field = field;
	         this.missingValue = missingValue;
	    }

	    //Metodo di SimpleFieldComparator
	    @Override
	    protected void doSetNextReader(LeafReaderContext context) throws IOException {
	    	currentReaderXValues = getNumericDocValues(context, "x");
	    	currentReaderYValues = getNumericDocValues(context, "y");
	    }
	    
	    /** Retrieves the NumericDocValues for the field in this segment */
	    protected NumericDocValues getNumericDocValues(LeafReaderContext context, String field) throws IOException {
	      return DocValues.getNumeric(context.reader(), field);
	    }

	    
	    
        //Metodo di LeafFieldComparator
		@Override
		public void setBottom(int slot) {
			this.bottom = values.get(slot);
			
		}

	    //Metodo di LeafFieldComparator
		@Override
		public int compareBottom(int doc) throws IOException {
			  float docDistance = getDistance(doc);
		      if (bottom < docDistance) return -1;              // #12
		      if (bottom > docDistance) return 1;               // #12
		      return 0;                                         // #12
		}
		
		//Metodo di LeafFieldComparator
		@Override
		public int compareTop(int doc) throws IOException {
			 float docDistance = getDistance(doc);
		      if (top < docDistance) return -1;              // #12
		      if (top > docDistance) return 1;               // #12
		      return 0;                                         // #12
		}

		
		//Metodo di LeafFieldComparator
        //slot e' la posizione di doc in TopDocsCollector PriorityQueue<T> pq;
		//copy e' chiamato da TopDocsCollector per dire al Comparator quale e' la hit corrente e la sua posizione.
		@Override
		public void copy(int slot, int doc) throws IOException {
	       long x =  currentReaderXValues.get(doc);
	       long y =  currentReaderYValues.get(doc);
	       xypositions.add(slot, new long[]{x,y}); 
	       values.add(slot, getDistance(doc));
		}

		//Metodo di FieldComparator
		//Chiamato da FieldValueHitQueue.lessThan()
		@Override
		public int compare(int slot1, int slot2) {
			float slot1value = values.get(slot1);
			float slot2value = values.get(slot2);
		    return Float.compare(slot1value, slot2value);                                      
		}

		//Metodo di FieldComparator
		@Override
		public void setTopValue(Float value) {
			this.top = value;
			
		}

		//Metodo di FieldComparator
		@Override
		public Float value(int slot) {
			return values.get(slot);
		}
		
		
	    private float getDistance(int doc) { 
	    	float out = 10000000f;
	    	if(xypositions.size()<(doc)){
	        long deltax = xypositions.get(doc)[0] - x;                    
	        long deltay = xypositions.get(doc)[1] - y; 
	         out = (float)Math.sqrt(deltax * deltax + deltay * deltay);
	    	}
	        return out ; 
	      }




	}


  public String toString() {
    return "Distance from ("+x+","+y+")";
  }
}

/*
#1 Extend FieldComparatorSource
#2 Give constructor base location
#3 Create comparator
#4 FieldComparator implementation
#5 Array of x, y per document
#6 Distances for documents in the queue
#7 Worst distance in the queue
#8 Get x, y values from field cache
#9 Compute distance for one document
#10 Compare two docs in the top N
#11 Record worst scoring doc in the top N
#12 Compare new doc to worst scoring doc
#13 Insert new doc into top N
#14 Extract value from top N
*/

