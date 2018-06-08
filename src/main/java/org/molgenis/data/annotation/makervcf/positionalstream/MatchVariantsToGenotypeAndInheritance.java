package org.molgenis.data.annotation.makervcf.positionalstream;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;
import org.molgenis.cgd.CGDEntry;
import org.molgenis.cgd.LoadCGD;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.GenoMatchSamples;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.data.annotation.makervcf.structs.RelevanceUtils;
import org.molgenis.vcf.VcfSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.molgenis.cgd.CGDEntry.generalizedInheritance;

/**
 * Created by joeri on 6/1/16.
 * <p>
 * Take the output of DiscoverRelevantVariants, re-iterate over the original VCF file, but this time check the genotypes.
 * We want to match genotypes to disease inheritance mode, ie. dominant/recessive acting.
 */
public class MatchVariantsToGenotypeAndInheritance
{
	private static final Logger LOG = LoggerFactory.getLogger(MatchVariantsToGenotypeAndInheritance.class);
	Iterator<GavinRecord> gavinRecords;
	Map<String, CGDEntry> cgd;

	int minDepth;
	private Set<String> parents;

	public enum Status
	{
		HETEROZYGOUS, HOMOZYGOUS, AFFECTED, CARRIER, BLOODGROUP, HOMOZYGOUS_COMPOUNDHET, AFFECTED_COMPOUNDHET, HETEROZYGOUS_MULTIHIT;

		public static boolean isCompound(Status status)
		{
			return (status == HOMOZYGOUS_COMPOUNDHET || status == AFFECTED_COMPOUNDHET) ? true : false;
		}

		public static boolean isPresumedCarrier(Status status)
		{
			return (status == HETEROZYGOUS || status == HETEROZYGOUS_MULTIHIT || status == CARRIER) ? true : false;
		}

		public static boolean isPresumedAffected(Status status)
		{
			return (status == HOMOZYGOUS || status == HOMOZYGOUS_COMPOUNDHET || status == AFFECTED
					|| status == AFFECTED_COMPOUNDHET) ? true : false;
		}

		public static boolean isHomozygous(String genotype) throws Exception
		{
			if (genotype.length() == 1)
			{
				return true;
			}
			if (genotype.length() != 3)
			{
				throw new Exception("Genotype '" + genotype + "' not length 3");
			}
			if (genotype.charAt(0) == genotype.charAt(2))
			{
				return true;
			}
			return false;
		}
	}

	public MatchVariantsToGenotypeAndInheritance(Iterator<GavinRecord> gavinRecords, File cgdFile,
			Set<String> parents) throws IOException
	{
		this.gavinRecords = gavinRecords;
		this.cgd = LoadCGD.loadCGD(cgdFile);
		this.minDepth = 1;
		this.parents = parents;
	}

	public Iterator<GavinRecord> go()
	{

		return new Iterator<GavinRecord>()
		{

			@Override
			public boolean hasNext()
			{
				return gavinRecords.hasNext();
			}

			@Override
			public GavinRecord next()
			{
				GavinRecord gavinRecord = gavinRecords.next();
				if(gavinRecord.isRelevant())
				{
					//key: gene, alt allele
					MultiKeyMap fullGenoMatch = null;
					try
					{
						fullGenoMatch = findMatchingSamples(gavinRecord);
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}

					for (Relevance rlv : gavinRecord.getRelevance())
					{

						String gene = rlv.getGene();

						CGDEntry ce = cgd.get(gene);
						rlv.setCgdInfo(ce);

						Status actingTerminology = Status.HOMOZYGOUS;
						Status nonActingTerminology = Status.HETEROZYGOUS;

						// regular inheritance types, recessive and/or dominant or some type, we use affected/carrier because we know how the inheritance acts
						// females can be X-linked carriers, though since X is inactivated, they might be (partly) affected
						if (cgd.containsKey(gene) && (generalizedInheritance.hasKnownInheritance(cgd.get(gene).getGeneralizedInheritance())))
						{
							actingTerminology = Status.AFFECTED;
							nonActingTerminology = Status.CARRIER;
						}

						Map<String, Status> sampleStatus = new HashMap<>();
						Map<String, String> sampleGenotypes = new HashMap<>();
						GenoMatchSamples genoMatch = (GenoMatchSamples) fullGenoMatch.get(rlv.getGene(), rlv.getAllele());

						if (genoMatch != null)
						{
							for (String key : genoMatch.affected.keySet())
							{
								sampleStatus.put(key, actingTerminology);
								sampleGenotypes.put(key,
										gavinRecord.getSampleFieldValue(genoMatch.affected.get(key), "GT"));
							}
							for (String key : genoMatch.carriers.keySet())
							{
								sampleStatus.put(key, nonActingTerminology);
								sampleGenotypes.put(key,
										gavinRecord.getSampleFieldValue(genoMatch.carriers.get(key), "GT"));
							}

							if (!sampleStatus.isEmpty())
							{
								rlv.setSampleStatus(sampleStatus);
								rlv.setSampleGenotypes(sampleGenotypes);
								rlv.setParentsWithReferenceCalls(genoMatch.parentsWithReferenceCalls);

								String parentsWithReferenceCalls = genoMatch.parentsWithReferenceCalls.toString();
								LOG.debug("[MatchVariantsToGenotypeAndInheritance] Assigned sample Status: "
										+ sampleStatus.toString() + ", having genotypes: " + sampleGenotypes
										+ ", plus trio parents with reference alleles: " + parentsWithReferenceCalls);
							}
						}
					}
				}
				return gavinRecord;
			}
		};
	}

