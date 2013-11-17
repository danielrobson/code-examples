using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Net.Sockets;
using System.IO;
using System.Windows.Forms;
using System.Diagnostics;
using System.Threading;

// RaveCat Email-Parser, by Daniel Robson.
// None of these account details work. And there are filters to send everything with a header that isn't from the ravecat site straight to trash
// For no discerable entertainment value whatsoever only.


namespace RaveCatParser
{
    public partial class RaveCat : Form
    {
        private delegate void updateLogDelegate(String toUpdate);

        public RaveCat()
        {
            InitializeComponent();
        }

        private void btnGetRaves_Click(object sender, EventArgs e)
        {
            txtLog.Text = String.Empty;
            Thread emailParserThread = new Thread(parseEmails);
            emailParserThread.IsBackground = true;
            emailParserThread.Start();
        }

        public void updateOutput(String update)
        {
            if (this.InvokeRequired)
                this.Invoke(new updateLogDelegate(this.updateOutput), update);
            else
            {
                txtLog.Text += update;
                txtLog.SelectionStart = txtLog.Text.Length;
                txtLog.ScrollToCaret();
            }
        }

        public void parseEmails()
        {
            try
            {
                // Setup Account details and Dictionary
                String account = "ravecat@ravecat.com";
                String password = "k4m9k@7";
                String server = "mail.shock-therapy.org";
                Dictionary<String, int> messages = new Dictionary<String, int>();

                updateOutput("Starting Rave Retrival" + Environment.NewLine + Environment.NewLine);

                // Initialise our TCP client, and associated streams.
                TcpClient tcpClient = new TcpClient();
                tcpClient.Connect(server, 110);
                NetworkStream netStrm = tcpClient.GetStream();
                System.IO.StreamReader strRead = new System.IO.StreamReader(netStrm);

                if (tcpClient.Connected)
                {
                    updateOutput("Connected to MailBox: " + Environment.NewLine + strRead.ReadLine() + Environment.NewLine + Environment.NewLine);
                } // if connected

                // Log into the server
                String login = "USER " + account + Environment.NewLine;

                byte[] WriteBuffer = new byte[1024];

                ASCIIEncoding en = new System.Text.ASCIIEncoding();

                WriteBuffer = en.GetBytes(login);

                netStrm.Write(WriteBuffer, 0, WriteBuffer.Length);

                login = "PASS " + password + Environment.NewLine;
                WriteBuffer = en.GetBytes(login);
                netStrm.Write(WriteBuffer, 0, WriteBuffer.Length);

                updateOutput("User Details Sent: " + strRead.ReadLine() + Environment.NewLine);
                updateOutput(strRead.ReadLine() + Environment.NewLine + Environment.NewLine);

                // Get the number of messages / message box size
                login = "STAT" + Environment.NewLine;
                WriteBuffer = en.GetBytes(login);
                netStrm.Write(WriteBuffer, 0, WriteBuffer.Length);

                String statResponse = strRead.ReadLine();

                updateOutput("Message Details: " + statResponse + Environment.NewLine + Environment.NewLine);

                char[] splitters = new char[] { ' ' };
                String[] statReturnArgs = statResponse.Split(splitters);

                int noOfMessages = Int32.Parse(statReturnArgs[1]);
                int currentMessage = 1;

                // Iterate through all the messages and pull out the body.

                while (currentMessage <= noOfMessages)
                {
                    login = "RETR " + currentMessage + Environment.NewLine;
                    WriteBuffer = en.GetBytes(login);
                    netStrm.Write(WriteBuffer, 0, WriteBuffer.Length);

                    String prevTextLine = null;
                    String textLine = String.Empty;
                    String nextLine = null;
                    String messageBody = String.Empty;
                    bool doLoop = true;

                    // Network stream is never technically terminated until we .Close() it. So the .ReadLine() will wait indefinitly for a chunk 'o data from
                    // the server. To prevent this, we'll look for the terminating "." in POP3, then check if we're on the last message or not.

                    while (doLoop)
                    {
                        // Watch out for multi-line messages
                        textLine = nextLine;
                        if (nextLine != ".")
                        {
                            nextLine = strRead.ReadLine();
                        }

                        if (prevTextLine == String.Empty)
                        {
                            if (textLine != ".")
                            {
                                // Irritating people like adding random newline characters to mess up counts
                                textLine = textLine.Replace("/r", "");
                                textLine = textLine.Replace("/n", "");

                                messageBody += textLine;
                            } // if ! Message terminator
                            else
                            {
                                Debug.Write(messageBody);
                                if (messages.ContainsKey(messageBody))
                                    messages[messageBody] = messages[messageBody] + 1;
                                else
                                    messages.Add(messageBody, 1);
                                doLoop = false;
                                updateOutput(messageBody);
                            } // else, if end of Message
                        } // If Message Body
                        else
                        {
                            prevTextLine = textLine;
                        } // else, Otherwise
                    } // while, End of Message

                    updateOutput(Environment.NewLine);
                    currentMessage++;
                } // End of all messages

                // Exit the POP3 server
                login = "QUIT" + Environment.NewLine;
                WriteBuffer = en.GetBytes(login);
                netStrm.Write(WriteBuffer, 0, WriteBuffer.Length);

                updateOutput("Quit MailBox: " + strRead.ReadLine() + Environment.NewLine + Environment.NewLine);

                // Close streams.
                strRead.Close();
                netStrm.Close();
                tcpClient.Close();

                updateOutput("Outputting Dictionary List: " + Environment.NewLine);

                // Get values from dictionary, and export to .txt file

                // Let's stick this on the user's desktop:

                TextWriter raveList = new StreamWriter((Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory) + "/raveList.txt"), false);
                raveList.WriteLine("<ol>");

                // Lets do a bit of sorting by value here:

                List<KeyValuePair<String, int>> orderedRaves = new List<KeyValuePair<string, int>>();

                foreach (KeyValuePair<String, int> pair in messages)
                    orderedRaves.Add(pair);

                // Message two first so in descending order of value
                orderedRaves.Sort(delegate(KeyValuePair<String, int> message1, KeyValuePair<String, int> message2)
                {
                    return message2.Value.CompareTo(message1.Value);
                });

                foreach (KeyValuePair<String, int> pair in orderedRaves)
                {
                    updateOutput(pair.Value + " - " + pair.Key + Environment.NewLine);
                    raveList.WriteLine("<li>" + pair.Value + " - " + pair.Key + "</li>");
                }

                raveList.WriteLine("</ol>");
                raveList.Close();

                updateOutput(Environment.NewLine + "Rave List output to 'raveList.txt'. Rave Parsing Complete.");
            } // Try, end of message log retrival
            catch (Exception err)
            {
                Debug.Write("You Fucked It Up:\n" + err);
            } //catch
        }

        private void RaveCat_FormClosed(object sender, FormClosedEventArgs e)
        {
            // Thread safety, ensure thread doesn't continue working after GUI closed. Also put Application.Exit in Program.cs to
            // handle alt-F4 etc.
            Application.Exit();
        }

    }
}
