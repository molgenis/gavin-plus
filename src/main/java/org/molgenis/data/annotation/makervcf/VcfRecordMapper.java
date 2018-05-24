package org.molgenis.data.annotation.makervcf;

import com.google.common.collect.Iterables;
import org.molgenis.data.annotation.makervcf.structs.AnnotatedVcfRecord;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
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

	VcfRecordMapper(VcfMeta vcfMeta, VcfRecordMapperSettings vcfRecordMapperSettings)
	{
		this.vcfMeta = requireNonNull(vcfMeta);
		this.vcfRecordMapperSettings = requireNonNull(vcfRecordMapperSettings);
	}

	public VcfRecord map(GavinRecord gavinRecord)
	{
		List<String> tokens = createTokens(gavinRecord);
		return new VcfRecord(vcfMeta, tokens.toArray(new String[0]));
	}

	// TODO ask JvdV write cadd phred scores and genes from gavin record or from source vcf?
	private List<String> createTokens(GavinRecord gavinRecord)
	{
		List<String> tokens = new ArrayList<>();
		tokens.add(gavinRecord.getChromosome());
		tokens.add(gavinRecord.getPosition() + "");
		tokens.add(gavinRecord.getId());
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

		AnnotatedVcfRecord annotatedVcfRecord = gavinRecord.getAnnotatedVcfRecord();
		String quality = annotatedVcfRecord.getQuality();
		tokens.add(quality != null ? quality : MISSING_VALUE);
		String filterStatus = annotatedVcfRecord.getFilterStatus();
		tokens.add(filterStatus != null ? filterStatus : MISSING_VALUE);

		tokens.add(createInfoToken(getVcfEntityInformation(annotatedVcfRecord)) + ";RLV=" + gavinRecord.getRlv());

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
		return escapeToken(vcfInfo.getKey()) + '=' + escapeToken(vcfInfo.getValRaw());
	}

	private String createFormatToken(AnnotatedVcfRecord vcfEntity)
	{
		String[] formatTokens = vcfEntity.getFormat();
		return stream(formatTokens).map(this::escapeToken).collect(joining(":"));
	}

	private String createSampleToken(VcfSample vcfSample)
	{
		String[] sampleTokens = vcfSample.getTokens();
		return stream(sampleTokens).map(this::escapeToken).collect(joining(":"));
	}

	/**
	 * TODO ask RK and JvdV: does this apply to v4.2 as well? are we interpreting this correctly?
	 * TODO check if our VcfReader unescapes tokens?
	 * <p>
	 * The Variant Call Format Specification VCFv4.3:
	 * Characters with special meaning (such as field delimiters ’;’ in INFO or ’:’ FORMAT fields) must be represented using the capitalized percent encoding:
	 * %3A : (colon)
	 * %3B ; (semicolon)
	 * %3D = (equal sign)
	 * %25 % (percent sign)
	 * %2C , (comma)
	 * %0D CR
	 * %0A LF
	 * %09 TAB
	 * <p>
	 */
	private String escapeToken(String token)
	{
		if (token == null || token.isEmpty())
		{
			return token;
		}

		StringBuilder stringBuilder = new StringBuilder(token.length());
		for (int i = 0; i < token.length(); ++i)
		{
			char c = token.charAt(i);
			switch (c)
			{
				case ':':
					stringBuilder.append("%3A");
					break;
				case ';':
					stringBuilder.append("%3B");
					break;
				case '=':
					stringBuilder.append("%3D");
					break;
				case '%':
					stringBuilder.append("%25");
					break;
				case ',':
					stringBuilder.append("%2C");
					break;
				case '\r':
					stringBuilder.append("%0D");
					break;
				case '\n':
					stringBuilder.append("%0A");
					break;
				case '\t':
					stringBuilder.append("%09");
					break;
				default:
					stringBuilder.append(c);
					break;
			}
		}
		return stringBuilder.toString();
	}



	// TODO refactor code such that method is removed
	public Iterable<VcfInfo> getVcfEntityInformation(AnnotatedVcfRecord annotatedVcfRecord)
	{
		List<RVCF> rvcf;
		rvcf = annotatedVcfRecord.getRvcf();

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
