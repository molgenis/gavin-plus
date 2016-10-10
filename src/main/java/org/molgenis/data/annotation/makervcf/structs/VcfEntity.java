package org.molgenis.data.annotation.makervcf.structs;

import net.didion.jwnl.data.Exc;
import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.entity.impl.snpEff.Impact;
import org.molgenis.data.vcf.VcfRepository;


import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by joeri on 6/1/16.
 */
public class VcfEntity {

    private Entity orignalEntity;
    private String chr;
    private Integer pos;
    private String id;
    private String ref;
    private String ann;
    private String clinvar;
    private String clsf;
    private String[] alts; //alternative alleles, in order
    private Double[] exac_AFs; //ExAC allele frequencies in order of alt alleles, null if no match
    private Double[] gonl_AFs; //ExAC allele frequencies in order of alt alleles, 0 if no match
    private Double[] caddPhredScores; //CADD scores in order of alt alleles, may be null
    private Set<String> genes; //any associated genes, not in any given order
    private Iterable<Entity> samples;
    private List<RVCF> rvcf;

    //more? "EXAC_AC_HOM", "EXAC_AC_HET"


    public VcfEntity(Entity record) throws Exception
    {
        this.orignalEntity = record;
        //this.samples = record.getEntities(VcfRepository.SAMPLES);
        this.chr = record.getString("#CHROM");
        this.pos = Integer.parseInt(record.getString("POS"));
        this.id = record.getString("ID");
        this.ref = record.getString("REF");
        this.clinvar = record.getString("CLINVAR"); //e.g. CLINVAR=NM_024596.4(MCPH1):c.215C>T (p.Ser72Leu)|MCPH1|Pathogenic
        this.clsf = record.getString("CLSF"); //e.g. CLSF=P;
        this.alts = record.getString("ALT").split(",", -1);
        this.exac_AFs = setAltAlleleOrderedDoubleField(record, "EXAC_AF");
        this.gonl_AFs = setAltAlleleOrderedDoubleField(record, "GoNL_AF");
        this.caddPhredScores = setAltAlleleOrderedDoubleField(record, "CADD_SCALED");
        this.ann = record.getString("ANN");
        this.genes = GavinUtils.getGenesFromAnn(ann);
        this.rvcf = setRvcfFromVcfInfoField(record.getString(RVCF.attributeName));
   //     this.rvcf = record.getString(RVCF.attributeName) != null ? RVCF.fromString(record.getString(RVCF.attributeName)) : null;

    }

    public String getChrPosRefAlt()
    {
        return this.getChr() + "_" + this.getPos() + "_" + this.getRef() + "_" + this.getAltsAsString();
    }

    public List<RVCF> setRvcfFromVcfInfoField(String infoField) throws Exception {
        if(infoField == null)
        {
            return null;
        }
        String[] split = infoField.split(",");
        ArrayList<RVCF> res = new ArrayList<>();
        for(String s : split)
        {
            res.add(RVCF.fromString(s));
        }
        return res;
    }

    public String getClsf() {
        return clsf != null ? clsf : "";
    }

    public String getId() {
        return id != null ? id : "";
    }

