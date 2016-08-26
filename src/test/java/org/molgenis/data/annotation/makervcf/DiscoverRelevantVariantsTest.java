package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Iterator;

import static org.testng.Assert.*;

public class DiscoverRelevantVariantsTest
{
	private File inputVcfFile;
	private File gavinFile;
	private File clinvarFile;
	private File caddFile;


	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException
	{
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/DiscoverRelevantVariantsTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "DiscoverRelevantVariantsTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

		InputStream gavin = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r0.1/GAVIN_calibrations_r0.1.tsv");
		gavinFile = new File(FileUtils.getTempDirectory(), "GAVIN_calibrations_r0.1.tsv");
		FileCopyUtils.copy(gavin, new FileOutputStream(gavinFile));

		InputStream clinvar = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r0.1/clinvar.patho.fix.5.5.16.vcf.gz");
		clinvarFile = new File(FileUtils.getTempDirectory(), "clinvar.patho.fix.5.5.16.vcf.gz");
		FileCopyUtils.copy(clinvar, new FileOutputStream(clinvarFile));

		InputStream cadd = DiscoverRelevantVariantsTest.class.getResourceAsStream("/fromCaddDummy.tsv");
		caddFile = new File(FileUtils.getTempDirectory(), "fromCaddDummy.tsv");
		FileCopyUtils.copy(cadd, new FileOutputStream(caddFile));
	}

	@Test
	public void testPredictedPathogenic() throws Exception
	{

		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
		Iterator<RelevantVariant> it = discover.findRelevantVariants();

		assertTrue(it.hasNext());
		assertTrue(it.next().getJudgment().toString().contains("reason=NM_004562.2(PARK2):c.823C>T (p.Arg275Trp)|PARK2|Pathogenic, classification=Pathogenic"));
		assertTrue(it.hasNext());
		assertTrue(it.next().getJudgment().toString().contains("Variant CADD score of 32.0 is greater than 30.700000000000003 for this gene., classification=Pathogenic"));
		assertFalse(it.hasNext());

	}

}
