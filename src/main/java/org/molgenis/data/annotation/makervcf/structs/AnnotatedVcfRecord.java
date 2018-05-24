package org.molgenis.data.annotation.makervcf.structs;

import com.google.common.collect.Iterables;
import org.molgenis.calibratecadd.support.GavinUtils;
import org.molgenis.data.annotation.core.entity.impl.snpeff.Impact;
import org.molgenis.vcf.VcfInfo;
import org.molgenis.vcf.VcfRecord;
import org.molgenis.vcf.VcfRecordUtils;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

/**
 * {@link VcfRecord} annotated with SnpEff, CADD (as much as possible), ExAC, GoNL and 1000G.
 */
public class AnnotatedVcfRecord extends VcfRecord
{
	private static final String EXAC_AF = "EXAC_AF";
	private static final String GO_NL_AF = "GoNL_AF";
	private static final String CLSF = "CLSF";
	private static final String ANN = "ANN";
	private static final String RLV = "RLV";
	private static final String CLINVAR = "CLINVAR";
	private static final String CADD_SCALED = "CADD_SCALED";

	public AnnotatedVcfRecord(VcfRecord record)
	{
		super(record.getVcfMeta(), record.getTokens());
	}

	public double getExAcAlleleFrequencies(int i)
	{
		Double[] alleleFrequencies = getAltAlleleOrderedDoubleField(EXAC_AF);
		return alleleFrequencies[i] != null ? alleleFrequencies[i] : 0;
	}

	public double getGoNlAlleleFrequencies(int i)
	{
		Double[] alleleFrequencies = getAltAlleleOrderedDoubleField(GO_NL_AF);
		return alleleFrequencies[i] != null ? alleleFrequencies[i] : 0;
	}

	public String getClsf()
	{
		Optional<VcfInfo> optionalVcfInfo = VcfRecordUtils.getInformation(CLSF, this);
		return optionalVcfInfo.map(vcfInfo -> (String) vcfInfo.getVal()).orElse("");
	}

	public Set<String> getGenesFromAnn()
	{
		Optional<VcfInfo> optionalVcfInfo = VcfRecordUtils.getInformation(ANN, this);
		return optionalVcfInfo.map(vcfInfo ->
		{
			String ann = vcfInfo.getValRaw();
			Set<String> genes = new HashSet<>();
			String[] annSplit = ann.split(",", -1);
			for (String oneAnn : annSplit)
			{
				String[] fields = oneAnn.split("\\|", -1);
				String gene = fields[3];
				genes.add(gene);
			}
			return genes;

		}).orElse(emptySet());
	}

	@Nullable
	public Impact getImpact(int i, String gene)
	{
		String allele = VcfRecordUtils.getAltsAsStringArray(this)[i];
		Optional<VcfInfo> optionalVcfInfo = VcfRecordUtils.getInformation(ANN, this);
		return optionalVcfInfo.map(vcfInfo -> GavinUtils.getImpact(vcfInfo.getValRaw(), gene, allele)).orElse(null);
	}

	@Nullable
	public String getTranscript(int i, String gene)
	{
		String allele = VcfRecordUtils.getAltsAsStringArray(this)[i];
		Optional<VcfInfo> optionalVcfInfo = VcfRecordUtils.getInformation(ANN, this);
		return optionalVcfInfo.map(vcfInfo -> GavinUtils.getTranscript(vcfInfo.getValRaw(), gene, allele)).orElse(null);
	}

	@Nullable
	public List<RVCF> getRvcf()
	{
		Optional<VcfInfo> optionalVcfInfo = VcfRecordUtils.getInformation(RLV, this);
		return optionalVcfInfo.map(RVCF::fromVcfInfo).orElse(null);
	}

	public Double[] getCaddPhredScores()
	{
		return getAltAlleleOrderedDoubleField(CADD_SCALED);
	}

	public Double getCaddPhredScores(int i)
	{
		return getCaddPhredScores()[i];
	}

	public String getClinvar()
	{
		return GavinUtils.getInfoStringValue(this, CLINVAR);
	}

