package org.molgenis.data.annotation.makervcf.genestream.core;

import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by joeri on 6/29/16.
 * FIXME: any way to clean up 'order' during streaming of results? ie. for any positions that have been written out?
 */
public class ConvertBackToPositionalStream {

    private Iterator<RelevantVariant> relevantVariants;
    private ArrayList<Integer> order;
    private boolean verbose;

    public ConvertBackToPositionalStream(Iterator<RelevantVariant> relevantVariants, ArrayList<Integer> order, boolean verbose)
    {
        this.relevantVariants = relevantVariants;
        this.order = order;
        this.verbose = verbose;
    }

    public Iterator<RelevantVariant> go() {
        return new Iterator<RelevantVariant>() {

            TreeMap<Integer, RelevantVariant> buffer = new TreeMap<Integer, RelevantVariant>();
            Iterator<RelevantVariant> bufferPrinter;
            RelevantVariant nextResult;
            int i = -1;

            @Override
            public boolean hasNext() {

                if(bufferPrinter != null && bufferPrinter.hasNext())
                {
                    if(verbose){ System.out.println("[ConvertBackToPositionalStream] Returning next element in the buffer"); }
                    nextResult = bufferPrinter.next();
                    return true;
                }

                while_:
                while (relevantVariants.hasNext()) {
                    try {
                        i++;

                        RelevantVariant rv = relevantVariants.next();
                        int pos = rv.getVariant().getPos();

                        //position of stream matches real variant position, stable situation
                        //write out any buffered variants in the correct order until this point
                        if(pos == order.get(i))
                        {
                            if(buffer.size() == 0)
                            {
                                if(verbose){ System.out.println("[ConvertBackToPositionalStream] Buffer empty, returning current element " + pos); }
                                nextResult = rv;
                                return true;
                            }
                            else
                            {
                                buffer.put(pos, rv);
                                if(verbose){ System.out.println("[ConvertBackToPositionalStream] Buffer size > 0, adding to buffer " + pos); }

                                // check if all positions are present up to the current one
                                // to prevent problem: we see 20, 23, 22, 21 where alignment at 20 and 22, and we output wrongly 20, 23, 22, 21 because we haven't seen 21 yet
                                // easiest check: all numbers in buffer are lower than current position
                                for(Integer checkPos : buffer.keySet())
                                {
                                    if(checkPos > pos)
                                    {
                                        continue while_;
                                    }
                                }

                                bufferPrinter = buffer.values().iterator();
                                if(verbose){ System.out.println("[ConvertBackToPositionalStream] Positions aligned again at "+pos+", all values smaller than current pos, so clearning buffer with "+buffer.size()+" elements");}
                                buffer = new TreeMap<>();
                                nextResult = bufferPrinter.next();
                                return true;
                            }

                        }
                        else
                        {
                            //buffer
                            if(verbose){ System.out.println("[ConvertBackToPositionalStream] Adding to buffer " + pos); }
                            buffer.put(pos, rv);
                        }
                        
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                if(bufferPrinter != null && bufferPrinter.hasNext())
                {
                    if(verbose){ System.out.println("[ConvertBackToPositionalStream] Clearing last elements from buffer");}
                    nextResult = bufferPrinter.next();
                    return true;
                }

                return false;
            }

            @Override
            public RelevantVariant next() {
                return nextResult;
            }
        };
    }
}
