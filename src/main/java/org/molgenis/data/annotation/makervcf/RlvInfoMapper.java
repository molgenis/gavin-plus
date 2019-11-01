package org.molgenis.data.annotation.makervcf;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.data.annotation.makervcf.structs.RVCF.FIELD_NAME;
import static org.molgenis.data.annotation.makervcf.structs.RVCFUtils.createRvcfValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import joptsimple.internal.Strings;
import org.molgenis.data.annotation.makervcf.Main.RlvMode;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.RVCFUtils;
import org.molgenis.data.annotation.makervcf.structs.Relevance;

/**
 * Maps {@link org.molgenis.data.annotation.makervcf.structs.Relevance} list to RLV field value.
 */
public class RlvInfoMapper
{

  public String map(List<Relevance> relevanceList, RlvMode rlvMode) {
    StringBuilder infoField = new StringBuilder();
    if (!relevanceList.isEmpty()) {
      List<RVCF> rvcfList = mapRelevanceListToRVcfList(relevanceList);

      if (rlvMode == RlvMode.SPLITTED || rlvMode == RlvMode.BOTH) {
        infoField.append(getSplittedFields(rvcfList));
      }
      if (rlvMode == RlvMode.BOTH) {
        infoField.append(";");
      }
      if (rlvMode == RlvMode.MERGED || rlvMode == RlvMode.BOTH) {
        infoField.append(getMergedFields(rvcfList));
      }
    } else {
      if (rlvMode == RlvMode.MERGED || rlvMode == RlvMode.BOTH) {
        if (infoField.length() != 0) {
          infoField.append(",");
        }
        infoField.append(RVCF.FIELD_NAME + "=" + RVCFUtils.EMPTY_VALUE);
      }
    }
    return infoField.toString();
  }

  private List<RVCF> mapRelevanceListToRVcfList(List<Relevance> relevanceList) {
    List<RVCF> rvcfList = newArrayList();
    for (Relevance rlv : relevanceList) {
      RVCF rvcf = new RVCF();
      rvcf.setGene(rlv.getGene());
      rvcf.setAllele(rlv.getAllele());
      rvcf.setVariantSignificance(rlv.getJudgment().getType());
      rvcf.setVariantSignificanceJustification(rlv.getJudgment().getReason());
      rvcfList.add(rvcf);
    }
    return rvcfList;
  }

  private String getSplittedFields(List<RVCF> rvcfList) {
    Map<String, String> rvcfValues = new HashMap<>();
    for (RVCF rvcf : rvcfList) {
      rvcfValues = createRvcfValues(rvcf, rvcfValues);
    }
    List<String> rlvInfoFields = new ArrayList<>();
    for (Map.Entry<String, String> entry : rvcfValues.entrySet()) {
      rlvInfoFields.add(entry.getKey() + "=" + entry.getValue());
    }
    return Strings.join(rlvInfoFields, ";");
  }

  private String getMergedFields(List<RVCF> rvcfList) {
    List<String> rvcfStringList = new ArrayList<>();
    StringBuilder infoField = new StringBuilder();
    infoField.append(FIELD_NAME + "=");
    for (RVCF rvcf : rvcfList) {
      rvcfStringList.add(RVCFUtils.getMergedFieldVcfString(rvcf));
    }
    infoField.append(Strings.join(rvcfStringList, ","));
    return infoField.toString();
  }
}
