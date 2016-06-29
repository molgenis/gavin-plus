package org.molgenis.data.annotation.makervcf;

import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by joeri on 6/29/16.
 */
public class CorrectPositionalOrderIterator {

    private Iterator<RelevantVariant> relevantVariants;
    ArrayList<Integer> order;

    public CorrectPositionalOrderIterator(Iterator<RelevantVariant> relevantVariants, ArrayList<Integer> order)
    {
        this.relevantVariants = relevantVariants;
        this.order = order;
    }

    public Iterator<RelevantVariant> go() {
        return new Iterator<RelevantVariant>() {

            List<RelevantVariant> buffer = new ArrayList<RelevantVariant>();
            RelevantVariant nextResult;
            int i = 0;

            @Override
            public boolean hasNext() {


                while (relevantVariants.hasNext()) {
                    try {
                        RelevantVariant rv = relevantVariants.next();

                        System.out.println("rvPos = " + rv.getVariant().getPos() + ", orderPos["+i+"] = " + order.get(i) + ", order.size() " + order.size());


                        //position of stream matches real variant position, stable situation
                        //write out any buffered variants in the correct order until this point
                        if(rv.getVariant().getPos().equals(order.get(i)))
                        {
                            if(buffer.size() > 0)
                            {
                                nextResult = buffer.get(0);
                            }
                            nextResult = rv;
                            return true;
                        }
                        else
                        {
                            //buffer
                            buffer.add(rv);
                        }

                        i++;

                        nextResult = rv;
                        return true;

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
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
