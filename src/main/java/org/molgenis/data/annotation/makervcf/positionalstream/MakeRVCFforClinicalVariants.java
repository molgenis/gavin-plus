package org.molgenis.data.annotation.makervcf.positionalstream;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.vcf.VcfRepository;

import java.util.Iterator;

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

    public MakeRVCFforClinicalVariants(Iterator<RelevantVariant> relevantVariants, AttributeMetaData rlv)
    {
        this.relevantVariants = relevantVariants;
        this.rlv = rlv;
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

                    boolean clinVarPatho = rv.getClinvarJudgment().getClassification().equals(Judgment.Classification.Pathogenic);
                    boolean gavinPatho = rv.getGavinJudgment() != null ? rv.getGavinJudgment().getClassification().equals(Judgment.Classification.Pathogenic) : false;


                    RVCF rvcf = new RVCF();

                    rvcf.setGene(rv.getGene());
                    rvcf.setAllele(rv.getAllele());
                    rvcf.setAlleleFreq(rv.getAlleleFreq()+"");
                    rvcf.setTranscript(rv.getTranscript());

                    if(rv.getCgdInfo() != null) {
                        rvcf.setPhenotype(rv.getCgdInfo().getCondition());
                        rvcf.setPhenotypeInheritance(rv.getCgdInfo().getGeneralizedInheritance().toString());
                        rvcf.setPhenotypeOnset(rv.getCgdInfo().getAge_group());
                        rvcf.setPhenotypeDetails(rv.getCgdInfo().getComments());
                        rvcf.setPhenotypeGroup(null);
                    }

                    rvcf.setVariantSignificance(clinVarPatho ? "Reported pathogenic" : gavinPatho ? "Predicted pathogenic" : "VUS");
                    rvcf.setVariantSignificanceSource(clinVarPatho ? "ClinVar" : gavinPatho ? "GAVIN" : "");
                    rvcf.setVariantSignificanceJustification(clinVarPatho ? rv.getClinvarJudgment().getReason() : gavinPatho ? rv.getGavinJudgment().getReason() : "");
                    rvcf.setVariantCompoundHet(null);
                    rvcf.setVariantGroup(null);

                    rvcf.setSampleStatus(rv.getSampleStatus());
                    rvcf.setSampleGenotype(rv.getSampleGenotypes());
                    rvcf.setSamplePhenotype(null);
                    rvcf.setSampleGroup(null);

                    Entity e = rv.getVariant().getOrignalEntity();
                    DefaultEntityMetaData emd = (DefaultEntityMetaData) e.getEntityMetaData();
                    DefaultAttributeMetaData infoAttribute = (DefaultAttributeMetaData) emd.getAttribute(VcfRepository.INFO);
                    infoAttribute.addAttributePart(rlv);

                    e.set(RVCF.attributeName,rvcf.toString());

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
