package org.molgenis.data.annotation.makervcf;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;
import org.molgenis.data.annotation.makervcf.Main.RlvMode;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfSample;
import org.molgenis.vcf.meta.VcfMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps {@link GavinRecord} to {@link VcfRecord}.
 */
class VcfRecordMapper {

  private static final Logger LOG = LoggerFactory.getLogger(VcfRecordMapper.class);
  private static final String MISSING_VALUE = ".";
  public static final String ANN = "ANN";
  public static final String CADD_SCALED = "CADD_SCALED";

  private final VcfMeta vcfMeta;
  private final VcfRecordMapperSettings vcfRecordMapperSettings;
  private final RlvInfoMapper rlvInfoMapper;

  VcfRecordMapper(VcfMeta vcfMeta, VcfRecordMapperSettings vcfRecordMapperSettings) {
    this.vcfMeta = requireNonNull(vcfMeta);
    this.vcfRecordMapperSettings = requireNonNull(vcfRecordMapperSettings);
    rlvInfoMapper = new RlvInfoMapper();
  }

  public VcfRecord map(GavinRecord gavinRecord) {
    List<String> tokens = createTokens(gavinRecord);
    return new VcfRecord(vcfMeta, tokens.toArray(new String[0]));
  }

  private List<String> createTokens(GavinRecord gavinRecord) {
    List<String> tokens = new ArrayList<>();
    tokens.add(gavinRecord.getVcfRecord().getChromosome());
    tokens.add(String.valueOf(gavinRecord.getVcfRecord().getPosition()));

    List<String> identifiers = gavinRecord.getVcfRecord().getIdentifiers();
    tokens.add(!identifiers.isEmpty() ? identifiers.stream().collect(joining(";")) : MISSING_VALUE);

    tokens.add(gavinRecord.getRef());
    String[] altTokens = gavinRecord.getAlts();
    if (altTokens.length == 0) {
      tokens.add(MISSING_VALUE);
    } else {
      tokens.add(stream(altTokens).collect(joining(",")));
    }

    tokens.add(gavinRecord.getQuality().map(Object::toString).orElse(MISSING_VALUE));
    List<String> filterStatus = gavinRecord.getFilterStatus();
    tokens
        .add(!filterStatus.isEmpty() ? filterStatus.stream().collect(joining(";")) : MISSING_VALUE);

    tokens.add(createInfoToken(gavinRecord, vcfRecordMapperSettings));

      VcfRecord annotatedVcfRecord = gavinRecord.getVcfRecord();
      Iterable<VcfSample> vcfSamples = annotatedVcfRecord.getSamples();
      if (vcfSamples.iterator().hasNext()) {
        tokens.add(createFormatToken(annotatedVcfRecord));
        tokens.addAll(getSampleTokens(annotatedVcfRecord));
      }
    return tokens;
  }
  private List<String> getSampleTokens(VcfRecord vcfRecord) {
    int firstSample = VcfMeta.COL_FORMAT_IDX + 1;
    return Arrays.asList(Arrays.copyOfRange(vcfRecord.getTokens(), firstSample, firstSample + vcfRecord.getNrSamples()));
  }

  private String createInfoToken(GavinRecord gavinRecord,
      VcfRecordMapperSettings vcfRecordMapperSettings) {
    Iterable<VcfInfo> vcfInformations = gavinRecord.getVcfRecord().getInformation();

    boolean hasInformation = vcfInformations.iterator().hasNext();

    StringBuilder stringBuilder = new StringBuilder();

    if (hasInformation) {
      stringBuilder.append(StreamSupport.stream(vcfInformations.spliterator(), false)
          .map(this::createInfoTokenPart)
          .collect(joining(";")));
    }

    if (stringBuilder.length() > 0) {
      stringBuilder.append(';');
    }
    if (!gavinRecord.getRelevance().isEmpty()) {
      stringBuilder.append(getRlv(gavinRecord, vcfRecordMapperSettings.rlvMode()));
    }

    return stringBuilder.toString();
  }

  private String createInfoTokenPart(VcfInfo vcfInfo) {
    return createInfoTokenPart(vcfInfo.getKey(), vcfInfo.getValRaw());
  }

  private String createInfoTokenPart(String key, String value) {
    return key + '=' + value;
  }

  private String createFormatToken(VcfRecord vcfEntity) {
    String[] formatTokens = vcfEntity.getFormat();
    return stream(formatTokens).collect(joining(":"));
  }

  private String getRlv(GavinRecord gavinRecord, RlvMode rlvMode) {
    LOG.debug("[MakeRVCFforClinicalVariants] Looking at: {}", gavinRecord);

    List<Relevance> relevance = gavinRecord.getRelevance();
    String rlv = rlvInfoMapper.map(relevance, rlvMode);

    LOG.debug(
        "[MakeRVCFforClinicalVariants] Converted relevant variant to a VCF INFO field for writing out: {}",
        rlv);

    return rlv;
  }
}
