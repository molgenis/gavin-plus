package org.molgenis.data.annotation.makervcf;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class VersionUtils
{
	private VersionUtils()
	{
	}

	public static String getVersion()
	{
		String version = null;
		try
		{
			Enumeration<URL> resources = VersionUtils.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
			while (resources.hasMoreElements())
			{
				Manifest manifest = new Manifest(resources.nextElement().openStream());
				Attributes mainAttributes = manifest.getMainAttributes();
				if ("gavin-plus".equals(mainAttributes.getValue("Implementation-Title")))
				{
					version = mainAttributes.getValue("Implementation-Version");
					break;
				}
			}
		}
		catch (IOException e)
		{
			throw new UncheckedIOException(e);
		}
		return version != null ? version : "development";
	}
}
