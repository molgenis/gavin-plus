package org.molgenis.data.annotation.makervcf;

import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.vcf.datastructures.Trio;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by joeri on 6/29/16.
 *
 * mark denovo?
 * remove variants that are no longer relevant?
 * remove compound het on same allele?
 * re-use phasing data?
 *
 *
 */
public class TrioAwareFilter {

    private Iterator<RelevantVariant> relevantVariants;
    private HashMap<String, Trio> trios;
    public TrioAwareFilter(Iterator<RelevantVariant> relevantVariants, HashMap<String, Trio> trios)
    {
        this.relevantVariants = relevantVariants;
        this.trios = trios;
    }

    public Iterator<RelevantVariant> go()
    {
        return new Iterator<RelevantVariant>(){

            RelevantVariant nextResult;

            @Override
            public boolean hasNext() {
                try {
                    while (relevantVariants.hasNext()) {
                        RelevantVariant rv = relevantVariants.next();

                        if (rv.getSampleStatus() != null) {

                            for (String sample : rv.getSampleStatus().keySet()) {
                                String status = rv.getSampleStatus().get(sample);

                                if (status.equals("HETEROZYGOUS")) {
                                    if (trios.containsKey(sample)) {
                                        Trio t = trios.get(sample);
                                        //get parental genotype.... or just check if affected too ??
                                    }
                                }
                            }
                        }

                        nextResult = rv;
                        return true;
                    }

                    return false;
                }
                catch(Exception e)
                {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public RelevantVariant next() {
                return nextResult;
            }
        };
    }
}
