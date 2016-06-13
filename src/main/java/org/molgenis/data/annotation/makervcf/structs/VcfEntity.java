package org.molgenis.data.annotation.makervcf.structs;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.snpEff.SnpEffRunner;
import org.molgenis.data.vcf.VcfRepository;


import java.util.Arrays;
import java.util.Set;

/**
 * Created by joeri on 6/1/16.
 */
public class VcfEntity {
    String chr;
    String pos;
    String ref;
    String ann;
    String clinvar;
    String[] alts; //alternative alleles, in order
    Double[] exac_AFs; //ExAC allele frequencies in order of alt alleles, 0 if no match
    Double[] caddPhredScores; //CADD scores in order of alt alleles, may be null
    Set<String> genes; //any associated genes, not in any given order
    Iterable<Entity> samples;

    //more? "EXAC_AC_HOM", "EXAC_AC_HET"


    public VcfEntity(Entity record) throws Exception
    {
        this.samples = record.getEntities(VcfRepository.SAMPLES);
        this.chr = record.getString("#CHROM");
        this.pos = record.getString("POS");
        this.ref = record.getString("REF");
        this.ref = record.getString("CLINVAR"); //e.g. CLINVAR=NM_024596.4(MCPH1):c.215C>T (p.Ser72Leu)|MCPH1|Pathogenic
        this.alts = record.getString("ALT").split(",", -1);
        this.exac_AFs = setAltAlleleOrderedDoubleField(record, "EXAC_AF", true);
        this.caddPhredScores = setAltAlleleOrderedDoubleField(record, "CADD_SCALED", true);
        this.ann = record.getString("ANN");
        this.genes = GavinUtils.getGenesFromAnn(ann);


    }

    public Double[] setAltAlleleOrderedDoubleField(Entity record, String fieldName, boolean zeroForNull) throws Exception {
        Double[] res = new Double[this.alts.length];
        if(record.get(fieldName) == null)
        {
            //the entire field is not present
            //for '0 for null' attributes, fill array with 0s
            if(zeroForNull)
            {
                for(int i = 0; i < this.alts.length; i++)
                {
                    res[i] = 0.0;
                }
                return res;
            }
            else
            {
                return res;
            }
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
                //  exac_AFs[i] = (exac_af_split[i] != null && !exac_af_split[i].isEmpty() && !exac_af_split[i].equals(".")) ? Double.parseDouble(exac_af_split[i]) : 0;
                res[i] = (split[i] != null && !split[i].isEmpty() && !split[i].equals(".")) ? Double.parseDouble(split[i]) : (zeroForNull ? 0 : null);
            }
        }
        else
        {
            throw new Exception(fieldName + " split is null");
        }

        return res;
    }


    public SnpEffRunner.Impact getImpact(int i, String gene) throws Exception {
        return GavinUtils.getImpact(this.ann, gene, this.alts[i]);
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
        return exac_AFs[i];
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
                '}';
    }
}
