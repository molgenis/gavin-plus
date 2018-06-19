package org.molgenis.calibratecadd.support;

import net.sf.samtools.util.BlockCompressedInputStream;
import org.molgenis.data.annotation.entity.impl.gavin.GavinEntry;
import org.molgenis.data.vcf.datastructures.Sample;
import org.molgenis.data.vcf.datastructures.Trio;
import org.molgenis.vcf.VcfReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.molgenis.data.annotation.entity.impl.gavin.GavinEntry.CADD_INDEX;
import static org.molgenis.data.annotation.entity.impl.gavin.GavinEntry.PATHO_MAF_INDEX;

public class GavinUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(GavinUtils.class);

	private static final String HEADER_PREFIX = "##";
	private static final String PEDIGREE = "##PEDIGREE";
	public static final String DEFAULT_KEY = "DEFAULT";

	private GavinUtils()
	{
	}

	public static Map<String, GavinEntry> getGeneToEntry(File gavin)
	{
		HashMap<String, GavinEntry> geneToEntry = new HashMap<>();
		try (Scanner s = new Scanner(gavin))
		{
			//skip header
			s.nextLine();

			String line;
			while (s.hasNextLine())
			{
				line = s.nextLine();
				if (line.startsWith(DEFAULT_KEY))
				{
					GavinEntry gavinEntry = new GavinEntry("",GavinEntry.Category.DEFAULT,"",-1L,-1L,getValueFromLine(line, PATHO_MAF_INDEX),getValueFromLine(line, CADD_INDEX));
					geneToEntry.put(DEFAULT_KEY, gavinEntry);
				}
				else
				{
					GavinEntry gavinEntry = new GavinEntry(line);
					geneToEntry.put(gavinEntry.getGene(), gavinEntry);
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return geneToEntry;
	}

	private static Double getValueFromLine(String line, int index)
	{
		String[] split = line.split("\t");
		return split[index].isEmpty() ? null : Double.parseDouble(split[index]);
	}

	public static VcfReader getVcfReader(File file) throws IOException
	{
		VcfReader reader;

		if (file.getName().endsWith(".gz"))
		{
			reader = new VcfReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), UTF_8));
		}
		else if (file.getName().endsWith(".zip"))
		{
			try (ZipFile zipFile = new ZipFile(file.getPath()))
			{
				Enumeration<? extends ZipEntry> e = zipFile.entries();
				ZipEntry entry = e.nextElement();
				reader = new VcfReader(
						new InputStreamReader(new GZIPInputStream(zipFile.getInputStream(entry)), UTF_8));
			}
		}
		else
		{
			reader = new VcfReader(new InputStreamReader(new FileInputStream(file), UTF_8));
		}

		return reader;
	}

	public static Map<String, Trio> getPedigree(Scanner inputVcfFileScanner)
	{
		HashMap<String, Trio> result = new HashMap<>();

		while (inputVcfFileScanner.hasNextLine())
		{
			String line = inputVcfFileScanner.nextLine();

			// quit when we don't see header lines anymore
			if (!line.startsWith(HEADER_PREFIX))
			{
				break;
			}

			// detect pedigree line
			// expecting e.g. ##PEDIGREE=<Child=100400,Mother=100402,Father=100401>
			if (line.startsWith(PEDIGREE))
			{
				parsePedigree(result, line);
			}
		}
		return result;
	}

	private static void parsePedigree(HashMap<String, Trio> result, String line)
	{
		LOG.info("Pedigree data line: {}", line);

		String childID = null;
		String motherID = null;
		String fatherID = null;

		String lineStripped = line.replace("##PEDIGREE=<", "").replace(">", "");
		String[] lineSplit = lineStripped.split(",", -1);
		for (String element : lineSplit)
		{
			if (element.startsWith("Child"))
			{
				childID = element.replace("Child=", "");
			}
			else if (element.startsWith("Mother"))
			{
				motherID = element.replace("Mother=", "");
			}
			else if (element.startsWith("Father"))
			{
				fatherID = element.replace("Father=", "");
			}
			else
			{
				throw new RuntimeException(
						"Expected Child, Mother or Father, but found: " + element + " in line " + line);
			}
		}
		Sample child = childID != null ? new Sample(childID, null, null) : null;
		Sample mother = motherID != null ? new Sample(motherID, null, null) : null;
		Sample father = fatherID != null ? new Sample(fatherID, null, null) : null;

		result.put(childID, new Trio(child, mother, father));
	}

	public static Scanner createVcfFileScanner(File vcfFile) throws IOException
	{
		InputStream inputStream = new FileInputStream(vcfFile);
		if (vcfFile.getName().endsWith(".gz"))
		{
			inputStream = new BlockCompressedInputStream(inputStream);
		}
		return new Scanner(inputStream, UTF_8.name());
	}
}