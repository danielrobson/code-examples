import PageObjects.Products.ProductItem;
import PageObjects.Products.ProductListPage;
import StepDefinitions.StepBase;
import FrameworkCore.Utils;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.util.List;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

public class ProductListPageSteps extends StepBase
{
	private final ProductListPage productListPage;

	public ProductListPageSteps(ProductListPage page)
	{
		super();
		productListPage = page;
	}

	@Then("^The \"([^\"]*)\" view is applied to the PLP$")
	public void On_search_result_item_view_applied(String appliedViewName)
			throws Throwable
	{
		assertThat(productListPage.isViewIsApplied(appliedViewName)).isTrue();
	}

	@Then("^The products page is sorted by \"([^\"]*)\"$")
	public void The_products_page_is_sorted_by(String sortType)
			throws Throwable
	{
		List<ProductItem> displayedItems = productListPage
				.getAllDisplayedProducts();
		assertThat(displayedItems).isSortedAccordingTo(
				productListPage.getProductSortComparator(sortType));
	}
}
