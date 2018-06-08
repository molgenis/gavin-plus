package org.molgenis.data.annotation.makervcf.genestream.impl;

import org.molgenis.data.annotation.makervcf.genestream.core.GeneStream;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by joeri on 6/29/16.
 *
 * Add gene FDR data to relevant variants
 *
 */
public class AddGeneFDR extends GeneStream{
    private static final Logger LOG = LoggerFactory.getLogger(AddGeneFDR.class);
    private Map<String, Double> affectedFrac;
    private Map<String, Double> carrierFrac;


    public AddGeneFDR(Iterator<GavinRecord> relevantVariants, File FDRfile) throws FileNotFoundException {
        super(relevantVariants);

        Map<String, Double> affectedFrac = new HashMap<>();
        Map<String, Double> carrierFrac = new HashMap<>();

        try (Scanner s = new Scanner(FDRfile))
        {
            s.nextLine(); //skip header
            while (s.hasNextLine())
            {
                //"Gene    AffectedAbs     CarrierAbs      AffectedFrac    CarrierFrac"
                String[] split = s.nextLine().split("\t", -1);
                affectedFrac.put(split[0], Double.parseDouble(split[3]));
                carrierFrac.put(split[0], Double.parseDouble(split[4]));
            }
        }
        this.affectedFrac = affectedFrac;
        this.carrierFrac = carrierFrac;

    }

    @Override
    public void perGene(String gene, List<GavinRecord> gavinRecords) {

        Double affectedFracForGene = this.affectedFrac.get(gene);
        Double carrierFracForGene = this.carrierFrac.get(gene);

        for(GavinRecord gavinRecord : gavinRecords)
        {
            if(gavinRecord.isRelevant())
            {
                for (Relevance rlv : gavinRecord.getRelevance())
                {
                    if (!rlv.getGene().equals(gene))
                    {
                        continue;
                    }
                    String fdrInfo = affectedFracForGene + "," + carrierFracForGene;
                    rlv.setFDR(fdrInfo);
                    LOG.debug("[AddGeneFDR] Added FDR info '" + fdrInfo + "' to a variant for gene " + gene);
                }
            }
        }
    }
}
