package org.molgenis.data.annotation.core.entity.impl.snpeff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.molgenis.exception.UnexpectedAnnValueException;

public class Annotation
{
	public static final String SNPEFF_SEPERATOR = "\\|";
	public static final String VARIANT_SEPERATOR = ",";
	public static final String POS_LEN_SEPERATOR = "\\/";
	public static final int ANNOTATION_SIZE = 16;
	public static final int CDNA_INDEX = 11;
	public static final int CDS_INDEX = 12;
	public static final int AA_INDEX = 13;

	public static final String PREFIX = "ANN_";
	public static final String ALLELE = PREFIX + "ALLELE";
	public static final String EFFECT = PREFIX + "EFFECT";
	public static final String IMPACT = PREFIX + "IMPACT";
	public static final String GENE = PREFIX + "GENE";
	public static final String GENEID = PREFIX + "GENEID";
	public static final String FEATURE = PREFIX + "FEATURE";
	public static final String FEATUREID = PREFIX + "FEATUREID";
	public static final String BIOTYPE = PREFIX + "BIOTYPE";
	public static final String RANK = PREFIX + "RANK";
	public static final String HGVS_C = PREFIX + "HGVS_C";
	public static final String HGVS_P = PREFIX + "HGVS_P";
	public static final String CDNA_POS = PREFIX + "CDNA_POS";
	public static final String CDNA_LEN = PREFIX + "CDNA_LEN";
	public static final String CDS_POS = PREFIX + "CDS_POS";
	public static final String CDS_LEN = PREFIX + "CDS_LEN";
	public static final String AA_POS = PREFIX + "AA_POS";
	public static final String AA_LEN = PREFIX + "AA_LEN";
	public static final String DISTANCE = PREFIX + "DISTANCE";
	public static final String ERRORS = PREFIX + "ERRORS";

	List<String> headers = new ArrayList<>(
			Arrays.asList(ALLELE, EFFECT, IMPACT, GENE, GENEID, FEATURE, FEATUREID, BIOTYPE, RANK, HGVS_C, HGVS_P,
					CDNA_POS, CDNA_LEN, CDS_POS, CDS_LEN, AA_POS, AA_LEN, DISTANCE, ERRORS));
	private final String annotationFieldValue;

	public Annotation(String annotationFieldValue)
	{
		this.annotationFieldValue = annotationFieldValue;
	}

	private String[] getAnnotationFieldValues()
	{
		return annotationFieldValue.split(VARIANT_SEPERATOR, -1);
	}

	public Map<String, String> getAnnInfoFields()
	{
		Map<String, String> result = new HashMap<>();
		for (String mergedAnnotationValue : getAnnotationFieldValues())
		{
			String[] annotationValues = mergedAnnotationValue.split(SNPEFF_SEPERATOR, -1);
			List<String> annotationValueList = new ArrayList<>();

			preprocessValues(annotationValues, annotationValueList);

			for (int i = 0; i < headers.size(); i++)
			{
				String value = annotationValueList.get(i);

				String header = headers.get(i);
				if (result.containsKey(header))
				{
					value = result.get(header) + VARIANT_SEPERATOR + value;
				}
				result.put(header, value);
			}
		}
		return result;
	}

	/*
	 * Method to preprocess the CDNA/CDS/AA values, thes values are actually to seperate values seperated by a slash
	 **/
	private void preprocessValues(String[] annotationValues, List<String> annotationValueList)
	{
		for (int i = 0; i < ANNOTATION_SIZE; i++)
		{
			String value = annotationValues[i];
			if (i == AA_INDEX || i == CDNA_INDEX || i == CDS_INDEX)
			{
				if (!value.isEmpty())
				{
					String[] split = value.split(POS_LEN_SEPERATOR, -1);
					if (split.length == 2)
					{
						annotationValueList.add(split[0]);
						annotationValueList.add(split[1]);
					}
					else
					{
            throw new UnexpectedAnnValueException(String
                .format("expected either no value or a value containing a '/', found [%s] instead.",
                    value));
					}
				}
				else
				{
					annotationValueList.add("");
					annotationValueList.add("");
				}
			}
			else
			{
				annotationValueList.add(value);
			}
		}
	}
}
