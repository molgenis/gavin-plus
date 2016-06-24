package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.snpEff.Impact;
import org.molgenis.data.vcf.VcfRepository;


import java.util.Arrays;
import java.util.Set;

/**
 * Created by joeri on 6/1/16.
 */
public class VcfEntity {

    private Entity orignalEntity;
    private String chr;
    private String pos;
    private String ref;
    private String ann;
    private String clinvar;
    private String[] alts; //alternative alleles, in order
    private Double[] exac_AFs; //ExAC allele frequencies in order of alt alleles, 0 if no match
    private Double[] caddPhredScores; //CADD scores in order of alt alleles, may be null
    private Set<String> genes; //any associated genes, not in any given order
    private Iterable<Entity> samples;

    //more? "EXAC_AC_HOM", "EXAC_AC_HET"


    public VcfEntity(Entity record) throws Exception
    {
        this.orignalEntity = record;
        this.samples = record.getEntities(VcfRepository.SAMPLES);
        this.chr = record.getString("#CHROM");
        this.pos = record.getString("POS");
        this.ref = record.getString("REF");
        this.clinvar = record.getString("CLINVAR"); //e.g. CLINVAR=NM_024596.4(MCPH1):c.215C>T (p.Ser72Leu)|MCPH1|Pathogenic
        this.alts = record.getString("ALT").split(",", -1);
        this.exac_AFs = setAltAlleleOrderedDoubleField(record, "EXAC_AF");
        this.caddPhredScores = setAltAlleleOrderedDoubleField(record, "CADD_SCALED");
        this.ann = record.getString("ANN");
        this.genes = GavinUtils.getGenesFromAnn(ann);


    }

    public Entity getOrignalEntity() {
        return orignalEntity;
    }

    public Double[] setAltAlleleOrderedDoubleField(Entity record, String fieldName) throws Exception {
        Double[] res = new Double[this.alts.length];
        if(record.get(fieldName) == null)
        {
            //the entire field is not present
            return res;
        }
        String[] split = record.get(fieldName) == null ? null : record.getString(fieldName).split(",", -1);
        if(split != null)
        {
            if(split.length != this.alts.length)
            {
                throw new Exception(fieldName + " split length not equal to alt allele split length");
            }
            for(int i = 0; i < split.length; i++)
            {
                res[i] = (split[i] != null && !split[i].isEmpty() && !split[i].equals(".")) ? Double.parseDouble(split[i]) : null;
            }
        }
        else
        {
            throw new Exception(fieldName + " split is null");
        }

        return res;
    }


    public Impact getImpact(int i, String gene) throws Exception {
        return GavinUtils.getImpact(this.ann, gene, this.alts[i]);
    }

    public String getTranscript(int i, String gene) throws Exception {
        return GavinUtils.getTranscript(this.ann, gene, this.alts[i]);
    }

    public Iterable<Entity> getSamples() {
        return samples;
    }

    public String getChr() {
        return chr;
    }

    public String getPos() {
        return pos;
    }

    public String getRef() {
        return ref;
    }

    public String[] getAlts() {
        return alts;
    }

    public String getAlts(int i) {
        return alts[i];
    }

    public String getAlt() throws Exception {
        if(alts.length > 1)
        {
            throw new Exception("more than 1 alt ! " + this.toString());
        }
        return alts[0];
    }

    public Double[] getExac_AFs() {
        return exac_AFs;
    }

    public String getClinvar() {
        return clinvar;
    }

    public double getExac_AFs(int i) {
        //return exac_AFs[i] == null ? 0 : exac_AFs[i];
        return exac_AFs[i] != null ? exac_AFs[i] : 0;
    }

    public Double[] getCaddPhredScores() {
        return caddPhredScores;
    }

    public Double getCaddPhredScores(int i) {
        return caddPhredScores[i];
    }

    public Set<String> getGenes() {
        return genes;
    }

    public static int getAltAlleleIndex(VcfEntity record, String alt) {
        return Arrays.asList(record.getAlts()).indexOf(alt) + 1;
    }

    @Override
    public String toString() {
        return "VcfEntity{" +
                "chr='" + chr + '\'' +
                ", pos='" + pos + '\'' +
                ", ref='" + ref + '\'' +
                ", ann='" + ann + '\'' +
                ", clinvar='" + clinvar + '\'' +
                ", alts=" + Arrays.toString(alts) +
                ", exac_AFs=" + Arrays.toString(exac_AFs) +
                ", caddPhredScores=" + Arrays.toString(caddPhredScores) +
                ", genes=" + genes +
                ", samples=" + samples +
                '}';
    }
}
