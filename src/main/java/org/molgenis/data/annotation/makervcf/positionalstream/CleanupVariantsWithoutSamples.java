package org.molgenis.data.annotation.makervcf.positionalstream;

import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;

/**
 * Created by joeri on 6/29/16.
 */
public class CleanupVariantsWithoutSamples
{

	private static final Logger LOG = LoggerFactory.getLogger(CleanupVariantsWithoutSamples.class);
	private Iterator<GavinRecord> gavinRecordIterator;
	private boolean keepAllVariants;

	public CleanupVariantsWithoutSamples(Iterator<GavinRecord> gavinRecordIterator, boolean keepAllVariants)
	{
		this.gavinRecordIterator = gavinRecordIterator;
		this.keepAllVariants = keepAllVariants;
	}

	public Iterator<GavinRecord> go()
	{
		return new Iterator<GavinRecord>()
		{

			GavinRecord nextResult;

			@Override
			public boolean hasNext()
			{
				try
				{
					while (gavinRecordIterator.hasNext())
					{
						GavinRecord gavinRecord = gavinRecordIterator.next();
						if (gavinRecord.isRelevant())
						{

							LOG.debug("[CleanupVariantsWithoutSamples] Looking at: " + gavinRecord.toString());

							for (Relevance rlv : gavinRecord.getRelevance())
							{
								if (rlv.getSampleStatus().size() != rlv.getSampleGenotypes().size())
								{
									throw new Exception(
											"[CleanupVariantsWithoutSamples] rv.getSampleStatus().size() != rv.getSampleGenotypes().size()");
								}

								//we want at least 1 interesting sample
								if (rlv.getSampleStatus().size() > 0)
								{
									nextResult = gavinRecord;
									return true;
								}
								else
								{
									if (keepAllVariants)
									{
										gavinRecord.setRelevances(Collections.emptyList());
										nextResult = gavinRecord;
										return true;
									}
								}
								//FIXME update this line to new situation
								LOG.debug("[CleanupVariantsWithoutSamples] Removing variant at "
										+ gavinRecord.getChromosome() + ":" + gavinRecord.getPosition()
										+ " because it has 0 samples left");
							}
						}
						else
						{
							nextResult = gavinRecord;
							return true;
						}

					}
					return false;
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}

			}

			@Override
			public GavinRecord next()
			{
				return nextResult;
			}
		};
	}
}
