package org.molgenis.data.annotation.makervcf.genestream.core;

import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.molgenis.data.annotation.makervcf.structs.Relevance;
import org.molgenis.data.annotation.makervcf.structs.RelevanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by joeri on 6/29/16.
 */
public abstract class GeneStream
{

	private static final Logger LOG = LoggerFactory.getLogger(GeneStream.class);
	private Iterator<GavinRecord> gavinRecordIterator;
	private boolean isFilterNonRelevant;

	public GeneStream(Iterator<GavinRecord> gavinRecordIterator)
	{
		this.gavinRecordIterator = gavinRecordIterator;
	}

	public Iterator<GavinRecord> go()
	{
		return new Iterator<GavinRecord>()
		{

			GavinRecord nextResult;
			Set<String> previousGenes;
			Set<String> currentGenes;

			HashMap<String, List<GavinRecord>> variantBufferPerGene = new HashMap<>();
			List<GavinRecord> variantBuffer = new ArrayList<>();
			Iterator<GavinRecord> resultBatch;

			@Override
			public boolean hasNext()
			{
				if (resultBatch != null && resultBatch.hasNext())
				{
					LOG.debug("[GeneStream] Returning subsequent result of gene stream batch");
					nextResult = resultBatch.next();
					return true;
				}
				else
				{

					while (gavinRecordIterator.hasNext())
					{
						GavinRecord gavinRecord = gavinRecordIterator.next();
						if (gavinRecord.isRelevant())
						{
							// cleanup of result batch after previously flushed results
							if (resultBatch != null)
							{
								LOG.debug("[GeneStream] Cleanup by setting result batch to null");
								resultBatch = null;
							}

							currentGenes = RelevanceUtils.getRelevantGenes(gavinRecord.getRelevance());
							LOG.debug("[GeneStream] Entering while, looking at a variant in gene " + currentGenes);

							// if the previously seen genes are fully disjoint from the current genes, start processing per gene and flush buffer
							if (previousGenes != null && Collections.disjoint(previousGenes, currentGenes))
							{
								LOG.debug("[GeneStream] Executing the abstract perGene() function on " + previousGenes);

								// process per gene in abstract function
								for (String gene : variantBufferPerGene.keySet())
								{
									LOG.debug("[GeneStream] Processing gene " + gene + " having " + variantBufferPerGene
											.get(gene)
											.size() + " variants");
									try
									{
										perGene(gene, variantBufferPerGene.get(gene));
									}
									catch (Exception e)
									{
										throw new RuntimeException(e);
									}
								}
								// create shallow copy, so that we can add another variant to buffer after we instantiate the iterator
								resultBatch = new ArrayList<>(variantBuffer).iterator();

								//reset buffers
								variantBuffer = new ArrayList<>();
								variantBufferPerGene = new HashMap<>();

							}

							// add current variant to gene-specific buffer
							for (Relevance rlv : gavinRecord.getRelevance())
							{
								String gene = rlv.getGene();
								if (variantBufferPerGene.containsKey(gene))
								{
									variantBufferPerGene.get(gene).add(gavinRecord);
								}
								else
								{
									List<GavinRecord> variants = new ArrayList<>();
									variants.add(gavinRecord);
									variantBufferPerGene.put(gene, variants);
								}
							}
							// add variant to global buffer
							variantBuffer.add(gavinRecord);

							// cycle previous and current genes
							previousGenes = currentGenes;

							// if result batch ready, start streaming it out
							if (resultBatch != null && resultBatch.hasNext())
							{
								LOG.debug("[GeneStream] Returning first result of gene stream batch");
								nextResult = resultBatch.next();
								return true;
							}
							else
							{
								//nothing to return for this gene after perGene(previousGene, variantsForGene)
								//so we go straight to cleanup in the next iteration
							}
						}else{
							if(!isFilterNonRelevant)
							{
								nextResult = gavinRecord;
								return true;
							}
						}
					}
				}

				//process the last remaining data before ending
				if (variantBuffer.size() > 0)
				{
					LOG.debug("[GeneStream] Buffer has " + variantBuffer.size() + " variants left in "
							+ variantBufferPerGene.keySet().toString());
					for (String gene : variantBufferPerGene.keySet())
					{
						try
						{
							perGene(gene, variantBufferPerGene.get(gene));
						}
						catch (Exception e)
						{
							throw new RuntimeException(e);
						}
					}
					resultBatch = new ArrayList<>(variantBuffer).iterator();
					variantBuffer = new ArrayList<>();
					variantBufferPerGene = new HashMap<>();
					if (resultBatch.hasNext())
					{
						LOG.debug("[GeneStream] Returning first of remaining variants");
						nextResult = resultBatch.next();
						return true;
					}
				}

				return false;
			}

			@Override
			public GavinRecord next()
			{
				return nextResult;
			}
		};
	}

	public abstract void perGene(String gene, List<GavinRecord> variantsPerGene) throws Exception;

}
