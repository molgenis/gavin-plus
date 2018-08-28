package org.molgenis.data.annotation.core.entity.impl.snpeff;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.molgenis.data.annotation.core.entity.impl.snpeff.Annotation.*;
import static org.testng.Assert.assertEquals;

public class AnnotationTest
{

	@Test
	public void testGetAnnInfoFieldSingle()
	{
		Annotation annotation = new Annotation(
				"A|missense_variant|MODERATE|ABCB4|ABCB4|transcript|NM_018849.2|protein_coding|15/28|c.1778C>T|p.Thr593Met|1854/3988|1778/3861|593/1286|dist|err");
		Map<String, String> actual = annotation.getAnnInfoFields();

		Map<String, String> expected = new HashMap();
		expected.put(ALLELE, "A");
		expected.put(HGVS_P, "p.Thr593Met");
		expected.put(CDS_LEN, "3861");
		expected.put(DISTANCE, "dist");
		expected.put(ERRORS, "err");
		expected.put(EFFECT, "missense_variant");
		expected.put(GENEID, "ABCB4");
		expected.put(RANK, "15/28");
		expected.put(HGVS_C, "c.1778C>T");
		expected.put(CDS_POS, "1778");
		expected.put(BIOTYPE, "protein_coding");
		expected.put(IMPACT, "MODERATE");
		expected.put(GENE, "ABCB4");
		expected.put(FEATUREID, "NM_018849.2");
		expected.put(FEATURE, "transcript");
		expected.put(CDNA_POS, "1854");
		expected.put(CDNA_LEN, "3988");
		expected.put(AA_LEN, "1286");
		expected.put(AA_POS, "593");

		assertEquals(actual, expected);
	}

	@Test
	public void testGetAnnInfoFieldMulti()
	{
		Annotation annotation = new Annotation(
				"ANN=T|missense_variant|MODERATE|ALDH5A1|ALDH5A1|||||||||||,A|missense_variant|HIGH|TERC|TERC|||||||||||,A|noeffect|MODIFIER|ALDH5A1|ALDH5A1|q|w|e|r|t|y|u/1|i/2|o/3|p|a,T|noeffect|MODIFIER|TERC|TERC|||||||||||;");
		Map<String, String> actual = annotation.getAnnInfoFields();

		Map<String, String> expected = new HashMap();
		expected.put(ALLELE, "ANN=T,A,A,T");
		expected.put(HGVS_P, ",,y,");
		expected.put(CDS_LEN, ",,2,");
		expected.put(DISTANCE, ",,p,");
		expected.put(ERRORS, ",,a,;");
		expected.put(EFFECT, "missense_variant,missense_variant,noeffect,noeffect");
		expected.put(GENEID, "ALDH5A1,TERC,ALDH5A1,TERC");
		expected.put(RANK, ",,r,");
		expected.put(HGVS_C, ",,t,");
		expected.put(CDS_POS, ",,i,");
		expected.put(BIOTYPE, ",,e,");
		expected.put(IMPACT, "MODERATE,HIGH,MODIFIER,MODIFIER");
		expected.put(GENE, "ALDH5A1,TERC,ALDH5A1,TERC");
		expected.put(FEATUREID, ",,w,");
		expected.put(FEATURE, ",,q,");
		expected.put(CDNA_POS, ",,u,");
		expected.put(CDNA_LEN, ",,1,");
		expected.put(AA_LEN, ",,3,");
		expected.put(AA_POS, ",,o,");
		assertEquals(actual, expected);
	}
}