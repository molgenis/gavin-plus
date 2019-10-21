package org.molgenis.data.annotation.makervcf.structs;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.molgenis.data.annotation.makervcf.structs.RVCF.*;
import static org.testng.Assert.assertEquals;

public class RVCFUtilsTest
{
	private RVCF rvcf1;

	@BeforeMethod
	public void setUp(){
		Map<String,String> sampleGenotype = new HashMap<>();
		sampleGenotype.put("1","1|0");
		sampleGenotype.put("2","1|1");
		Map<String,String> samplePhenotype = new HashMap<>();
		samplePhenotype.put("1","pheno1");
		samplePhenotype.put("2","pheno2");
		Map<String,String> sampleGroup = new HashMap<>();
		sampleGroup.put("1","group1");
		sampleGroup.put("2","group2");

		rvcf1 = new RVCF("allele","gene","variantSignificance", "variantSignificanceJustification"); }

	@Test
	public void testGetMergedFieldVcfString()
	{
		assertEquals(RVCFUtils.getMergedFieldVcfString(rvcf1),"allele|alleleFreq|gene|FDR|transcript||phenotypeInheritance|phenotypeOnset|phenotypeDetails|phenotypeGroup|1:CARRIER/2:AFFECTED|1:pheno1/2:pheno2|1:1p0/2:1p1|1:group1/2:group2|variantSignificance|variantSignificanceSource|variantSignificanceJustification|variantMultiGenic|variantGroup");
	}

	@Test
	public void testCreateRvcfInfoFields()
	{
		Map<String, String> expected = new HashMap<>();
		expected.put(RLV_ALLELE, "[allele|gene]allele");
		expected.put(RLV_GENE, "[allele|gene]gene");
		expected.put(RLV_VARIANTSIGNIFICANCE, "[allele|gene]variantSignificance");
		expected.put(RLV_VARIANTSIGNIFICANCEJUSTIFICATION, "[allele|gene]variantSignificanceJustification");

		assertEquals(RVCFUtils.createRvcfValues(rvcf1, Collections.emptyMap()), expected);
	}
	@Test
	public void testCreateRvcfInfoFieldsExistingValues()
	{
		Map<String, String> expected = new HashMap<>();
		expected.put(RLV_ALLELE, "ALLELE,[allele|gene]allele");
		expected.put(RLV_GENE, "GENE,[allele|gene]gene");
		expected.put(RLV_VARIANTSIGNIFICANCE, "VARIANTSIGNIFICANCE,[allele|gene]variantSignificance");
		expected.put(RLV_VARIANTSIGNIFICANCEJUSTIFICATION, "VARIANTSIGNIFICANCEJUSTIFICATION,[allele|gene]variantSignificanceJustification");

		Map<String, String> existing = new HashMap<>();
		existing.put(RLV_ALLELE, "ALLELE");
		existing.put(RLV_GENE, "GENE");
		existing.put(RLV_VARIANTSIGNIFICANCE, "VARIANTSIGNIFICANCE");
		existing.put(RLV_VARIANTSIGNIFICANCEJUSTIFICATION, "VARIANTSIGNIFICANCEJUSTIFICATION");

		assertEquals(RVCFUtils.createRvcfValues(rvcf1, existing), expected);
	}

}