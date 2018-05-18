package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

public class MakeRVCFforClinicalVariantsTest extends Setup
{

	protected File inputVcfFile;
	protected File cgdFile;


	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/DiscoverRelevantVariantsTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "DiscoverRelevantVariantsTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));
		InputStream cgd = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r0.1/CGD_1jun2016.txt.gz");
		cgdFile = new File(FileUtils.getTempDirectory(), "CGD_1jun2016.txt.gz");
		FileCopyUtils.copy(cgd, new FileOutputStream(cgdFile));

	}

	@Test
	public void test() throws Exception
	{

/*		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
		Iterator<GavinRecord> match = new MatchVariantsToGenotypeAndInheritance(discover.findRelevantVariants(), cgdFile, new HashSet<String>(), false).go();
		Iterator<Entity> it = new MakeRVCFforClinicalVariants(match, Pipeline.RLV, false).addRVCFfield();

		assertTrue(it.hasNext());
		String rlv1 = it.next().getString("RLV");
		assertEquals(rlv1, "A|0.002026|PARK2||NM_004562.2:4BM9_A:275_322|Parkinson disease 2  autosomal recessive juvenile|RECESSIVE|N A|Individuals have been reported as responding to therapies such as levodopa||p03:CARRIER||p03:0s1||Reported pathogenic|ClinVar|NM_004562.2(PARK2):c.823C>T (p.Arg275Trp) PARK2 Pathogenic||");

		assertTrue(it.hasNext());
		String rlv2 = it.next().getString("RLV");
		assertEquals(rlv2, "T|0.012|ALDH5A1||NM_170740.1|Succinic semialdehyde dehydrogenase deficiency|RECESSIVE|N A|||p45:CARRIER/p66:CARRIER/p21:CARRIER/p64:CARRIER||p45:0s1/p66:0s1/p21:0s1/p64:0s1||Predicted pathogenic|GAVIN|Variant CADD score of 32.0 is greater than 30.700000000000003 for this gene.||");

		// here, we fine allele T pathogenic for ALDH5A1 and allele A pathogenic for TERC, albeit without genotype matches (not filtered out in this test)
		assertTrue(it.hasNext());
		String rlv3 = it.next().getString("RLV");
		assertEquals(rlv3, "T|0.012|ALDH5A1|||Succinic semialdehyde dehydrogenase deficiency|RECESSIVE|N A|||p45:CARRIER/p66:CARRIER/p21:CARRIER/p64:CARRIER||p45:0s1/p66:0s1/p21:0s1/p64:0s1||Predicted pathogenic|GAVIN|Variant CADD score of 32.0 is greater than 30.700000000000003 for this gene.||,A|0.0|TERC|||Dyskeratosis congenita  autosomal dominant  Aplastic anemia  Pulmonary fibrosis and or bone marrow failure  telomere-related 2|DOMINANT|Pediatric|The presence of mutations has also been reported as increasing risk of malignancy  including melanoma||||||Predicted pathogenic|GAVIN|Variant is of high moderate low impact  while there are no known high moderate low impact variants in the population. Also  the variant MAF of 0.0 is less than a MAF of 0.004622819999999994.||");

		assertFalse(it.hasNext());*/
	}

}
