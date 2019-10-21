package org.molgenis.data.annotation.makervcf.structs;

import java.util.Iterator;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaInfo;

public class InfoUtils {
  public static String getInfoValue(String infoField, VcfRecord record, int alleleIndex) {
    return getInfoValue(new InfoField(infoField), record, alleleIndex);
  }

  public static String getInfoValue(InfoField infoField, VcfRecord record, int alleleIndex) {
    Iterator<VcfInfo> infofields = record.getInformation().iterator();
    while(infofields.hasNext()){
      VcfInfo info = infofields.next();
      if(info.getKey().equals(infoField.getName())){
        if(infoField.getKey() == null){
          return info.getValRaw();
        }else{
          int index = getIndex(infoField, record.getVcfMeta());
          String[] infoParts = info.getValRaw().split(infoField.getSeperator());
          return infoParts[index];
        }
      }
    }
    return null;
  }

  public static String getInfoValue(String infoField, VcfRecord record) {
    return getInfoValue(new InfoField(infoField), record);
  }

  public static String getInfoValue(InfoField infoField, VcfRecord record) {
    Iterator<VcfInfo> infofields = record.getInformation().iterator();
    while(infofields.hasNext()){
      VcfInfo info = infofields.next();
      if(info.getKey().equals(infoField.getName())){
        if(infoField.getKey() == null){
          return info.getValRaw();
        }else{
          int index = getIndex(infoField, record.getVcfMeta());
          String[] infoParts = info.getValRaw().split(infoField.getSeperator());
          return infoParts[index];
        }
      }
    }
    return null;
  }

  private static int getIndex(InfoField infoField, VcfMeta meta) {
    Iterator<VcfMetaInfo> infoMetaIterator = meta.getInfoMeta().iterator();
    while (infoMetaIterator.hasNext()) {
      VcfMetaInfo infoMeta = infoMetaIterator.next();
      if (infoMeta.getId().equals(infoField.getName())) {
        //FIXME: vep specific prefix removal
        String desc = infoMeta.getDescription().replace("Consequence annotations from Ensembl VEP. Format: ","");
        String[] header = desc.split(infoField.getSeperator());
        for (int i = 0; i < header.length; i++) {
          if (header[i].equals(infoField.getKey())) {
            return i;
          }
        }
      }
    }
    return -1;
  }
}
