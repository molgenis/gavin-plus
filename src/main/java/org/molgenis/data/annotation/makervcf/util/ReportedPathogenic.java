package org.molgenis.data.annotation.makervcf.util;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.core.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.structs.AnnotatedVcfRecord;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.vcf.utils.FixVcfAlleleNotation;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfRecordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Created by joeri on 6/1/16.
 */
public class ReportedPathogenic
{
	private static final Logger LOG = LoggerFactory.getLogger(ReportedPathogenic.class);

	private Map<String, AnnotatedVcfRecord> posRefAltToRepPath;

	public ReportedPathogenic(File repPathFile) throws Exception
	{
		VcfReader repPath = GavinUtils.getVcfReader(repPathFile);
		//ClinVar match
		Iterator<VcfRecord> cvIt = repPath.iterator();
		this.posRefAltToRepPath = new HashMap<>();
		while (cvIt.hasNext())
		{
			AnnotatedVcfRecord record = new AnnotatedVcfRecord(cvIt.next());
			for (String alt : VcfRecordUtils.getAlts(record))
			{
				String trimmedRefAlt = FixVcfAlleleNotation.backTrimRefAlt(VcfRecordUtils.getRef(record), alt, "_");

				String key = record.getChromosome() + "_" + record.getPosition() + "_" + trimmedRefAlt;
				posRefAltToRepPath.put(key, record);
			}
		}
	}

	public Judgment classifyVariant(GavinRecord record, String alt, String gene, boolean overrideGeneWithClinvarGene)
			throws Exception
	{
		String trimmedRefAlt = FixVcfAlleleNotation.backTrimRefAlt(record.getRef(), alt, "_");
		String key = record.getChromosome() + "_" + record.getPosition() + "_" + trimmedRefAlt;

		if (posRefAltToClinVar.containsKey(key))
		{
			// e.g.
			// REPORTEDPATHOGENIC=CLINVAR:NM_007375.3(TARDBP):c.1043G>T (p.Gly348Val)|TARDBP|Pathogenic
			Optional<String> clinVar = posRefAltToClinVar.get(key).getClinvar();
			if (clinVar.isPresent())
			{
				String clinvarInfo = clinVar.get();
				//FIXME: is this check robust enough?
				if (StringUtils.containsIgnoreCase(clinvarInfo,"pathogenic"))
				{
					String clinvarGene = clinvarInfo.split("\\|", -1)[1];
					if (!clinvarGene.equalsIgnoreCase(gene))
					{
						if (overrideGeneWithClinvarGene)
						{
							gene = clinvarGene;
						}
						else
						{
							LOG.debug(
									"genes did not match: {} vs {}. Reporting under '{}' while preserving ClinVar data '{}'.",
									clinvarGene, gene, gene, clinvarInfo);

						}
					}
					return new Judgment(Judgment.Classification.Pathogenic, Judgment.Method.genomewide, gene,
							clinvarInfo, "ClinVar", "Reported pathogenic");
				}
				else
				{
					throw new Exception("clinvar hit is not pathogenic: " + clinvarInfo);
				}
			}
		}
		return null;//TODO JvdV: return VOUS?
	}
}
