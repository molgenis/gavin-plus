package org.molgenis.data.annotation.makervcf.util;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.core.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.structs.AnnotatedVcfRecord;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.vcf.utils.FixVcfAlleleNotation;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfRecordUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by joeri on 6/1/16.
 * Custom list of variants and classifications
 *
 */
public class LabVariants {

    private Map<String, AnnotatedVcfRecord> posRefAltToLabVariant;

    public LabVariants(File labVariantsFile) throws Exception {
        VcfReader clinvar = GavinUtils.getVcfReader(labVariantsFile);
        //ClinVar match
        Iterator<VcfRecord> cvIt = clinvar.iterator();
        this.posRefAltToLabVariant = new HashMap<>();
        while (cvIt.hasNext())
        {
            AnnotatedVcfRecord vcfEntity = new AnnotatedVcfRecord(cvIt.next());
            for(String alt : VcfRecordUtils.getAlts(vcfEntity))
            {
                String trimmedRefAlt = FixVcfAlleleNotation.backTrimRefAlt(VcfRecordUtils.getRef(vcfEntity), alt, "_");
                String key = vcfEntity.getChromosome() + "_" + vcfEntity.getPosition() + "_" + trimmedRefAlt;
                posRefAltToLabVariant.put(key, vcfEntity);
            }
        }
        System.out.println("Lab variants ("+posRefAltToLabVariant.size()+") loaded");
    }


    public Judgment classifyVariant(GavinRecord record, String alt, String gene) throws Exception {
        String trimmedRefAlt = FixVcfAlleleNotation.backTrimRefAlt(record.getRef(), alt, "_");
        String key = record.getChromosome() + "_" + record.getPosition() + "_" + trimmedRefAlt;

        if(posRefAltToLabVariant.containsKey(key)) {
            // e.g.
            // CLSF=P;
            // CLSF=V;
            // CLSF=LB;
            if(posRefAltToLabVariant.get(key).getClsf() == null)
            {
                throw new Exception("No CLSF field for lab variant at " + key);
            }
            String labVariantInfo = posRefAltToLabVariant.get(key).getClsf();

            if (labVariantInfo.equals("P") || labVariantInfo.equals("LP"))
            {
                return new Judgment(Judgment.Classification.Pathogenic, Judgment.Method.genomewide, gene, labVariantInfo,"Lab variant","Reported pathogenic");
            }
            else if(labVariantInfo.equals("V"))
            {
                return new Judgment(Judgment.Classification.VOUS, Judgment.Method.genomewide, gene, labVariantInfo,"Lab variant","Reported VUS");
            }
            else if (labVariantInfo.equals("B") || labVariantInfo.equals("LB"))
            {
                return new Judgment(Judgment.Classification.Benign, Judgment.Method.genomewide, gene, labVariantInfo,"Lab variant","Reported benign");
            }
            else
            {
                throw new Exception("lab variant hit is not B, LB, V, LP or P: " + labVariantInfo);
            }
        }
        return null;
    }
}
