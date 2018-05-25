package org.molgenis.data.annotation.makervcf.positionalstream;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.core.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.core.entity.impl.snpeff.Impact;
import org.molgenis.data.annotation.entity.impl.gavin.GavinAlgorithm;
import org.molgenis.data.annotation.entity.impl.gavin.GavinEntry;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.data.annotation.makervcf.util.ClinVar;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores.Mode;
import org.molgenis.data.annotation.makervcf.util.LabVariants;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfRecordUtils;

import java.io.File;
import java.util.*;

/**
 * Created by joeri on 6/1/16.
 * <p>
 * Scan through a VCF, apply GAVIN to site-variant-allele-gene combinations, mark variants that are pathogenic or VOUS.
 * Also run past known pathogenic/likely pathogenic variants in ClinVar.
 * Result is a list of 'judged variants' with all meta data stored within.
 */
public class DiscoverRelevantVariants
{

	private VcfReader vcf;
	private LabVariants lab;
	private Map<String, GavinEntry> gavinData;
	private GavinAlgorithm gavin;
	private HandleMissingCaddScores hmcs;
	private ClinVar clinvar;
	private boolean verbose;

	public DiscoverRelevantVariants(File vcfFile, File gavinFile, File clinvarFile, File caddFile, File labVariants,
			Mode mode, boolean verbose) throws Exception
	{
		this.vcf = GavinUtils.getVcfReader(vcfFile);
		this.clinvar = new ClinVar(clinvarFile);
		if (labVariants != null)
		{
			this.lab = new LabVariants(labVariants);
		}
		this.gavin = new GavinAlgorithm();
		this.gavinData = GavinUtils.getGeneToEntry(gavinFile);
		this.hmcs = new HandleMissingCaddScores(mode, caddFile);
		this.verbose = verbose;
	}

