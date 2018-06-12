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
public class ClinVar
{
	private static final Logger LOG = LoggerFactory.getLogger(ClinVar.class);

	private Map<String, AnnotatedVcfRecord> posRefAltToClinVar;

	public ClinVar(File clinvarFile) throws Exception
	{
		VcfReader clinvar = GavinUtils.getVcfReader(clinvarFile);
		//ClinVar match
		Iterator<VcfRecord> cvIt = clinvar.iterator();
		this.posRefAltToClinVar = new HashMap<>();
		while (cvIt.hasNext())
		{
			AnnotatedVcfRecord record = new AnnotatedVcfRecord(cvIt.next());
			for (String alt : VcfRecordUtils.getAlts(record))
			{
				String trimmedRefAlt = FixVcfAlleleNotation.backTrimRefAlt(VcfRecordUtils.getRef(record), alt, "_");

				String key = record.getChromosome() + "_" + record.getPosition() + "_" + trimmedRefAlt;
				posRefAltToClinVar.put(key, record);
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
			// CLINVAR=NM_005691.3(ABCC9):c.2554C>T (p.Gln852Ter)|ABCC9|Likely pathogenic
			// CLINVAR=NM_004612.3(TGFBR1):c.428T>A (p.Leu143Ter)|TGFBR1|Pathogenic
			Optional<String> clinVar = posRefAltToClinVar.get(key).getClinvar();
			if (clinVar.isPresent())
			{
				String clinvarInfo = clinVar.get();
				if (clinvarInfo.contains("athogenic"))
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
							LOG.debug("genes did not match: " + clinvarGene + " vs " + gene + ". Reporting under '" + gene
									+ "' while preserving ClinVar data '" + clinvarInfo + "'.");

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
