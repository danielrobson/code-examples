package STT_Computing.WebImage_and_Screenshots;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import org.openqa.selenium.WebDriver;
import org.sikuli.api.*;
import org.sikuli.api.robot.Mouse;
import org.sikuli.api.robot.desktop.DesktopMouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example of integrating Selenium and Sikuli to create a custom WebImage object
 * 
 * @author Daniel Robson - www.sttcomputing.co.uk
 *
 */
public final class SikuliDriver
{
	public static final String baseURI = System.getProperty("user.dir")
			+ File.separator + ConfigCore.instance().sikuliImageDirectory()
			+ File.separator;

	protected Logger logger = LoggerFactory.getLogger("FrameworkCore.Sikuli");
	private WebDriver browserContext;

	public SikuliDriver(WebDriver driverContext)
	{
		browserContext = driverContext;
	}

	private ScreenRegion getImage(String imageLocator, Double score)
	{
		// Do something a bit clever, and hook off the (TakesScreenshot) ability
		// of WebDriver to capture an image of the screen. Then pass THIS in as
		// our screen region, rather than using the entire desktop. If nothing
		// else, this means our code will still work _even when the browser is
		// minimised_
		ScreenRegion browser = new DefaultScreenRegion(
				new SikuliDriver.WebDriverScreen());

		Target imageTarget = new ImageTarget(new File(imageLocator));
		imageTarget.setMinScore(score);
		logger.info("Sikuli comparison against " + imageLocator
				+ " with match score of " + score);

		return browser.find(imageTarget);
	}

	public boolean checkImageExistsInBrowser(String imageName, Double score)
	{
		ScreenRegion foundImage = getImage(imageName, score);

		if (null == foundImage)
		{
			return false;
		}
		return true;
	}

	/**
	 * Note - Will NOT work while running remotely against SauceLabs
	 * 
	 * @param imageName
	 */
	@Deprecated
	public void clickImage(String imageName)
	{
		// TODO: Rewrite this using Selenium co-ords, as it this isn't a good
		// way to do this at all
		// Use the desktop entire, not the browser
		ScreenRegion browser = new DesktopScreenRegion();

		Target imageTarget = new ImageTarget(new File(baseURI + imageName));
		imageTarget.setMinScore(0.90);

		ScreenRegion foundImage = browser.find(imageTarget);

		if (null != foundImage)
		{
			Mouse mouse = new DesktopMouse();
			mouse.click(foundImage.getCenter());
		}
	}

	private class WebDriverScreen implements Screen
	{
		private BufferedImage browserSnapshot;

		public WebDriverScreen()
		{
			updateBrowserSnapshot();
		}

		public BufferedImage getScreenshot(int x, int y, int width, int height)
		{
			updateBrowserSnapshot();
			return getBrowserSnapshot();
		}

		public Dimension getSize()
		{
			if (null != getBrowserSnapshot())
			{
				return new Dimension(getBrowserSnapshot().getWidth(),
						getBrowserSnapshot().getHeight());
			}
			else
			{
				return new Dimension(0, 0);
			}
		}

		private void updateBrowserSnapshot()
		{
			browserSnapshot = Screenshots.captureOpenBrowser(browserContext);
		}

		public BufferedImage getBrowserSnapshot()
		{
			return browserSnapshot;
		}

	}
}