	/**
	 *
	 */
	public MultiKeyMap findMatchingSamples(GavinRecord record) throws Exception
	{
		Set<String> alts = RelevanceUtils.getRelevantAlts(record.getRelevance());
		Set<String> genes = RelevanceUtils.getRelevantGenes(record.getRelevance());

		MultiKeyMap result = new MultiKeyMap();

		Set<String> parentsWithReferenceCalls = new HashSet<>();

		Iterator<VcfSample> samples = record.getSamples().iterator();
		int sampleIndex = -1;
		while (samples.hasNext())
		{
			sampleIndex++;
			VcfSample sample = samples.next().createClone();

			if (record.getSampleFieldValue(sample, "GT") == null)
			{
				continue;
			}

			String genotype = record.getSampleFieldValue(sample, "GT");
			String sampleName = record.getVcfMeta().getSampleName(sampleIndex);

			// quality filter: we want depth X or more, if available
			if (record.getSampleFieldValue(sample, "DP") != null)
			{
				int depthOfCoverage = Integer.parseInt(record.getSampleFieldValue(sample, "DP"));
				if (depthOfCoverage < minDepth)
				{
					continue;
				}
			}

			// skip empty genotypes
			if (genotype.equals("./.") || genotype.equals(".|.") || genotype.equals("."))
			{
				continue;
			}

			// skip reference genotypes unless parents of a child for de novo detection
			if (genotype.equals("0/0") || genotype.equals("0|0") || genotype.equals("0"))
			{
				if (parents.contains(sampleName))
				{
					parentsWithReferenceCalls.add(sampleName);
				}
				continue;
			}

			//now that everything is okay, we can match to inheritance mode for each alt
			for (String alt : alts)
			{
				int altIndex = record.getAltAlleleIndex(alt);

				//and each gene
				for (String gene : genes)
				{
					HashMap<String, VcfSample> carriers = new HashMap<>();
					HashMap<String, VcfSample> affected = new HashMap<>();

					CGDEntry ce = cgd.get(gene);
					generalizedInheritance inheritance =
							ce != null ? ce.getGeneralizedInheritance() : generalizedInheritance.NOTINCGD;

					//all dominant types, so no carriers, and only requirement is that genotype contains 1 alt allele somewhere
					if (inheritance.equals(generalizedInheritance.DOMINANT_OR_RECESSIVE) || inheritance.equals(
							generalizedInheritance.DOMINANT))
					{
						// 1 or more, so works for hemizygous too
						if (genotype.contains(altIndex + ""))
						{
							affected.put(sampleName, sample);
						}
					}

					//all other types, unknown, complex or recessive
					//for recessive we know if its acting or not, but this is handled in the terminology of a homozygous hit being labeled as 'AFFECTED'
					//for other (digenic, maternal, YL, etc) and not-in-CGD we don't know, but we still report homozygous as 'acting' and heterozygous as 'carrier' to make that distinction
					else if (inheritance.equals(generalizedInheritance.RECESSIVE) || inheritance.equals(
							generalizedInheritance.XL_LINKED) || inheritance.equals(generalizedInheritance.OTHER)
							|| inheritance.equals(generalizedInheritance.NOTINCGD) || inheritance.equals(
							generalizedInheritance.BLOODGROUP))
					{
						boolean homozygous = genotype.equals(altIndex + "/" + altIndex) || genotype.equals(
								altIndex + "|" + altIndex);
						boolean hemizygous = genotype.equals(altIndex + "");
						boolean heterozygous =
								genotype.length() == 3 && StringUtils.countMatches(genotype, altIndex + "") == 1;

						// regular homozygous
						if (homozygous)
						{
							affected.put(sampleName, sample);
						}
						//for hemizygous, 1 allele is enough of course
						else if (hemizygous)
						{
							affected.put(sampleName, sample);
						}
						// heterozygous, ie. carriers when disease is recessive
						else if (heterozygous)
						{
							carriers.put(sampleName, sample);
						}

					}
					else
					{
						throw new Exception("inheritance unknown: " + inheritance);
					}

					//FIXME: set directly above with put instead of via putAll afterwards?
					if (result.containsKey(gene, alt))
					{
						GenoMatchSamples match = (GenoMatchSamples) result.get(gene, alt);
						match.carriers.putAll(carriers);
						match.affected.putAll(affected);
					}
					else
					{
						GenoMatchSamples match = new GenoMatchSamples(carriers, affected);
						result.put(gene, alt, match);
					}
				}
			}
		}

		//for relevant combinations, set parents with reference calls (--> this is not related to alternative alleles or gene combinations)
		// FIXME JvdV: : also this can be set directly, earlier?
		for (String alt : alts)
		{
			for (String gene : genes)
			{
				if (result.get(gene, alt) != null)
				{
					((GenoMatchSamples) result.get(gene, alt)).setParentsWithReferenceCalls(parentsWithReferenceCalls);
				}
			}
		}

		return result;
	}
}
