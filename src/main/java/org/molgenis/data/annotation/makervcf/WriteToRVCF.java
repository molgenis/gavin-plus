package org.molgenis.data.annotation.makervcf;

import static org.molgenis.data.annotation.makervcf.structs.RVCF.RLV_ALLELE;
import static org.molgenis.data.annotation.makervcf.structs.RVCF.RLV_GENE;
import static org.molgenis.data.annotation.makervcf.structs.RVCF.RLV_VARIANTSIGNIFICANCE;
import static org.molgenis.data.annotation.makervcf.structs.RVCF.RLV_VARIANTSIGNIFICANCEJUSTIFICATION;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.makervcf.Main.RlvMode;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfWriter;
import org.molgenis.vcf.VcfWriterFactory;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WriteToRVCF
{
	private static final Logger LOG = LoggerFactory.getLogger(WriteToRVCF.class);
	public static final String STRING = "String";
	public static final String INTEGER = "Integer";

	private static final String KEY_ID = "ID";
	private static final String KEY_NUMBER = "Number";
	private static final String KEY_TYPE = "Type";
	private static final String KEY_DESCRIPTION = "Description";

	void writeRVCF(Iterator<GavinRecord> gavinRecords, File writeTo, File inputVcfFile, String version,
			String cmdString, boolean writeToDisk, VcfRecordMapperSettings vcfRecordMapperSettings)
			throws Exception
	{
		VcfMeta vcfMeta = createRvcfMeta(inputVcfFile, vcfRecordMapperSettings);
		vcfMeta.add("GavinVersion", StringUtils.wrap(version, "\""));
		vcfMeta.add("GavinCmd", StringUtils.wrap(cmdString, "\""));
		LOG.debug("[WriteToRVCF] Writing header");

		try (VcfWriter vcfWriter = new VcfWriterFactory().create(writeTo, vcfMeta))
		{
			VcfRecordMapper vcfRecordMapper = new VcfRecordMapper(vcfMeta, vcfRecordMapperSettings);
			while (gavinRecords.hasNext())
			{
				GavinRecord gavinRecord = gavinRecords.next();
				if (writeToDisk)
				{
					LOG.debug("[WriteToRVCF] Writing VCF record");
					vcfWriter.write(vcfRecordMapper.map(gavinRecord));
				}
			}
		}
	}

	private VcfMeta createRvcfMeta(File inputVcfFile, VcfRecordMapperSettings vcfRecordMapperSettings)
			throws IOException
	{
		VcfMeta vcfMeta;
		try (VcfReader vcfReader = GavinUtils.getVcfReader(inputVcfFile))
		{
			vcfMeta = vcfReader.getVcfMeta();
		}
		if (vcfRecordMapperSettings.rlvMode() == RlvMode.MERGED
				|| vcfRecordMapperSettings.rlvMode() == RlvMode.BOTH)
		{
			addInfoField(vcfMeta, "RLV", ".", STRING,
					"Allele | AlleleFreq | Gene | FDR | Transcript | Phenotype | PhenotypeInheritance | PhenotypeOnset | PhenotypeDetails | PhenotypeGroup | SampleStatus | SamplePhenotype | SampleGenotype | SampleGroup | VariantSignificance | VariantSignificanceSource | VariantSignificanceJustification | VariantCompoundHet | VariantGroup");
		}
		if (vcfRecordMapperSettings.rlvMode() == RlvMode.SPLITTED
				|| vcfRecordMapperSettings.rlvMode() == RlvMode.BOTH)
		{
			addRvcfHeaders(vcfMeta);
		}

		return vcfMeta;
	}

	private void addRvcfHeaders(VcfMeta vcfMeta)
	{
		addInfoField(vcfMeta, RLV_ALLELE, "1", STRING, "\"Allele\"");
		addInfoField(vcfMeta, RLV_GENE, "1", STRING, "\"Gene\"");
		addInfoField(vcfMeta, RLV_VARIANTSIGNIFICANCE, "1", STRING, "\"VariantSignificance\"");
		addInfoField(vcfMeta, RLV_VARIANTSIGNIFICANCEJUSTIFICATION, "1", STRING,
				"\"VariantSignificanceJustification\"");
	}


	private void addInfoField(VcfMeta vcfMeta, String id, String number, String type, String description)
	{
		Map<String, String> properties = new LinkedHashMap<>();

		properties.put(KEY_ID, id);
		properties.put(KEY_NUMBER, number);
		properties.put(KEY_TYPE, type);
		properties.put(KEY_DESCRIPTION, description);
		vcfMeta.addInfoMeta(new VcfMetaInfo(properties));
	}
}
