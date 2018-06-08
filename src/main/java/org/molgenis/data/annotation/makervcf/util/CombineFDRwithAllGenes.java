package org.molgenis.data.annotation.makervcf.util;

import org.molgenis.data.annotation.reportrvcf.FDR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by joeri on 7/19/16.
 */
public class CombineFDRwithAllGenes {
    private static final Logger LOG = LoggerFactory.getLogger(CombineFDRwithAllGenes.class);

    private final String allGenesFilename;
    private final String fdrR0_1Filename;
    private final String fdrAllGenesFilename;

    public CombineFDRwithAllGenes(String allGenesFilename, String fdrR0_1Filename, String fdrAllGenesFilename) throws FileNotFoundException {
        this.allGenesFilename = allGenesFilename;
        this.fdrR0_1Filename = fdrR0_1Filename;
        this.fdrAllGenesFilename = fdrAllGenesFilename;

        Set<String> allGenes = new HashSet<>();

        //as produced by GetAllGeneNamesFromVCF
        //columns: no header, 1 gene per line
        File allGenesFile = new File(this.allGenesFilename);

        //as produced by FDR, as ran on 1000G
        //using: https://molgenis26.gcc.rug.nl/downloads/5gpm/gavin/1000G_FDR/
        //columns: header "Gene Affected  Carrier"
        File FDRfile = new File(this.fdrR0_1Filename);

        //output file: combine FDR results with all genes
        //for some genes, we have found no false results, so their FDR will be 0
        File outputFDR = new File(this.fdrAllGenesFilename);

        //read all genes
        Scanner allGenesScanner = new Scanner(allGenesFile);
        while(allGenesScanner.hasNextLine())
        {
            allGenes.add(allGenesScanner.nextLine());
        }
        allGenesScanner.close();

        //read FDR genes
        Scanner fdrScanner = new Scanner(FDRfile);
        fdrScanner.nextLine(); //skip header
        Map<String, String> geneToLine = new HashMap<>();
        while(fdrScanner.hasNextLine())
        {
            String line = fdrScanner.nextLine();
            String[] split = line.split("\t", -1);
            geneToLine.put(split[0], line);
        }
        fdrScanner.close();

        LOG.info("read FDR data for " + geneToLine.size() + " genes");

        PrintWriter pw = new PrintWriter(outputFDR);

        pw.println(FDR.HEADER);
        for(String gene : allGenes)
        {
            if(geneToLine.containsKey(gene))
            {
                pw.println(geneToLine.get(gene));
                geneToLine.remove(gene);
            }
            else
            {
                pw.println(gene + "\t" + "0" + "\t" + "0" + "\t" + "0.0" + "\t" + "0.0");
            }
        }

        if(geneToLine.size() > 0)
        {
            LOG.info("weird - check all genes but still some left...");
            for(String key : geneToLine.keySet())
            {
                pw.println(geneToLine.get(key));
                LOG.info("leftover gene: " + key);

            }
        }

        pw.flush();
        pw.close();


    }

    public static void main(String[] args) throws Exception {
        String allGenesFilename = args[0];
        String fdrR0_1Filename = args[1];
        String fdrAllGenesFilename = args[2];
        new CombineFDRwithAllGenes(allGenesFilename,fdrR0_1Filename,fdrAllGenesFilename);
    }
}
