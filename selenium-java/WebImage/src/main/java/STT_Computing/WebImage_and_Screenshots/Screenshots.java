package STT_Computing.WebImage_and_Screenshots;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example of integrating Selenium and Sikuli to create a custom WebImage object
 * 
 * @author Daniel Robson - www.sttcomputing.co.uk
 *
 */
public final class Screenshots
{
	public static final String ERROR = "Error";
	public static final String STANDARD = "Standard";
	public static final String VALIDATION = "Validation";
	public static final String NONE = "None";
	protected static Logger logger = LoggerFactory
			.getLogger("FrameworkCore.Screenshots");
	private static List<BufferedImage> scenarioScreenshots;

	private Screenshots()
	{
	}

	/**
	 * Return a BufferedImage showing the currently focused browser instance.
	 * 
	 * @return
	 */
	protected static BufferedImage captureOpenBrowser(WebDriver selenium)
	{
		// NOTE: Safari will only capture the open viewport, as per
		// https://code.google.com/p/selenium/issues/detail?id=3752. If you want
		// to take an image of something specific (for instance for image
		// appearing validation) you need to scroll the browser first
		dismissOpenModalDialogIfExists(selenium);

		logger.debug("Taking Screenshot");

		BufferedImage image = null;
		try
		{
			byte[] screenshot = ((TakesScreenshot) selenium).getScreenshotAs(OutputType.BYTES);

			InputStream in = new ByteArrayInputStream(screenshot);
			image = ImageIO.read(in);
			in.close();

			// Temporary Sikuli Debugging code - Uncomment if needed
			// File outFile = new File("temp.png");
			// ImageIO.write(image, "png", outFile);
		}
		catch (Exception ex)
		{
			logger.error("Failed to capture screenshot: " + ex.getMessage());
		}
		return image;
	}

	/**
	 * Takes a screenshot of a certain sub-section of the browser as defined by
	 * starting x, y and width/height
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */

	protected static void takeScreenshot(String priority,
			WebDriver selenium)
	{
		takeScreenshot(priority, null, selenium);
	}

	public static List<BufferedImage> getScreenshots()
	{
		return scenarioScreenshots;
	}

	/***
	 * Takes a picture
	 * 
	 * @param priority
	 *        - Determines whether the screenshot will actually be taken, based
	 *        on ScreenshotType
	 * @param focusElement
	 *        - Element which the screenshot should focus on, with additional
	 *        padding
	 */
	protected static void takeScreenshot(String priority,
			WebElement focusElement, WebDriver selenium)
	{
		String sMethod = ConfigCore.instance().screenshotMethod();
		boolean exitEarly = false;

		switch (sMethod)
		{
			case Screenshots.STANDARD:
				break;
			case Screenshots.NONE:
				exitEarly = true;
				break;
			case Screenshots.ERROR:
				exitEarly = !priority.equals(Screenshots.ERROR);
				break;
			case Screenshots.VALIDATION:
				exitEarly = !(priority.equals(Screenshots.ERROR) || priority
						.equals(Screenshots.VALIDATION));
				break;
			default:
				logger.error("Unrecognised Screenshot Type: " + sMethod);
				exitEarly = true;
				break;
		}

		if (exitEarly)
		{
			return;
		}

		if (null == scenarioScreenshots)
		{
			scenarioScreenshots = new ArrayList<BufferedImage>();
		}

		// If a focus element isn't passed in, just use the standard
		// getScreenshotAs to take an image of the entire page and resize it
		BufferedImage image = captureOpenBrowser(selenium);
		if (null != focusElement)
		{
			image = generateFocusedScreenshot(focusElement, image);
		}

		// Work out if we should be resizing things
		if (ConfigCore.instance().resizeErrorScreenshots()
				|| !priority.equals(Screenshots.ERROR))
		{
			image = Scalr.resize(image, Scalr.Method.BALANCED,
					Scalr.Mode.FIT_TO_WIDTH, (int) ConfigCore.instance()
							.screenshotSize(), Scalr.OP_ANTIALIAS);
		}

		scenarioScreenshots.add(image);
	}

	private static BufferedImage generateFocusedScreenshot(
			WebElement focusElement, BufferedImage image)
	{
		int width = 0;
		int startX = focusElement.getLocation().getX();
		if (focusElement.getSize().getWidth() > ConfigCore.instance()
				.screenshotSize())
		{
			width = focusElement.getSize().getWidth();
		}
		else
		{
			int padding = (int) (ConfigCore.instance().screenshotSize() - focusElement
					.getSize().getWidth()) / 2;

			startX = startX - padding;
			if (startX < 0)
			{
				startX = 0;
			}

			width = (int) ConfigCore.instance().screenshotSize();
			if (width + startX > image.getWidth())
			{
				width = image.getWidth() - startX;
			}
		}

		// Slightly different here. When we resize the image, we fit it
		// to width. So to get nice square images we'd ideally want our
		// height and width to be the same
		int height = 0;
		int startY = focusElement.getLocation().getY();

		if (focusElement.getSize().getHeight() > width)
		{
			height = focusElement.getSize().getHeight();
		}
		else
		{
			int padding = (int) (width - focusElement.getSize().getHeight()) / 2;

			startY = startY - padding;
			if (startY < 0)
			{
				startY = 0;
			}

			height = width;
			if (height + startY > image.getHeight())
			{
				height = image.getHeight() - startY;
			}
		}

		// Crop our image based on the co-ordinates we've worked out
		try
		{
			return Scalr.crop(image, startX, startY, width, height);
		}
		catch (Exception ex)
		{
			// We failed resizing the image, but it isn't a critical error
			logger.error("Failed to Resize Image: " + ex.getMessage());
			return image;
		}
	}

	public static void clearScreenshots()
	{
		scenarioScreenshots.clear();
	}

	public static void takeValidationScreenshot(WebElement el,
			WebDriver selenium)
	{
		highlightElementAndTakeScreenshot(el, Screenshots.VALIDATION, selenium);
	}

	public static void takeErrorScreenshot(WebDriver selenium)
	{
		takeScreenshot(Screenshots.ERROR, selenium);
	}

	protected static void highlightElementAndTakeScreenshot(WebElement element,
			String priority, WebDriver selenium)
	{
		if (null == element)
		{
			return;
		}

		JavascriptExecutor js = (JavascriptExecutor) selenium;
		String oldStyle = element.getAttribute("style");

		String args = "arguments[0].setAttribute('style', arguments[1]);";
		js.executeScript(args, element,
				"border: 4px solid yellow;display:block;");

		// Rely on the execution time of this function (and the JS itself)
		// for some kind of wait so we get a pretty highlight on the monitor for
		// those watching along at home
		takeScreenshot(priority, element, selenium);

		js.executeScript(args, element, oldStyle);
	}

	public static void takeStandardScreenshot(WebElement el,
			WebDriver selenium)
	{
		highlightElementAndTakeScreenshot(el, Screenshots.STANDARD, selenium);
	}

	/**
	 * Get rid of (dismiss) an open popup box. This should actually be in
	 * another class, but this is a minified example anyway so leave it here
	 */
	public static void dismissOpenModalDialogIfExists(WebDriver selenium)
	{
		logger.debug("Attempting to close open dialog box");
		if (!ConfigCore.instance().browserType()
				.equals(ConfigCore.Safari))
		{
			try
			{
				Alert alert = selenium.switchTo().alert();
				alert.dismiss();
			}
			catch (NoAlertPresentException | NoSuchWindowException ex)
			{
				// We don't care if there's an exception here really
				logger.debug("No alert box actually existed");
			}
		}
	}
}