	private Double[] getAltAlleleOrderedDoubleField(String fieldName)
	{
		Double[] res = new Double[VcfRecordUtils.getAltsAsStringArray(this).length];
		if (GavinUtils.getInfoStringValue(this, fieldName) == null)
		{
			//the entire field is not present
			return res;
		}
		String[] split =
				GavinUtils.getInfoStringValue(this, fieldName) == null ? null : GavinUtils.getInfoStringValue(this,
						fieldName).split(",", -1);
		if (split != null)
		{
			if (split.length != VcfRecordUtils.getAltsAsStringArray(this).length)
			{
				//todo what is happening? loading back RVCF file:
				//Exception in thread "main" java.lang.Exception: CADD_SCALED split length not equal to alt allele split length for record vcf=[#CHROM=1,ALT=TG,C,POS=1116188,REF=CG,FILTER=PASS,QUAL=100.0,ID=rs367560627,INTERNAL_ID=RNWUDmMnfJqUyWdP6mlXlA,INFO={#CHROM_vcf=null,ALT_vcf=null,POS_vcf=null,REF_vcf=null,FILTER_vcf=null,QUAL_vcf=null,ID_vcf=null,INTERNAL_ID_vcf=null,CIEND=null,CIPOS=null,CS=null,END=null,IMPRECISE=false,MC=null,MEINFO=null,MEND=null,MLEN=null,MSTART=null,SVLEN=null,SVTYPE=null,TSD=null,AC=3,13,AF=5.99042E-4,0.00259585,NS=2504,AN=5008,LEN=null,TYPE=null,OLD_VARIANT=null,VT=null,EAS_AF=0.0,0.0129,EUR_AF=0.0,0.0,AFR_AF=0.0023,0.0,AMR_AF=0.0,0.0,SAS_AF=0.0,0.0,DP=6911,AA=null,ANN=C|frameshift_variant|HIGH|TTLL10|TTLL10|transcript|NM_001130045.1|protein_coding|8/16|c.706delG|p.Ala236fs|857/2259|706/2022|236/673||INFO_REALIGN_3_PRIME,TG|missense_variant|MODERATE|TTLL10|TTLL10|transcript|NM_001130045.1|protein_coding|8/16|c.703C>T|p.Arg235Trp|854/2259|703/2022|235/673||,LOF=(TTLL10|TTLL10|1|1.00),NMD=null,EXAC_AF=3.148E-4,0.001425,EXAC_AC_HOM=0,1,EXAC_AC_HET=31,145,GoNL_GTC=null,GoNL_AF=null,CADD=3.339984,CADD_SCALED=22.9,RLV=TG|3.148E-4|TTLL10|NM_001130045.1||||||NA19346:HOMOZYGOUS_COMPOUNDHET/NA19454:HETEROZYGOUS/HG03130:HETEROZYGOUS||NA19346:0p1/NA19454:0p1/HG03130:1p0||Predicted pathogenic|GAVIN|Variant MAF of 3.148E-4 is rare enough to be potentially pathogenic and its CADD score of 22.9 is greater than a global threshold of 15.||},SAMPLES_ENTITIES=org.molgenis.data.vcf.format.VcfToEntity$1@7f416310]
				throw new RuntimeException(
						fieldName + " split length " + split.length + " of string '" + GavinUtils.getInfoStringValue(
								this, fieldName) + "' not equal to alt allele split length "
								+ VcfRecordUtils.getAltsAsStringArray(this).length + " for record " + this.toString());
				//   System.out.println("WARNING: fieldName split length not equal to alt allele split length for record " + record.toString());
			}
			for (int i = 0; i < split.length; i++)
			{
				res[i] = (split[i] != null && !split[i].isEmpty() && !split[i].equals(".")) ? Double.parseDouble(
						split[i]) : null;
			}
		}
		else
		{
			throw new RuntimeException(fieldName + " split is null");
		}

		return res;
	}

	/**
	 * Returns information without RVCF information
	 */
	public Iterable<VcfInfo> getVcfEntityInformation()
	{
		List<RVCF> rvcf;
		rvcf = getRvcf();

		Iterable<VcfInfo> rvcfInformation;
		if (rvcf == null)
		{
			rvcfInformation = super.getInformation();
		}
		else
		{
			String key = RLV;
			String value = rvcf.stream().map(RVCF::toString).collect(Collectors.joining(","));
			VcfInfo rlvVcfInfo = new VcfInfo(getVcfMeta(), key, value);
			rvcfInformation = Iterables.concat(super.getInformation(), singleton(rlvVcfInfo));
		}
		return rvcfInformation;
	}
}
