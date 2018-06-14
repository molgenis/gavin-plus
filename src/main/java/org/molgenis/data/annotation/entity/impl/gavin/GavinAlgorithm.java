package org.molgenis.data.annotation.entity.impl.gavin;

import org.molgenis.data.annotation.core.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.core.entity.impl.snpeff.Impact;
import org.molgenis.data.annotation.entity.impl.gavin.GavinEntry.Category;

import java.util.Map;
import java.util.Optional;

import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Classification.Benign;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Classification.Pathogenic;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Method.calibrated;
import static org.molgenis.data.annotation.core.entity.impl.gavin.Judgment.Method.genomewide;
import static org.molgenis.data.annotation.core.entity.impl.snpeff.Impact.*;
import static org.molgenis.data.annotation.entity.impl.gavin.GavinEntry.Category.*;

public class GavinAlgorithm
{
	//this is the MAF threshold as calculated in r0.3 of the calibration data
	public static final double GENOMEWIDE_MAF_THRESHOLD = 0.003456145;
	public static final int GENOMEWIDE_CADD_THRESHOLD = 15;

	// sensitivity is more important than specificity, so we can adjust this parameter to globally adjust the thresholds
	// in r0.2, at a setting of 1, ie. default behaviour, GAVIN is 87% sensitive and 82% specific
	// in r0.2, at a setting of 5, a more sensitive setting, GAVIN is 91% sensitive and 77% specific
	// technically we lose more than we gain, but having >90% sensitivity is really important
	// better to have a few more false positives than to miss a true positive
	public static final int extraSensitivityFactor = 5;

	/**
	 * @param impact
	 * @param caddScaled
	 * @param exacMAF
	 * @param gene
	 * @param geneToEntry
	 * @return
	 */
	public Judgment classifyVariant(Optional<Impact> impact, Double caddScaled, Double exacMAF, String gene,
			Map<String, GavinEntry> geneToEntry) {
		Double pathoMAFThreshold;
		Double meanPathogenicCADDScore;
		Double meanPopulationCADDScore;
		Double spec95thPerCADDThreshold;
		Double sens95thPerCADDThreshold;
		Category category;

			//get data from map, for reuse in GAVIN-related tools other than the annotator
			if (!geneToEntry.containsKey(gene))
			{
				//if we have no data for this gene, immediately fall back to the genomewide method
				return genomewideClassifyVariant(impact, caddScaled, exacMAF, gene);
			}
			else
			{
				pathoMAFThreshold = geneToEntry.get(gene).getPathoMAFThreshold() != null ?
						geneToEntry.get(gene).getPathoMAFThreshold() * extraSensitivityFactor * 2 : null;
				meanPathogenicCADDScore = geneToEntry.get(gene).getMeanPathogenicCADDScore() != null ?
						geneToEntry.get(gene).getMeanPathogenicCADDScore() - extraSensitivityFactor : null;
				meanPopulationCADDScore = geneToEntry.get(gene).getMeanPopulationCADDScore() != null ?
						geneToEntry.get(gene).getMeanPopulationCADDScore() - extraSensitivityFactor : null;
				spec95thPerCADDThreshold = geneToEntry.get(gene).getSpec95thPerCADDThreshold() != null ?
						geneToEntry.get(gene).getSpec95thPerCADDThreshold() - extraSensitivityFactor : null;
				sens95thPerCADDThreshold = geneToEntry.get(gene).getSens95thPerCADDThreshold() != null ?
						geneToEntry.get(gene).getSens95thPerCADDThreshold() - extraSensitivityFactor : null;
				category = geneToEntry.get(gene).getCategory();
		}

		// CADD score based classification, calibrated
		if (caddScaled != null)
		{
			switch (category)
			{
				case C1:
				case C2:
					if (caddScaled > meanPathogenicCADDScore)
					{
						return new Judgment(Pathogenic, calibrated, gene,
								"Variant CADD score of " + caddScaled + " is greater than " + meanPathogenicCADDScore
										+ " in a gene for which CADD scores are informative.",null,null);
					}
					else if (caddScaled < meanPopulationCADDScore)
					{
						return new Judgment(Benign, calibrated, gene,
								"Variant CADD score of " + caddScaled + " is less than " + meanPopulationCADDScore
										+ " in a gene for which CADD scores are informative.",null,null);
					}
					//else: this rule does not classify apparently, just continue onto the next rules
					break;
				case C3:
				case C4:
				case C5:
					if (caddScaled > spec95thPerCADDThreshold)
					{
						return new Judgment(Pathogenic, calibrated, gene,
								"Variant CADD score of " + caddScaled + " is greater than " + spec95thPerCADDThreshold
										+ " for this gene.",null,null);
					}
					else if (caddScaled < sens95thPerCADDThreshold)
					{
						return new Judgment(Benign, calibrated, gene,
								"Variant CADD score of " + caddScaled + " is less than " + sens95thPerCADDThreshold
										+ " for this gene.",null,null);
					}
					//else: this rule does not classify apparently, just continue onto the next rules
					break;
			}
		}

		// MAF-based classification, calibrated
		if (pathoMAFThreshold != null && exacMAF > pathoMAFThreshold)
		{
			return new Judgment(Benign, calibrated, gene,
					"Variant MAF of " + exacMAF + " is greater than " + pathoMAFThreshold + ".",null,null);
		}

		String mafReason = "the variant MAF of " + exacMAF + " is less than a MAF of " + pathoMAFThreshold + ".";

		// Impact based classification, calibrated
		if (impact.isPresent())
		{
			if (category == I1 && (impact.get() == HIGH))
			{
				return new Judgment(Pathogenic, calibrated, gene,
						"Variant is of high impact, while there are no known high impact variants in the population. Also, "
								+ mafReason,null,null);
			}
			else if (category == I2 && (impact.get() == MODERATE || impact.get() == HIGH))
			{

				return new Judgment(Pathogenic, calibrated, gene,
						"Variant is of high/moderate impact, while there are no known high/moderate impact variants in the population. Also, "
								+ mafReason,null,null);
			}
			else if (category == I3 && (impact.get() == LOW || impact.get() == MODERATE || impact.get() == HIGH))
			{
				return new Judgment(Pathogenic, calibrated, gene,
						"Variant is of high/moderate/low impact, while there are no known high/moderate/low impact variants in the population. Also, "
								+ mafReason,null,null);
			}
			else if (impact.get() == MODIFIER)
			{
				return new Judgment(Benign, calibrated, gene,
						"Variant is of 'modifier' impact, and therefore unlikely to be pathogenic. However, " + mafReason,null,null);
			}
		}

		//if everything so far has failed, we can still fall back to the genome-wide method
		return genomewideClassifyVariant(impact, caddScaled, exacMAF, gene);
	}

