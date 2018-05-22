package org.molgenis.data.annotation.makervcf;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfWriter;
import org.molgenis.vcf.VcfWriterFactory;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaInfo;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by joeri on 7/18/16.
 */
class WriteToRVCF
{
	void writeRVCF(Iterator<GavinRecord> relevantVariants, File writeTo, File inputVcfFile, boolean writeToDisk,
			boolean verbose) throws Exception
	{
		VcfMeta vcfMeta = createRvcfMeta(inputVcfFile);
		if (verbose)
		{
			System.out.println("[WriteToRVCF] Writing header");
		}

		try (VcfWriter vcfWriter = new VcfWriterFactory().create(writeTo, vcfMeta))
		{
			VcfRecordMapper vcfRecordMapper = new VcfRecordMapper(vcfMeta);
			while (relevantVariants.hasNext())
			{
				GavinRecord gavinRecord = relevantVariants.next();
				if (writeToDisk)
				{
					if (verbose)
					{
						System.out.println("[WriteToRVCF] Writing VCF record");
					}
					vcfWriter.write(vcfRecordMapper.map(gavinRecord));
				}
			}
		}
	}

	private VcfMeta createRvcfMeta(File inputVcfFile) throws IOException
	{
		VcfMeta vcfMeta;
		try (VcfReader vcfReader = GavinUtils.getVcfReader(inputVcfFile))
		{
			vcfMeta = vcfReader.getVcfMeta();
		}
		vcfMeta.setColNames(Arrays.copyOfRange(vcfMeta.getColNames(), 0, VcfMeta.COL_FORMAT_IDX));
		vcfMeta.setVcfMetaSamples(Collections.emptyMap());

		Map<String, String> properties = new LinkedHashMap<>();
		properties.put("ID", "RLV");
		properties.put("NUMBER", ".");
		properties.put("TYPE", "String");
		properties.put("Description",
				"Allele | AlleleFreq | Gene | FDR | Transcript | Phenotype | PhenotypeInheritance | PhenotypeOnset | PhenotypeDetails | PhenotypeGroup | SampleStatus | SamplePhenotype | SampleGenotype | SampleGroup | VariantSignificance | VariantSignificanceSource | VariantSignificanceJustification | VariantCompoundHet | VariantGroup");
		vcfMeta.addInfoMeta(new VcfMetaInfo(properties));

		return vcfMeta;
	}
}
