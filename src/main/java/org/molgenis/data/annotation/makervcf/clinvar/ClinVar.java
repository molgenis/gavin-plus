package org.molgenis.data.annotation.makervcf.clinvar;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.entity.impl.snpEff.SnpEffRunner;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.vcf.VcfRepository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by joeri on 6/1/16.
 */
public class ClinVar {

    //  /Users/joeri/github/clinvar.5.5.16/clinvar.patho.fix.5.5.16.vcf.gz

    private Map<String, VcfEntity> posToClinVar;

    public ClinVar(File clinvarFile) throws Exception {
        VcfRepository clinvar = new VcfRepository(clinvarFile, "clinvar");
        //ClinVar match
        Iterator<Entity> cvIt = clinvar.iterator();
        this.posToClinVar = new HashMap<>();
        while (cvIt.hasNext())
        {
            VcfEntity record = new VcfEntity(cvIt.next());
            posToClinVar.put(record.getChr()+"_"+record.getPos() + "_", record); //todo map on chr_pos_ref_alt ? use trimming of alleles like for CADD ? do this a bit better..
        }
    }


    public Judgment classifyVariant(VcfEntity record, String alt, String gene) throws Exception {
        String clinvarKey = record.getChr() + "_"+record.getPos() + "_"; //TODO ref/alt ?

        if(posToClinVar.containsKey(clinvarKey)) {
            // e.g.
            // CLINVAR=NM_005691.3(ABCC9):c.2554C>T (p.Gln852Ter)|ABCC9|Likely pathogenic
            // CLINVAR=NM_004612.3(TGFBR1):c.428T>A (p.Leu143Ter)|TGFBR1|Pathogenic
            String clinvarInfo = posToClinVar.get(clinvarKey).getClinvar();

            if (clinvarInfo.contains("athogenic")) //avoid casing problem "Pathogenic" vs "pathogenic" ...
            {
                String clinvarGene = clinvarInfo.split("\\|", -1)[1];
                if (clinvarGene.equalsIgnoreCase(gene)) {
                    return new Judgment(Judgment.Classification.Pathogn, Judgment.Method.genomewide, gene, clinvarInfo);
                } else {
                    System.out.println("WARNING: genes did not match: " + clinvarGene + " vs " + gene);
                    return new Judgment(Judgment.Classification.Pathogn, Judgment.Method.genomewide, clinvarGene, clinvarInfo);
                }

            } else {
                throw new Exception("clinvar hit is not pathogenic: " + clinvarInfo);
            }

        }

        return new Judgment(Judgment.Classification.VOUS, Judgment.Method.genomewide, gene, "not in clinvar pathogenic list");
    }
}
