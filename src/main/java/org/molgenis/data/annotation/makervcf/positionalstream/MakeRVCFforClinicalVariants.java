package org.molgenis.data.annotation.makervcf.positionalstream;

import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by joeri on 6/1/16.
 *
 * Take the results from MatchVariantsToGenotypeAndInheritance, and report the findins in RVCF format.
 * We grab the header from the original VCF, the original VCF records, and add onto this our results.
 *
 */
public class MakeRVCFforClinicalVariants {

    private Iterator<RelevantVariant> relevantVariants;
    private String rlvMetadata;
    private boolean verbose;

    public MakeRVCFforClinicalVariants(Iterator<RelevantVariant> relevantVariants, String rlvMetadata, boolean verbose)
    {
        this.relevantVariants = relevantVariants;
        this.rlvMetadata = rlvMetadata;
        this.verbose = verbose;
    }

    public Iterator<RelevantVariant> addRVCFfield()
    {

        return new Iterator<RelevantVariant>() {


            @Override
            public boolean hasNext() {
                return relevantVariants.hasNext();
            }

            @Override
            public RelevantVariant next() {
                try {
                    RelevantVariant relevantVariant = relevantVariants.next();

                    if(verbose) {
                        System.out.println("[MakeRVCFforClinicalVariants] Looking at: " + relevantVariant.toString());
                    }

                    List<RVCF> rvcfList = new ArrayList<>();
                    for(Relevance rlv : relevantVariant.getRelevance())
                    {
                        RVCF rvcf = new RVCF();

                        rvcf.setGene(rlv.getGene());
                        rvcf.setFDR(rlv.getFDR());
                        rvcf.setAllele(rlv.getAllele());
                        rvcf.setAlleleFreq(rlv.getAlleleFreq()+"");
                        rvcf.setTranscript(rlv.getTranscript());

                        if(rlv.getCgdInfo() != null) {
                            rvcf.setPhenotype(rlv.getCgdInfo().getCondition());
                            rvcf.setPhenotypeInheritance(rlv.getCgdInfo().getGeneralizedInheritance().toString());
                            rvcf.setPhenotypeOnset(rlv.getCgdInfo().getAge_group());
                            rvcf.setPhenotypeDetails(rlv.getCgdInfo().getComments());
                            rvcf.setPhenotypeGroup(null);
                        }

                        rvcf.setVariantSignificance(rlv.getJudgment().getType());
                        rvcf.setVariantSignificanceSource(rlv.getJudgment().getSource());
                        rvcf.setVariantSignificanceJustification(rlv.getJudgment().getReason());
                        rvcf.setVariantMultiGenic(null);
                        rvcf.setVariantGroup(null);

                        rvcf.setSampleStatus(rlv.getSampleStatus());
                        rvcf.setSampleGenotype(rlv.getSampleGenotypes());
                        rvcf.setSamplePhenotype(null);
                        rvcf.setSampleGroup(null);

                        rvcfList.add(rvcf);
                    }

                    relevantVariant.setRlvMetadata(rlvMetadata);
                    StringBuffer rvcfListSB = new StringBuffer();
                    for(RVCF rvcf: rvcfList)
                    {
                        rvcfListSB.append(rvcf.toString() + ",");
                    }
                    rvcfListSB.deleteCharAt(rvcfListSB.length()-1);

                    relevantVariant.setRlv(rvcfListSB.toString());

                    if(verbose) {
                        System.out.println("[MakeRVCFforClinicalVariants] Converted relevant variant to a VCF INFO field for writing out: " + rvcfListSB.toString());
                    }

                    return relevantVariant;
                }
                catch(Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        };

    }
}
