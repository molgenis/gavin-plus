package org.molgenis.vcf.meta;

import java.util.Map;

public class VcfMetaInfo extends VcfMetaEntry
{
	public static final String KEY_ID = "ID";
	public static final String KEY_NUMBER = "Number";
	public static final String KEY_TYPE = "Type";
	public static final String KEY_DESCRIPTION = "Description";
	public static final String KEY_SOURCE = "Source";
	public static final String KEY_VERSION = "Version";

	public enum Type
	{
		INTEGER("Integer"), FLOAT("Float"), FLAG("Flag"), CHARACTER("Character"), STRING("String");

		private final String type;

		private Type(String type)
		{
			this.type = type;
		}

		public static Type from(String str)
		{
			for (Type type : values())
			{
				if (type.toString().equals(str)) return type;
			}
			return null;
		}

		@Override
		public String toString()
		{
			return type;
		}
	}

	public VcfMetaInfo(Map<String, String> properties)
	{
		super(properties);
	}

	@Override
	public String getName()
	{
		return "INFO";
	}

	public String getId()
	{
		return properties.get(KEY_ID);
	}

	public String getNumber()
	{
		return properties.get(KEY_NUMBER);
	}

	public Type getType()
	{
		return Type.from(properties.get(KEY_TYPE));
	}

	public String getDescription()
	{
		return properties.get(KEY_DESCRIPTION);
	}

	public String getSource()
	{
		return properties.get(KEY_SOURCE);
	}

	public String getVersion()
	{
		return properties.get(KEY_VERSION);
	}

	public Map<String, String> getProperties()
	{
		return properties;
	}
}
