package org.molgenis.data.annotation.makervcf.genestream.impl;

import org.molgenis.data.annotation.makervcf.genestream.core.GeneStream;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.Iterator;
import java.util.List;

/**
 * Created by joeri on 6/29/16.
 *
 * Add gene FDR data to relevant variants
 *
 */
public class AddGeneFDR extends GeneStream{

    private Iterator<RelevantVariant> relevantVariants;
    private boolean verbose;

    public AddGeneFDR(Iterator<RelevantVariant> relevantVariants, boolean verbose)
    {
        super(relevantVariants, verbose);
    }

    @Override
    public void perGene(String gene, List<RelevantVariant> variantsPerGene) throws Exception {

    }


}
