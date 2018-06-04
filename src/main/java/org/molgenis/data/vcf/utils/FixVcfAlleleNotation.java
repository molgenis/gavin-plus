package org.molgenis.data.vcf.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;
import org.apache.commons.lang.StringUtils;

public class FixVcfAlleleNotation
{
	/**
	 * Fix N notation in VCFs produced by Ensembl VEP web service.
	 * 
	 * Example:
	 * 11	47354445	MYBPC3:c.3407_3409delACT	NAGT	N
	 * 
	 * Get location for UCSC:
	 * http://genome.ucsc.edu/cgi-bin/das/hg19/dna?segment=chr11:47354445,47354445
	 * Returns:
	 * "<DNA length="1">a</DNA>"
	 * 
	 * We need:
	 * "a"
	 * 
	 * And use it to turn the VCF record into:
	 * 11	47354445	MYBPC3:c.3407_3409delACT	AAGT	A
	 * 
	 * Both deletions and insertions suffer from this, e.g.:
	 * 6	51612724	PKHD1:c.9689delA	NT	N
	 * 6	51824680	PKHD1:c.5895dupA	N	NT
	 * 
	 * Caveat:
	 * Some original HGVS/cDNA notation inplies an insertion/deletion event involving the same bases, for example "c.652delAinsTA"
	 * This gets turned into "NA/NTA" and subsequently fixed into "TA/TTA". Note that the "A" is unnecessary here!
	 * This is not nice and some tools get confused. For example, CADD webservice doesn't understand until you change it into "T/TT".
	 * So we want either ref or alt to be 1 basepair, which is the right way to express variants in VCF format.
	 * This is why we do a check and fix it here.
	 * 
	 * 
	 * 
	 */
	public static void main(String[] args) throws Exception
	{
		File in = new File(args[0]);
		File out = new File(args[1]);

		Scanner s;
		try (PrintWriter pw = new PrintWriter(out))
		{
			s = new Scanner(in);
			String line;
			while (s.hasNextLine())
			{
				//write out header untouched
				line = s.nextLine();
				if (line.startsWith("#"))
				{
					pw.println(line);
					continue;
				}
				String[] split = line.split("\t");

				String chr = split[0];
				String pos = split[1];
				String ref = split[3];
				String alt = split[4];

				if (alt.contains(","))
				{
					throw new Exception("multiple alt alleles not supported, stopping at line: " + line);
				}

				//first check if ref or alt need trimming. it does not matter that there may be an N.
				String[] trimmedRefAlt = backTrimRefAlt(ref, alt, "_").split("_");
				if (!ref.equals(trimmedRefAlt[0]) || !alt.equals(trimmedRefAlt[1]))
				{
					System.out.println(
							"back-trimming ref/alt, from " + ref + "/" + alt + " to " + trimmedRefAlt[0] + "/" + trimmedRefAlt[1]);
					ref = trimmedRefAlt[0];
					alt = trimmedRefAlt[1];
				}

				//front trim, trickier
				String[] frontTrimmedRefAlt = frontTrimRefAlt(ref, alt, "_").split("_");
				int posDiff = ref.length() - frontTrimmedRefAlt[0].length();
				if (!ref.equals(frontTrimmedRefAlt[0]) || !alt.equals(frontTrimmedRefAlt[1]))
				{
					System.out.println(
							"front-trimming ref/alt, from " + ref + "/" + alt + " to " + frontTrimmedRefAlt[0] + "/" + frontTrimmedRefAlt[1] + ", position update needed!");
					ref = frontTrimmedRefAlt[0];
					alt = frontTrimmedRefAlt[1];
					pos = Integer.parseInt(pos) + posDiff + ""; //shift position if ref base has 'moved'
				}

				boolean queryUCSC = false;
				//if not both start with N, we expect neither to start with N (see example)
				if (!(ref.startsWith("N") && alt.startsWith("N")))
				{
					// could mean we DID fix the notation, so dont quit yet!
					//				System.out.println("no reason to adjust variant " + chr + ":"+pos+" " + ref + "/" + alt + " because there is no N");
					//				pw.println(line);
					//				continue;
				}
				else if (ref.startsWith("N") && alt.startsWith("N"))
				{
					System.out.println("need to adjust variant " + chr + ":pos " + ref + "/" + alt + " because there is an N");
					int refNOccurence = org.springframework.util.StringUtils.countOccurrencesOf(ref, "N");
					int altNOccurence = org.springframework.util.StringUtils.countOccurrencesOf(alt, "N");
					if (refNOccurence != 1 || altNOccurence != 1)
					{
						s.close();
						pw.close();
						throw new Exception("expecting 'N' occurence == 1 for " + ref + " and " + alt);
					}
					queryUCSC = true;
				}
				//sanity check
				else
				{
					s.close();
					pw.close();
					throw new Exception("either ref " + ref + " or alt " + alt + " starts with N, not expected this");
				}

				String replacementRefBase = "if you see this, we did not get a replacement base while we needed one!";
				if (queryUCSC)
				{
					//get replacement base for N from UCSC
					URL ucsc = new URL(
							"http://genome.ucsc.edu/cgi-bin/das/hg19/dna?segment=chr" + chr + ":" + pos + "," + pos);
					BufferedReader getUrlContent = new BufferedReader(new InputStreamReader(ucsc.openStream()));
					String urlLine;

					while ((urlLine = getUrlContent.readLine()) != null)
					{
						//the base ('g', 'c', 'a', 't') is on line of its own, so length == 1
						if (urlLine.length() == 1)
						{
							replacementRefBase = urlLine.toUpperCase();
							System.out.println("we found replacement base for N = " + replacementRefBase);
						}
					}
					getUrlContent.close();

					//wait a little bit
					Thread.sleep(100);
				}

				//print the fixed notation
				StringBuffer fixedLine = new StringBuffer();
				for (int i = 0; i < split.length; i++)
				{
					if (i == 1)
					{
						//update the pos if trimmed
						fixedLine.append(pos + "\t");
					}
					else if (i == 3 || i == 4)
					{
						String fixedNotation =
								i == 3 ? ref.replace("N", replacementRefBase) : alt.replace("N", replacementRefBase);
						fixedLine.append(fixedNotation + "\t");
					}
					else
					{
						fixedLine.append(split[i] + "\t");
					}
				}

				//remove trailing \t
				fixedLine.deleteCharAt(fixedLine.length() - 1);

				//print & flush
				pw.println(fixedLine);
				pw.flush();

			}
			pw.flush();
			pw.close();
		}
		
		s.close();
		
		System.out.println("Done!");

	}