	/**
	 * @param impact
	 * @param caddScaled
	 * @param exacMAF
	 * @param gene
	 * @return
	 */
	public Judgment genomewideClassifyVariant(Optional<Impact> impact, Double caddScaled, Double exacMAF, String gene) {

		exacMAF = exacMAF != null ? exacMAF : 0;

		if (exacMAF > GENOMEWIDE_MAF_THRESHOLD)
		{
			return new Judgment(Benign, genomewide, gene,
					"Variant MAF of " + exacMAF + " is not rare enough to generally be considered pathogenic.",null,null);
		}
		if (impact.isPresent() && impact.get() == MODIFIER)
		{
			return new Judgment(Benign, genomewide, gene,
					"Variant is of 'modifier' impact, and therefore unlikely to be pathogenic.",null,null);
		}
		else
		{
			if (caddScaled != null && caddScaled > GENOMEWIDE_CADD_THRESHOLD)
			{
				return new Judgment(Pathogenic, genomewide, gene, "Variant MAF of " + exacMAF
						+ " is rare enough to be potentially pathogenic and its CADD score of " + caddScaled
						+ " is greater than a global threshold of " + GENOMEWIDE_CADD_THRESHOLD + ".",null,null);
			}
			else if (caddScaled != null && caddScaled <= GENOMEWIDE_CADD_THRESHOLD)
			{
				return new Judgment(Benign, genomewide, gene,
						"Variant CADD score of " + caddScaled + " is less than a global threshold of "
								+ GENOMEWIDE_CADD_THRESHOLD + ", although the variant MAF of " + exacMAF
								+ " is rare enough to be potentially pathogenic.",null,null);
			}
			else
			{
				return new Judgment(Judgment.Classification.VOUS, Judgment.Method.genomewide, gene,
						"Unable to classify variant as benign or pathogenic. The combination of " + impact
								+ " impact, a CADD score of " + caddScaled + " and MAF of " + exacMAF + " in " + gene
								+ " is inconclusive.",null,null);
			}
		}
	}
}