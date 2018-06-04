package org.molgenis.data.annotation.makervcf.genestream.impl;

import org.molgenis.data.annotation.makervcf.genestream.core.GeneStream;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.Relevance;

import java.util.Iterator;
import java.util.List;

/**
 * Created by joeri on 6/29/16.
 *  take on MANTA / DELLY output
 *  annotate, elevate carrier status to affected when SV on same location
 *
 *  TODO JvdV
 *
 */
public class CombineWithSVcalls extends GeneStream{
    public CombineWithSVcalls(Iterator<GavinRecord> relevantVariants)
    {
        super(relevantVariants);
    }

    @Override
    public void perGene(String gene, List<GavinRecord> variantsPerGene){

        for(GavinRecord rv: variantsPerGene) {
            for (Relevance rlv : rv.getRelevance()) {
                if (!rlv.getGene().equals(gene)) {
                    continue;
                }
                for (String sample : rlv.getSampleStatus().keySet()) {

                }
            }
        }
    }


}
