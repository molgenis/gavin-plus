package org.molgenis.data.annotation.makervcf.util;

import org.molgenis.calibratecadd.support.LoadCADDWebserviceOutput;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.vcf.VcfRepository;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by joeri on 6/1/16.
 */
public class ClinVar {

    //  /Users/joeri/github/clinvar.5.5.16/clinvar.patho.fix.5.5.16.vcf.gz

    private Map<String, VcfEntity> posRefAltToClinVar;

    public ClinVar(File clinvarFile) throws Exception {
        VcfRepository clinvar = new VcfRepository(clinvarFile, "clinvar");
        //ClinVar match
        Iterator<Entity> cvIt = clinvar.iterator();
        this.posRefAltToClinVar = new HashMap<>();
        while (cvIt.hasNext())
        {
            VcfEntity record = new VcfEntity(cvIt.next());
            for(String alt : record.getAlts())
            {
                String trimmedRefAlt = LoadCADDWebserviceOutput.trimRefAlt(record.getRef(), alt, "_");

//                if(!(record.getRef() + "_" + alt).equals(trimmedRefAlt))
//                {
//                    System.out.println("trimmed " + (record.getRef() + "_" + alt) + " to " + trimmedRefAlt);
//                }

                String key = record.getChr() + "_" + record.getPos() + "_" + trimmedRefAlt;
                posRefAltToClinVar.put(key, record);
            }
        }
    }


    public Judgment classifyVariant(VcfEntity record, String alt, String gene, boolean overrideGeneWithClinvarGene) throws Exception {
        String trimmedRefAlt = LoadCADDWebserviceOutput.trimRefAlt(record.getRef(), alt, "_");
        String key = record.getChr() + "_" + record.getPos() + "_" + trimmedRefAlt;

        if(posRefAltToClinVar.containsKey(key)) {
            // e.g.
            // CLINVAR=NM_005691.3(ABCC9):c.2554C>T (p.Gln852Ter)|ABCC9|Likely pathogenic
            // CLINVAR=NM_004612.3(TGFBR1):c.428T>A (p.Leu143Ter)|TGFBR1|Pathogenic
            String clinvarInfo = posRefAltToClinVar.get(key).getClinvar();

            if (clinvarInfo.contains("athogenic")) //avoid casing problem "Pathogenic" vs "pathogenic" ...
            {
                String clinvarGene = clinvarInfo.split("\\|", -1)[1];
                if (!clinvarGene.equalsIgnoreCase(gene)) {
                    if(overrideGeneWithClinvarGene)
                    {
            //            System.out.println("Processing MT data? Reporting variant under under '" + clinvarGene + "'.");
                        gene = clinvarGene;
                    }
                    else {
         //               System.out.println("WARNING: genes did not match: " + clinvarGene + " vs " + gene + ". Reporting under '" + gene + "' while preserving ClinVar data '" + clinvarInfo + "'.");

                    }
                }
                return new Judgment(Judgment.Classification.Pathogenic, Judgment.Method.genomewide, gene, clinvarInfo);
            } else {
                throw new Exception("clinvar hit is not pathogenic: " + clinvarInfo);
            }

        }

        return new Judgment(Judgment.Classification.VOUS, Judgment.Method.genomewide, gene, "not in clinvar pathogenic list");
    }
}
