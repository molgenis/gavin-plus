package org.molgenis.data.annotation.makervcf;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.annotation.makervcf.structs.AnnotatedVcfRecord.CADD_SCALED;
import static org.molgenis.data.annotation.makervcf.structs.RVCF.RLV_PRESENT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;
import joptsimple.internal.Strings;
import org.molgenis.data.annotation.core.entity.impl.snpeff.Annotation;
import org.molgenis.data.annotation.makervcf.structs.AnnotatedVcfRecord;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfRecordUtils;
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
    tokens.add(gavinRecord.getChromosome());
    tokens.add(String.valueOf(gavinRecord.getPosition()));

    List<String> identifiers = gavinRecord.getIdentifiers();
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

    if (vcfRecordMapperSettings.includeSamples()) {

      AnnotatedVcfRecord annotatedVcfRecord = gavinRecord.getAnnotatedVcfRecord();
      Iterable<VcfSample> vcfSamples = annotatedVcfRecord.getSamples();
      if (vcfSamples.iterator().hasNext()) {
        tokens.add(createFormatToken(annotatedVcfRecord));
        tokens.addAll(Arrays.asList(annotatedVcfRecord.getSampleTokens()));
      }
    }
    return tokens;
  }

  private String createInfoToken(GavinRecord gavinRecord,
      VcfRecordMapperSettings vcfRecordMapperSettings) {
    Iterable<VcfInfo> vcfInformations = gavinRecord.getAnnotatedVcfRecord().getInformation();

    boolean hasInformation = vcfInformations.iterator().hasNext();

    StringBuilder stringBuilder = new StringBuilder();

    if (hasInformation) {
      //process all info fields except CADD_SCALED, we might have added values there so we process it seperately
      stringBuilder.append(StreamSupport.stream(vcfInformations.spliterator(), false)
          .filter(vcfInfo -> !vcfInfo.getKey().equals(CADD_SCALED))
          .map(this::createInfoTokenPart)
          .collect(joining(";")));

      Double[] caddScores = gavinRecord.getCaddPhredScores();
      if (caddScores != null && caddScores.length > 0) {
        List<String> caddScoresList = Arrays.stream(caddScores).map(this::caddToString)
            .collect(toList());
        if (!caddScoresList.isEmpty()) {
          stringBuilder.append(";")
              .append(createInfoTokenPart(CADD_SCALED, Strings.join(caddScoresList, ",")));
        }
      }
    }

    if (stringBuilder.length() > 0) {
      stringBuilder.append(';');
    }
    if (!gavinRecord.getRelevance().isEmpty()) {
      stringBuilder.append(getRlv(gavinRecord, vcfRecordMapperSettings.splitRlvField(),
          vcfRecordMapperSettings.prefixSplittedRlvFields()));
    } else {
      stringBuilder.append(createInfoTokenPart(RLV_PRESENT, "FALSE"));
    }
    if (vcfRecordMapperSettings.addSplittedAnnFields()) {
      stringBuilder.append(';');
      stringBuilder.append(getAnn(gavinRecord));
    }

    return stringBuilder.toString();
  }

  private String caddToString(Double score) {
    String stringValue;
    if (score != null) {
      stringValue = Double.toString(score);
    } else {
      stringValue = ".";
    }
    return stringValue;
  }

  private String createInfoTokenPart(VcfInfo vcfInfo) {
    return createInfoTokenPart(vcfInfo.getKey(), vcfInfo.getValRaw());
  }

  private String createInfoTokenPart(String key, String value) {
    return key + '=' + value;
  }

  private String createFormatToken(AnnotatedVcfRecord vcfEntity) {
    String[] formatTokens = vcfEntity.getFormat();
    return stream(formatTokens).collect(joining(":"));
  }

  private String getRlv(GavinRecord gavinRecord, boolean splitRlvField,
      boolean prefixSplittedFields) {
    LOG.debug("[MakeRVCFforClinicalVariants] Looking at: {}", gavinRecord);

    List<Relevance> relevance = gavinRecord.getRelevance();
    String rlv = rlvInfoMapper.map(relevance, splitRlvField, prefixSplittedFields);

    LOG.debug(
        "[MakeRVCFforClinicalVariants] Converted relevant variant to a VCF INFO field for writing out: {}",
        rlv);

    return rlv;
  }

  private String getAnn(GavinRecord gavinRecord) {
    String result = "";
    Optional<VcfInfo> annotationInfoField = VcfRecordUtils.getInformation(ANN,
        gavinRecord.getAnnotatedVcfRecord());
    if (annotationInfoField.isPresent()) {
      Annotation annotation = new Annotation(annotationInfoField.get().getValRaw());
      List<String> results = new ArrayList<>();
      Map<String, String> infoFields = annotation.getAnnInfoFields();
      Set<String> fields = infoFields.keySet();
      for (String field : fields) {
        results.add(field + "=" + infoFields.get(field));
      }
      result = Strings.join(results, ";");
    }
    return result;
  }
}
