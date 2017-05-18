# GAVIN+ 
## Gene-Aware Variant INterpretation for genome diagnostics

Detect potentially relevant clinical variants and matching samples in a VCF file.

Stand-alone demo is available at: http://molgenis.org/downloads/gavin/demo/GAVIN-Plus_Demo_r1.0.txt

If you use GAVIN+, please cite the following manuscript:
GAVIN - Gene-Aware Variant INterpretation for medical sequencing. K. Joeri van der Velde, Eddy N. de Boer, Cleo C. van Diemen, Birgit Sikkema-Raddatz, Kristin M. Abbott, Alain Knopperts, Lude Franke, Rolf H. Sijmons, Tom J. de Koning, Cisca Wijmenga, Richard J. Sinke and Morris A. Swertz. Genome Biology. 2017, 18(1). doi:10.1186/s13059-016-1141-7

Your input VCF must be fully annotated with SnpEff, ExAC frequencies and CADD scores, and optionally frequencies from GoNL and 1000G.
This can be done with MOLGENIS CmdlineAnnotator, available at https://github.com/molgenis/molgenis/releases/download/v1.21.1/CmdLineAnnotator-1.21.1.jar

Typical usage:
`java -jar GAVIN-APP-1.0.jar [inputfile] [outputfile] [helperfiles] [mode/flags]
Example usage:
java -Xmx4g -jar GAVIN-APP-1.0.jar \
-i patient76.snpeff.exac.gonl.caddsnv.vcf \
-o patient76_RVCF.vcf \
-g GAVIN_calibrations_r0.3.tsv \
-c clinvar.patho.fix.11oct2016.vcf.gz \
-d CGD_11oct2016.txt.gz \
-f FDR_allGenes_r1.0.tsv \
-a fromCadd.tsv \
-m ANALYSIS`

Dealing with CADD intermediate files:
You first want to generate a intermediate file with any missing CADD annotations using `-d toCadd.tsv -m CREATEFILEFORCADD`
After which you want to score the variants in toCadd.tsv with the web service at http://cadd.gs.washington.edu/score
The resulting scored file should be unpacked and then used for analysis with `-d fromCadd.tsv -m ANALYSIS`

Details on the various helper files:
The required helper files for -g, -c, -d and -f can be downloaded from: http://molgenis.org/downloads/gavin at 'data_bundle'.
The -a file is either produced by the analysis (using `-m CREATEFILEFORCADD`) or used as an existing file (using `-m ANALYSIS`).
The -l is a user-supplied VCF of interpreted variants. Use `CLSF=LP` or `CLSF=P` as info field to denote (likely) pathogenic variants.

Using pedigree data for filtering:
Please use the standard PEDIGREE notation in your VCF header, e.g. `##PEDIGREE=<Child=p01,Mother=p02,Father=p03>`. Trios and duos are allowed.
Parents are assumed unaffected, children affected. Using complex family trees, grandparents and siblings is not yet supported.

Some other notes:
Phased genotypes are used to remove obvious false compound heterozygous hits. These are demoted to heterozygous multihit.
If GoNL annotations are provided, variants above 5% MAF are removed as presumed false positives (in addition to ExAC >5%).
The gene FDR values are based on 2,504 individuals from The 1000 Genomes project and may be used as a general indication of significance -
however - high FDR values may be caused by either faulty detection OR false positives from the low-coverage sequencing data.

Available options:
```
Option                Description
-a, --cadd <File>     Input/output CADD missing annotations
-c, --clinvar <File>  ClinVar pathogenic VCF file
-d, --cgd <File>      CGD file
-e, --restore [File]  [not available] Supporting tool.
                        Combine RVCF results with original
                        VCF.
-f, --fdr <File>      Gene-specific FDR file
-g, --gavin <File>    GAVIN calibration file
-h, --help            Prints this help text
-i, --input <File>    Input VCF file
-l, --lab [File]      VCF file with lab specific variant
                        classifications
-m, --mode            Create or use CADD file for missing
                        annotations, either ANALYSIS or
                        CREATEFILEFORCADD
-o, --output <File>   Output RVCF file
-r, --replace         Enables output RVCF and CADD
                        intermediate file override,
                        replacing a file with the same name
                        as the argument for the -o option
-s, --sv [File]       [not available] Structural variation
                        VCF file outputted by Delly, Manta
                        or compatible
-v, --verbose         Verbally express what is happening
                        underneath the programmatic hood.
```
