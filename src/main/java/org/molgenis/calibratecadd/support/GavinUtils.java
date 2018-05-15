package org.molgenis.calibratecadd.support;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sf.samtools.util.BlockCompressedInputStream;
import org.molgenis.data.annotation.core.entity.impl.snpeff.Impact;
import org.molgenis.data.annotation.entity.impl.gavin.GavinEntry;
import org.molgenis.data.vcf.datastructures.Sample;
import org.molgenis.data.vcf.datastructures.Trio;
import org.molgenis.genotype.Allele;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.codec.Charsets.UTF_8;

public class GavinUtils
{
	private static final String HEADER_PREFIX = "##";
	private static final String PEDIGREE = "##PEDIGREE";
	private final HashMap<String, GavinEntry> geneToEntry = new HashMap<>();

	public GavinUtils(File gavin) throws Exception
	{
		try (Scanner s = new Scanner(gavin))
		{

			//skip header
			s.nextLine();

			String line;
			while (s.hasNextLine())
			{
				line = s.nextLine();

				GavinEntry e = new GavinEntry(line);
				geneToEntry.put(e.gene, e);
			}
		}

	}

	public HashMap<String, GavinEntry> getGeneToEntry()
	{
		return geneToEntry;
	}

	public GavinEntry.Category getCategory(String gene)
	{
		return geneToEntry.get(gene).category;
	}

	public boolean contains(String gene)
	{
		return geneToEntry.containsKey(gene);
	}

	public static Set<String> getGenesFromAnn(String ann)
	{
		if (ann == null)
		{
			return new HashSet<>();
		}
		Set<String> genes = new HashSet<>();
		String[] annSplit = ann.split(",", -1);
		for (String oneAnn : annSplit)
		{
			String[] fields = oneAnn.split("\\|", -1);
			String gene = fields[3];
			genes.add(gene);
		}

		return genes;
	}

	public static Double getInfoForAllele(VcfRecord record, String infoField, String altAllele) throws Exception
	{
		String info_STR = getInfoStringValue(record, infoField);
		if (info_STR == null)
		{
			return null;
		}
		List<Allele> alts = record.getAlternateAlleles();
		String[] info_split = info_STR.split(",", -1);

		if (alts.size() != info_split.length)
		{
			throw new Exception("length of alts not equal to length of info field for " + record);
		}

		int i = 0;
		for (Allele allele : alts)
		{
			if (allele.getAlleleAsString().equals(altAllele))
			{
				return (info_split[i] != null && !info_split[i].equals(".")) ? Double.parseDouble(info_split[i]) : null;
			}
			i++;
		}
		return null;
	}

	//FIXME: review if this can be done "better"
	public static String getInfoStringValue(VcfRecord record, String infoField)
	{
		String result = null;
		Iterable<VcfInfo> infoFields = record.getInformation();
		for (VcfInfo info : infoFields)
		{
			if (info.getKey().equals(infoField))
			{
				Object vcfInfoVal = info.getVal();
				if (vcfInfoVal instanceof List<?>)
				{
					List<?> vcfInfoValTokens = (List<?>) vcfInfoVal;
					result = vcfInfoValTokens.stream()
										  .map(vcfInfoValToken ->
												  vcfInfoValToken != null ? vcfInfoValToken.toString() : ".")
										  .collect(joining(","));
				}else
				{
					result = vcfInfoVal.toString();
				}
			}
		}
		return result;
	}

	public static String getEffect(String ann, String gene, String allele) throws Exception
	{
		//get the right annotation entry that matches both gene and allele
		String findAnn = getAnn(ann, gene, allele);
		if (findAnn == null)
		{
			System.out.println(
					"WARNING: failed to get effect for gene '" + gene + "', allele '" + allele + "' in " + ann);
			return null;
		}
		else
		{
			//from the right one, get the impact
			String[] fields = findAnn.split("\\|", -1);
			return fields[1];
		}
	}

	public static Impact getImpact(String ann, String gene, String allele) throws Exception
	{
		//get the right annotation entry that matches both gene and allele
		String findAnn = getAnn(ann, gene, allele);
		if (findAnn == null)
		{
			System.out.println(
					"WARNING: failed to get impact for gene '" + gene + "', allele '" + allele + "' in " + ann);
			return null;
		}
		else
		{
			//from the right one, get the impact
			String[] fields = findAnn.split("\\|", -1);
			String impact = fields[2];
			return Impact.valueOf(impact);
		}
	}

	public static String getTranscript(String ann, String gene, String allele) throws Exception
	{
		//get the right annotation entry that matches both gene and allele
		String findAnn = getAnn(ann, gene, allele);
		if (findAnn == null)
		{
			System.out.println(
					"WARNING: failed to get impact for gene '" + gene + "', allele '" + allele + "' in " + ann);
			return null;
		}
		else
		{
			//from the right one, get the impact
			String[] fields = findAnn.split("\\|", -1);
			String transcript = fields[6];
			return transcript;
		}
	}

	private static String getAnn(String ann, String gene, String allele)
	{
		String[] annSplit = ann.split(",", -1);
		for (String oneAnn : annSplit)
		{
			String[] fields = oneAnn.split("\\|", -1);
			String geneFromAnn = fields[3];
			if (!gene.equals(geneFromAnn))
			{
				continue;
			}
			String alleleFromAnn = fields[0];
			if (!allele.equals(alleleFromAnn))
			{
				continue;
			}
			return oneAnn;
		}
		System.out.println(
				"WARNING: annotation could not be found for " + gene + ", allele=" + allele + ", ann=" + ann);
		return null;
	}

	public static void main(String[] args) throws Exception
	{
		File gavin = new File(args[0]);
		new GavinUtils(gavin);

	}

	public static VcfReader getVcfReader(File file) throws IOException
	{
		VcfReader reader;

		InputStream inputStream = new FileInputStream(file);

		if (file.getName().endsWith(".gz"))
		{
			reader = new VcfReader(new InputStreamReader(new GZIPInputStream(inputStream), Charset.forName("UTF-8")));
		}
		else if (file.getName().endsWith(".zip"))
		{
			try (ZipFile zipFile = new ZipFile(file.getPath()))
			{
				Enumeration<? extends ZipEntry> e = zipFile.entries();
				ZipEntry entry = e.nextElement();
				reader = new VcfReader(new InputStreamReader(new GZIPInputStream(zipFile.getInputStream(entry)),
						Charset.forName("UTF-8")));
			}
		}
		else
		{
			reader = new VcfReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
		}

		return reader;
	}

	public static HashMap<String, Trio> getPedigree(Scanner inputVcfFileScanner)
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
				System.out.println("Pedigree data line: " + line);
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
				Sample child = childID != null ? new Sample(childID):null;
				Sample mother = motherID != null ? new Sample(motherID):null;
				Sample father = fatherID != null ? new Sample(fatherID):null;

				result.put(childID, new Trio(child, mother, father));
			}
		}
		return result;
	}

	//FIXME: was bufferedreader in Joeri's branch? try to find the code with this version...
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