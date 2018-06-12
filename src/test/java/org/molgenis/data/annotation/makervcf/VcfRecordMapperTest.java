package org.molgenis.data.annotation.makervcf;

import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.molgenis.data.annotation.core.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.structs.AnnotatedVcfRecord;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfSample;
import org.molgenis.vcf.meta.VcfMeta;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.STRICT_STUBS;
import static org.testng.Assert.assertEquals;

public class VcfRecordMapperTest
{
	private MockitoSession mockito;

	@Mock
	private VcfMeta vcfMeta;
	@Mock
	private VcfRecordMapperSettings vcfRecordMapperSettings;
	private VcfRecordMapper vcfRecordMapper;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		mockito = mockitoSession().initMocks(this).strictness(STRICT_STUBS).startMocking();
	}

	@AfterMethod
	public void tearDownAfterMethod()
	{
		mockito.finishMocking();
	}

	@Test
	public void testMap()
	{
		GavinRecord gavinRecord = createMock(true, true, true, true, true, true, true);
		when(vcfRecordMapperSettings.includeSamples()).thenReturn(true);
		vcfRecordMapper = new VcfRecordMapper(vcfMeta, vcfRecordMapperSettings);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);

		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", "rs6054257;rs6040355", "GTC", "G,GTCT", "123.45", "q10;s50",
						"key0=val0;key1=val1;RLV=A|0.1|gene0||transcript0||||||||||type0|source0|my_reason_#0||,G|3.4|gene1||transcript1||||||||||type1|source1|my_reason_#1||","GT:DP","0|1:1","1|1:2" }));
	}

	@Test
	public void testMapNoSamples()
	{
		vcfRecordMapper = new VcfRecordMapper(vcfMeta, vcfRecordMapperSettings);
		GavinRecord gavinRecord = createMock(true, true, true, true, true, true, false);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", "rs6054257;rs6040355", "GTC", "G,GTCT", "123.45", "q10;s50",
						"key0=val0;key1=val1;RLV=A|0.1|gene0||transcript0||||||||||type0|source0|my_reason_#0||,G|3.4|gene1||transcript1||||||||||type1|source1|my_reason_#1||" }));
	}

	@Test
	public void testMapSplitRlv()
	{
		when(vcfRecordMapperSettings.splitRlvField()).thenReturn(true);
		vcfRecordMapper = new VcfRecordMapper(vcfMeta, vcfRecordMapperSettings);
		GavinRecord gavinRecord = createMock(true, true, true, true, true, true, false);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", "rs6054257;rs6040355", "GTC", "G,GTCT", "123.45", "q10;s50",
						"key0=val0;key1=val1;RLV_ALLELEFREQ=[A|gene0]0.1,[G|gene1]3.4;RLV_SAMPLEGENOTYPE=[A|gene0].,[G|gene1].;RLV_VARIANTGROUP=[A|gene0].,[G|gene1].;RLV_SAMPLEGROUP=[A|gene0].,[G|gene1].;RLV_FDR=[A|gene0].,[G|gene1].;RLV_PHENOTYPEINHERITANCE=[A|gene0].,[G|gene1].;RLV_ALLELE=[A|gene0]A,[G|gene1]G;RLV_VARIANTCOMPOUNDHET=[A|gene0].,[G|gene1].;RLV_GENE=[A|gene0]gene0,[G|gene1]gene1;RLV_PHENOTYPE=[A|gene0].,[G|gene1].;RLV_PHENOTYPEDETAILS=[A|gene0].,[G|gene1].;RLV_PHENOTYPEGROUP=[A|gene0].,[G|gene1].;RLV_VARIANTSIGNIFICANCE=[A|gene0]type0,[G|gene1]type1;RLV_VARIANTSIGNIFICANCEJUSTIFICATION=[A|gene0]my_reason_#0,[G|gene1]my_reason_#1;RLV_TRANSCRIPT=[A|gene0]transcript0,[G|gene1]transcript1;RLV_PRESENT=[A|gene0]TRUE,[G|gene1]TRUE;RLV_SAMPLESTATUS=[A|gene0].,[G|gene1].;RLV_VARIANTSIGNIFICANCESOURCE=[A|gene0]source0,[G|gene1]source1;RLV_PHENOTYPEONSET=[A|gene0].,[G|gene1].;RLV_SAMPLEPHENOTYPE=[A|gene0].,[G|gene1]." }));
	}

	@Test
	public void testMapNoIdentifiers()
	{
		vcfRecordMapper = new VcfRecordMapper(vcfMeta, vcfRecordMapperSettings);
		GavinRecord gavinRecord = createMock(false, true, true, true, true, true, false);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", ".", "GTC", "G,GTCT", "123.45", "q10;s50",
						"key0=val0;key1=val1;RLV=A|0.1|gene0||transcript0||||||||||type0|source0|my_reason_#0||,G|3.4|gene1||transcript1||||||||||type1|source1|my_reason_#1||" }));
	}

	@Test
	public void testMapNoAlt()
	{
		vcfRecordMapper = new VcfRecordMapper(vcfMeta, vcfRecordMapperSettings);
		GavinRecord gavinRecord = createMock(true, false, true, true, true, true, false);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", "rs6054257;rs6040355", "GTC", ".", "123.45", "q10;s50",
						"key0=val0;key1=val1;RLV=A|0.1|gene0||transcript0||||||||||type0|source0|my_reason_#0||,G|3.4|gene1||transcript1||||||||||type1|source1|my_reason_#1||" }));
	}

	@Test
	public void testMapNoQuality()
	{
		vcfRecordMapper = new VcfRecordMapper(vcfMeta, vcfRecordMapperSettings);
		GavinRecord gavinRecord = createMock(true, true, false, true, true, true, false);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", "rs6054257;rs6040355", "GTC", "G,GTCT", ".", "q10;s50",
						"key0=val0;key1=val1;RLV=A|0.1|gene0||transcript0||||||||||type0|source0|my_reason_#0||,G|3.4|gene1||transcript1||||||||||type1|source1|my_reason_#1||" }));
	}

	@Test
	public void testMapNoFilterStatus()
	{
		vcfRecordMapper = new VcfRecordMapper(vcfMeta, vcfRecordMapperSettings);
		GavinRecord gavinRecord = createMock(true, true, true, false, true, true, false);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", "rs6054257;rs6040355", "GTC", "G,GTCT", "123.45", ".",
						"key0=val0;key1=val1;RLV=A|0.1|gene0||transcript0||||||||||type0|source0|my_reason_#0||,G|3.4|gene1||transcript1||||||||||type1|source1|my_reason_#1||" }));
	}

	@Test
	public void testMapAnnotatedVcfRecordNoRlv()
	{
		vcfRecordMapper = new VcfRecordMapper(vcfMeta, vcfRecordMapperSettings);
		GavinRecord gavinRecord = createMock(true, true, true, true, false, true, false);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", "rs6054257;rs6040355", "GTC", "G,GTCT", "123.45", "q10;s50",
						"key0=val0;key1=val1" }));
	}

	@Test
	public void testMapNoInfo()
	{
		vcfRecordMapper = new VcfRecordMapper(vcfMeta, vcfRecordMapperSettings);
		GavinRecord gavinRecord = createMock(true, true, true, true, true, false, false);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", "rs6054257;rs6040355", "GTC", "G,GTCT", "123.45", "q10;s50",
						"RLV=A|0.1|gene0||transcript0||||||||||type0|source0|my_reason_#0||,G|3.4|gene1||transcript1||||||||||type1|source1|my_reason_#1||" }));
	}

	@Test
	public void testMapNoInfoNoRlv()
	{
		vcfRecordMapper = new VcfRecordMapper(vcfMeta, vcfRecordMapperSettings);
		GavinRecord gavinRecord = createMock(true, true, true, true, false, false, false);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", "rs6054257;rs6040355", "GTC", "G,GTCT", "123.45", "q10;s50",
						"." }));
	}

	private GavinRecord createMock(boolean includeIdentifiers, boolean includeAlt, boolean includeQuality,
			boolean includeFilter, boolean includeRlv, boolean includeInfo, boolean includeSamples)
	{
		AnnotatedVcfRecord annotatedVcfRecord = mock(AnnotatedVcfRecord.class);
		if (includeInfo)
		{
			VcfInfo vcfInfo0 = mock(VcfInfo.class);
			when(vcfInfo0.getKey()).thenReturn("key0");
			when(vcfInfo0.getValRaw()).thenReturn("val0");
			VcfInfo vcfInfo1 = mock(VcfInfo.class);
			when(vcfInfo1.getKey()).thenReturn("key1");
			when(vcfInfo1.getValRaw()).thenReturn("val1");
			when(annotatedVcfRecord.getInformation()).thenReturn(asList(vcfInfo0, vcfInfo1));
		}
		else
		{
			when(annotatedVcfRecord.getInformation()).thenReturn(emptyList());
		}

		GavinRecord gavinRecord = mock(GavinRecord.class);
		when(gavinRecord.getChromosome()).thenReturn("1");
		when(gavinRecord.getPosition()).thenReturn(123);
		if (includeIdentifiers)
		{
			when(gavinRecord.getIdentifiers()).thenReturn(asList("rs6054257", "rs6040355"));
		}
		else
		{
			when(gavinRecord.getIdentifiers()).thenReturn(emptyList());
		}
		when(gavinRecord.getRef()).thenReturn("GTC");
		if (includeAlt)
		{
			when(gavinRecord.getAlts()).thenReturn(new String[] { "G", "GTCT" });
		}
		else
		{
			when(gavinRecord.getAlts()).thenReturn(new String[] {});
		}
		if (includeQuality)
		{
			when(gavinRecord.getQuality()).thenReturn(Optional.of(123.45));
		}
		else
		{
			when(gavinRecord.getQuality()).thenReturn(Optional.empty());
		}
		if (includeFilter)
		{
			when(gavinRecord.getFilterStatus()).thenReturn(asList("q10", "s50"));
		}
		else
		{
			when(gavinRecord.getFilterStatus()).thenReturn(emptyList());
		}
		when(gavinRecord.getAnnotatedVcfRecord()).thenReturn(annotatedVcfRecord);

		if (includeRlv)
		{
			Relevance relevance0 = new Relevance("A", Optional.of("transcript0"), 0.1, 2.3, "gene0",
					new Judgment(Judgment.Classification.Pathogenic, Judgment.Method.calibrated, "gene0",
							"my reason #0", "source0", "type0"));
			Relevance relevance1 = new Relevance("G", Optional.of("transcript1"), 3.4, 5.6, "gene1",
					new Judgment(Judgment.Classification.Pathogenic, Judgment.Method.calibrated, "gene1",
							"my reason #1", "source1", "type1"));
			when(gavinRecord.getRelevance()).thenReturn(asList(relevance0, relevance1));
		}
		else
		{
			when(gavinRecord.getRelevance()).thenReturn(emptyList());
		}

		if (includeSamples)
		{
			when(annotatedVcfRecord.getFormat()).thenReturn(new String[]{"GT","DP"});
			VcfSample sample0 = mock(VcfSample.class);
			when(sample0.getTokens()).thenReturn(new String[]{"0|1","1"});
			VcfSample sample1 = mock(VcfSample.class);
			when(sample1.getTokens()).thenReturn(new String[]{"1|1","2"});
			when(annotatedVcfRecord.getSamples()).thenReturn(asList(sample0, sample1));
		}
		return gavinRecord;
	}
}