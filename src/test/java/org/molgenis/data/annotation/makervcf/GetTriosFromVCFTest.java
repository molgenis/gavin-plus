package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.genestream.impl.TrioFilter;
import org.molgenis.data.annotation.makervcf.structs.TrioData;
import org.molgenis.data.vcf.datastructures.Trio;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GetTriosFromVCFTest
{

	protected File inputVcfFile;


	@BeforeClass
	public void beforeClass() throws IOException {
		InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/GetTriosFromVCFTestFile.vcf");
		inputVcfFile = new File(FileUtils.getTempDirectory(), "GetTriosFromVCFTestFile.vcf");
		FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));
	}

	@Test
	public void test() throws Exception
	{
		TrioData td = TrioFilter.getTrioData(inputVcfFile);

		Map<String, Trio> trios = td.getTrios();
		Set<String> parents = td.getParents();

		assertEquals(trios.size(), 6);
		assertEquals(parents.size(), 7);

		assertEquals(trios.get("p01").getChild().getId(), "p01");
		assertEquals(trios.get("p01").getMother().getId(), "p02");
		assertEquals(trios.get("p01").getFather().getId(), "p03");

		assertEquals(trios.get("p04").getChild().getId(), "p04");
		assertEquals(trios.get("p04").getMother().getId(), "p02");
		assertEquals(trios.get("p04").getFather().getId(), "p03");

		assertEquals(trios.get("p05").getChild().getId(), "p05");
		assertEquals(trios.get("p05").getMother().getId(), "p02");
		assertEquals(trios.get("p05").getFather().getId(), "p06");

		assertEquals(trios.get("p07").getChild().getId(), "p07");
		assertEquals(trios.get("p07").getMother().getId(), "p08");
		assertEquals(trios.get("p07").getFather().getId(), "p09");

		assertEquals(trios.get("p10").getChild().getId(), "p10");
		assertEquals(trios.get("p10").getMother().getId(), "p11");
		assertEquals(trios.get("p10").getFather(), null);

		assertEquals(trios.get("p12").getChild().getId(), "p12");
		assertEquals(trios.get("p12").getMother(), null);
		assertEquals(trios.get("p12").getFather().getId(), "p13");

		HashSet expectedParents = new HashSet<String>(Arrays.asList("p02", "p03", "p06", "p08", "p09", "p11", "p13"));
		assertEquals(parents, expectedParents);
	}

}
