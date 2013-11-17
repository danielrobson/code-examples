import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import javax.mail.*;
import javax.mail.search.SearchTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.fest.assertions.api.Assertions.fail;

/***
 * Example of logging in to GMail and parsing emails
 *
 * Written by Daniel Robson - www.sttcomputing.co.uk 
*/
public final class GMailFunctions
{
	protected static Logger logger = LoggerFactory
			.getLogger("FrameworkCore.Email");
	private static String protocol = "imaps";

	private GMailFunctions()
	{
	}

	public static Store connectToGmail(String accountEmail, String password)
	{
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", protocol);

		Session session = Session.getInstance(props, null);
		try
		{
			Store store = session.getStore(protocol);
			store.connect("imap.gmail.com", accountEmail, password);
			logger.info("Successfully connected to Gmail");
			return store;
		}
		catch (Exception ex)
		{
			fail("Unable to connect to GMail: " + ex.getMessage());
		}
		return null;
	}

	/**
	 * Polls the inbox for extraLongWaitTime, or until at least one message
	 * matches the search criteria passed in.
	 * 
	 * @param searchFilters
	 *        List of SearchTerms
	 * @param store
	 * @return List of all matching messages
	 */
	public static List<Message> pollForEmails(List<SearchTerm> searchFilters,
			Store store, Date dateLimiter)
	{
		logger.info("Retrieving Messages");
		if (null == store)
		{
			fail("Not connected to Gmail");
		}

		try
		{
			Folder inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);

			// Poll every second until the email polling timeout is hit
			boolean keepPolling = true;
			int pollCount = 0;

			while (keepPolling)
			{
				// Apply the filters, needs to match all of them to get reported
				Message[] messages = inbox.getMessages();
				for (SearchTerm term : searchFilters)
				{
					messages = inbox.search(term, messages);
				}

				// RecievedDateTerms are limited by an IMAP issue to only
				// filtering down to the day. So we're going to have to manually
				// compare the dates. This also has the effect of returning a
				// mutable list, as compared to calling .asList on the array
				List<Message> matchingMessages = new ArrayList<Message>();
				for (Message msg : messages)
				{
					if (null == dateLimiter)
					{
						matchingMessages.add(msg);
					}
					else
					{
						if (msg.getReceivedDate().compareTo(dateLimiter) == 1)
						{
							matchingMessages.add(msg);
						}
					}
				}

				if (matchingMessages.size() > 0)
				{
					// Sort by newest
					matchingMessages = sortByDate(matchingMessages);

					// Then return the newest
					return matchingMessages;
				}

				pollCount++;
				if (pollCount > ConfigCore.instance().extraLongWaitTime())
				{
					fail("Failed to find requested email in inbox after "
							+ ConfigCore.instance().extraLongWaitTime()
							+ " seconds");
				}
				Thread.sleep(1000);
			}
		}
		catch (Exception ex)
		{
			fail("Unexpected error polling for emails: " + ex.getMessage());
		}
		return null;
	}

	private static List<Message> sortByDate(List<Message> matchingMessages)
	{
		Collections.sort(matchingMessages, new Comparator<Message>() {
			public int compare(Message p1, Message p2)
			{
				try
				{
					return p2.getReceivedDate().compareTo(p1.getReceivedDate());
				}
				catch (MessagingException e)
				{
					fail("Comparison Failure while retrieving email dates");
				}
				return 0;
			}
		});
		return matchingMessages;
	}

	public static String getMailContent(Message message)
	{
		if (null == message)
		{
			fail("Invalid GMail message");
		}

		try
		{
			String line;
			StringBuffer buff = new StringBuffer();
			InputStream inStream = message.getInputStream();
			InputStreamReader in = new InputStreamReader(inStream, "UTF-8");
			BufferedReader reader = new BufferedReader(in);

			while ((line = reader.readLine()) != null)
			{
				buff.append(line);
			}
			reader.close();
			return buff.toString();
		}
		catch (Exception e)
		{
			fail("Unexpected error when getting mail contents" + e.getMessage());
		}
		return null;
	}
}
