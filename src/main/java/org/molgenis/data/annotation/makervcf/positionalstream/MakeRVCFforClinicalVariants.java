package org.molgenis.data.annotation.makervcf.positionalstream;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.vcf.VcfRepository;

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
    private AttributeMetaData rlv;
    private boolean verbose;

    public MakeRVCFforClinicalVariants(Iterator<RelevantVariant> relevantVariants, AttributeMetaData rlv, boolean verbose)
    {
        this.relevantVariants = relevantVariants;
        this.rlv = rlv;
        this.verbose = verbose;
    }

    public Iterator<Entity> addRVCFfield()
    {

        return new Iterator<Entity>() {


            @Override
            public boolean hasNext() {
                return relevantVariants.hasNext();
            }

            @Override
            public Entity next() {


                try {
                    RelevantVariant rv = relevantVariants.next();

                    if(verbose) {
                        System.out.println("[MakeRVCFforClinicalVariants] Looking at: " + rv.toString());
                    }

                    List<RVCF> rvcfList = new ArrayList<>();
                    for(Relevance rlv : rv.getRelevance())
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


                    Entity e = rv.getVariant().getOrignalEntity();
                    DefaultEntityMetaData emd = (DefaultEntityMetaData) e.getEntityMetaData();
                    DefaultAttributeMetaData infoAttribute = (DefaultAttributeMetaData) emd.getAttribute(VcfRepository.INFO);
                    infoAttribute.addAttributePart(rlv);

                    StringBuffer rvcfListSB = new StringBuffer();
                    for(RVCF rvcf: rvcfList)
                    {
                        rvcfListSB.append(rvcf.toString() + ",");
                    }
                    rvcfListSB.deleteCharAt(rvcfListSB.length()-1);

                    e.set(RVCF.attributeName,rvcfListSB.toString());

                    if(verbose) {
                        System.out.println("[MakeRVCFforClinicalVariants] Converted relevant variant to a VCF INFO field for writing out: " + rvcfListSB.toString());
                    }

                    return e;

                }
                catch(Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        };

    }
}
