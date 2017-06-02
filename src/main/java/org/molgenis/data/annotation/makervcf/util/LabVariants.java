package org.molgenis.data.annotation.makervcf.util;

import org.molgenis.calibratecadd.support.LoadCADDWebserviceOutput;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.FixVcfAlleleNotation;
import org.molgenis.genotype.util.FixedSizeIterable;

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

    private Map<String, VcfEntity> posRefAltToLabVariant;

    public LabVariants(File labVariantsFile) throws Exception {
        VcfRepository clinvar = new VcfRepository(labVariantsFile, "lab");
        //ClinVar match
        Iterator<Entity> cvIt = clinvar.iterator();
        this.posRefAltToLabVariant = new HashMap<>();
        while (cvIt.hasNext())
        {
            VcfEntity record = new VcfEntity(cvIt.next());
            for(String alt : record.getAlts())
            {
                String trimmedRefAlt = FixVcfAlleleNotation.backTrimRefAlt(record.getRef(), alt, "_");
                String key = record.getChr() + "_" + record.getPos() + "_" + trimmedRefAlt;
                posRefAltToLabVariant.put(key, record);
            }
        }
        System.out.println("Lab variants ("+posRefAltToLabVariant.size()+") loaded");
    }


    public Judgment classifyVariant(VcfEntity record, String alt, String gene) throws Exception {
        String trimmedRefAlt = FixVcfAlleleNotation.backTrimRefAlt(record.getRef(), alt, "_");
        String key = record.getChr() + "_" + record.getPos() + "_" + trimmedRefAlt;

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
                return new Judgment(Judgment.Classification.Pathogenic, Judgment.Method.genomewide, gene, labVariantInfo).setSource("Lab variant").setType("Reported pathogenic");
            }
            else if(labVariantInfo.equals("V"))
            {
                return new Judgment(Judgment.Classification.VOUS, Judgment.Method.genomewide, gene, labVariantInfo).setSource("Lab variant").setType("Reported VUS");
            }
            else if (labVariantInfo.equals("B") || labVariantInfo.equals("LB"))
            {
                return new Judgment(Judgment.Classification.Benign, Judgment.Method.genomewide, gene, labVariantInfo).setSource("Lab variant").setType("Reported benign");
            }
            else
            {
                throw new Exception("lab variant hit is not B, LB, V, LP or P: " + labVariantInfo);
            }
        }
        return null;
    }
}