    public void setCaddPhredScore(int i, Double setMe) {
        this.caddPhredScores[i] = setMe;
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
                //todo what is happening? loading back RVCF file:
                //Exception in thread "main" java.lang.Exception: CADD_SCALED split length not equal to alt allele split length for record vcf=[#CHROM=1,ALT=TG,C,POS=1116188,REF=CG,FILTER=PASS,QUAL=100.0,ID=rs367560627,INTERNAL_ID=RNWUDmMnfJqUyWdP6mlXlA,INFO={#CHROM_vcf=null,ALT_vcf=null,POS_vcf=null,REF_vcf=null,FILTER_vcf=null,QUAL_vcf=null,ID_vcf=null,INTERNAL_ID_vcf=null,CIEND=null,CIPOS=null,CS=null,END=null,IMPRECISE=false,MC=null,MEINFO=null,MEND=null,MLEN=null,MSTART=null,SVLEN=null,SVTYPE=null,TSD=null,AC=3,13,AF=5.99042E-4,0.00259585,NS=2504,AN=5008,LEN=null,TYPE=null,OLD_VARIANT=null,VT=null,EAS_AF=0.0,0.0129,EUR_AF=0.0,0.0,AFR_AF=0.0023,0.0,AMR_AF=0.0,0.0,SAS_AF=0.0,0.0,DP=6911,AA=null,ANN=C|frameshift_variant|HIGH|TTLL10|TTLL10|transcript|NM_001130045.1|protein_coding|8/16|c.706delG|p.Ala236fs|857/2259|706/2022|236/673||INFO_REALIGN_3_PRIME,TG|missense_variant|MODERATE|TTLL10|TTLL10|transcript|NM_001130045.1|protein_coding|8/16|c.703C>T|p.Arg235Trp|854/2259|703/2022|235/673||,LOF=(TTLL10|TTLL10|1|1.00),NMD=null,EXAC_AF=3.148E-4,0.001425,EXAC_AC_HOM=0,1,EXAC_AC_HET=31,145,GoNL_GTC=null,GoNL_AF=null,CADD=3.339984,CADD_SCALED=22.9,RLV=TG|3.148E-4|TTLL10|NM_001130045.1||||||NA19346:HOMOZYGOUS_COMPOUNDHET/NA19454:HETEROZYGOUS/HG03130:HETEROZYGOUS||NA19346:0p1/NA19454:0p1/HG03130:1p0||Predicted pathogenic|GAVIN|Variant MAF of 3.148E-4 is rare enough to be potentially pathogenic and its CADD score of 22.9 is greater than a global threshold of 15.||},SAMPLES_ENTITIES=org.molgenis.data.vcf.format.VcfToEntity$1@7f416310]
                throw new Exception(fieldName + " split length "+split.length+" of string '"+record.get(fieldName)+"' not equal to alt allele split length "+this.alts.length+" for record " + record.toString());
             //   System.out.println("WARNING: fieldName split length not equal to alt allele split length for record " + record.toString());
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

    public Iterator<Entity> getSamples() {
        return (Iterator)this.orignalEntity.get(VcfRepository.SAMPLES);
    }

    public String getChr() {
        return chr;
    }

    public Integer getPos() {
        return pos;
    }

    public String getRef() {
        return ref;
    }

    public int getAltIndex(String alt) throws Exception {
        for(int i = 0; i < alts.length; i++)
        {
            if(alt.equals(alts[i])){
                return i+1;
            }
        }
        throw new Exception("alt not found");
    }

    public String[] getAlts() {
        return alts;
    }

    public List<String> getAltsAsList() {
        return Arrays.asList(alts);
    }

    public String getAltsAsString() {
        return Arrays.asList(alts).toString();
    }

    public List<RVCF> getRvcf() {
        return rvcf != null ? rvcf : null;
    }

    public String getAltString() {
        StringBuffer sb = new StringBuffer();
        for(String alt : alts)
        {
            sb.append(alt + ",");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public String getAlts(int i) {
        return alts[i];
    }

    public String getAlt() throws RuntimeException {
        if(alts.length > 1)
        {
            throw new RuntimeException("more than 1 alt ! " + this.toString());
        }
        return alts[0];
    }

    public String getClinvar() {
        return clinvar;
    }

    public Double[] getExac_AFs() {
        return exac_AFs;
    }

    public double getExac_AFs(int i) {
        //return exac_AFs[i] == null ? 0 : exac_AFs[i];
        return exac_AFs[i] != null ? exac_AFs[i] : 0;
    }

    public Double[] getGoNL_AFs() {
        return gonl_AFs;
    }

    public double getGoNL_AFs(int i) {
        return gonl_AFs[i] != null ? gonl_AFs[i] : 0;
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

    public void setGenes(String gene) {
        Set<String> genes = new HashSet<>();
        genes.add(gene);
        this.genes = genes;
    }

    public void setGenes(Set<String> genes) {
        this.genes = genes;
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
