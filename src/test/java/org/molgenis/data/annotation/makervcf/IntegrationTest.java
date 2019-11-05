package org.molgenis.data.annotation.makervcf;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.commons.io.FileUtils;
import org.molgenis.data.annotation.makervcf.Main.RlvMode;
import org.molgenis.data.annotation.makervcf.positionalstream.DiscoverRelevantVariants;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;

public class IntegrationTest {
  protected File inputVcfFile;
  protected File expectedOutputVcfFile;
  private File gavinFile;


  @Test
  public void testKeepAll() throws Exception
  {
    InputStream inputVcf = IntegrationTest.class.getResourceAsStream(
        "/test_input.vcf");
    inputVcfFile = new File(FileUtils.getTempDirectory(), "test_input.vcf");
    FileCopyUtils.copy(inputVcf, new FileOutputStream(inputVcfFile));

    InputStream gavin = IntegrationTest.class.getResourceAsStream(
        "/bundle_r1.2/GAVIN_calibrations_r0.5.tsv");
    gavinFile = new File(FileUtils.getTempDirectory(), "GAVIN_calibrations_r0.5.tsv");
    FileCopyUtils.copy(gavin, new FileOutputStream(gavinFile));

    File observedOutputVcfFile = new File(FileUtils.getTempDirectory(), "outputVcfFile.vcf");

    InputStream outputVcf = IntegrationTest.class.getResourceAsStream(
        "/expected_output.vcf");
    expectedOutputVcfFile = new File(FileUtils.getTempDirectory(), "expectedOutput.vcf");
    FileCopyUtils.copy(outputVcf, new FileOutputStream(expectedOutputVcfFile));

    Main.run("",VersionUtils.getVersion(),inputVcfFile,observedOutputVcfFile,gavinFile, RlvMode.MERGED, true);

    assertEquals(readVcfLinesWithoutHeader(observedOutputVcfFile),
        readVcfLinesWithoutHeader(expectedOutputVcfFile));
  }

  public ArrayList<String> readVcfLinesWithoutHeader(File vcf) throws FileNotFoundException
  {
    ArrayList<String> res = new ArrayList<>();
    Scanner s = new Scanner(vcf);
    while (s.hasNext())
    {
      String line = s.nextLine();
      res.add(line);
    }
    return res;
  }
}
