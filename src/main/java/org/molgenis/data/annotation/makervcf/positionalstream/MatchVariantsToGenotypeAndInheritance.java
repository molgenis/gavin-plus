package org.molgenis.data.annotation.makervcf.positionalstream;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;
import org.molgenis.cgd.CGDEntry;
import org.molgenis.cgd.LoadCGD;
import org.molgenis.data.annotation.makervcf.structs.GenoMatchSamples;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.vcf.VcfSample;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.molgenis.cgd.CGDEntry.*;

/**
 * Created by joeri on 6/1/16.
 * <p>
 * Take the output of DiscoverRelevantVariants, re-iterate over the original VCF file, but this time check the genotypes.
 * We want to match genotypes to disease inheritance mode, ie. dominant/recessive acting.
 */
public class MatchVariantsToGenotypeAndInheritance
{

	Iterator<RelevantVariant> relevantVariants;
	Map<String, CGDEntry> cgd;
	int minDepth;
	boolean verbose;
	private Set<String> parents;

	public enum status
	{
		HETEROZYGOUS, HOMOZYGOUS, AFFECTED, CARRIER, BLOODGROUP, HOMOZYGOUS_COMPOUNDHET, AFFECTED_COMPOUNDHET, HETEROZYGOUS_MULTIHIT;

		public static boolean isCompound(status status)
		{
			return (status == HOMOZYGOUS_COMPOUNDHET || status == AFFECTED_COMPOUNDHET) ? true : false;
		}

		public static boolean isPresumedCarrier(status status)
		{
			return (status == HETEROZYGOUS || status == HETEROZYGOUS_MULTIHIT || status == CARRIER) ? true : false;
		}

		public static boolean isPresumedAffected(status status)
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

	public MatchVariantsToGenotypeAndInheritance(Iterator<RelevantVariant> relevantVariants, File cgdFile,
			Set<String> parents, boolean verbose) throws IOException
	{
		this.relevantVariants = relevantVariants;
		this.cgd = LoadCGD.loadCGD(cgdFile);
		this.minDepth = 1;
		this.verbose = verbose;
		this.parents = parents;
	}

	public Iterator<RelevantVariant> go()
	{

		return new Iterator<RelevantVariant>()
		{

			@Override
			public boolean hasNext()
			{
				return relevantVariants.hasNext();
			}

			@Override
			public RelevantVariant next()
			{

					RelevantVariant rv = relevantVariants.next();

					//key: gene, alt allele
				MultiKeyMap fullGenoMatch = null;
				try
				{
					fullGenoMatch = findMatchingSamples(rv);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				for (Relevance rlv : rv.getRelevance())
					{

						String gene = rlv.getGene();

						CGDEntry ce = cgd.get(gene);
						rlv.setCgdInfo(ce);

						status actingTerminology = status.HOMOZYGOUS;
						status nonActingTerminology = status.HETEROZYGOUS;

						// regular inheritance types, recessive and/or dominant or some type, we use affected/carrier because we know how the inheritance acts
						// females can be X-linked carriers, though since X is inactivated, they might be (partly) affected
						if (cgd.containsKey(gene) && (generalizedInheritance.hasKnownInheritance(
								cgd.get(gene).getGeneralizedInheritance())))
						{
							actingTerminology = status.AFFECTED;
							nonActingTerminology = status.CARRIER;
						}

						Map<String, status> sampleStatus = new HashMap<>();
						Map<String, String> sampleGenotypes = new HashMap<>();
						GenoMatchSamples genoMatch = (GenoMatchSamples) fullGenoMatch.get(rlv.getGene(),
								rlv.getAllele());

						if (genoMatch != null)
						{
							for (String key : genoMatch.affected.keySet())
							{
								sampleStatus.put(key, actingTerminology);
								sampleGenotypes.put(key, getSampleFieldValue(genoMatch.affected.get(key), rv.getVariant(), "GT"));
							}
							for (String key : genoMatch.carriers.keySet())
							{
								sampleStatus.put(key, nonActingTerminology);
								sampleGenotypes.put(key, getSampleFieldValue(genoMatch.carriers.get(key), rv.getVariant(), "GT"));
							}
						}

						if (!sampleStatus.isEmpty())
						{
							rlv.setSampleStatus(sampleStatus);
							rlv.setSampleGenotypes(sampleGenotypes);
							rlv.setParentsWithReferenceCalls(genoMatch.parentsWithReferenceCalls);
							if (verbose)
							{
								System.out.println("[MatchVariantsToGenotypeAndInheritance] Assigned sample status: "
										+ sampleStatus.toString() + ", having genotypes: " + sampleGenotypes
										+ ", plus trio parents with reference alleles: "
										+ genoMatch.parentsWithReferenceCalls.toString());
							}
						}
					}
					return rv;
			}
		};
	}

	/**
	 *
	 */
	public MultiKeyMap findMatchingSamples(RelevantVariant rv) throws Exception
	{
		VcfEntity record = rv.getVariant();
		Set<String> alts = rv.getRelevantAlts();
		Set<String> genes = rv.getRelevantGenes();

		MultiKeyMap result = new MultiKeyMap();

		Set<String> parentsWithReferenceCalls = new HashSet<>();

		Iterator<VcfSample> samples = record.getSamples().iterator();
		int sampleIndex = -1;
		while (samples.hasNext())
		{
			sampleIndex++;
			VcfSample sample = samples.next().createClone();

			if (getSampleFieldValue(sample, record, "GT") == null)
			{
				continue;
			}

			String genotype = getSampleFieldValue(sample, record, "GT");
			String sampleName = record.getVcfMeta().getSampleName(sampleIndex);//FIXME verify that this is correct

			// quality filter: we want depth X or more, if available
			if (getSampleFieldValue(sample, record, "DP") != null)
			{
				int depthOfCoverage = Integer.parseInt(getSampleFieldValue(sample, record, "DP"));
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
		// FIXME: also this can be set directly, earlier?
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

	//FIXME: move method to appropriate class, which one? VcfEntity? GavinUtils? new utils class?
	private String getSampleFieldValue(VcfSample sample, VcfEntity vcfEntity, String field)
	{
		String[] format = vcfEntity.getFormat();
		for (int i = 0; i < format.length; i++) {
			if(format[i].equals(field)){
				return sample.getData(i);
			}
		}
		return null;
	}

}
