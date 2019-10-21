package org.molgenis.data.annotation.makervcf.structs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SplittableRandom;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaInfo;

public class VepUtils {
  public static String 	ALLELE	=	 "Allele";
  public static String 	CONSEQUENCE	=	 "Consequence";
  public static String 	IMPACT	=	 "IMPACT";
  public static String 	SYMBOL	=	 "SYMBOL";
  public static String 	GENE	=	 "Gene";
  public static String 	FEATURE_TYPE	=	 "Feature_type";
  public static String 	FEATURE	=	 "Feature";
  public static String 	BIOTYPE	=	 "BIOTYPE";
  public static String 	EXON	=	 "EXON";
  public static String 	INTRON	=	 "INTRON";
  public static String 	HGVSC	=	 "HGVSc";
  public static String 	HGVSP	=	 "HGVSp";
  public static String 	CDNA_POSITION	=	 "cDNA_position";
  public static String 	CDS_POSITION	=	 "CDS_position";
  public static String 	PROTEIN_POSITION	=	 "Protein_position";
  public static String 	AMINO_ACIDS	=	 "Amino_acids";
  public static String 	CODONS	=	 "Codons";
  public static String 	EXISTING_VARIATION	=	 "Existing_variation";
  public static String 	DISTANCE	=	 "DISTANCE";
  public static String 	STRAND	=	 "STRAND";
  public static String 	FLAGS	=	 "FLAGS";
  public static String 	VARIANT_CLASS	=	 "VARIANT_CLASS";
  public static String 	SYMBOL_SOURCE	=	 "SYMBOL_SOURCE";
  public static String 	HGNC_ID	=	 "HGNC_ID";
  public static String 	CANONICAL	=	 "CANONICAL";
  public static String 	MANE	=	 "MANE";
  public static String 	TSL	=	 "TSL";
  public static String 	APPRIS	=	 "APPRIS";
  public static String 	CCDS	=	 "CCDS";
  public static String 	ENSP	=	 "ENSP";
  public static String 	SWISSPROT	=	 "SWISSPROT";
  public static String 	TREMBL	=	 "TREMBL";
  public static String 	UNIPARC	=	 "UNIPARC";
  public static String 	GENE_PHENO	=	 "GENE_PHENO";
  public static String 	SIFT	=	 "SIFT";
  public static String 	POLYPHEN	=	 "PolyPhen";
  public static String 	DOMAINS	=	 "DOMAINS";
  public static String 	MIRNA	=	 "miRNA";
  public static String 	AF	=	 "AF";
  public static String 	AFR_AF	=	 "AFR_AF";
  public static String 	AMR_AF	=	 "AMR_AF";
  public static String 	EAS_AF	=	 "EAS_AF";
  public static String 	EUR_AF	=	 "EUR_AF";
  public static String 	SAS_AF	=	 "SAS_AF";
  public static String 	AA_AF	=	 "AA_AF";
  public static String 	EA_AF	=	 "EA_AF";
  public static String 	GNOMAD_AF	=	 "gnomAD_AF";
  public static String 	GNOMAD_AFR_AF	=	 "gnomAD_AFR_AF";
  public static String 	GNOMAD_AMR_AF	=	 "gnomAD_AMR_AF";
  public static String 	GNOMAD_ASJ_AF	=	 "gnomAD_ASJ_AF";
  public static String 	GNOMAD_EAS_AF	=	 "gnomAD_EAS_AF";
  public static String 	GNOMAD_FIN_AF	=	 "gnomAD_FIN_AF";
  public static String 	GNOMAD_NFE_AF	=	 "gnomAD_NFE_AF";
  public static String 	GNOMAD_OTH_AF	=	 "gnomAD_OTH_AF";
  public static String 	GNOMAD_SAS_AF	=	 "gnomAD_SAS_AF";
  public static String 	MAX_AF	=	 "MAX_AF";
  public static String 	MAX_AF_POPS	=	 "MAX_AF_POPS";
  public static String 	CLIN_SIG	=	 "CLIN_SIG";
  public static String 	SOMATIC	=	 "SOMATIC";
  public static String 	PHENO	=	 "PHENO";
  public static String 	PUBMED	=	 "PUBMED";
  public static String 	MOTIF_NAME	=	 "MOTIF_NAME";
  public static String 	MOTIF_POS	=	 "MOTIF_POS";
  public static String 	HIGH_INF_POS	=	 "HIGH_INF_POS";
  public static String 	MOTIF_SCORE_CHANGE	=	 "MOTIF_SCORE_CHANGE";


