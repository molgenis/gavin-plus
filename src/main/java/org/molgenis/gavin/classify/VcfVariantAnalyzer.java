package org.molgenis.gavin.classify;

import org.molgenis.genotype.Allele;
import org.molgenis.vcf.VcfReader;
import org.molgenis.vcf.VcfRecord;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

/**
 * Scan through a VCF, apply GAVIN to site-variant-allele-gene combinations, mark variants that are pathogenic or VOUS.
 * Also run past known pathogenic/likely pathogenic variants in ClinVar.
 * Result is a list of 'judged variants' with all meta data stored within.
 */
public class VcfVariantAnalyzer
{
	private final VariantAnalyzer variantAnalyzer;

	public VcfVariantAnalyzer(VariantAnalyzer variantAnalyzer)
	{
		this.variantAnalyzer = requireNonNull(variantAnalyzer);
	}

	public Stream<AnalyzedVariant> analyzeVcf(VcfReader vcfReader)
	{
		Stream<VcfRecord> vcfRecordStream = stream(spliteratorUnknownSize(vcfReader.iterator(), ORDERED), false);

		VcfRecordContext vcfRecordContext = new VcfRecordContext();
		return vcfRecordStream.filter(vcfRecord -> validateVcfRecord(vcfRecord, vcfRecordContext))
							  .map(variantAnalyzer::analyze);
	}

	private boolean validateVcfRecord(VcfRecord vcfRecord, VcfRecordContext vcfRecordContext)
	{
		validateVcfRecordChromPos(vcfRecord, vcfRecordContext);
		validateVcfRecordChromPosAltRef(vcfRecord, vcfRecordContext);
		validateVcfRecordAnalyzedChrom(vcfRecord, vcfRecordContext);
		updateVcfRecordContext(vcfRecord, vcfRecordContext);
		return true;
	}

	private void validateVcfRecordChromPos(VcfRecord vcfRecord, VcfRecordContext vcfRecordContext)
	{
		Integer position = vcfRecord.getPosition();
		Integer previousPosition = vcfRecordContext.getPosition();
		String chromosome = vcfRecord.getChromosome();
		String previousChromosome = vcfRecordContext.getChromosome();

		if (previousPosition != null && chromosome.equals(previousChromosome) && position < previousPosition)
		{
			throw new AnalyzerValidationException(String.format(
					"Site position %d before %d on the same chromosome (%s) not allowed. Please sort your VCF file.",
					position, previousPosition, chromosome));
		}
	}

	private void validateVcfRecordChromPosAltRef(VcfRecord vcfRecord, VcfRecordContext vcfRecordContext)
	{
		Integer position = vcfRecord.getPosition();
		Integer previousPosition = vcfRecordContext.getPosition();
		String chromosome = vcfRecord.getChromosome();
		String previousChromosome = vcfRecordContext.getChromosome();
		Allele referenceAllele = vcfRecord.getReferenceAllele();
		Allele previousReferenceAllele = vcfRecordContext.getReferenceAllele();
		List<Allele> alternateAlleles = vcfRecord.getAlternateAlleles();
		List<Allele> previousAlternateAlleles = vcfRecordContext.getAlternateAlleles();
		if (Objects.equals(position, previousPosition) && Objects.equals(chromosome, previousChromosome)
				&& Objects.equals(referenceAllele, previousReferenceAllele) && alternateAlleles.equals(
				previousAlternateAlleles))
		{
			throw new AnalyzerValidationException(String.format(
					"Chrom-pos-ref-alt combination seen twice: %s%d%s%s. This is not allowed. Please check your VCF file.",
					chromosome, position, referenceAllele,
					alternateAlleles.stream().map(Allele::toString).collect(Collectors.joining(","))));
		}
	}

	private void validateVcfRecordAnalyzedChrom(VcfRecord vcfRecord, VcfRecordContext vcfRecordContext)
	{
		String chromosome = vcfRecord.getChromosome();
		String previousChromosome = vcfRecordContext.getChromosome();
		if (previousChromosome != null && !chromosome.equals(previousChromosome)
				&& vcfRecordContext.hasAnalyzedChromose(chromosome))
		{
			throw new AnalyzerValidationException(
					String.format("Chromosome %s was interrupted by other chromosomes. Please sort your VCF file.",
							chromosome));
		}
	}

	private void updateVcfRecordContext(VcfRecord vcfRecord, VcfRecordContext vcfRecordContext)
	{
		String chromosome = vcfRecord.getChromosome();
		String previousChromosome = vcfRecordContext.getChromosome();
		if (previousChromosome != null && !chromosome.equals(previousChromosome))
		{
			vcfRecordContext.addAnalyzedChromose(previousChromosome);
		}
		vcfRecordContext.setPosition(vcfRecord.getPosition());
		vcfRecordContext.setReferenceAllele(vcfRecord.getReferenceAllele());
		vcfRecordContext.setAlternateAlleles(vcfRecord.getAlternateAlleles());
	}
}
