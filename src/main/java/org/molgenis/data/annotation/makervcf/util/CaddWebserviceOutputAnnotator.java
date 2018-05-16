package org.molgenis.data.annotation.makervcf.util;

import com.google.common.collect.Lists;
import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;

import java.io.*;
import java.util.Iterator;
import java.util.List;

/** 
 * Annotator that adds the output of the CADD webservice to a VCF file.
 * TODO: rewrite into a proper annotator!
 *  
 * Many indels in your VCF are not present in the static CADD files for SNV, 1000G, etc.
 * The webservice can calculate CADD scores for these variants to get more complete data.
 * This is what you get back when you use the webservice of CADD:
 * 
 * ## CADD v1.3 (c) University of Washington and Hudson-Alpha Institute for Biotechnology 2013-2015. All rights reserved.
 * #CHROM	POS	REF	ALT	RawScore	PHRED
 * 1	3102852	G	A	0.458176	7.103
 * 1	3102888	G	T	-0.088829	1.815
 * 1	3103004	G	A	1.598097	13.84
 * 1	3319479	G	A	0.717654	8.942
 * 
 * This information can be added to a VCF based on chrom, pos, ref, alt.
 *
 */
public class CaddWebserviceOutputAnnotator
{

	File vcfToAnnotate;
	File caddWebserviceOutput;
	HandleMissingCaddScores hmcs;
	BufferedWriter outputVCFWriter;

	public CaddWebserviceOutputAnnotator(File vcfToAnnotate, File caddWebserviceOutput, File outputFile) throws Exception
	{
		if (!vcfToAnnotate.isFile())
		{
			throw new FileNotFoundException("VCF file " + vcfToAnnotate.getAbsolutePath()
					+ " does not exist or is directory");
		}
		if (!caddWebserviceOutput.isFile())
		{
			throw new FileNotFoundException("CADD webservice output file " + caddWebserviceOutput.getAbsolutePath()
					+ " does not exist or is directory");
		}
		if (outputFile.isFile())
		{
			System.out.println("Warning: output file " + outputFile.getAbsolutePath()
					+ " already exists, overwriting content!");
		}
		this.vcfToAnnotate = vcfToAnnotate;
		this.caddWebserviceOutput = caddWebserviceOutput;
		this.hmcs = new HandleMissingCaddScores(HandleMissingCaddScores.Mode.ANALYSIS, caddWebserviceOutput);
		this.outputVCFWriter = new BufferedWriter(new FileWriter(outputFile));
	}
	
	public void annotate() throws Exception
	{
		VcfReader vcfReader = GavinUtils.getVcfReader(vcfToAnnotate);
		//TODO: VcfWriterUtils.writeVcfHeader(vcfToAnnotate, outputVCFWriter, attributes);
		Iterator<VcfRecord> vcfIterator = vcfReader.iterator();

		while(vcfIterator.hasNext())
		{
			VcfEntity vcfEntity = new VcfEntity(vcfIterator.next(), vcfReader.getVcfMeta());
			StringBuffer cadd_scaled = new StringBuffer();
			for(int altIndex = 0; altIndex < vcfEntity.getAlts().length; altIndex++)
			{
				if(vcfEntity.getCaddPhredScores(altIndex) == null){
					Double cadd = hmcs.dealWithCaddScores(vcfEntity, altIndex);
					vcfEntity.setCaddPhredScore(altIndex, cadd);
					System.out.println("setting missing CADD score " + cadd);
				}
			}

			//TODO: need for cadd_scaled? this is a concatenation of the phredScores
			//TODO: VcfWriterUtils.writeToVcf(record.getOrignalEntity(), outputVCFWriter);
			//TODO: outputVCFWriter.newLine();
		}

		//TODO: outputVCFWriter.flush();
		//TODO: outputVCFWriter.close();

		System.out.println("Done!");
	}

	public static void main(String[] args) throws Exception
	{
		File vcfToAnnotate = new File(args[0]);
		File caddWebserviceOutput = new File(args[1]);
		File outputFile = new File(args[2]);
		CaddWebserviceOutputAnnotator cwoa = new CaddWebserviceOutputAnnotator(vcfToAnnotate, caddWebserviceOutput, outputFile);
		cwoa.annotate();
	}

}
