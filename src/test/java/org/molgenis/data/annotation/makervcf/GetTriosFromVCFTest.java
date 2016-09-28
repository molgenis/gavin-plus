package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.genestream.core.ConvertToGeneStream;
import org.molgenis.data.annotation.makervcf.genestream.impl.TrioFilter;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.molgenis.data.annotation.makervcf.positionalstream.MatchVariantsToGenotypeAndInheritance;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.structs.TrioData;
import org.molgenis.data.annotation.makervcf.util.HandleMissingCaddScores;
import org.molgenis.data.vcf.datastructures.Trio;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GetTriosFromVCFTest
{

	protected File inputVcfFile;


	@BeforeClass
	public void beforeClass() throws FileNotFoundException, IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/GetTriosFromVCFTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "GetTriosFromVCFTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));
	}

	@Test
	public void test() throws Exception
	{
		TrioData td = TrioFilter.getTrioData(inputVcfFile);

		HashMap<String, Trio> trios = td.getTrios();
		Set<String> parents = td.getParents();

		System.out.println(trios.toString());
		System.out.println(parents.toString());


		assertEquals(trios.size(), 6);
		assertEquals(parents.size(), 7);

		


	}

}
