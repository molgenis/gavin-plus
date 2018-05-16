package org.molgenis.vcf;

import net.sf.samtools.util.BlockCompressedOutputStream;
import org.molgenis.vcf.meta.VcfMeta;
import org.molgenis.vcf.v4_2.Vcf42Writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static java.lang.String.format;
import static org.molgenis.vcf.VcfWriterFactory.Format.GZIP;
import static org.molgenis.vcf.VcfWriterFactory.Format.UNCOMPRESSED;

public class VcfWriterFactory
{
	public enum Format
	{
		GZIP, UNCOMPRESSED
	}

	public enum Version
	{
		V4_1, V4_2, V4_3
	}

	/**
	 * Returns a {@link Vcf42Writer} to write gzipped or uncompressed content to file based on the file name extension.
	 */
	@SuppressWarnings("unused")
	public VcfWriter create(File file, VcfMeta vcfMeta) throws FileNotFoundException
	{
		return create(file, vcfMeta, getFormat(file));
	}

	@SuppressWarnings("WeakerAccess")
	public VcfWriter create(File file, VcfMeta vcfMeta, Format format) throws FileNotFoundException
	{
		return create(file, vcfMeta, format, Version.V4_2);
	}

	@SuppressWarnings("WeakerAccess")
	public VcfWriter create(File file, VcfMeta vcfMeta, Format format, Version version) throws FileNotFoundException
	{
		OutputStream outputStream;
		switch (format)
		{
			case GZIP:
				outputStream = new BlockCompressedOutputStream(file);
				break;
			case UNCOMPRESSED:
				outputStream = new FileOutputStream(file);
				break;
			default:
				throw new IllegalArgumentException(format("Unknown format '%s'", format));
		}
		return create(outputStream, vcfMeta, version);
	}

	@SuppressWarnings("unused")
	public VcfWriter create(OutputStream outputStream, VcfMeta vcfMeta)
	{
		return create(outputStream, vcfMeta, Format.UNCOMPRESSED);
	}

	@SuppressWarnings("WeakerAccess")
	public VcfWriter create(OutputStream outputStream, VcfMeta vcfMeta, Format format)
	{
		return create(outputStream, vcfMeta, format, Version.V4_2);
	}

	@SuppressWarnings("WeakerAccess")
	public VcfWriter create(OutputStream outputStream, VcfMeta vcfMeta, Format format, Version version)
	{
		switch (format)
		{
			case GZIP:
				outputStream = new BlockCompressedOutputStream(outputStream, null);
				break;
			case UNCOMPRESSED:
				break;
			default:
				throw new IllegalArgumentException(format("Unknown format '%s'", format));
		}
		return create(outputStream, vcfMeta, version);
	}

	private VcfWriter create(OutputStream outputStream, VcfMeta vcfMeta, Version version)
	{
		VcfWriter vcfWriter;
		switch (version)
		{
			case V4_1:
			case V4_3:
				throw new IllegalArgumentException(format("Unsupported version '%s'", version));
			case V4_2:
				vcfWriter = new Vcf42Writer(outputStream, vcfMeta);
				break;
			default:
				throw new IllegalArgumentException(format("Unknown version '%s'", version));
		}
		return vcfWriter;
	}

	private Format getFormat(File file)
	{
		if (file.getName().toLowerCase().endsWith(".gz"))
		{
			return GZIP;
		}
		else
		{
			return UNCOMPRESSED;
		}
	}
}
