package telnetd;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.Date;
import java.text.SimpleDateFormat ;

public final class PropertyHelper {

	private Properties properties;


	public static String getTimeStamp()
	{

		SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss:ms" );
		return dateFormat.format( new Date() );

	}

	public PropertyHelper(String file) {
		try {
			properties = new Properties();
			properties.load(new FileInputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getPropertyDefault(String keyName, String defaultValue) {
		return properties.getProperty(keyName, defaultValue).trim();
	}

	public int getIntegerDefault(String keyName, int defaultValue) {
		String value = getPropertyDefault(keyName, new Integer(defaultValue).toString());
		return Integer.parseInt(value);
	}

	public long getLongDefault(String keyName, long defaultValue) {
		String value = getPropertyDefault(keyName, new Long(defaultValue).toString());
		return Long.parseLong(value);
	}

	public boolean getBooleanDefault(String keyName, boolean defaultValue) {
		String value = getPropertyDefault(keyName, new Boolean(defaultValue).toString());
		return Boolean.parseBoolean(value);
	}

	public double getDoubleDefault(String keyName, double defaultValue) {
		String value = getPropertyDefault(keyName, new Double(defaultValue).toString());
		return Double.parseDouble(value);
	}

	public static PropertyHelper serverProperties = new PropertyHelper("../server.properties");
}