	public Iterator<GavinRecord> findRelevantVariants()
	{

		Iterator<VcfRecord> vcfIterator = vcf.iterator();

		return new Iterator<GavinRecord>()
		{

			GavinRecord nextResult;

			int pos = -1;
			int previousPos = -1;

			String chrom;
			String previousChrom = null;

			String chrPosRefAlt;
			String previouschrPosRefAlt = null;

			Set<String> chromosomesSeenBefore = new HashSet<>();

			@Override
			public boolean hasNext()
			{
				while (vcfIterator.hasNext())
				{
					try
					{
						GavinRecord vcfEntity = new GavinRecord(vcfIterator.next());

						pos = vcfEntity.getPosition();
						chrom = vcfEntity.getChromosome();
						chrPosRefAlt = vcfEntity.getChrPosRefAlt();

						// check: no 'before' positions on the same chromosome allowed
						if (previousPos != -1 && previousChrom != null && pos < previousPos && previousChrom.equals(
								chrom))
						{
							throw new Exception(
									"Site position " + pos + " before " + previousPos + " on the same chromosome ("
											+ chrom + ") not allowed. Please sort your VCF file.");
						}

						// check: same chrom+pos+ref+alt combinations not allowed
						if (previouschrPosRefAlt != null && previouschrPosRefAlt.equals(chrPosRefAlt))
						{
							throw new Exception("Chrom-pos-ref-alt combination seen twice: " + chrPosRefAlt
									+ ". This is not allowed. Please check your VCF file.");
						}

						// check: when encountering new chromosome, save previous one seen before
						// subsequently, we should not encounter this chromosome again (e.g. 1, 2, 3, then 2 again)
						if (previousChrom != null && !previousChrom.equals(chrom))
						{
							chromosomesSeenBefore.add(previousChrom);
						}
						if (chromosomesSeenBefore.contains(chrom))
						{
							throw new Exception("Chromosome " + chrom
									+ " was interrupted by other chromosomes. Please sort your VCF file.");

						}

						// cycle for next iteration
						previousPos = pos;
						previousChrom = chrom;
						previouschrPosRefAlt = chrPosRefAlt;

						List<Relevance> relevance = new ArrayList<>();

						/**
						 * Iterate over alternatives, if applicable multi allelic example: 1:1148100-1148100
						 */
						for (int i = 0; i < vcfEntity.getAlts().length; i++)
						{
							Double cadd = hmcs.dealWithCaddScores(vcfEntity, i);

							//if mitochondrial, we have less tools / data, can't do much, just match to clinvar
							if (vcfEntity.getChromosome().equals("MT") || vcfEntity.getChromosome().equals("M")
									|| vcfEntity.getChromosome().equals("mtDNA"))
							{
								Judgment judgment = null;
								Judgment labJudgment = lab != null ? lab.classifyVariant(vcfEntity,
										vcfEntity.getAlt(i), "MT") : null;
								Judgment clinvarJudgment = clinvar.classifyVariant(vcfEntity,
										vcfEntity.getAlt(i), "MT", true);

								if (labJudgment != null
										&& labJudgment.getClassification() == Judgment.Classification.Pathogenic)
								{
									judgment = labJudgment;
								}
								else if (clinvarJudgment != null
										&& clinvarJudgment.getClassification() == Judgment.Classification.Pathogenic)
								{
									judgment = clinvarJudgment;
								}

								if (judgment != null && judgment.getClassification()
																.equals(Judgment.Classification.Pathogenic))
								{
									vcfEntity.setGenes(judgment.getGene());
									relevance.add(new Relevance(vcfEntity.getAlt(i),
											clinvarJudgment.getGene(), vcfEntity.getExAcAlleleFrequencies(i),
											vcfEntity.getGoNlAlleleFrequencies(i), clinvarJudgment.getGene(),
											clinvarJudgment));
								}
							}

							else
							{

								if (vcfEntity.getGenes().size() == 0)
								{
									if (verbose)
									{
										System.out.println("[DiscoverRelevantVariants] WARNING: no genes for variant "
												+ vcfEntity.toString());
									}
								}
								for (String gene : vcfEntity.getGenes())
								{
									Impact impact = vcfEntity.getImpact(i, gene);
									String transcript = vcfEntity.getTranscript(i, gene);

									Judgment judgment = null;
									Judgment labJudgment = lab != null ? lab.classifyVariant(vcfEntity,
											vcfEntity.getAlt(i), gene) : null;
									Judgment clinvarJudgment = clinvar.classifyVariant(vcfEntity,
											vcfEntity.getAlt(i), gene, false);
									//FIXME: where to get gavin thresholds
									Judgment gavinJudgment = gavin.classifyVariant(impact, cadd,
											vcfEntity.getExAcAlleleFrequencies(i), gene, gavinData);

									if (labJudgment != null
											&& labJudgment.getClassification() == Judgment.Classification.Pathogenic)
									{
										judgment = labJudgment;
									}
									else if (clinvarJudgment != null && clinvarJudgment.getClassification()
											== Judgment.Classification.Pathogenic)
									{
										judgment = clinvarJudgment;
									}
									else if (gavinJudgment != null
											&& gavinJudgment.getClassification() == Judgment.Classification.Pathogenic)
									{
										judgment = gavinJudgment.setSource("GAVIN").setType("Predicted pathogenic");
									}

									if (judgment != null
											&& judgment.getClassification() == Judgment.Classification.Pathogenic)
									{
										relevance.add(new Relevance(vcfEntity.getAlt(i), transcript,
												vcfEntity.getExAcAlleleFrequencies(i),
												vcfEntity.getGoNlAlleleFrequencies(i), gene, judgment));
									}
								}
							}
						}

						if (relevance.size() > 0)
						{
							vcfEntity.setRelevances(relevance);
							nextResult = vcfEntity;
							if (verbose)
							{
								System.out.println("[DiscoverRelevantVariants] Found relevant variant: "
										+ nextResult.toStringShort());
							}
							return true;
						}
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}
				}
				return false;
			}

			@Override
			public GavinRecord next()
			{
				return nextResult;
			}
		};
	}
}
