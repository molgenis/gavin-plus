package org.molgenis.data.annotation.makervcf;

import org.apache.commons.io.FileUtils;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CmdlineInterfaceTest {

    protected File inputVcfFile;
    protected File expectedOutputVcfFile;

    protected File cgdFile;
    protected File clinvarFile;
    protected File fdrFile;
    protected File gavinFile;
    protected File caddFile;

    @BeforeClass
    public void beforeClass() throws IOException {
        InputStream inputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/GAVIN-Plus_TinyDemo_1000G_Spiked.vcf");
        inputVcfFile = new File(FileUtils.getTempDirectory(), "GAVIN-Plus_TinyDemo_1000G_Spiked.vcf");
        FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

        InputStream expectedOutputVcf = DiscoverRelevantVariantsTest.class.getResourceAsStream("/GAVIN-Plus_TinyDemo_1000G_Spiked.RVCF.vcf");
        expectedOutputVcfFile = new File(FileUtils.getTempDirectory(), "GAVIN-Plus_TinyDemo_1000G_Spiked.vcf");
        FileCopyUtils.copy(expectedOutputVcf, new FileOutputStream(expectedOutputVcfFile));

        InputStream cgd = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r1.0/CGD_11oct2016.txt.gz");
        cgdFile = new File(FileUtils.getTempDirectory(), "CGD_11oct2016.txt.gz");
        FileCopyUtils.copy(cgd, new FileOutputStream(cgdFile));

        InputStream clinvar = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r1.0/clinvar.patho.fix.11oct2016.vcf.gz");
        clinvarFile = new File(FileUtils.getTempDirectory(), "clinvar.patho.fix.11oct2016.vcf.gz");
        FileCopyUtils.copy(clinvar, new FileOutputStream(clinvarFile));

        InputStream fdr = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r1.0/FDR_allGenes_r1.0.tsv");
        fdrFile = new File(FileUtils.getTempDirectory(), "FDR_allGenes_r1.,.tsv");
        FileCopyUtils.copy(fdr, new FileOutputStream(fdrFile));

        InputStream gavin = DiscoverRelevantVariantsTest.class.getResourceAsStream("/bundle_r1.0/GAVIN_calibrations_r0.3.tsv");
        gavinFile = new File(FileUtils.getTempDirectory(), "GAVIN_calibrations_r0.3.tsv");
        FileCopyUtils.copy(gavin, new FileOutputStream(gavinFile));

        InputStream cadd = DiscoverRelevantVariantsTest.class.getResourceAsStream("/GAVIN-Plus_TinyDemo_1000G_Spiked.fromCadd.tsv");
        caddFile = new File(FileUtils.getTempDirectory(), "GAVIN-Plus_TinyDemo_1000G_Spiked.fromCadd.tsv");
        FileCopyUtils.copy(cadd, new FileOutputStream(caddFile));
    }


    @Test
    public void test() throws Exception
    {
        // location of observed output to be written by gavin+
        File observedOutputVcfFile = new File(FileUtils.getTempDirectory(), "outputVcfFile.vcf");

//        // make sure there is no output file when we start
        if(observedOutputVcfFile.exists())
        {
            observedOutputVcfFile.delete();
        }
        assertTrue(!observedOutputVcfFile.exists());

        // run GAVIN+ using String[] args to simulate cmdline usage
        String[] args = {"-i" + inputVcfFile.getAbsolutePath(), "-o " + observedOutputVcfFile, "-g" + gavinFile.getAbsolutePath(), "-c" + clinvarFile.getAbsolutePath(), "-d" + cgdFile.getAbsolutePath(), "-f" + fdrFile.getAbsolutePath(), "-a" + caddFile.getAbsolutePath(), "-m" + "ANALYSIS"};
        Main.main(args);

        // compare results
        System.out.println("Going to compare files:\n" + expectedOutputVcfFile.getAbsolutePath() + "\nvs.\n" + gavinFile.getAbsolutePath());
        assertEquals(FileUtils.readLines(observedOutputVcfFile), FileUtils.readLines(expectedOutputVcfFile));
    }
}
