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

public class DiscoverRelevantVariantsTest extends Setup
{

	protected File inputVcfFile;

	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/DiscoverRelevantVariantsTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "DiscoverRelevantVariantsTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

	}

	@Test
	public void testPredictedPathogenic() throws Exception
	{

		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
		Iterator<RelevantVariant> it = discover.findRelevantVariants();

		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getJudgment().toString().contains("reason=NM_004562.2(PARK2):c.823C>T (p.Arg275Trp)|PARK2|Pathogenic, classification=Pathogenic"));
		assertTrue(it.hasNext());
		assertTrue(it.next().getRelevance().get(0).getJudgment().toString().contains("Variant CADD score of 32.0 is greater than 30.700000000000003 for this gene., classification=Pathogenic"));
		assertTrue(it.hasNext());

		// multigene, multiallele
		assertEquals(it.next().getRelevance().get(0).getAllele(), "T");
		assertEquals(it.next().getRelevance().get(1).getAllele(), "A");
		assertEquals(it.next().getRelevance().get(0).getGene(), "ALDH5A1");
		assertEquals(it.next().getRelevance().get(1).getGene(), "OTHERGENE");
		assertTrue(it.next().getRelevance().get(0).getJudgment().toString().contains("Variant CADD score of 32.0 is greater than 30.700000000000003 for this gene"));
		assertTrue(it.next().getRelevance().get(1).getJudgment().toString().contains("Variant MAF of 0.0 is rare enough to be potentially pathogenic and its CADD score of 99.0 is greater than a global threshold of 15"));

		assertFalse(it.hasNext());
	}

}
