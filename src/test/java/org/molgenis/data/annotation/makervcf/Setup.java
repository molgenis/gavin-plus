package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;

import java.io.*;

/**
 * Created by joeri on 8/26/16.
 */
public class Setup {

    protected File gavinFile;
    protected File repPathoFile;
    protected File caddFile;

    @BeforeClass
    public void prepResources() throws FileNotFoundException, IOException
    {

        System.out.println("Java tmp dir: " + FileUtils.getTempDirectory().getAbsolutePath());

        InputStream gavin = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r1.0/GAVIN_calibrations_r0.3.tsv");
        gavinFile = new File(FileUtils.getTempDirectory(), "GAVIN_calibrations_r0.3.tsv");
        FileCopyUtils.copy(gavin, new FileOutputStream(gavinFile));

        InputStream clinvar = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r0.1/clinvar.patho.fix.5.5.16.vcf.gz");
        repPathoFile = new File(FileUtils.getTempDirectory(), "clinvar.patho.fix.5.5.16.vcf.gz");
        FileCopyUtils.copy(clinvar, new FileOutputStream(repPathoFile));

        InputStream cadd = DiscoverRelevantVariantsTest.class.getResourceAsStream("/fromCaddDummy.tsv");
        caddFile = new File(FileUtils.getTempDirectory(), "fromCaddDummy.tsv");
        FileCopyUtils.copy(cadd, new FileOutputStream(caddFile));
    }

}
