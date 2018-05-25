package org.molgenis.data.annotation.makervcf;

import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.molgenis.data.annotation.core.entity.impl.gavin.Judgment;
import org.molgenis.data.annotation.makervcf.structs.AnnotatedVcfRecord;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
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
		vcfRecordMapper = new VcfRecordMapper(vcfMeta, vcfRecordMapperSettings, false);
	}

	@AfterMethod
	public void tearDownAfterMethod()
	{
		mockito.finishMocking();
	}

	@Test
	public void testMap()
	{
		GavinRecord gavinRecord = createMock(true, true, true, true, true);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", "rs6054257;rs6040355", "GTC", "G,GTCT", "123.45", "q10;s50",
						"key0=val0;key1=val1;RLV=A|0.1|gene0||transcript0||||||||||type0|source0|my reason #0||,G|3.4|gene1||transcript1||||||||||type1|source1|my reason #1||" }));
	}

	@Test
	public void testMapNoIdentifiers()
	{
		GavinRecord gavinRecord = createMock(false, true, true, true, true);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", ".", "GTC", "G,GTCT", "123.45", "q10;s50",
						"key0=val0;key1=val1;RLV=A|0.1|gene0||transcript0||||||||||type0|source0|my reason #0||,G|3.4|gene1||transcript1||||||||||type1|source1|my reason #1||" }));
	}

	@Test
	public void testMapNoAlt()
	{
		GavinRecord gavinRecord = createMock(true, false, true, true, true);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", "rs6054257;rs6040355", "GTC", ".", "123.45", "q10;s50",
						"key0=val0;key1=val1;RLV=A|0.1|gene0||transcript0||||||||||type0|source0|my reason #0||,G|3.4|gene1||transcript1||||||||||type1|source1|my reason #1||" }));
	}

	@Test
	public void testMapNoQuality()
	{
		GavinRecord gavinRecord = createMock(true, true, false, true, true);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", "rs6054257;rs6040355", "GTC", "G,GTCT", ".", "q10;s50",
						"key0=val0;key1=val1;RLV=A|0.1|gene0||transcript0||||||||||type0|source0|my reason #0||,G|3.4|gene1||transcript1||||||||||type1|source1|my reason #1||" }));
	}

	@Test
	public void testMapNoFilterStatus()
	{
		GavinRecord gavinRecord = createMock(true, true, true, false, true);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", "rs6054257;rs6040355", "GTC", "G,GTCT", "123.45", ".",
						"key0=val0;key1=val1;RLV=A|0.1|gene0||transcript0||||||||||type0|source0|my reason #0||,G|3.4|gene1||transcript1||||||||||type1|source1|my reason #1||" }));
	}

	@Test
	public void testMapAnnotatedVcfRecordNoRlv()
	{
		GavinRecord gavinRecord = createMock(true, true, true, true, false);
		VcfRecord mappedVcfRecord = vcfRecordMapper.map(gavinRecord);
		assertEquals(mappedVcfRecord, new VcfRecord(vcfMeta,
				new String[] { "1", "123", "rs6054257;rs6040355", "GTC", "G,GTCT", "123.45", "q10;s50",
						"key0=val0;key1=val1" }));
	}

	private GavinRecord createMock(boolean includeIdentifiers, boolean includeAlt, boolean includeQuality,
			boolean includeFilter, boolean includeRlv)
	{
		VcfInfo vcfInfo0 = mock(VcfInfo.class);
		when(vcfInfo0.getKey()).thenReturn("key0");
		when(vcfInfo0.getValRaw()).thenReturn("val0");
		VcfInfo vcfInfo1 = mock(VcfInfo.class);
		when(vcfInfo1.getKey()).thenReturn("key1");
		when(vcfInfo1.getValRaw()).thenReturn("val1");

		AnnotatedVcfRecord annotatedVcfRecord = mock(AnnotatedVcfRecord.class);
		when(annotatedVcfRecord.getInformation()).thenReturn(asList(vcfInfo0, vcfInfo1));
		when(annotatedVcfRecord.getRvcf()).thenReturn(null);

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
			Relevance relevance0 = new Relevance("A", "transcript0", 0.1, 2.3, "gene0",
					new Judgment(Judgment.Classification.Pathogenic, Judgment.Method.calibrated, "gene0",
							"my reason #0", "source0", "type0"));
			Relevance relevance1 = new Relevance("G", "transcript1", 3.4, 5.6, "gene1",
					new Judgment(Judgment.Classification.Pathogenic, Judgment.Method.calibrated, "gene1",
							"my reason #1", "source1", "type1"));
			when(gavinRecord.getRelevance()).thenReturn(asList(relevance0, relevance1));
		}
		else
		{
			when(gavinRecord.getRelevance()).thenReturn(emptyList());
		}
		return gavinRecord;
	}
}