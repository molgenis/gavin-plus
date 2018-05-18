package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertToGeneStream;
import org.molgenis.data.annotation.makervcf.genestream.core.GeneStream;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class GeneStreamTest extends Setup
{

	protected File inputVcfFile;
	protected HashMap<String, Integer> expectedVariantsPerGene;

	protected File inputVcfFile2;


	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/ConvertToGeneStreamTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "ConvertToGeneStreamTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));
		setExpectedVariantsPerGene();

		InputStream inputVcf2 = DiscoverRelevantVariantsTest.class.getResourceAsStream("/AssignCompoundHetTestFile.vcf");
		inputVcfFile2 = new File(FileUtils.getTempDirectory(), "AssignCompoundHetTestFile.vcf");
		FileCopyUtils.copy(inputVcf2, new FileOutputStream(inputVcfFile2));
	}

	@Test
	public void test() throws Exception
	{
		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
		Iterator<RelevantVariant> reorder = new ConvertToGeneStream(discover.findRelevantVariants(), false).go();

		HashMap<String, Integer> observedVariantsPerGene = new HashMap<>();

		GeneStream gsTest = new GeneStream(reorder, false) {
			@Override
			public void perGene(String gene, List<RelevantVariant> variantsPerGene) throws Exception {
				observedVariantsPerGene.put(gene, variantsPerGene.size());
			}
		};

		Iterator<RelevantVariant> it = gsTest.go();

		int nrOfVariants = 0;
		StringBuffer positions = new StringBuffer();
		while(it.hasNext())
		{
			nrOfVariants++;
			positions.append(it.next().getVariant().getPosition() + "_");

		}

		//there are 45 in the file, but 1 does not contain any relevant hits, so we are left with 44
		assertEquals(nrOfVariants, 44);
		assertEquals(expectedVariantsPerGene, observedVariantsPerGene);

		// double check that order is OK
		assertEquals(positions.toString(), ConvertToGeneStreamTest.expected1);


	}


	private void setExpectedVariantsPerGene()
	{
		expectedVariantsPerGene = new HashMap<>();
		expectedVariantsPerGene.put("geneA", 5);
		expectedVariantsPerGene.put("geneB", 2);
		expectedVariantsPerGene.put("geneC", 7);
		expectedVariantsPerGene.put("geneD", 1);
		expectedVariantsPerGene.put("geneE", 2);
		expectedVariantsPerGene.put("geneF", 2);
		expectedVariantsPerGene.put("geneG", 1);
		expectedVariantsPerGene.put("geneH", 1);
		expectedVariantsPerGene.put("geneI", 2);
		expectedVariantsPerGene.put("geneJ", 5);
		expectedVariantsPerGene.put("geneK", 3);
		expectedVariantsPerGene.put("geneL", 2);
		expectedVariantsPerGene.put("geneM", 5);
		expectedVariantsPerGene.put("geneN", 3);
		expectedVariantsPerGene.put("geneO", 1);
		expectedVariantsPerGene.put("geneP", 5);
		expectedVariantsPerGene.put("geneQ", 3);
		expectedVariantsPerGene.put("geneR", 3);
		expectedVariantsPerGene.put("geneS", 4);
		expectedVariantsPerGene.put("geneT", 3);
		expectedVariantsPerGene.put("geneU", 4);
		expectedVariantsPerGene.put("geneW", 2);
		expectedVariantsPerGene.put("geneX", 1);
		expectedVariantsPerGene.put("geneZ", 2);
		expectedVariantsPerGene.put("OverlapA", 2);
		expectedVariantsPerGene.put("OverlapB", 2);
		expectedVariantsPerGene.put("geneLast", 1);
	}


	@Test
	public void test2() throws Exception
	{
		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile2, gavinFile, clinvarFile, caddFile, null, HandleMissingCaddScores.Mode.ANALYSIS, false);
		Iterator<RelevantVariant> reorder = new ConvertToGeneStream(discover.findRelevantVariants(), false).go();

		GeneStream gsTest = new GeneStream(reorder, false) {
			@Override
			public void perGene(String gene, List<RelevantVariant> variantsPerGene) throws Exception {}
		};

		Iterator<RelevantVariant> it = gsTest.go();
		StringBuffer positions = new StringBuffer();

		while(it.hasNext())
		{
			positions.append(it.next().getVariant().getPosition() + "_");

		}
		// originally a bug found in ConvertToGeneStreamTest
		// double check here that GeneStream did not alter the order
		assertEquals(positions.toString(), ConvertToGeneStreamTest.expected2);

	}

}