  public static List<String> getVepValues(String key, VcfRecord record){
    List<String> result = new ArrayList<>();
    String[] vepResults = getVepValues(record);
    for(String singleVepResult : vepResults) {
      result.add(getValueForKey(key, record.getVcfMeta(), singleVepResult));
    }
    return result;
  }

  public static List<String> getVepValues(String key, VcfRecord record, String allele){
    List<String> result = new ArrayList<>();
    String[] vepResults = getVepValues(record);
    for(String singleVepResult : vepResults) {
      String vepAllele = getValueForKey(ALLELE, record.getVcfMeta(), singleVepResult);
      if(allele.equals(vepAllele)) {
        result.add(getValueForKey(key, record.getVcfMeta(), singleVepResult));
      }
    }
    return result;
  }

  public static List<String> getVepValues(String key, VcfRecord record, String allele, String gene){
    List<String> result = new ArrayList<>();
    String[] vepResults = getVepValues(record);
    for(String singleVepResult : vepResults) {
      String vepAllele = getValueForKey(ALLELE, record.getVcfMeta(), singleVepResult);
      String vepGene = getValueForKey(GENE, record.getVcfMeta(), singleVepResult);
      if(allele.equals(vepAllele) && gene.equals(vepGene)) {
        result.add(getValueForKey(key, record.getVcfMeta(), singleVepResult));
      }
      if(allele.equals(vepAllele)) {
        result.add(getValueForKey(key, record.getVcfMeta(), singleVepResult));
      }
    }
    return result;
  }

  public static String getVepValue(String key, VcfRecord record, String allele, String gene, String transcript){
    List<String> result = new ArrayList<>();
    String[] vepResults = getVepValues(record);
    for(String singleVepResult : vepResults) {
      //FIXME: checks
      result.add(getValueForKey(key, record.getVcfMeta(), singleVepResult));
    }
    //FIXME expect only one
    return result.get(0);
  }

  private static String[] getVepValues(VcfRecord record) {
    VcfInfo vepInfoField = getVepInfoField(record);
    String multiVepResult = vepInfoField.getValRaw();
    return multiVepResult.split(",");
  }

  private static String getValueForKey(String key, VcfMeta vcfMeta,
      String singleVepResult) {
    int index = getIndex(key, vcfMeta);
    String[] vepValues = singleVepResult.split("|");
    if(vepValues.length >= index) {
      return vepValues[index];
    }else{
      throw new RuntimeException("The index found in the headers for key "+key+" was not present in the actual info field.");
    }
  }

  private static VcfInfo getVepInfoField(VcfRecord record) {
    for(VcfInfo info : record.getInformation()){
      if(info.getKey().equals("CSQ")){
        return info;
      }
    }
    throw new RuntimeException("No VEP info field found in vcf");
  }

  private static int getIndex(String key, VcfMeta meta) {
    Iterator<VcfMetaInfo> infoMetaIterator = meta.getInfoMeta().iterator();
    while (infoMetaIterator.hasNext()) {
      VcfMetaInfo infoMeta = infoMetaIterator.next();
      if (infoMeta.getId().equals("CSQ")) {
        String desc = infoMeta.getDescription().replace("Consequence annotations from Ensembl VEP. Format: ","");
        String[] header = desc.split("|");
        for (int i = 0; i < header.length; i++) {
          if (header[i].equals(key)) {
            return i;
          }
        }
      }
    }
    return -1;
  }
}
