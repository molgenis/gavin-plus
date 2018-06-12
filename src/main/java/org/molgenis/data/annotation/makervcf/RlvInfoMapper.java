package org.molgenis.data.annotation.makervcf;

import joptsimple.internal.Strings;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.RVCFUtils;
import org.molgenis.data.annotation.makervcf.structs.Relevance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.molgenis.data.annotation.makervcf.structs.RVCF.FIELD_NAME;
import static org.molgenis.data.annotation.makervcf.structs.RVCFUtils.createRvcfValues;

/**
 * Maps {@link org.molgenis.data.annotation.makervcf.structs.Relevance} list to RLV field value.
 */
public class RlvInfoMapper
{
	private String infoField;

	public String map(List<Relevance> relevanceList, boolean splitRlvField)
	{
		if(!relevanceList.isEmpty())
		{
			List<RVCF> rvcfList = new ArrayList<>();
			for (Relevance rlv : relevanceList)
			{
				RVCF rvcf = new RVCF();

				rvcf.setGene(rlv.getGene());
				rvcf.setFDR(rlv.getFDR());
				rvcf.setAllele(rlv.getAllele());
				rvcf.setAlleleFreq(String.valueOf(rlv.getAlleleFreq()));
				rvcf.setTranscript(rlv.getTranscript());

				if (rlv.getCgdInfo() != null)
				{
					rvcf.setPhenotype(rlv.getCgdInfo().getCondition());
					rvcf.setPhenotypeInheritance(rlv.getCgdInfo().getGeneralizedInheritance().toString());
					rvcf.setPhenotypeOnset(rlv.getCgdInfo().getAge_group());
					rvcf.setPhenotypeDetails(rlv.getCgdInfo().getComments());
					rvcf.setPhenotypeGroup(null);
				}

				rvcf.setVariantSignificance(rlv.getJudgment().getType());
				rvcf.setVariantSignificanceSource(rlv.getJudgment().getSource());
				rvcf.setVariantSignificanceJustification(rlv.getJudgment().getReason());
				rvcf.setVariantMultiGenic(null);
				rvcf.setVariantGroup(null);

				rvcf.setSampleStatus(rlv.getSampleStatus());
				rvcf.setSampleGenotype(rlv.getSampleGenotypes());
				rvcf.setSamplePhenotype(null);
				rvcf.setSampleGroup(null);

				rvcfList.add(rvcf);
			}

			if (splitRlvField)
			{
				Map<String, String> rvcfValues = new HashMap<>();
				for (RVCF rvcf : rvcfList)
				{
					rvcfValues = createRvcfValues(rvcf, rvcfValues);
				}
				List<String> rlvInfoFields = new ArrayList<>();
				for(Map.Entry<String,String> entry : rvcfValues.entrySet()){
					rlvInfoFields.add(entry.getKey() +"="+ entry.getValue());
				}
				infoField = Strings.join(rlvInfoFields, ";");
			}
			else
			{
				List<String> rvcfStringList = new ArrayList<>();
				for (RVCF rvcf : rvcfList)
				{
					rvcfStringList.add(RVCFUtils.getMergedFieldVcfString(rvcf));
				}
				infoField = FIELD_NAME + "=" + Strings.join(rvcfStringList, ",");
			}
		}
		else{
			if (splitRlvField)
			{
				infoField = RVCF.RLV_PRESENT + "=" + "FALSE";
			}
			else{
				infoField = RVCF.FIELD_NAME + "=" + RVCFUtils.EMPTY_VALUE;
			}
		}
		return infoField;
	}
}
