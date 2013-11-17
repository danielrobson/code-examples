import FrameworkCore.SeleniumInterface;
import FrameworkCore.Utils;
import MailPage;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.util.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

public class EmailSteps
{
	public MailPage mail;
	protected final SeleniumInterface selenium;

	public EmailSteps(SeleniumInterface driverContext, MailPage dpMail)
	{
		selenium = driverContext;
		mail = dpMail;
	}

	@Given("^Email processing has occured$")
	public void Email_processing_has_occured() throws Throwable
	{
		// TODO: Do something to wait for your email processing job to fire, if it's batched
	}

	@When("^I connect to the gmail account \"([^\"]*)\" with the password \"([^\"]*)\"$")
	public void I_connect_to_the_gmail_account_with_the_password(String email,
			String password) throws Throwable
	{
		mail.connectToMail(email, password);
	}

	@Then("^I check the email text contains$")
	public void I_validate_the_body_of_text_in_the_email(String expectedText)
			throws Throwable
	{
		assertThat(mail.getEmailText().toLowerCase()).containsOnlyOnce(Utils.trimString(expectedText.toLowerCase()));
	}

	@Then("^I validate the links within the selected email$")
	public void I_validate_the_links_within_the_registration_email(
			DataTable links) throws Throwable
	{
		for (Map<String, String> link : links.asMaps())
		{
			Elements matchingLinks = mail
					.getMatchingLinks(link.get("Link Text"));
			assertThat(matchingLinks).isNotNull();
			
			if (matchingLinks.size() < 1)
			{
				fail("Unable to find specified link: " + link.get("Link Text"));
			}

			for (Element actLink : matchingLinks)
			{
				assertThat(actLink.attr("href")).isEqualToIgnoringCase(
						link.get("Target"));
			}
		}

	}
}
