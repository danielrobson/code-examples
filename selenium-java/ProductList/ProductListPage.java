
import PageObjects.PageBase;
import CustomControls.CommonXPath;
import FrameworkCore.ConfigCore;
import FrameworkCore.SeleniumInterface;
import FrameworkCore.Utils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import static org.fest.assertions.api.Assertions.fail;

public class ProductListPage extends PageBase
{
	private static final By productListerPage = By.id("productlisterpage");
	private static final By productList = By
			.cssSelector("div.row > div[class*= 'product']");
	private static final By gridViewOption = By
			.cssSelector("li.grid > div.viewoption > a");
	private static final By listViewOption = By
			.cssSelector("li.list > div.viewoption > a");

	public List<ProductItem> getAllDisplayedProducts()
	{
		selenium.waitForAjaxRefresh();
		List<WebElement> allProduct = selenium.getElements(productList);
		List<ProductItem> allProducts = new ArrayList<ProductItem>();

		for (WebElement name : allProduct)
		{
			allProducts.add(new ProductItem(name, selenium));
		}
		return allProducts;
	}

	public ProductItem getProductIfExists(String productName)
	{
		for (ProductItem itm : getAllDisplayedProducts())
		{
			if (itm.getName().equalsIgnoreCase(Utils.trimString(productName)))
			{
				return itm;
			}
		}
		fail("Unable to find Product: " + productName);
		return null;
	}

	public Boolean isViewIsApplied(String appliedViewName)
	{
		Boolean applied = false;

		// The only difference is in CSS stylesheets, so we will pull the first
		// product and check the styles applied to it
		WebElement product = selenium.getElement(productList);

		switch (appliedViewName)
		{
			case "grid":
				applied = "center".equalsIgnoreCase(product
						.getCssValue("text-align"))
						&& "left"
								.equalsIgnoreCase(product.getCssValue("float"));
				break;
			case "list":
				applied = "left".equalsIgnoreCase(product
						.getCssValue("text-align"))
						&& "none"
								.equalsIgnoreCase(product.getCssValue("float"));
				break;
			default:
				fail("Unrecognized view option : " + appliedViewName);
		}
		return applied;
	}

	public Comparator<ProductItem> getProductSortComparator(String sortType)
	{
		Comparator<ProductItem> retVal = null;
		switch (sortType)
		{
			case "Price Descending":
				retVal = new Comparator<ProductItem>() {
					public int compare(ProductItem p1, ProductItem p2)
					{
						return p2.getOfferPrice().compareTo(p1.getOfferPrice());
					}
				};
				break;
			case "Price Ascending":
				retVal = new Comparator<ProductItem>() {
					public int compare(ProductItem p1, ProductItem p2)
					{
						return p1.getOfferPrice().compareTo(p2.getOfferPrice());
					}
				};
				break;
			case "Product Name":
				retVal = new Comparator<ProductItem>() {
					public int compare(ProductItem p1, ProductItem p2)
					{
						return p1.getName().compareToIgnoreCase(p2.getName());
					}
				};
				break;
			default:
				fail("Sort Type not recognised: " + sortType);
		}
		return retVal;
	}
}
