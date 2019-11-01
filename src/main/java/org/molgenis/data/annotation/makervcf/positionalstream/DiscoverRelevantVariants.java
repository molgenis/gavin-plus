package org.molgenis.data.annotation.makervcf.positionalstream;

import static org.molgenis.data.annotation.makervcf.structs.VepUtils.IMPACT;
import static org.molgenis.data.annotation.makervcf.structs.VepUtils.SYMBOL;
import static org.molgenis.data.annotation.makervcf.structs.VepUtils.getVepValue;
import static org.molgenis.data.annotation.makervcf.structs.VepUtils.getVepValues;
import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.core.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.core.entity.impl.gavin.Impact;
import org.molgenis.data.annotation.entity.impl.gavin.GavinAlgorithm;
import org.molgenis.data.annotation.makervcf.structs.GavinCalibrations;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOG = LoggerFactory.getLogger(DiscoverRelevantVariants.class);
	private VcfReader vcf;
	private GavinCalibrations gavinCalibrations;
	private GavinAlgorithm gavin;
	private boolean keepAllVariants;

	public DiscoverRelevantVariants(File vcfFile, File gavinFile, boolean keepAllVariants) throws Exception
	{
		this.vcf = GavinUtils.getVcfReader(vcfFile);
		this.keepAllVariants = keepAllVariants;
		this.gavin = new GavinAlgorithm();
		this.gavinCalibrations = GavinUtils.getGeneToEntry(gavinFile);
	}

	public Iterator<GavinRecord> findRelevantVariants()
	{
		Iterator<VcfRecord> vcfIterator = vcf.iterator();
		return new Iterator<GavinRecord>()
		{
			GavinRecord nextResult;

			@Override
			public boolean hasNext()
			{
				while (vcfIterator.hasNext())
				{
						GavinRecord gavinRecord = new GavinRecord(vcfIterator.next());
						List<Relevance> relevance = new ArrayList<>();
						List<String> genes = getVepValues(SYMBOL, gavinRecord.getVcfRecord());

					String[] alts = gavinRecord.getAlts();
					for(int i = 0; i < alts.length; i++)
						{
							//FIXME: expect one transcript
								String caddValue = getVepValues("CADD_SCALED", gavinRecord.getVcfRecord(), alts[i]).get(0);
								Double cadd = caddValue != null ? Double.parseDouble(caddValue) : null;
								String mafValue = getVepValues("gnomAD_AF",gavinRecord.getVcfRecord(), alts[i]).get(0);
								Double maf = mafValue !=null ? Double.parseDouble(mafValue):null;
							if (genes.isEmpty())
							{
								LOG.debug("[DiscoverRelevantVariants] WARNING: no genes for variant {}",
										gavinRecord.getChrPosRefAlt());
							}
								for (String gene : genes)
								{
									List<String> vepImpact = getVepValues(IMPACT, gavinRecord.getVcfRecord(), alts[i],gene);
									Impact impact = vepImpact != null ? Impact.valueOf(vepImpact.get(0)) : null;
									Judgment judgment = gavin.classifyVariant(impact, cadd, maf, gene, gavinCalibrations);
									relevance.add(new Relevance(gavinRecord.getAlt(i), gene, judgment));
									}
						}

						if (!relevance.isEmpty())
						{
							gavinRecord.setRelevances(relevance);
							nextResult = gavinRecord;
							LOG.debug("[DiscoverRelevantVariants] Found relevant variant: {}",
									nextResult.getChrPosRefAlt());
							return true;
						}
						else if (keepAllVariants)
						{
							nextResult = gavinRecord;
							return true;
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
