package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.core.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
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
	public void beforeClass() throws IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/DiscoverRelevantVariantsTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "DiscoverRelevantVariantsTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

	}

	@Test
	public void testPredictedPathogenic() throws Exception
	{

		DiscoverRelevantVariants discover = new DiscoverRelevantVariants(inputVcfFile, gavinFile, false);
		Iterator<GavinRecord> it = discover.findRelevantVariants();

		assertTrue(it.hasNext());
		Judgment expected = new Judgment(Judgment.Classification.Pathogenic,Judgment.Method.genomewide,"PARK2","CLINVAR|NM_004562.2(PARK2):c.823C>T (p.Arg275Trp)|PARK2|Pathogenic","GAVIN+RepPatho","Reported pathogenic");
		assertEquals(it.next().getRelevance().get(0).getJudgment(),expected);

		assertTrue(it.hasNext());
		assertEquals(it.next().getRelevance().get(0).getJudgment().getReason(),"Variant CADD score of 32.0 is greater than 30.4 for this gene.");
		assertEquals(it.next().getRelevance().get(0).getJudgment().getClassification(),Judgment.Classification.Pathogenic);
		assertTrue(it.hasNext());

		// multigene, multiallele
		assertEquals(it.next().getRelevance().get(0).getAllele(), "T");
		assertEquals(it.next().getRelevance().get(1).getAllele(), "A");
		assertEquals(it.next().getRelevance().get(0).getGene(), "ALDH5A1");
		assertEquals(it.next().getRelevance().get(1).getGene(), "TERC");
		assertEquals(it.next().getRelevance().get(0).getJudgment().getReason(),"Variant CADD score of 32.0 is greater than 30.4 for this gene.");
		assertEquals(it.next().getRelevance().get(1).getJudgment().getReason(),"Variant is of high/moderate/low impact, while there are no known high/moderate/low impact variants in the population. Also, the variant MAF of 0.0 is less than a MAF of 0.005591579999999973.");

		// mitochondrial with gene name derived from reported pathogenic annotation
		assertTrue(it.hasNext());
		assertEquals(it.next().getVcfRecord().getChromosome(),"MT");
		assertEquals(it.next().getRelevance().get(0).getAllele(), "T");
		assertEquals(it.next().getRelevance().get(0).getGene(), "MT-TP");
		assertEquals(it.next().getRelevance().get(0).getJudgment().getReason(), "CLINVAR|m.15990C>T|MT-TP|Pathogenic");
		assertFalse(it.hasNext());
	}

}
