package org.molgenis.data.annotation.makervcf.genestream.impl;

import org.molgenis.data.annotation.makervcf.genestream.core.GeneStream;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by joeri on 6/29/16.
 *
 * Add gene FDR data to relevant variants
 *
 */
public class AddGeneFDR extends GeneStream{

    private Iterator<RelevantVariant> relevantVariants;
    private boolean verbose;
    private Map<String, Double> affectedFrac;
    private Map<String, Double> carrierFrac;


    public AddGeneFDR(Iterator<RelevantVariant> relevantVariants, File FDRfile, boolean verbose) throws FileNotFoundException {
        super(relevantVariants, verbose);

        Map<String, Double> affectedFrac = new HashMap<>();
        Map<String, Double> carrierFrac = new HashMap<>();

        Scanner s = new Scanner(FDRfile);
        s.nextLine(); //skip header
        while(s.hasNextLine())
        {
            //"Gene    AffectedAbs     CarrierAbs      AffectedFrac    CarrierFrac"
            String[] split = s.nextLine().split("\t", -1);
            affectedFrac.put(split[0], Double.parseDouble(split[3]));
            carrierFrac.put(split[0], Double.parseDouble(split[4]));
        }
        this.affectedFrac = affectedFrac;
        this.carrierFrac = carrierFrac;

    }

    @Override
    public void perGene(String gene, List<RelevantVariant> variantsPerGene) throws Exception {


        Double affectedFracForGene = this.affectedFrac.get(gene);
        Double carrierFracForGene = this.carrierFrac.get(gene);


        for(RelevantVariant rv : variantsPerGene)
        {
            for(Relevance rlv : rv.getRelevance())
            {
                if(!rlv.getGene().equals(gene))
                {
                    continue;
                }
                String fdrInfo = affectedFracForGene+","+carrierFracForGene;
                rlv.setFDR(fdrInfo);
                if(verbose){
                    System.out.println("[AddGeneFDR] Added FDR info '"+fdrInfo+"' to a variant for gene " + gene);
                }
            }

        }
    }


}
