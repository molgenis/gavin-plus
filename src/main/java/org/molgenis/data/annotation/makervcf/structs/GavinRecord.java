package org.molgenis.data.annotation.makervcf.structs;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.annotation.core.entity.impl.gavin.Impact;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfRecordUtils;

public class GavinRecord
{
	private VcfRecord vcfRecord;
	private List<Relevance> relevances;

	public GavinRecord(VcfRecord record)
	{
		this(record, emptyList());
	}

	public GavinRecord(VcfRecord record, List<Relevance> relevances)
	{
		this.vcfRecord = record;
		this.relevances = requireNonNull(relevances);
	}

	public VcfRecord getVcfRecord()
	{
		return vcfRecord;
	}

	public void setRelevances(List<Relevance> relevances)
	{
		this.relevances = requireNonNull(relevances);
	}

	public boolean isRelevant()
	{
		return !relevances.isEmpty();
	}

	public List<Relevance> getRelevance()
	{
		return relevances;
	}

	public Optional<Double> getQuality()
	{
		String quality = vcfRecord.getQuality();
		return quality != null ? Optional.of(Double.valueOf(quality)) : Optional.empty();
	}

	/**
	 * Returns:
	 * - [] if filters have not been applied
	 * - ["PASS"] if this position has passed all filters, If filters have not been applied an empty list.
	 * - Otherwise, a list of codes for filters that fail. e.g. ["q10", "s50"]
	 */
	public List<String> getFilterStatus()
	{
		String filterStatus = vcfRecord.getFilterStatus();
		if (filterStatus == null)
		{
			return emptyList();
		}
		else if (filterStatus.equals("PASS"))
		{
			return singletonList("PASS");
		}
		else
		{
			return asList(StringUtils.split(filterStatus, ';'));
		}
	}

	public String getRef()
	{
		return VcfRecordUtils.getRef(vcfRecord);
	}

	public String getAlt()
	{
		return VcfRecordUtils.getAlt(vcfRecord);
	}

	public String getAlt(int altIndex)
	{
		return VcfRecordUtils.getAlt(vcfRecord, altIndex);
	}

	public String getChrPosRefAlt()
	{
		return VcfRecordUtils.getChrPosRefAlt(vcfRecord);
	}

	public int getAltIndex(String allele) throws Exception
	{
		return VcfRecordUtils.getAltIndex(vcfRecord, allele);
	}

	public String[] getAlts()
	{
		return VcfRecordUtils.getAlts(vcfRecord);
	}

	public String getId()
	{
		return VcfRecordUtils.getId(vcfRecord);
	}
}
