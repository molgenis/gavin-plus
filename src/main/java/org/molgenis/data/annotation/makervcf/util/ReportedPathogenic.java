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

	private Map<String, AnnotatedVcfRecord> posRefAltToRepPatho;

	public ReportedPathogenic(File repPathoFile) throws Exception
	{
		VcfReader repPatho = GavinUtils.getVcfReader(repPathoFile);
		//ClinVar match
		Iterator<VcfRecord> cvIt = repPatho.iterator();
		this.posRefAltToRepPatho = new HashMap<>();
		while (cvIt.hasNext())
		{
			AnnotatedVcfRecord record = new AnnotatedVcfRecord(cvIt.next());
			for (String alt : VcfRecordUtils.getAlts(record))
			{
				String trimmedRefAlt = FixVcfAlleleNotation.backTrimRefAlt(VcfRecordUtils.getRef(record), alt, "_");

				String key = record.getChromosome() + "_" + record.getPosition() + "_" + trimmedRefAlt;
				posRefAltToRepPatho.put(key, record);
			}
		}
	}

	public Judgment classifyVariant(GavinRecord record, String alt, String gene)
			throws Exception
	{
		String trimmedRefAlt = FixVcfAlleleNotation.backTrimRefAlt(record.getRef(), alt, "_");
		String key = record.getChromosome() + "_" + record.getPosition() + "_" + trimmedRefAlt;

		if (posRefAltToRepPatho.containsKey(key))
		{
			// e.g.
			// REPORTEDPATHOGENIC=CLINVAR|NM_002074.4(GNB1):c.284T>C (p.Leu95Pro)|GNB1|Pathogenic
			Optional<String> repPatho = posRefAltToRepPatho.get(key).getReportedPathogenic();
			if (repPatho.isPresent())
			{
				String repPathoInfo = repPatho.get();
				return new Judgment(Judgment.Classification.Pathogenic, Judgment.Method.genomewide, gene, repPathoInfo, "GAVIN+RepPatho", "Reported pathogenic");
			}
		}
		return null;//TODO JvdV: return VOUS?
	}
}
