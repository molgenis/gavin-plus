package org.molgenis.data.annotation.makervcf.structs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaInfo;

public class VepUtils {

  public static final String VEP_SEPERATOR = "\\|";
  public static final String VEP_INFO_NAME = "CSQ";
  public static String 	ALLELE	=	 "Allele";
  public static String 	IMPACT	=	 "IMPACT";
  public static String 	SYMBOL	=	 "SYMBOL";
  public static String 	GENE	=	 "Gene";


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

  private static String[] getVepValues(VcfRecord record) {
    VcfInfo vepInfoField = getVepInfoField(record);
    String multiVepResult = vepInfoField.getValRaw();
    return multiVepResult.split(",");
  }

  private static String getValueForKey(String key, VcfMeta vcfMeta,
      String singleVepResult) {
    int index = getIndex(key, vcfMeta);
    String[] vepValues = singleVepResult.split(VEP_SEPERATOR);
    if(vepValues.length >= index) {
      return vepValues[index];
    }else{
      throw new RuntimeException("The index found in the headers for key "+key+" was not present in the info field.");
    }
  }

  private static VcfInfo getVepInfoField(VcfRecord record) {
    for(VcfInfo info : record.getInformation()){
      if(info.getKey().equals(VEP_INFO_NAME)){
        return info;
      }
    }
    throw new RuntimeException("No VEP info field found in vcf");
  }

  private static int getIndex(String key, VcfMeta meta) {
    Iterator<VcfMetaInfo> infoMetaIterator = meta.getInfoMeta().iterator();
    while (infoMetaIterator.hasNext()) {
      VcfMetaInfo infoMeta = infoMetaIterator.next();
      if (infoMeta.getId().equals(VEP_INFO_NAME)) {
        String desc = infoMeta.getDescription().replace("Consequence annotations from Ensembl VEP. Format: ","");
        String[] header = desc.split(VEP_SEPERATOR);
        for (int i = 0; i < header.length; i++) {
          if (header[i].equals(key)) {
            return i;
          }
        }
      }
    }
    throw new RuntimeException("Key: [" + key + "] not found in VEP values");
  }
}
