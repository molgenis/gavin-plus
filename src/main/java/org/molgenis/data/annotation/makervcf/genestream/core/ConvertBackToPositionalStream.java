package org.molgenis.data.annotation.makervcf.genestream.core;

import org.molgenis.data.annotation.makervcf.structs.GavinRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * FIXME JvdV: any way to clean up 'order' during streaming of results? ie. for any positions that have been written out?
 */
public class ConvertBackToPositionalStream
{

	private static final Logger LOG = LoggerFactory.getLogger(ConvertBackToPositionalStream.class);
	private Iterator<GavinRecord> gavinRecordIterator;
	private ArrayList<Integer> order;

	public ConvertBackToPositionalStream(Iterator<GavinRecord> gavinRecordIterator, ArrayList<Integer> order)
	{
		this.gavinRecordIterator = gavinRecordIterator;
		this.order = order;
	}

	public Iterator<GavinRecord> go()
	{
		return new Iterator<GavinRecord>()
		{

			TreeMap<Integer, ArrayList<GavinRecord>> buffer = new TreeMap<>();
			Iterator<GavinRecord> bufferPrinter;
			GavinRecord nextResult;
			int i = -1;

			@Override
			public boolean hasNext()
			{

				if (bufferPrinter != null && bufferPrinter.hasNext())
				{
					LOG.debug("[ConvertBackToPositionalStream] Returning next element in the buffer");
					nextResult = bufferPrinter.next();
					return true;
				}

				while_:
				while (gavinRecordIterator.hasNext())
				{
					GavinRecord gavinRecord = gavinRecordIterator.next();
					try
					{
						i++;
							int pos = gavinRecord.getPosition();

							//position of stream matches real variant position, stable situation
							//write out any buffered variants in the correct order until this point
							if (pos == order.get(i))
							{
								if (buffer.size() == 0)
								{
									LOG.debug("[ConvertBackToPositionalStream] Buffer empty, returning current element "
											+ pos);
									nextResult = gavinRecord;
									return true;
								}
								else
								{
									// add to list of potential duplicate site positions
									if (buffer.containsKey(pos))
									{
										buffer.get(pos).add(gavinRecord);
									}
									else
									{
										buffer.put(pos, new ArrayList(Arrays.asList(gavinRecord)));
									}

									LOG.debug(
											"[ConvertBackToPositionalStream] Buffer size > 0, adding to buffer " + pos);

									// check if all positions are present up to the current one
									// to prevent problem: we see 20, 23, 22, 21 where alignment at 20 and 22, and we output wrongly 20, 23, 22, 21 because we haven't seen 21 yet
									// easiest check: all numbers in buffer are lower than current position
									for (Integer checkPos : buffer.keySet())
									{
										if (checkPos > pos)
										{
											continue while_;
										}
									}

									bufferPrinter = getIterator(buffer);
									LOG.debug(
											"[ConvertBackToPositionalStream] Positions aligned again at {0}, all values smaller than current pos, so clearning buffer with {1} elements",
											pos, buffer.size());
									buffer = new TreeMap<>();
									nextResult = bufferPrinter.next();
									return true;
								}

							}
							else
							{
								LOG.debug("[ConvertBackToPositionalStream] Adding to buffer {}", pos);
								if (buffer.containsKey(pos))
								{
									buffer.get(pos).add(gavinRecord);
								}
								else
								{
									buffer.put(pos, new ArrayList(Arrays.asList(gavinRecord)));
								}
							}
					}
					catch (Exception e)
					{
						throw new RuntimeException(e);
					}
				}

				bufferPrinter = getIterator(buffer);
				if (bufferPrinter != null && bufferPrinter.hasNext())
				{
					LOG.debug("[ConvertBackToPositionalStream] Clearing last elements from buffer");
					buffer = new TreeMap<>();
					nextResult = bufferPrinter.next();
					return true;
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

	private Iterator<GavinRecord> getIterator(TreeMap<Integer, ArrayList<GavinRecord>> buffer)
	{
		List<GavinRecord> variants = new ArrayList<>();
		//keys supposed to be sorted because TreeMap
		for (Integer pos : buffer.keySet())
		{
			variants.addAll(buffer.get(pos));
		}
		return variants.iterator();
	}
}
