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
               //     System.out.println("returning next element in the buffer");
                    nextResult = bufferPrinter.next();
                    return true;
                }

                while (relevantVariants.hasNext()) {
                    try {

             //           System.out.println("hasNext called, buffer size = " + buffer.size());

                        i++;

                        RelevantVariant rv = relevantVariants.next();
                        int pos = rv.getVariant().getPos();
         //               System.out.println("rvPos = " + pos + ", orderPos["+i+"] = " + order.get(i) + ", order.size() " + order.size());


                        //position of stream matches real variant position, stable situation
                        //write out any buffered variants in the correct order until this point
                        if(pos == order.get(i))
                        {
                            if(buffer.size() == 0)
                            {
             //                   System.out.println("buffer empty, returning current element");
                                nextResult = rv;
                                return true;
                            }
                            else
                            {
                                buffer.put(pos, rv);
                                bufferPrinter = buffer.values().iterator();
                                if(verbose){ System.out.println("[ConvertBackToPositionalStream] Positions aligned again at "+pos+", clearning buffer with "+buffer.size()+" elements");}
                                buffer = new TreeMap<>(); //todo ok??
                                nextResult = bufferPrinter.next();
                                return true;
                            }

                        }
                        else
                        {
                            //buffer
             //               System.out.println("adding to buffer");
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