	/**
	 * AT ATT -> A AT
	 * ATGTG ATG -> ATG A
	 * ATGTG ATGTGTGTG -> A ATGTG
	 * GATAT GAT -> GAT G
	 *
	 * Examples:
	 * GATA GATAGATA -> G GATAG
	 * TTCTT T -> TTCTT T (don't touch)
	 */
	public static String backTrimRefAlt(String ref, String alt, String sep)
	{
		// GATA -> ATAG
		char[] refRev = StringUtils.reverse(ref).toCharArray();
		// GATAGATA -> ATAGATAG
		char[] altRev = StringUtils.reverse(alt).toCharArray();

		int nrToDelete = 0;
		//iterate over: A, T, A. Do not touch the last reference base (G).
		for(int i = 0; i < refRev.length-1; i++)
		{
			char refBase = refRev[i];
			char altBase = altRev[i];

			//altRev.length > i+1 prevents the last matching alt base from being/attempted deleted, e.g. TTCTT_T -> TTCT_
			//this may happen because we iterate over reference, which may be longer in case of a deletion
			if(refBase == altBase && altRev.length > i+1)
			{
				nrToDelete++;
			}
			else
			{
				break;
			}
		}
		String newRef = ref.substring(0, ref.length()-nrToDelete);
		String newAlt = alt.substring(0, alt.length()-nrToDelete);

		//result: GATA GATAGATA -> G GATAG
		return newRef + sep + newAlt;
	}

	/**
	 * cut off from the front, needs pos update afterwards
	 * e.g.
	 * 2	27693815	587777083	CT	CTCT
	 * to
	 * 2	27693816	587777083	T	TCT
	 *
	 * and
	 * GAGAGT/GAGC to GAGT/GC
	 * and
	 * AAACGCTCATAGAGTAACTGGTTGTGCAGTAAAAGCAACTGGTCTC/AAACGCTCATAGAGTAACTGGTTGTGCAGTAAAAGCAACTGGTCTCAAACGCTCAT to C/CAAACGCTCAT
	 * and
	 * CTTTTAAATTTGATTTTATTGAGCACTTTCCTCT/CTTTTAAATTTGATTTTATTGAGCACTTTCCTCTCTTTTAAATTTGATTTTATTGA to T/TCTTTTAAATTTGATTTTATTGA
	 *
	 *
	 * however cannot shorten
	 * 7	150644438	794728471	CATCCAGCCTGCTCTCCAC	CTG
	 *
	 * @param ref
	 * @param alt
	 * @param sep
     * @return
     */
	public static String frontTrimRefAlt(String ref, String alt, String sep)
	{
		char[] refChars = ref.toCharArray();
		char[] altChars = alt.toCharArray();

		int nrToDelete = 0;

		for(int i = 0; i < refChars.length-1; i++)
		{
			char refBase = refChars[i];
			char altBase = altChars[i];

			//altChars.length > i+1 prevents the last matching alt base from being/attempted deleted
			//refChars[i+1] == altChars[i+1] checks if we still have at lease 1 matching ref base left in alt notation
			if(refBase == altBase && altChars.length > i+1 && refChars[i+1] == altChars[i+1] )
			{
				nrToDelete++;
			}
			else
			{
				break;
			}
		}
		String newRef = ref.substring(nrToDelete, ref.length());
		String newAlt = alt.substring(nrToDelete, alt.length());

		if(nrToDelete > 0) {
			System.out.println("input ref: " + ref + ", alt: " + alt + ", nr to delete: " + nrToDelete + " resulting in:" + newRef + " / " + newAlt);
		}

		//result: GATA GATAGATA -> G GATAG
		return newRef + sep + newAlt;
	}

}