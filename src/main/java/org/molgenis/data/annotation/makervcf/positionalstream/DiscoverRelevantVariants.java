package org.molgenis.data.annotation.makervcf.positionalstream;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.entity.impl.gavin.GavinAlgorithm;
import org.molgenis.data.annotation.entity.impl.gavin.GavinEntry;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores.Mode;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.entity.impl.snpEff.Impact;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.molgenis.data.annotation.makervcf.util.ClinVar;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.annotation.makervcf.util.LabVariants;
import org.molgenis.data.vcf.VcfRepository;

import java.io.File;
import java.util.*;

/**
 * Created by joeri on 6/1/16.
 *
 * Scan through a VCF, apply GAVIN to site-variant-allele-gene combinations, mark variants that are pathogenic or VOUS.
 * Also run past known pathogenic/likely pathogenic variants in ClinVar.
 * Result is a list of 'judged variants' with all meta data stored within.
 *
 */
public class DiscoverRelevantVariants {

    private VcfRepository vcf;
    private LabVariants lab;
    private HashMap<String, GavinEntry> gavinData;
    private GavinAlgorithm gavin;
    private HandleMissingCaddScores hmcs;
    private ClinVar clinvar;
    private EntityMetaData vcfMeta;
    private boolean verbose;

    public DiscoverRelevantVariants(File vcfFile, File gavinFile, File clinvarFile, File caddFile, File labVariants, Mode mode, boolean verbose) throws Exception
    {
        this.vcf = new VcfRepository(vcfFile, "vcf");
        this.clinvar = new ClinVar(clinvarFile);
        if(labVariants!= null){
            this.lab = new LabVariants(labVariants);
        }
        this.gavin = new GavinAlgorithm();
        this.gavinData = new GavinUtils(gavinFile).getGeneToEntry();
        this.hmcs = new HandleMissingCaddScores(mode, caddFile);
        this.vcfMeta = vcf.getEntityMetaData();
        this.verbose = verbose;
    }

    public EntityMetaData getVcfMeta() {
        return vcfMeta;
    }

    public Iterator<RelevantVariant> findRelevantVariants() throws Exception
    {
        Iterator<Entity> vcfIterator = vcf.iterator();

        return new Iterator<RelevantVariant>(){

            RelevantVariant nextResult;

            @Override
            public boolean hasNext()
            {
                while(vcfIterator.hasNext())
                {
                    try
                    {
                        VcfEntity record = new VcfEntity(vcfIterator.next());

                        /**
                         * Iterate over alternatives, if applicable multi allelic example: 1:1148100-1148100
                         */
                        for (int i = 0; i < record.getAlts().length; i++)
                        {
                            Double cadd = hmcs.dealWithCaddScores(record, i);

                            //if mitochondrial, we have less tools / data, can't do much, just match to clinvar
                            if(record.getChr().equals("MT")|| record.getChr().equals("M") || record.getChr().equals("mtDNA"))
                            {
                                Judgment judgment = null;
                                Judgment labJudgment = lab != null ? lab.classifyVariant(record, record.getAlts(i), "MT") : null;
                                Judgment clinvarJudgment = clinvar.classifyVariant(record, record.getAlts(i), "MT", true);

                                if(labJudgment != null && labJudgment.getClassification() == Judgment.Classification.Pathogenic)
                                {
                                    judgment = labJudgment;
                                }
                                else if(clinvarJudgment != null && clinvarJudgment.getClassification() == Judgment.Classification.Pathogenic)
                                {
                                    judgment = clinvarJudgment;
                                }

                                if (judgment != null && judgment.getClassification().equals(Judgment.Classification.Pathogenic))
                                {
                                    record.setGenes(judgment.getGene());
                                    nextResult = new RelevantVariant(record, record.getAlts(i), clinvarJudgment.getGene(), record.getExac_AFs(i), record.getGoNL_AFs(i), clinvarJudgment.getGene(), clinvarJudgment);
                                    if(verbose){ System.out.println("[DiscoverRelevantVariants] Found relevant variant in mitochondrial DNA: " + nextResult.toStringShort()); }
                                    return true;
                                }
                            }

                            else {

                                if(record.getGenes().size() == 0)
                                {
                                    if(verbose){ System.out.println("[DiscoverRelevantVariants] WARNING: no genes for variant " + record.toString()); }
                                }
                                for (String gene : record.getGenes()) {
                                    Impact impact = record.getImpact(i, gene);
                                    String transcript = record.getTranscript(i, gene);

                                    Judgment judgment = null;
                                    Judgment labJudgment = lab != null ? lab.classifyVariant(record, record.getAlts(i), gene) : null;
                                    Judgment clinvarJudgment = clinvar.classifyVariant(record, record.getAlts(i), gene, false);
                                    Judgment gavinJudgment = gavin.classifyVariant(impact, cadd, record.getExac_AFs(i), gene, null, gavinData);

                                    if(labJudgment != null && labJudgment.getClassification() == Judgment.Classification.Pathogenic)
                                    {
                                        judgment = labJudgment;
                                    }
                                    else if(clinvarJudgment != null && clinvarJudgment.getClassification() == Judgment.Classification.Pathogenic)
                                    {
                                        judgment = clinvarJudgment;
                                    }
                                    else if(gavinJudgment != null && gavinJudgment.getClassification() == Judgment.Classification.Pathogenic)
                                    {
                                        judgment = gavinJudgment.setSource("GAVIN").setType("Predicted pathogenic");
                                    }

                                    if (judgment != null && judgment.getClassification() == Judgment.Classification.Pathogenic) {
                                        nextResult = new RelevantVariant(record, record.getAlts(i), transcript, record.getExac_AFs(i), record.getGoNL_AFs(i), gene, judgment);
                                        if(verbose){ System.out.println("[DiscoverRelevantVariants] Found relevant variant: " + nextResult.toStringShort()); }
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    catch(Exception e)
                    {
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
