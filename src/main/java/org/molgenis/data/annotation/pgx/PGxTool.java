package org.molgenis.data.annotation.pgx;

import org.molgenis.data.Entity;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.annotator.tabix.TabixRepository;
import org.molgenis.data.annotator.tabix.TabixVcfRepository;
import org.molgenis.data.vcf.VcfRepository;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;


/**
 * Created by joeri on 10/13/16.
 */
public class PGxTool
{

    final static String DRUG = "DRUG";

    public void start(File inputVcfFile, File pgxVcfFile, File outputVCFFile) throws Exception
    {
        HashMap<String, String> pgxVariants = new HashMap<>();

        // put PGx VCF file in memory
        VcfRepository vcf = new VcfRepository(pgxVcfFile, "vcf");
        Iterator<Entity> vcfIterator = vcf.iterator();
        while(vcfIterator.hasNext())
        {
            VcfEntity record = new VcfEntity(vcfIterator.next());
            if(record.getRef().length() != 1 || record.getAltsAsString().length() != 1)
            {
//                throw new Exception("You cannot input multiple alleles for a PGx variant, offending: " + record.toString());
            }
            if(record.getOrignalEntity().get(DRUG) == null || record.getOrignalEntity().get(DRUG).toString().length() == 0)
            {
                throw new Exception("No drug information for variant, offending: " + record.toString());
            }

            pgxVariants.put(record.getChr() + "_" + record.getPos() + "_" + record.getRef() + "_" + record.getAltsAsString(), record.getOrignalEntity().get(DRUG).toString());
        }

        System.out.println("pgxVariants = " + pgxVariants);


        TabixVcfRepository inputTabixRepo = new TabixVcfRepository(inputVcfFile, "inputVcfFile");

        for(String pgxVariant : pgxVariants.keySet())
        {
            String[] pgxVariantSplit  = pgxVariant.split("_", -1);
            String chrom = pgxVariantSplit[0];
            Long pos = Long.parseLong(pgxVariantSplit[1]);

        //    System.out.println(chrom + ":" + pos + "-"+ pos);

            List<Entity> queryInput = inputTabixRepo.query(chrom, pos, pos);

            for(Entity e: queryInput)
            {


                VcfEntity record = new VcfEntity(e);
                System.out.println(record.toString());

            }



        }



    }
}
