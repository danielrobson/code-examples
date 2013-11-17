import CustomControls.CommonXPath;
import FrameworkCore.SeleniumInterface;
import FrameworkCore.Utils;
import org.joda.money.Money;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import static org.fest.assertions.api.Assertions.fail;

public class ProductItem extends ProductListPage
{
	private static final By nameWrapper = By
			.className("product_display_box_name");
	private static final By nameLocator = By.xpath("h2/a");
	private static final By price = By.className("offer");

	// May cause stale element reference exceptions
	private WebElement productReference;

	public ProductItem(WebElement prod, SeleniumInterface driverContext)
	{
		super(driverContext);
		productReference = prod;
	}

	public String getName()
	{
		String retValue = selenium.getElementText(
				selenium.getChainedElement(productReference, nameWrapper),
				nameLocator);
		return retValue;
	}

	/**
	 * Returns a Money object, trimmed of the 'Now', if present
	 * 
	 * @return
	 */
	public Money getOfferPrice()
	{
		String offerPrice = selenium.getElementText(productReference, price);
		return Utils.safeConvertStringToMoney(offerPrice);
	}
}
