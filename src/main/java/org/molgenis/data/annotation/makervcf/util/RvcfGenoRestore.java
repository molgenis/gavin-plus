package org.molgenis.data.annotation.makervcf.util;

import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.makervcf.structs.AnnotatedVcfRecord;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.meta.VcfMeta;

import java.io.File;
import java.util.*;

/**
 * Created by joeri on 9/2/16.
 */
public class RvcfGenoRestore {

    public RvcfGenoRestore(File inputRVCFFile, File outputRVCFFile, boolean verbose) throws Exception {

        VcfReader rvcfInput = GavinUtils.getVcfReader(inputRVCFFile);

        System.out.println("Getting unique sample identifiers..");
        Set<String> sampleIds = new HashSet<String>();
        Iterator<VcfRecord> it = rvcfInput.iterator();
        while (it.hasNext()) {
            AnnotatedVcfRecord record = new AnnotatedVcfRecord(it.next());
            //FIXME: enable or remove: Map<String, String> sampleGenotypes = record.getRvcf().getSampleGenotype();
            //FIXME: enable or remove: sampleIds.addAll(sampleGenotypes.keySet());
        }
        System.out.println(sampleIds);

        Iterator<VcfRecord> newIt = rvcfInput.iterator();

        System.out.println("Reconstructing genotype columns..");
        Iterator<AnnotatedVcfRecord> rve = addGenotypes(newIt,rvcfInput.getVcfMeta(), sampleIds);

        //write Entities output VCF file
/* FIXME: write VCF
   AttributeMetaData rlv = new DefaultAttributeMetaData(RVCF.attributeName).setDescription(RVCF.attributeMetaData);
        List<AttributeMetaData> attributes = Lists.newArrayList(rvcfInput.getEntityMetaData().getAttributes());
        AttributeMetaData format = new DefaultAttributeMetaData(VcfRepository.FORMAT_GT);
        AttributeMetaData samples = new DefaultAttributeMetaData(VcfRepository.SAMPLES);
        attributes.add(rlv);
        attributes.add(format);
        attributes.add(samples);
        new WriteToRVCF().writeRVCF(rve, outputRVCFFile, inputRVCFFile, attributes, true, verbose);*/


    }

    public Iterator<AnnotatedVcfRecord> addGenotypes(Iterator<VcfRecord> it, VcfMeta vcfMeta, Set<String> sampleIds)
    {

        return new Iterator<AnnotatedVcfRecord>() {


            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public AnnotatedVcfRecord next() {

                AnnotatedVcfRecord rv = null;
                try
                {
                    rv = new AnnotatedVcfRecord(it.next());
                }
                catch (Exception e)
                {
                    //FIXME: what to do?
                }

                Map<String, String> sampleGenotypes = null;
                try {
                    //sampleGenotypes = new VcfEntity(rv).getRvcf().getSampleGenotype();
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

                //FIXME rv.setFormat(new String[]{"GT"});
                throw new RuntimeException("TODO");
                //return rv;

            }
        };
    }

}
