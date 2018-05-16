package org.molgenis.vcf;

import org.molgenis.vcf.meta.VcfMeta;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;

import static java.util.Objects.requireNonNull;

class AbstractVcfWriter implements VcfWriter
{
	private final BufferedWriter bufferedWriter;
	private final VcfMeta vcfMeta;
	private boolean writeVcfMeta = false;

	AbstractVcfWriter(BufferedWriter bufferedWriter, VcfMeta vcfMeta)
	{
		this.bufferedWriter = requireNonNull(bufferedWriter);
		this.vcfMeta = requireNonNull(vcfMeta);
	}

	@Override
	public void write(VcfRecord vcfRecord)
	{
		if (!writeVcfMeta)
		{
			writeVcfMeta();
		}
		writeVcfRecord(vcfRecord);
	}

	private void writeVcfMeta()
	{
		new VcfMetaWriter(bufferedWriter).write(vcfMeta);
		writeVcfColumnHeader();
		writeVcfMeta = true;
	}

	private void writeVcfColumnHeader()
	{
		try
		{
			String[] colNames = vcfMeta.getColNames();
			for (int i = 0; i < colNames.length; ++i)
			{
				if (i > 0)
				{
					bufferedWriter.write('\t');
				}
				bufferedWriter.write(colNames[i]);
			}
			bufferedWriter.write('\n');
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	private void writeVcfRecord(VcfRecord vcfRecord)
	{
		try
		{
			String[] tokens = vcfRecord.getTokens();
			for (int i = 0; i < tokens.length; ++i)
			{
				if (i > 0)
				{
					bufferedWriter.write('\t');
				}
				bufferedWriter.write(tokens[i]);
			}
			bufferedWriter.write('\n');
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void close() throws Exception
	{
		if (!writeVcfMeta)
		{
			writeVcfMeta();
		}
		bufferedWriter.close();
	}
}
