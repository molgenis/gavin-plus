package org.molgenis.data.annotation.makervcf;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfWriter;
import org.molgenis.vcf.VcfWriterFactory;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.molgenis.data.annotation.makervcf.structs.RVCF.*;

class WriteToRVCF
{
	private static final Logger LOG = LoggerFactory.getLogger(WriteToRVCF.class);
	public static final String STRING = "String";

	void writeRVCF(Iterator<GavinRecord> gavinRecords, File writeTo, File inputVcfFile, String version,
			String cmdString, boolean writeToDisk, boolean splitRlvField) throws Exception
	{
		VcfMeta vcfMeta = createRvcfMeta(inputVcfFile, splitRlvField);
		vcfMeta.add("GavinVersion", StringUtils.wrap(version, "\""));
		vcfMeta.add("GavinCmd", StringUtils.wrap(cmdString, "\""));
		LOG.debug("[WriteToRVCF] Writing header");

		try (VcfWriter vcfWriter = new VcfWriterFactory().create(writeTo, vcfMeta))
		{
			VcfRecordMapperSettings vcfRecordMapperSettings = VcfRecordMapperSettings.create(false,
					false, splitRlvField); // TODO create settings based on CLI arguments
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

	private VcfMeta createRvcfMeta(File inputVcfFile, boolean splitRlvField) throws IOException
	{
		VcfMeta vcfMeta;
		try (VcfReader vcfReader = GavinUtils.getVcfReader(inputVcfFile))
		{
			vcfMeta = vcfReader.getVcfMeta();
		}
		vcfMeta.setColNames(Arrays.copyOfRange(vcfMeta.getColNames(), 0, VcfMeta.COL_FORMAT_IDX));
		vcfMeta.setVcfMetaSamples(Collections.emptyMap());

		if (!splitRlvField)
		{
			addInfoField(vcfMeta, "RLV", ".", STRING,
					"Allele | AlleleFreq | Gene | FDR | Transcript | Phenotype | PhenotypeInheritance | PhenotypeOnset | PhenotypeDetails | PhenotypeGroup | SampleStatus | SamplePhenotype | SampleGenotype | SampleGroup | VariantSignificance | VariantSignificanceSource | VariantSignificanceJustification | VariantCompoundHet | VariantGroup");
		}
		else
		{
			addInfoField(vcfMeta, RLV_PRESENT, "1", STRING, "\"RLV present\"");
			addInfoField(vcfMeta, RLV_ALLELE, "1", STRING, "\"Allele\"");
			addInfoField(vcfMeta, RLV_ALLELEFREQ, "1", STRING, "\"AlleleFreq\"");
			addInfoField(vcfMeta, RLV_GENE, "1", STRING, "\"Gene\"");
			addInfoField(vcfMeta, RLV_FDR, "1", STRING, "\"FDR\"");
			addInfoField(vcfMeta, RLV_TRANSCRIPT, "1", STRING, "\"Transcript\"");
			addInfoField(vcfMeta, RLV_PHENOTYPE, "1", STRING, "\"Phenotype\"");
			addInfoField(vcfMeta, RLV_PHENOTYPEINHERITANCE, "1", STRING, "\"PhenotypeInheritance\"");
			addInfoField(vcfMeta, RLV_PHENOTYPEONSET, "1", STRING, "\"PhenotypeOnset\"");
			addInfoField(vcfMeta, RLV_PHENOTYPEDETAILS, "1", STRING, "\"PhenotypeDetails\"");
			addInfoField(vcfMeta, RLV_PHENOTYPEGROUP, "1", STRING, "\"PhenotypeGroup\"");
			addInfoField(vcfMeta, RLV_SAMPLESTATUS, "1", STRING, "\"SampleStatus\"");
			addInfoField(vcfMeta, RLV_SAMPLEPHENOTYPE, "1", STRING, "\"SamplePhenotype\"");
			addInfoField(vcfMeta, RLV_SAMPLEGENOTYPE, "1", STRING, "\"SampleGenotype\"");
			addInfoField(vcfMeta, RLV_SAMPLEGROUP, "1", STRING, "\"SampleGroup\"");
			addInfoField(vcfMeta, RLV_VARIANTSIGNIFICANCE, "1", STRING, "\"VariantSignificance\"");
			addInfoField(vcfMeta, RLV_VARIANTSIGNIFICANCESOURCE, "1", STRING, "\"VariantSignificanceSource\"");
			addInfoField(vcfMeta, RLV_VARIANTSIGNIFICANCEJUSTIFICATION, "1", STRING,
					"\"VariantSignificanceJustification\"");
			addInfoField(vcfMeta, RLV_VARIANTCOMPOUNDHET, "1", STRING, "\"VariantCompoundHet\"");
			addInfoField(vcfMeta, RLV_VARIANTGROUP, "1", STRING, "\"VariantGroup\"");
		} return vcfMeta;
	}

	private void addInfoField(VcfMeta vcfMeta, String id, String number, String type, String description)
	{
		Map<String, String> properties = new LinkedHashMap<>();

		properties.put("ID", id);
		properties.put("NUMBER", number);
		properties.put("TYPE", type);
		properties.put("Description", description);
		vcfMeta.addInfoMeta(new VcfMetaInfo(properties));
	}
}
