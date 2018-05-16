package org.molgenis.vcf;

import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.meta.VcfMetaEntry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Map;

import static java.util.Objects.requireNonNull;

class VcfMetaWriter
{
	private static final String HEADER_PREFIX = "##";

	private static final String HEADER_STRUCTURED_ID_KEY = "ID";

	private static final String HEADER_INFO_KEY = "INFO";
	private static final String HEADER_INFO_NUMBER_KEY = "Number";
	private static final String HEADER_INFO_TYPE_KEY = "Type";
	private static final String HEADER_INFO_DESCRIPTION_KEY = "Description";
	private static final String HEADER_INFO_SOURCE_KEY = "Source";
	private static final String HEADER_INFO_VERSION_KEY = "Version";

	private static final String HEADER_FILTER_KEY = "FILTER";
	private static final String HEADER_FILTER_DESCRIPTION_KEY = "Description";

	private static final String HEADER_FORMAT_KEY = "FORMAT";
	private static final String HEADER_FORMAT_NUMBER_KEY = "Number";
	private static final String HEADER_FORMAT_TYPE_KEY = "Type";
	private static final String HEADER_FORMAT_DESCRIPTION_KEY = "Description";

	private static final String HEADER_ALT_KEY = "ALT";
	private static final String HEADER_ALT_DESCRIPTION_KEY = "Description";

	private static final String HEADER_CONTIG_KEY = "contig";

	private static final String HEADER_PEDIGREE_KEY = "PEDIGREE";
	private static final String HEADER_SAMPLE_KEY = "SAMPLE";

	private final BufferedWriter writer;

	VcfMetaWriter(BufferedWriter writer)
	{
		this.writer = requireNonNull(writer);
	}

	void write(VcfMeta vcfMeta)
	{
		writeFileFormatHeader();
		writeUnstructuredMeta(vcfMeta);
		writeStructuredMeta(vcfMeta);
	}

	private void writeFileFormatHeader()
	{
		try
		{
			writer.write("##fileformat=VCFv4.2");
			writer.write('\n');
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private void writeUnstructuredMeta(VcfMeta vcfMeta)
	{
		vcfMeta.getMeta().forEach((key, value) ->
		{
			if (!key.equals("fileformat"))
			{
				writeHeader(key, value);
			}
		});
	}

	private void writeStructuredMeta(VcfMeta vcfMeta)
	{
		vcfMeta.getInfoMeta().forEach(this::writeHeader);
		vcfMeta.getFilterMeta().forEach(this::writeHeader);
		vcfMeta.getFormatMeta().forEach(this::writeHeader);
		vcfMeta.getAltMeta().forEach(this::writeHeader);
		vcfMeta.getContigMeta().forEach(this::writeHeader);
		vcfMeta.getSampleMeta().forEach(this::writeHeader);
		vcfMeta.getPedigreeMeta().forEach(this::writeHeader);
	}

	private void writeHeader(String key, String value)
	{
		try
		{
			writer.write(HEADER_PREFIX);
			writer.write(key);
			writer.write('=');
			writer.write(value);
			writer.write('\n');
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private void writeHeader(VcfMetaEntry vcfMetaEntry)
	{
		try
		{
			String name = vcfMetaEntry.getName();

			writer.write(HEADER_PREFIX);
			writer.write(name);
			writer.write('=');
			writer.write('<');
			for (Iterator<Map.Entry<String, String>> it = vcfMetaEntry.getProperties()
																	  .entrySet()
																	  .iterator(); it.hasNext(); )
			{
				Map.Entry<String, String> entry = it.next();
				writeKeyValue(name, entry.getKey(), entry.getValue());
				if (it.hasNext())
				{
					writer.write(',');
				}
			}
			writer.write('>');
			writer.write('\n');
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private void writeKeyValue(String name, String key, String value) throws IOException
	{
		writer.write(key);
		writer.write('=');
		if (isDoubleQuoteValue(name, key, value))
		{
			writer.write('"');
			writer.write(escape(value));
			writer.write('"');
		}
		else
		{
			writer.write(value);
		}
	}

	private boolean isDoubleQuoteValue(String name, String key, String value)
	{
		boolean doubleQuoteValue = value.matches("\\W"); // TODO check if this is the correct pattern?

		// value must be surrounded by double-quotes in some cases according to VCF specification
		switch (name)
		{
			case HEADER_INFO_KEY:
				switch (key)
				{
					case HEADER_STRUCTURED_ID_KEY:
					case HEADER_INFO_NUMBER_KEY:
					case HEADER_INFO_TYPE_KEY:
						doubleQuoteValue = false;
						break;
					case HEADER_INFO_DESCRIPTION_KEY:
					case HEADER_INFO_SOURCE_KEY:
					case HEADER_INFO_VERSION_KEY:
						doubleQuoteValue = true;
						break;
					default:
						break;
				}
				break;
			case HEADER_FILTER_KEY:
				switch (key)
				{
					case HEADER_STRUCTURED_ID_KEY:
						doubleQuoteValue = false;
						break;
					case HEADER_FILTER_DESCRIPTION_KEY:
						doubleQuoteValue = true;
						break;
					default:
						break;
				}
				break;
			case HEADER_FORMAT_KEY:
				switch (key)
				{
					case HEADER_STRUCTURED_ID_KEY:
					case HEADER_FORMAT_NUMBER_KEY:
					case HEADER_FORMAT_TYPE_KEY:
						doubleQuoteValue = false;
						break;
					case HEADER_FORMAT_DESCRIPTION_KEY:
						doubleQuoteValue = true;
						break;
					default:
						break;
				}
				break;
			case HEADER_ALT_KEY:
				switch (key)
				{
					case HEADER_STRUCTURED_ID_KEY:
						doubleQuoteValue = false;
						break;
					case HEADER_ALT_DESCRIPTION_KEY:
						doubleQuoteValue = true;
						break;
					default:
						break;
				}
				break;
			case HEADER_CONTIG_KEY:
			case HEADER_SAMPLE_KEY:
			case HEADER_PEDIGREE_KEY:
				if (HEADER_STRUCTURED_ID_KEY.equals(key))
				{
					doubleQuoteValue = false;
				}
				break;
			default:
				break;
		}
		return doubleQuoteValue;
	}

	private String escape(String value)
	{
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
