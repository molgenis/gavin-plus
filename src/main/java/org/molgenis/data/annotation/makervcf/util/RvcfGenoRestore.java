package org.molgenis.data.annotation.makervcf.util;

import com.google.common.collect.Lists;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.makervcf.WriteToRVCF;
import org.molgenis.data.annotation.makervcf.structs.RVCF;
import org.molgenis.data.annotation.makervcf.structs.RelevantVariant;
import org.molgenis.data.annotation.makervcf.structs.VcfEntity;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfWriterUtils;

import java.io.File;
import java.util.*;

/**
 * Created by joeri on 9/2/16.
 */
public class RvcfGenoRestore {

    public RvcfGenoRestore(File inputRVCFFile, File outputRVCFFile, boolean verbose) throws Exception {

        VcfRepository rvcfInput = new VcfRepository(inputRVCFFile, "rvcf");

        System.out.println("Getting unique sample identifiers..");
        Set<String> sampleIds = new HashSet<String>();
        Iterator<Entity> it = rvcfInput.iterator();
        while (it.hasNext()) {
            VcfEntity record = new VcfEntity(it.next());
            Map<String, String> sampleGenotypes = record.getRvcf().getSampleGenotype();
            sampleIds.addAll(sampleGenotypes.keySet());
        }
        System.out.println(sampleIds);

        Iterator<Entity> newIt = rvcfInput.iterator();

        System.out.println("Reconstructing genotype columns..");
        Iterator<Entity> rve = addGenotypes(newIt, sampleIds);

        //write Entities output VCF file
        AttributeMetaData rlv = new DefaultAttributeMetaData(RVCF.attributeName).setDescription(RVCF.attributeMetaData);
        List<AttributeMetaData> attributes = Lists.newArrayList(rvcfInput.getEntityMetaData().getAttributes());
        AttributeMetaData format = new DefaultAttributeMetaData(VcfRepository.FORMAT_GT);
        AttributeMetaData samples = new DefaultAttributeMetaData(VcfRepository.SAMPLES);
        attributes.add(rlv);
        attributes.add(format);
        attributes.add(samples);
        new WriteToRVCF().writeRVCF(rve, outputRVCFFile, inputRVCFFile, attributes, true, verbose);


    }

    public Iterator<Entity> addGenotypes(Iterator<Entity> it, Set<String> sampleIds)
    {

        return new Iterator<Entity>() {


            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Entity next() {

                Entity rv = it.next();

                Map<String, String> sampleGenotypes = null;
                try {
                    sampleGenotypes = new VcfEntity(rv).getRvcf().getSampleGenotype();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                for (String sampleId : sampleIds) {
                    if (sampleGenotypes.keySet().contains(sampleId)) {
                        System.out.print("\t" + sampleGenotypes.get(sampleId));
                    } else {
                        System.out.print("\t" + "0/0");
                    }
                }

                System.out.print("\n");

                rv.set(VcfRepository.FORMAT_GT, "GT");

                return rv;

            }
        };
    }

}
