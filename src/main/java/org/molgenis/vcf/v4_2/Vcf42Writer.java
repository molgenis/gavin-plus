package org.molgenis.vcf.v4_2;

import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfWriter;
import org.molgenis.vcf.meta.VcfMeta;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * VCF v4.2 writer
 */
public class Vcf42Writer implements VcfWriter
{
	private final Writer writer;
	private final VcfMeta vcfMeta;
	private boolean writeVcfMeta = false;

	public Vcf42Writer(OutputStream outputStream, VcfMeta vcfMeta)
	{
		this.writer = new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8));
		this.vcfMeta = requireNonNull(vcfMeta);
	}

	@Override
	public void write(VcfRecord vcfRecord) throws IOException
	{
		if (!writeVcfMeta)
		{
			writeVcfMeta();
		}
		writeVcfRecord(vcfRecord);
	}

	private void writeVcfMeta() throws IOException
	{
		new Vcf42MetaWriter(writer).write(vcfMeta);
		writeVcfColumnHeader();
		writeVcfMeta = true;
	}

	private void writeVcfColumnHeader() throws IOException
	{
		String[] colNames = vcfMeta.getColNames();
		for (int i = 0; i < colNames.length; ++i)
		{
			if (i > 0)
			{
				writer.write('\t');
			}
			writer.write(colNames[i]);
		}
		writer.write('\n');
	}

	private void writeVcfRecord(VcfRecord vcfRecord) throws IOException
	{
		String[] tokens = vcfRecord.getTokens();
		for (int i = 0; i < tokens.length; ++i)
		{
			if (i > 0)
			{
				writer.write('\t');
			}
			writer.write(tokens[i]);
		}
		writer.write('\n');
	}

	@Override
	public void close() throws Exception
	{
		if (!writeVcfMeta)
		{
			writeVcfMeta();
		}
		writer.close();
	}
}
