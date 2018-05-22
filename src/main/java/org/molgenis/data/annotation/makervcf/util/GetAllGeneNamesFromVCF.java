package org.molgenis.data.annotation.makervcf.util;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by joeri on 7/14/16.
 */
public class GetAllGeneNamesFromVCF
{
	public static void main(String[] args) throws Exception
	{
		File vcfFile = new File(
				"/Users/joeri/Desktop/1000G_diag_FDR/exomePlus/ALL.chr1to22plusXYMT.phase3_20130502.variantsOnly.snpEffNoIntergenic.exac.gonl.cadd.vcf.gz");
		try (PrintWriter pw = new PrintWriter(new File("/Users/joeri/Desktop/1000G_diag_FDR/exomePlus/allGenes.txt")))
		{
			Iterator<VcfRecord> vcfIterator;
			try (VcfReader vcf = GavinUtils.getVcfReader(vcfFile))
			{
				vcfIterator = vcf.iterator();

				Set<String> genes = new HashSet<>();
				int i = 0;
				while (vcfIterator.hasNext())
				{
					GavinRecord record = new GavinRecord(vcfIterator.next());
					i++;
					Set<String> genesForVariant = record.getGenes();
					if (genesForVariant != null)
					{
						genes.addAll(record.getGenes());
					}

					if (i % 10000 == 0)
					{
						System.out.println("Seen " + i + " variants, found " + genes.size() + " unique genes so far..");
					}
				}
				System.out.println("Writing results..");
				for (String gene : genes)
				{
					pw.println(gene);
				}
				pw.flush();
				pw.close();
			}
			System.out.println("Done");
		}
	}
}
