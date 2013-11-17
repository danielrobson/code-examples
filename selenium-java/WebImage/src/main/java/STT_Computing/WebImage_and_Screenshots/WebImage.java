package STT_Computing.WebImage_and_Screenshots;

import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

/**
 * An example of integrating Selenium and Sikuli to create a custom WebImage object
 * 
 * @author Daniel Robson - www.sttcomputing.co.uk
 *
 */
public class WebImage
{
	private String imageURL;
	private String imageAltText;
	private String imageLocalCopy;
	private final WebDriver selenium;

	public WebImage(String name, String altText,
			String imageComparatorLocation, WebDriver driverContext)
	{
		setImageURL(name);
		setImageAltText(altText);
		setImageComparator(SikuliDriver.baseURI + imageComparatorLocation);
		selenium = driverContext;
	}

	public WebImage(String imageComparator, WebDriver driverContext)
	{
		this("", "", imageComparator, driverContext);
	}

	public WebImage(Map<String, String> imageDetails,
			WebDriver driverContext)
	{
		this(imageDetails.get("Image Name"), imageDetails.get("Alt Text"),
				imageDetails.get("File For Comparison"), driverContext);
	}

	public String getImageComparator()
	{
		return imageLocalCopy;
	}

	public void setImageComparator(String localCopy)
	{
		imageLocalCopy = localCopy;
	}

	public String getImageAltText()
	{
		return imageAltText;
	}

	public void setImageAltText(String altText)
	{
		imageAltText = altText;
	}

	public String getImageURL()
	{
		return imageURL;
	}

	public void setImageURL(String URL)
	{
		imageURL = URL;
	}

	public Boolean imageExistsOnWebpage()
	{
		By imageBy = getUnderlyingElement();
		try
		{
			WebElement image = selenium.findElement(imageBy);
			Actions move = (Actions) selenium;
			move.moveToElement(image).build();

			assertThat(image.getAttribute("alt")).isEqualToIgnoringCase(
					getImageAltText());
			return true;
		}
		catch (NoSuchElementException ex)
		{
			fail("Unable to find specified image: " + imageURL
					+ " with alt text: " + imageAltText);
			return false;
		}
	}

	public Boolean imageMatchesStoredCopy()
	{
		return imageMatchesStoredCopy(0.80);
	}

	public Boolean imageMatchesStoredCopy(Double similarityScore)
	{
		// This will fail under Safari if the image is not above the fold
		return new SikuliDriver(selenium).checkImageExistsInBrowser(
				getImageComparator(), similarityScore);
	}

	public By getUnderlyingElement()
	{
		return By.xpath("//img[contains(@src, '" + imageURL + "')]");
	}

}
