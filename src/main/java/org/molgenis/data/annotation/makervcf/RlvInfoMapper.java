package org.molgenis.data.annotation.makervcf;

import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.Relevance;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps {@link org.molgenis.data.annotation.makervcf.structs.Relevance} list to RLV field value.
 */
public class RlvInfoMapper
{
	public String map(List<Relevance> relevanceList)
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

		StringBuilder rvcfListSB = new StringBuilder();
		for (RVCF rvcf : rvcfList)
		{
			rvcfListSB.append(rvcf.getVcfString()).append(',');
		}
		rvcfListSB.deleteCharAt(rvcfListSB.length() - 1);

		return rvcfListSB.toString();
	}
}
