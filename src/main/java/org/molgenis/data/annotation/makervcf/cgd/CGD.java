package org.molgenis.data.annotation.makervcf.cgd;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by joeri on 6/1/16.
 */
public class CGD {

    public CGD(File cgdLoc) throws IOException {
        InputStream fileStream = new FileInputStream(cgdLoc);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
        BufferedReader buffered = new BufferedReader(decoder);




        buffered.lines().forEach(line -> {
            // test
            System.out.println(line);

          //  HashMap<String, CGDEntry> cgd = LoadCGD.loadCGD(cgdFile);
        });
    }

    public static void main(String[] args) throws IOException {
        new CGD(new File("/Users/joeri/github/rvcf/data/CGD_1jun2016.txt.gz"));
    }
}
