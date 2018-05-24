package org.molgenis.data.annotation.makervcf;

import com.google.common.collect.Iterables;
import org.molgenis.data.annotation.makervcf.structs.AnnotatedVcfRecord;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfSample;
import org.molgenis.vcf.meta.VcfMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Arrays.stream;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * Maps {@link GavinRecord} to {@link VcfRecord}.
 */
class VcfRecordMapper
{
	private static final String MISSING_VALUE = ".";

	private final VcfMeta vcfMeta;
	private final VcfRecordMapperSettings vcfRecordMapperSettings;
	private final boolean verbose;
	private final RlvInfoMapper rlvInfoMapper;

	VcfRecordMapper(VcfMeta vcfMeta, VcfRecordMapperSettings vcfRecordMapperSettings, boolean verbose)
	{
		this.vcfMeta = requireNonNull(vcfMeta);
		this.vcfRecordMapperSettings = requireNonNull(vcfRecordMapperSettings);
		this.verbose = verbose;
		rlvInfoMapper = new RlvInfoMapper();
	}

	public VcfRecord map(GavinRecord gavinRecord)
	{
		List<String> tokens = createTokens(gavinRecord);
		return new VcfRecord(vcfMeta, tokens.toArray(new String[0]));
	}

	private List<String> createTokens(GavinRecord gavinRecord)
	{
		List<String> tokens = new ArrayList<>();
		tokens.add(gavinRecord.getChromosome());
		tokens.add(String.valueOf(gavinRecord.getPosition()));

		List<String> identifiers = gavinRecord.getIdentifiers();
		tokens.add(!identifiers.isEmpty() ? identifiers.stream().collect(joining(";")) : MISSING_VALUE);

		tokens.add(gavinRecord.getRef());
		String[] altTokens = gavinRecord.getAlts();
		if (altTokens.length == 0)
		{
			tokens.add(MISSING_VALUE);
		}
		else
		{
			tokens.add(stream(altTokens).collect(joining(",")));
		}

		tokens.add(gavinRecord.getQuality().map(Object::toString).orElse(MISSING_VALUE));
		List<String> filterStatus = gavinRecord.getFilterStatus();
		tokens.add(!filterStatus.isEmpty() ? filterStatus.stream().collect(joining(";")) : MISSING_VALUE);

		AnnotatedVcfRecord annotatedVcfRecord = gavinRecord.getAnnotatedVcfRecord();
		String infoToken = createInfoToken(getVcfEntityInformation(annotatedVcfRecord));
		if (!gavinRecord.getRelevance().isEmpty())
		{
			infoToken += ";RLV=" + getRlv(gavinRecord);
		}
		tokens.add(infoToken);

		if (vcfRecordMapperSettings.includeSamples())
		{
			Iterable<VcfSample> vcfSamples = gavinRecord.getSamples();
			if (vcfSamples.iterator().hasNext())
			{
				tokens.add(createFormatToken(annotatedVcfRecord));
				vcfSamples.forEach(vcfSample -> tokens.add(createSampleToken(vcfSample)));
			}
		}
		return tokens;
	}

	private String createInfoToken(Iterable<VcfInfo> vcfInformations)
	{
		String infoToken;
		if (vcfInformations.iterator().hasNext())
		{
			infoToken = StreamSupport.stream(vcfInformations.spliterator(), false)
									 .map(this::createInfoTokenPart)
									 .collect(joining(";"));
		}
		else
		{
			infoToken = MISSING_VALUE;
		}
		return infoToken;
	}

	private String createInfoTokenPart(VcfInfo vcfInfo)
	{
		return vcfInfo.getKey() + '=' + vcfInfo.getValRaw();
	}

	private String createFormatToken(AnnotatedVcfRecord vcfEntity)
	{
		String[] formatTokens = vcfEntity.getFormat();
		return stream(formatTokens).collect(joining(":"));
	}

	private String createSampleToken(VcfSample vcfSample)
	{
		String[] sampleTokens = vcfSample.getTokens();
		return stream(sampleTokens).collect(joining(":"));
	}

	private String getRlv(GavinRecord gavinRecord)
	{
		if (verbose)
		{
			System.out.println("[MakeRVCFforClinicalVariants] Looking at: " + gavinRecord.toString());
		}

		List<Relevance> relevance = gavinRecord.getRelevance();
		String rlv = !relevance.isEmpty() ? rlvInfoMapper.map(relevance) : null;

		if (verbose)
		{
			System.out.println(
					"[MakeRVCFforClinicalVariants] Converted relevant variant to a VCF INFO field for writing out: "
							+ rlv);
		}

		return rlv;
	}

	// TODO refactor code such that method is removed
	private Iterable<VcfInfo> getVcfEntityInformation(AnnotatedVcfRecord annotatedVcfRecord)
	{
		List<RVCF> rvcf = annotatedVcfRecord.getRvcf();

		Iterable<VcfInfo> rvcfInformation;
		if (rvcf == null)
		{
			rvcfInformation = annotatedVcfRecord.getInformation();
		}
		else
		{
			String key = "RLV";
			String value = rvcf.stream().map(RVCF::toString).collect(Collectors.joining(","));
			VcfInfo rlvVcfInfo = new VcfInfo(annotatedVcfRecord.getVcfMeta(), key, value);
			rvcfInformation = Iterables.concat(annotatedVcfRecord.getInformation(), singleton(rlvVcfInfo));
		}
		return rvcfInformation;
	}
}
