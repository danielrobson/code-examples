import java.util.*;
import javax.mail.Message;
import javax.mail.Store;
import javax.mail.search.SearchTerm;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

/***
 * Example of logging in to GMail and parsing emails
 *
 * Written by Daniel Robson - www.sttcomputing.co.uk 
*/
public class MailPage
{
	private static final String gmailStoreName = "gmailStore";
	private Message selectedEmail;
	private final SeleniumInterface selenium;

	public MailPage(SeleniumInterface driverContext)
	{
		selenium = driverContext;
	}

	public void connectToMail(String email, String password)
	{
		selenium.persistedScenarioData.put(gmailStoreName,
				GMailFunctions.connectToGmail(email, password));
	}

	public void pollForAndSelectEmail(List<SearchTerm> requiredFilters,
			Date dateLimiter)
	{
		List<Message> returnEmails = new ArrayList<Message>(
				GMailFunctions.pollForEmails(requiredFilters,
						safeGetGmailStore(), dateLimiter));
		selectedEmail = returnEmails.get(0);
	}

	public void validateHeader()
	{
		Document doc = getMailHTML();
		Element headerDiv = doc.getElementById("ad_ems_EMAIL_Header");
		assertThat(headerDiv).isNotNull();
	}

	public void validateFooter()
	{
		Document doc = getMailHTML();
		Element footerDiv = doc.getElementById("ad_ems_EMAIL_Footer");
		assertThat(footerDiv).isNotNull();
	}

	public String getEmailText()
	{
		Document doc = getMailHTML();
		String actualText = doc.body().text();
		return actualText;
	}

	public Document getMailHTML()
	{
		if (null == selectedEmail)
		{
			fail("No email currently selected");
		}

		String mailContents = GMailFunctions.getMailContent(selectedEmail);
		return Jsoup.parse(mailContents);
	}

	@Override
	protected void finalize()
	{
		try
		{
			super.finalize();
			safeGetGmailStore().close();
		}
		catch (Exception ex)
		{
			fail("Failed to close GMail connection");
		}
		catch (Throwable e)
		{
			fail(e.getMessage());
		}
	}

	public Elements getMatchingLinks(String linkName)
	{
		Document doc = getMailHTML();
		return doc.select("a:contains(" + linkName + ")");
	}

	public void simulateEmailLinkClick(String forgottenpasswordemaillink)
	{
		Document doc = getMailHTML();
		Element linkToSimulateClick = doc.select(forgottenpasswordemaillink)
				.first();

		String URL = linkToSimulateClick.attr("href");
		selenium.navigateToURL(URL);
	}

	private Store safeGetGmailStore()
	{
		Store retStore;

		Object storedObject = selenium.persistedScenarioData
				.get(gmailStoreName);
		if (null == storedObject)
		{
			fail("Not connected to GMail");
		}

		try
		{
			retStore = (Store) storedObject;
			return retStore;
		}
		catch (ClassCastException ex)
		{
			fail("Unable to retrieve gmail data store from scenario context");
		}
		return null;
	}
}
