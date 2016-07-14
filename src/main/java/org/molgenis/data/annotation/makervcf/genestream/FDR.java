package org.molgenis.data.annotation.makervcf.genestream;

import org.molgenis.data.annotation.makervcf.genestream.core.GeneStream;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance.status;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by joeri on 6/29/16.
 *
 * False Discovery Rate
 */
public class FDR extends GeneStream{

    private PrintWriter pw;

    public FDR(Iterator<RelevantVariant> relevantVariants, File writeTo, boolean verbose) throws FileNotFoundException {
        super(relevantVariants, verbose);
        this.pw = new PrintWriter(writeTo);
        this.pw.println("Gene\tFDR");
    }

    @Override
    public void perGene(String gene, List<RelevantVariant> variantsPerGene) throws Exception {

        Set<String> uniquelyAffectedSamplesForThisGene = new HashSet<String>();
        for(RelevantVariant rv : variantsPerGene)
        {
            //extra check, may be removed FIXME
            if(!rv.getGene().equals(gene))
            {
                throw new Exception("ERROR Gene mismatch: " + rv.getGene() + " vs " + gene);
            }
            for(String sample : rv.getSampleStatus().keySet())
            {
                if(!status.isCarrier(rv.getSampleStatus().get(sample))){
                    uniquelyAffectedSamplesForThisGene.add(sample);
                }
            }
        }

        pw.println(gene + "\t" + (uniquelyAffectedSamplesForThisGene.size()));
        pw.flush();
    }

}
