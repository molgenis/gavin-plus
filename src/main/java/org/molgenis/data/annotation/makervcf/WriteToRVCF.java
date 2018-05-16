package org.molgenis.data.annotation.makervcf;

import com.google.common.collect.Lists;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by joeri on 7/18/16.
 */
public class WriteToRVCF {

/*    public void writeRVCF(Iterator<Entity> relevantVariants, File writeTo, File inputVcfFile, List<AttributeMetaData> attributes, boolean writeToDisk, boolean verbose) throws IOException
        FileWriter fw = new FileWriter(writeTo);
        BufferedWriter outputVCFWriter = new BufferedWriter(fw);
        if(verbose) { System.out.println("[WriteToRVCF] Writing header"); }
        VcfWriterUtils.writeVcfHeader(inputVcfFile, outputVCFWriter, attributes, Collections.emptyList(), true);

        while(relevantVariants.hasNext())
        {
            Entity e = relevantVariants.next();

            if(writeToDisk)
            {
                if(verbose) { System.out.println("[WriteToRVCF] Writing VCF record"); }
                VcfWriterUtils.writeToVcf(e, outputVCFWriter);
                outputVCFWriter.newLine();
            }
        }
        if(verbose) { System.out.println("[WriteToRVCF] Flushing and closing"); }
        outputVCFWriter.flush();
        outputVCFWriter.close();
    }*/
}
