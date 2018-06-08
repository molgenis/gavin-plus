package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertToGeneStream;
import org.molgenis.data.annotation.makervcf.genestream.impl.AddGeneFDR;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AddGeneFDRTest extends Setup
{

	protected File inputVcfFile;
	protected File cgdFile;
	protected File fdrFile;

	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/MatchVariantsToGenotypeAndInheritanceTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "TrioFilterTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

		InputStream cgd = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r0.1/CGD_1jun2016.txt.gz");
		cgdFile = new File(FileUtils.getTempDirectory(), "CGD_1jun2016.txt.gz");
		FileCopyUtils.copy(cgd, new FileOutputStream(cgdFile));

		InputStream fdr = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r0.1/FDR_allGenes.tsv");
		fdrFile = new File(FileUtils.getTempDirectory(), "FDR_allGenes.tsv");
		FileCopyUtils.copy(fdr, new FileOutputStream(fdrFile));
	}

	@Test
	public void test() throws Exception
	{

		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, true);
		Iterator<GavinRecord> rv3 = new MatchVariantsToGenotypeAndInheritance(discover.findRelevantVariants(), cgdFile, new HashSet<String>()).go();
		ConvertToGeneStream gs = new ConvertToGeneStream(rv3);

		Iterator<GavinRecord> it = new AddGeneFDR(gs.go(), fdrFile).go();

		/*
		FDR data:
		ADCY5 -> ADCY5	20	0	0.007987220447284345	0.0
		ABCG5 -> ABCG5	1	94	3.9936102236421724E-4	0.037539936102236424
		AIFM1 -> AIFM1	4	10	0.001597444089456869	0.003993610223642172
		ALB -> ALB	12	0	0.004792332268370607	0.0
		USP9Y -> USP9Y	32	0	0.012779552715654952	0.0
		 */

		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getFDR(), "0.007987220447284345,0.0");

		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getFDR(), "3.9936102236421724E-4,0.037539936102236424");

		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getFDR(), "0.001597444089456869,0.003993610223642172");

		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getFDR(), "0.004792332268370607,0.0");

		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getFDR(), "0.012779552715654952,0.0");

		assertFalse(it.hasNext());

	}

}
