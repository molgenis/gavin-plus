package org.molgenis.data.annotation.entity.impl.gavin;

public class GavinEntry
{
	public static final int PATHO_MAF_INDEX = 9;
	public static final int CADD_INDEX = 29;
	private final String gene;
	private final Category category;
	private final String chromosome;
	private final Long start;
	private final Long end;
	private Integer nrOfPopulationVariants;
	private Integer nrOfPathogenicVariants;
	private Integer nrOfOverlappingVariants;
	private Integer nrOfFilteredPopVariants;
	private Double pathoMAFThreshold;
	private Double popImpactHighPerc;
	private Double popImpactModeratePerc;
	private Double popImpactLowPerc;
	private Double popImpactModifierPerc;
	private Double pathoImpactHighPerc;
	private Double pathoImpactModeratePerc;
	private Double pathoImpactLowPerc;
	private Double pathoImpactModifierPerc;
	private Double popImpactHighEq;
	private Double popImpactModerateEq;
	private Double popImpactLowEq;
	private Double popImpactModifierEq;
	private Integer nrOfCADDScoredPopulationVars;
	private Integer nrOfCADDScoredPathogenicVars;
	private Double meanPopulationCADDScore;
	private Double meanPathogenicCADDScore;
	private Double meanDifference;
	private Double uTestPvalue;
	private Double sens95thPerCADDThreshold;
	private Double spec95thPerCADDThreshold;

	public enum Category
	{
		N1, N2, T1, T2, I1, I2, I3, C1, C2, C3, C4, C5, Cx
	}

	public GavinEntry(String lineFromFile)
	{
		String[] split = lineFromFile.split("\t", -1);
		if (split.length != 30)
		{
			throw new RuntimeException("not 30 elements, have " + split.length + " at line " + lineFromFile);
		}

		this.gene = split[0];
		this.category = Category.valueOf(split[1]);
		this.chromosome = split[2];
		this.start = Long.valueOf(split[3]);
		this.end = Long.valueOf(split[4]);
		this.nrOfPopulationVariants = split[5].isEmpty() ? null : Integer.valueOf(split[5]);
		this.nrOfPathogenicVariants = split[6].isEmpty() ? null : Integer.valueOf(split[6]);
		this.nrOfOverlappingVariants = split[7].isEmpty() ? null : Integer.valueOf(split[7]);
		this.nrOfFilteredPopVariants = split[8].isEmpty() ? null : Integer.valueOf(split[8]);
		this.pathoMAFThreshold = split[PATHO_MAF_INDEX].isEmpty() ? null : Double.parseDouble(split[PATHO_MAF_INDEX]);

		this.popImpactHighPerc = split[10].isEmpty() ? null : Double.parseDouble(split[10]);
		this.popImpactModeratePerc = split[11].isEmpty() ? null : Double.parseDouble(split[11]);
		this.popImpactLowPerc = split[12].isEmpty() ? null : Double.parseDouble(split[12]);
		this.popImpactModifierPerc = split[13].isEmpty() ? null : Double.parseDouble(split[13]);

		this.pathoImpactHighPerc = split[14].isEmpty() ? null : Double.parseDouble(split[14]);
		this.pathoImpactModeratePerc = split[15].isEmpty() ? null : Double.parseDouble(split[15]);
		this.pathoImpactLowPerc = split[16].isEmpty() ? null : Double.parseDouble(split[16]);
		this.pathoImpactModifierPerc = split[17].isEmpty() ? null : Double.parseDouble(split[17]);

		this.popImpactHighEq = split[18].isEmpty() ? null : Double.parseDouble(split[18]);
		this.popImpactModerateEq = split[19].isEmpty() ? null : Double.parseDouble(split[19]);
		this.popImpactLowEq = split[20].isEmpty() ? null : Double.parseDouble(split[20]);
		this.popImpactModifierEq = split[21].isEmpty() ? null : Double.parseDouble(split[21]);

		this.nrOfCADDScoredPopulationVars = split[22].isEmpty() ? null : Integer.parseInt(split[22]);
		this.nrOfCADDScoredPathogenicVars = split[23].isEmpty() ? null : Integer.parseInt(split[23]);

		this.meanPopulationCADDScore = split[24].isEmpty() ? null : Double.parseDouble(split[24]);
		this.meanPathogenicCADDScore = split[25].isEmpty() ? null : Double.parseDouble(split[25]);
		this.meanDifference = split[26].isEmpty() ? null : Double.parseDouble(split[26]);
		this.uTestPvalue = split[27].isEmpty() ? null : Double.parseDouble(split[27]);
		this.sens95thPerCADDThreshold = split[28].isEmpty() ? null : Double.parseDouble(split[28]);
		this.spec95thPerCADDThreshold = split[CADD_INDEX].isEmpty() ? null : Double.parseDouble(split[CADD_INDEX]);
	}

	public String getGene()
	{
		return gene;
	}

	public Category getCategory()
	{
		return category;
	}

	public String getChromosome()
	{
		return chromosome;
	}

	public Long getStart()
	{
		return start;
	}

	public Long getEnd()
	{
		return end;
	}

	public Integer getNrOfPopulationVariants()
	{
		return nrOfPopulationVariants;
	}

	public Integer getNrOfPathogenicVariants()
	{
		return nrOfPathogenicVariants;
	}

	public Integer getNrOfOverlappingVariants()
	{
		return nrOfOverlappingVariants;
	}

	public Integer getNrOfFilteredPopVariants()
	{
		return nrOfFilteredPopVariants;
	}

	public Double getPathoMAFThreshold()
	{
		return pathoMAFThreshold;
	}

	public Double getPopImpactHighPerc()
	{
		return popImpactHighPerc;
	}

	public Double getPopImpactModeratePerc()
	{
		return popImpactModeratePerc;
	}

	public Double getPopImpactLowPerc()
	{
		return popImpactLowPerc;
	}

	public Double getPopImpactModifierPerc()
	{
		return popImpactModifierPerc;
	}

	public Double getPathoImpactHighPerc()
	{
		return pathoImpactHighPerc;
	}

	public Double getPathoImpactModeratePerc()
	{
		return pathoImpactModeratePerc;
	}

	public Double getPathoImpactLowPerc()
	{
		return pathoImpactLowPerc;
	}

	public Double getPathoImpactModifierPerc()
	{
		return pathoImpactModifierPerc;
	}

	public Double getPopImpactHighEq()
	{
		return popImpactHighEq;
	}

	public Double getPopImpactModerateEq()
	{
		return popImpactModerateEq;
	}

	public Double getPopImpactLowEq()
	{
		return popImpactLowEq;
	}

	public Double getPopImpactModifierEq()
	{
		return popImpactModifierEq;
	}

	public Integer getNrOfCADDScoredPopulationVars()
	{
		return nrOfCADDScoredPopulationVars;
	}

	public Integer getNrOfCADDScoredPathogenicVars()
	{
		return nrOfCADDScoredPathogenicVars;
	}

	public Double getMeanPopulationCADDScore()
	{
		return meanPopulationCADDScore;
	}

	public Double getMeanPathogenicCADDScore()
	{
		return meanPathogenicCADDScore;
	}

	public Double getMeanDifference()
	{
		return meanDifference;
	}

	public Double getuTestPvalue()
	{
		return uTestPvalue;
	}

	public Double getSens95thPerCADDThreshold()
	{
		return sens95thPerCADDThreshold;
	}

	public Double getSpec95thPerCADDThreshold()
	{
		return spec95thPerCADDThreshold;
	}
}