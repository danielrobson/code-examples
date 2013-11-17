package STT_Computing.WebImage_and_Screenshots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example of integrating Selenium and Sikuli to create a custom WebImage object
 * 
 * @author Daniel Robson - www.sttcomputing.co.uk
 *
 */
public final class ConfigCore
{
	// Set it up as a lazily instantiated singleton, shouldn't need to worry
	// about multi-threading
	private static ConfigCore instance;

	protected Logger logger = LoggerFactory.getLogger("FrameworkCore.Config");

	// Private variables
	private long screenshotMaxSize = 500;
	private String browserType;
	private String screenshotMethod = "Never";
	private boolean resizeErrorScreenshots;
	private String sikuliImageDirectory;
	
	public static final String Safari = "Safari";
	public static final String Chrome = "Chrome";

	private ConfigCore()
	{
		logger.info("Importing Configuration Values");
		
		//Normally you'd pull values from an XML file or something here ... but ignore that for this minified example
		browserType = ConfigCore.Chrome;
		resizeErrorScreenshots = false;
		sikuliImageDirectory = "src/test/images";
	}


	// Make sure we have only getters, we don't want outside classes setting
	// these variables.
	public String sikuliImageDirectory()
	{
		return this.sikuliImageDirectory;
	}

	public String screenshotMethod()
	{
		return this.screenshotMethod;
	}

	public long screenshotSize()
	{
		return this.screenshotMaxSize;
	}

	public boolean resizeErrorScreenshots()
	{
		return this.resizeErrorScreenshots;
	}

	public String browserType()
	{
		return browserType;
	}

	public static ConfigCore instance()
	{
		if (instance == null)
		{
			instance = new ConfigCore();
		}
		return instance;
	}


}
