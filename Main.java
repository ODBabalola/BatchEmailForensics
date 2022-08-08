import javax.swing.*;
import java.io.File;    // Import File class
import java.io.FileNotFoundException;   // Import this class to handle errors
import java.util.*;
import java.text.SimpleDateFormat; // Import this class to format string dates
import java.text.ParseException; // Import class for parse exception

// The attributes class here stores the attributes
// from the forwarded section and reply
class attributes {
    public attributes() {
        from    = "";
        date    = new Date();
        subject = "";
        to      = new ArrayList<>();
    }

    public String   from;
    public Date     date;
    public String   subject;
    public ArrayList<String>   to;
}

class mail {
    public mail(String n, Date d, String t, String f,
                String sub, String sM, String qM, String frm, ArrayList<String> sTo) {
        name = n;
        date = d;
        replyToId = t;
        messageId = f;
        replies = new ArrayList<>();
        flag = false;
        fwd = false;    // forwarded mail tag
        subject = sub;
        sentMsg = sM;
        qtdMsg  = qM;
        from    = frm;
        to  = sTo;
    }
    public String name;
    public String from;
    public ArrayList<String> to;
    public Date date;
    public String replyToId;
    public String messageId;
    public ArrayList<mail> replies;
    public boolean fwd; // A tag to identify a forwarded mail within the replies array
    public String subject;
    public boolean flag;
    public String sentMsg;
    public String qtdMsg;
    public double similarity;
    /*
        The number of attributes of a forward or reply that match the original
        From, Date, Subject and To. Making a total of 4 attributes
     */
    public attributes atr;
    /*
        The percentage x/4 of attribute matches between descendant
        and ancestor
     */
    public double atrSimilarity;
}

public class Main {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_PURPLE = "\u001b[35m";
    public static final String ANSI_B_YELLOW = "\u001b[1m\u001b[33m";
    public static final String ANSI_BOLD = "\u001b[1m";

    private static String getFromAddress(String fName) {
        String address = null;
        String[] temp;

        try {
            File myObj = new File(fName);
            Scanner myReader = new Scanner(myObj);

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.matches("From:.*")) {
                    if (data.contains("<")) {
                        temp = data.split("<");
                       address = temp[1].replace(">", "").trim();
                    } else {
                        temp = data.split(" ");
                        address = temp[1].trim();
                    }
                    break;
                }
            }

            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An Error Occurred.");
            e.printStackTrace();
        }
        return address;
    }

    private static String getMessageID(String fName) {
        String address = null;
        String[] temp;

        try {
            File myObj = new File(fName);
            Scanner myReader = new Scanner(myObj);

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.matches("Message-ID:.*")) {
                    if (data.contains("<")) {
                        temp = data.split("<");
                        address = temp[1].replace(">", "").trim();
                    } else {
                        temp = data.split(" ");
                        address = temp[1].trim();
                    }
                    break;
                }
            }

            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An Error Occurred.");
            e.printStackTrace();
        }
        return address;
    }

    private static String getInReplyTo(String fName) {
        String address = null;
        String[] temp;

        try {
            File myObj = new File(fName);
            Scanner myReader = new Scanner(myObj);

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.matches("In-Reply-To:.*")) {
                    if (data.contains("<")) {
                        temp = data.split("<");
                        address = temp[1].replace(">", "").trim();
                    } else {
                        temp = data.split(" ");
                        address = temp[1].trim();
                    }
                    break;
                }
            }

            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An Error Occurred.");
            e.printStackTrace();
        }
        return address;
    }

    private static Date getDate(String fName) {
        String  rawDate;
        Date date = null;

        try {
            File myObj = new File(fName);
            Scanner myReader = new Scanner(myObj);

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if(data.matches("Date:.*")) {
                    rawDate = data.substring(5).trim();
                    // Instantiating the SimpleDateFormat class
                    //SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");
                    SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm");
                    // Parsing the given String to Date object
                    try {
                        date = formatter.parse(rawDate);
                        //System.out.println("Date object value: " + date);
                    } catch (ParseException e) {
                        System.out.println("An Error Occurred.");
                        e.printStackTrace();
                    }
                    break;
                }
            }

            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An Error Occurred.");
            e.printStackTrace();
        }
        return date;
    }

    private static void parseMail(String line, ArrayList<String> s) {
        String[] temp, temp2;

        temp2 = line.split(",");
        for (String l : temp2) {
            temp = l.split("<");
            if (1 < temp.length) {
                s.add(temp[1].replace(">", "").trim());
            }
        }
    }

    private static ArrayList<String> getToAddress(String fName) {
        ArrayList<String> sentTos = new ArrayList<>();

        try {
            File myObj = new File(fName);
            Scanner myReader = new Scanner(myObj);

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                if (getToSubMethod(myReader, sentTos, data)) break;
            }

            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An Error Occurred.");
            e.printStackTrace();
        }

        return sentTos;
    }

    private static void printAttributes(String fileName) {
        // Extract core attributes from first email
        String fromAddress = getFromAddress(fileName);
        Date date = getDate(fileName);
        ArrayList<String> S = getToAddress(fileName);
        String subject = getSubject(fileName);

        // Printing out output
        /*
         * The second string begins after 40 characters. The dash means that the
         * first string is left-justified.
         */
        String format = "%-20s%s%n";

        System.out.printf(format, ANSI_BOLD + "The Sender: \t\t" + ANSI_RESET, fromAddress);
        System.out.printf(format, ANSI_BOLD + "The Subject: \t\t" + ANSI_RESET, subject);
        System.out.printf(format, ANSI_BOLD + "The Date: \t\t\t" + ANSI_RESET, date);

        StringBuilder output = new StringBuilder();


        for (int k = 0; k < S.size(); k++) {
            //System.out.print(S.get(k));
            output.append(S.get(k));
            if (k != S.size() - 1) {
                //System.out.print(", ");
                output.append(", ");
            }
        }

        System.out.printf(format, ANSI_BOLD + "The Destination: \t" + ANSI_RESET, output);
        //System.out.println();
    }

    private static String getSubject(String fName) {
        String subject = "";

        try {
            File myObj = new File(fName);
            Scanner myReader = new Scanner(myObj);

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if(data.matches("Subject:.*")) {
                    subject = data.substring(8).trim();
                    break;
                }
            }

            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An Error Occurred.");
            e.printStackTrace();
        }

        return subject;
    }

    private static boolean checkSubject(String fileName, String fileName2) {
        String sub1 = getSubject(fileName);
        String sub2 = getSubject(fileName2);

        return checkSub(sub1, sub2);
    }

    private static boolean checkSub(String sub1, String sub2) {
        if (sub1.contains(":")) {
            String[] part1 = sub1.split(":");
            sub1 = part1[1].trim();
        }

        if (sub2.contains(":")) {
            String[] part2 = sub2.split(":");
            sub2 = part2[1].trim();
        }

        return sub1.contains(sub2) || sub2.contains(sub1);
    }

    private static void prefaceCheck(String f1, String f2) {
        Date date = getDate(f1);
        Date date2 = getDate(f2);

        System.out.println("______________________________________________________");
        boolean reply = checkSubject(f1,f2);
        if (reply) {
            System.out.println(ANSI_GREEN + "✓ Email subjects match" + ANSI_RESET);
        }
        else {
            System.out.println(ANSI_RED + "x Email subjects do not match" + ANSI_RESET);
        }

        if (date2.compareTo(date) > 0) {
            System.out.println(ANSI_GREEN + "✓ The date from the corresponding email occurs after the first email"
                    + ANSI_RESET);
        } else {
            System.out.println(ANSI_RED + "x The date from the corresponding email does not occur " +
                    "after the first email" + ANSI_RESET);
        }
    }

    private static void checkReply(String f1, String f2) {
        String fromAddress = getFromAddress(f1);
        ArrayList<String> S = getToAddress(f1);

        String fromAddress2 = getFromAddress(f2);
        ArrayList<String> S2 = getToAddress(f2);

        String messageId = getMessageID(f1);
        String replyToId = getInReplyTo(f2);

        prefaceCheck(f1, f2);

        String secondSubject = getSubject(f2);

        if (secondSubject.contains("Re: ")) {
            System.out.println(ANSI_GREEN + "✓ The second email contains the replied message indicator" +
                    " in the subject" + ANSI_RESET);
        }
        else {
            System.out.println(ANSI_RED + "x The second email does not contain the replied message indicator" +
                    " in the subject" + ANSI_RESET);
        }

        boolean validSent = false;
        for (String sent : S) {
            if (sent.equals(fromAddress2)) {
                validSent = true;
                System.out.println(ANSI_GREEN + "✓ The first emails 'to address' contains the" +
                        " corresponding email's 'from address':" + sent + ANSI_RESET);
            }
        }

        if (!validSent) {
            System.out.println(ANSI_RED + "x The first email's 'to address' does not contain " +
                    "the corresponding email's address" + ANSI_RESET);
        }

        boolean valid = false;
        for (String s : S2) {
            if (s.equals(fromAddress)) {
                valid = true;
                System.out.println(ANSI_GREEN + "✓ The first email's 'from address' and corresponding email's " +
                        "'to address' are equivalent: " + s + ANSI_RESET);
            }
        }

        if (!valid) {
            System.out.println(ANSI_RED + "x The first email's 'from address' and corresponding email's 'to address' " +
                    "are not equivalent" + ANSI_RESET);
        }

        if (messageId.equals(replyToId)) {
            System.out.println(ANSI_GREEN + "✓ The first email's 'messageID' matches the corresponding email's " +
                    "'inReplyTo'" + ANSI_RESET);
        }
        else {
            System.out.println(ANSI_RED + "x The first email's 'messageID' does not match the corresponding email's " +
                    "'inReplyTo'" + ANSI_RESET);
        }

        double percentage = findSimilarity(getSentMessage(f1), getQuotedReply(f2));
        percentage *= 100;

        if (percentage > 79) {
            System.out.println((ANSI_GREEN + "✓ The similarity between the sent message and the quoted reply" +
                    " message is : " + percentage + "%" + ANSI_RESET));
        }
        else {
            System.out.println((ANSI_RED + "x The similarity between the sent message and the quoted reply" +
                    " message is : " + percentage + "%" + ANSI_RESET));
        }

        System.out.println("______________________________________________________");
    }

    private static void checkForward(String f1, String f2) {
        prefaceCheck(f1, f2);

        ArrayList<String> S = getToAddress(f1);

        String fromAddress = getFromAddress(f1);
        String fromAddress2 = getFromAddress(f2);

        String messageId = getMessageID(f1);
        String replyToId = getInReplyTo(f2);

        String secondSubject = getSubject(f2);

        if (secondSubject.contains("Fwd: ")) {
            System.out.println(ANSI_GREEN + "✓ The second email contains the forwarded message indicator" +
                    " in the subject" + ANSI_RESET);
        }
        else {
            System.out.println(ANSI_RED + "x The second email does not contain the forwarded message indicator" +
                    " in the subject" + ANSI_RESET);
        }

        boolean validSent = false;
        for (String sent : S) {
            if (sent.equals(fromAddress2) || fromAddress.equals(fromAddress2)) {
                validSent = true;
                System.out.println(ANSI_GREEN + "✓ The original email was sent to the second email's from address: "
                        + sent + ANSI_RESET);
            }
        }

        if (!validSent) {
            System.out.println(ANSI_RED + "x The original email was not sent to the second " +
                    "from email's address" + ANSI_RESET);
        }

        if (messageId.equals(replyToId)) {
            System.out.println(ANSI_GREEN + "✓ The first email's 'messageID' matches the corresponding email's " +
                    "'inReplyTo'" + ANSI_RESET);
        }
        else {
            System.out.println(ANSI_RED + "x The first email's 'messageID' does not match the corresponding email's " +
                    "'inReplyTo'" + ANSI_RESET);
        }

        double percentage = findSimilarity(getSentMessage(f1), getQuotedForward(f2));
        percentage *= 100;

        if (percentage > 79) {
            System.out.println((ANSI_GREEN + "✓ The similarity between the sent message and the quoted forward" +
                    " message is : " + percentage + "%" + ANSI_RESET));
        }
        else {
            System.out.println((ANSI_RED + "x The similarity between the sent message and the quoted forward" +
                    " message is : " + percentage + "%" + ANSI_RESET));
        }

        System.out.println("______________________________________________________");
    }

    private static void checkDescendant(String f1, String f2) {
        prefaceCheck(f1,f2);
        String sentMsg = getSentMessage(f1);
        String qtdFull = getQuotedFull(f2);
        String firstLine = getSentFirstLine(f1);
        int degree;
        if (firstLine == null) {
            degree = -1;
        } else {
            degree = getDescendentDegree(f2,firstLine);
        }

        if (qtdFull.contains(sentMsg)) {
            System.out.println(ANSI_GREEN + "✓ The first email is quoted fully by the second." + ANSI_RESET);
        }
        else {
            System.out.println(ANSI_RED + "x The first email is NOT quoted fully by the second." + ANSI_RESET);
        }

        if (degree == -1) {
            System.out.println(ANSI_RED + "x The reply degree/depth is not applicable." + ANSI_RESET);
        }
        else {
            System.out.println(ANSI_GREEN + "✓ The reply degree/depth/level is: " + degree + ANSI_RESET);
        }
    }

    private static int getEditDistance(String X, String Y) {
        /* Edit distance formula otherwise known as the Levenshtein distance */
        int m = X.length();
        int n = Y.length();

        int[][] T = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            T[i][0] = i;
        }
        for (int j = 1; j <= n; j++) {
            T[0][j] = j;
        }

        int cost;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                cost = X.charAt(i - 1) == Y.charAt(j - 1) ? 0: 1;
                T[i][j] = Integer.min(Integer.min(T[i - 1][j] + 1, T[i][j - 1] + 1),
                        T[i - 1][j - 1] + cost);
            }
        }

        return T[m][n];
    }

    private static double findSimilarity(String x, String y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        double maxLength = Double.max(x.length(), y.length());
        if (maxLength > 0) {
            // optionally ignore case if needed
            return (maxLength - getEditDistance(x, y)) / maxLength;
        }
        return 1.0;
    }

    private static String getSentMessage(String fName) {
        StringBuilder message = new StringBuilder();
        String output;

        try {
            File myObj = new File(fName);
            Scanner myReader = new Scanner(myObj);

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
               if (data.contains("Content-Type: text/")) {
                   while (myReader.hasNextLine()) {
                       data = myReader.nextLine();
                       if (!data.contains("Content-Transfer-Encoding") && !data.contains("--")
                               && !data.contains("wrote:") && !data.contains("Mime-Version:")) {
                           data += " ";
                           message.append(data);
                       }
                       else if (data.contains("--") || data.contains("wrote:")) {
                           output = message.toString().trim();
                           output = output.replace("  ", " ");
                           return output;
                       }
                   }
                   break;
               }
            }

            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An Error Occurred.");
            e.printStackTrace();
        }
        output = message.toString().trim();
        output = output.replace("  ", " ");
        return output;
    }

    private static String getSentFirstLine(String fName) {
        try {
            File myObj = new File(fName);
            Scanner myReader = new Scanner(myObj);

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.contains("Content-Type: text/")) {
                    while (myReader.hasNextLine()) {
                        data = myReader.nextLine();
                        if (!data.contains("Content-Transfer-Encoding") && !data.contains("--")
                                && !data.contains("wrote:") && !data.isBlank()) {
                            return data.trim();
                        }
                    }
                    break;
                }
            }

            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An Error Occurred.");
            e.printStackTrace();
        }

        return null;
    }

    private static int getDescendentDegree(String fName, String line) {
        try {
            File myObj = new File(fName);
            Scanner myReader = new Scanner(myObj);

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.contains(line)) {
                    String[] result = data.trim().split(" ",2);
                    //System.out.println(result[0]);
                    return result[0].length();
                }
            }

            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An Error Occurred.");
            e.printStackTrace();
        }

        return -1;
    }

    private static String getQuotedReply(String fName) {
        StringBuilder reply = new StringBuilder();
        String output;

        try {
            File myObj = new File(fName);
            Scanner myReader = new Scanner(myObj);

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                /*
                In the case the email is actually forward of a reply,
                the forwarded message indicator would appear first and
                thus an empty string should be returned
                 */
                if (data.contains("Forwarded message")) {
                    return "";
                }

                if (data.contains("wrote:")) {
                    while (myReader.hasNextLine()) {
                        data = myReader.nextLine();
                        if (!data.contains("--") && !data.contains("> wrote:")) {
                            //data += " ";
                            data = data.replace(">","");
                            reply.append(data);
                        }
                        else {
                            output = reply.toString().trim();
                            output = output.replace("  ", " ");
                            output = output.replace("=", "");
                            return output;
                        }
                    }
                    break;
                }
            }

            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An Error Occurred.");
            e.printStackTrace();
        }

        output = reply.toString().trim();
        output = output.replace("  ", " ");
        return output;
    }

    private static String getQuotedFull(String fName) {
        StringBuilder reply = new StringBuilder();
        String output;

        try {
            File myObj = new File(fName);
            Scanner myReader = new Scanner(myObj);

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.contains("wrote:")) {
                    while (myReader.hasNextLine()) {
                        data = myReader.nextLine();
                        if (!data.contains("--")) {
                            //data += " ";
                            data = data.replace(">","");
                            reply.append(data);
                        }
                        else {
                            output = reply.toString().trim();
                            output = output.replace("  ", " ");
                            return output;
                        }
                    }
                    break;
                }
            }

            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An Error Occurred.");
            e.printStackTrace();
        }

        output = reply.toString().trim();
        output = output.replace("  ", " ");
        return output;
    }

    private static String getQuotedForward(String fName) {
        StringBuilder fwd = new StringBuilder();
        String output;

        try {
            File myObj = new File(fName);
            Scanner myReader = new Scanner(myObj);

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.contains("Forwarded message")) {
                    while (myReader.hasNextLine()) {
                        data = myReader.nextLine();
                        // This ensures the quoted text does not read more lines than it should
                        if (!data.contains("--") && !data.contains("> wrote:")) {
                            // Forwarded messages contain attributes of the message being forwarded,
                            // this need to be ignored.
                            if (!data.contains(": ") && !(data.contains(" <") && data.contains(">"))) {
                                data = data.replace(">","");
                                data = " " + data;
                                fwd.append(data);
                            }
                        }
                        else {
                            output = fwd.toString().trim();
                            output = output.replace("  ", " ");
                            output = output.replace("=", "");
                            return output;
                        }
                    }
                    break;
                }
            }

            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An Error Occurred.");
            e.printStackTrace();
        }

        output = fwd.toString().trim();
        output = output.replace("  ", " ");
        return output;
    }

    private static ArrayList<mail> bubbleSort(ArrayList<mail> m) {
        int len = m.size();
        Date dateOne, dateTwo;

        for (int i = 0; i < len-1; ++i) {
            for (int j = 0; j < len - i - 1; ++j) {
                dateOne = m.get(j+1).date;
                dateTwo = m.get(j).date;
                if (dateOne.before(dateTwo)) {
                    Collections.swap(m,j,j+1);
                }
            }
        }
        return m;
    }

    private static void printNTree(mail x, boolean[] flag, int depth, boolean isLast) {
        if ((x == null) || x.flag) {
            return;
        }

        // Retrieves the stored date and similarity attribute from the mail object
        // and formats it for presentation

        String str = String.format("%1$tB %1$td, %1$tY",x.date);
        String str2 = String.format("%.2f", x.similarity);
        String str3 = String.format("%.2f", x.atrSimilarity);

        for (int i = 1; i < depth && i < flag.length; ++i) {
            if (flag[i]) {
                System.out.print("| "
                        + " "
                        + " "
                        + " ");
            }
            else {
                System.out.print(" "
                        + " "
                        + " "
                        + " ");
            }
        }

        // root
        if (depth == 0) {
            System.out.println(x.name + ", " + ANSI_BLUE + str + ANSI_RESET); //replaced x.date
        }
        else if (isLast) {
            treeBranches(x, str, str2, str3);
            if (depth < flag.length) {
                flag[depth] = false;
            }
        }
        else {
            treeBranches(x, str, str2, str3);
        }

        int it = 0;
        int size = 1;
        for (mail i : x.replies) {
            ++it;
            if (x.replies != null) {
                size = x.replies.size();
            }
            printNTree(i,flag, depth+1,it==size-1);
        }

        if (depth < flag.length) {
            flag[depth] = true;
        }
    }

    private static void treeBranches(mail x, String str, String str2, String str3) {
         /*
            If the mail is actually a forwarded mail, and not a reply mail.
            It should be shown with a different printout syntax
         */
        if (!x.fwd) {
            System.out.print(ANSI_YELLOW + "└── " + ANSI_RESET +  x.name + ", " + ANSI_BLUE + str + ANSI_RESET +
                    ", Quoted Similarity: " + ANSI_GREEN + str2 + "%" + ANSI_RESET + '\n');
        }
        else {
            System.out.print(ANSI_PURPLE + "└── " + ANSI_RESET +  x.name + ", " + ANSI_BLUE + str + ANSI_RESET +
                    ", Quoted Forward Similarity: " + ANSI_GREEN + str2 + "%" + ANSI_RESET +
                    ", Quoted Attributes Similarity: " + ANSI_GREEN + str3 + "%" + ANSI_RESET  + '\n');
        }
        x.flag = true;
    }

    private static attributes getFwdAttributes(String flName) {
        // Output attributes class object
        attributes output   = new attributes();

        try {
            File myObj  = new File(flName);
            Scanner myReader    = new Scanner(myObj);

            String[] temp;
            ArrayList<String> sentTos = new ArrayList<>();
            String  rawDate;

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.contains("Forwarded message")) {
                    while (myReader.hasNextLine()) {
                        data = myReader.nextLine();
                        // This ensures the quoted text does not read more lines than it should
                        if (!data.contains("--") && !data.contains("> wrote:")) {
                            // Forwarded messages contain attributes of the message being forwarded,
                            // this is captured here

                            // From attribute captured
                            if (data.matches("From:.*")) {
                                if (data.contains("<")) {
                                    temp = data.split("<");
                                    output.from = temp[1].replace(">", "").trim();
                                } else {
                                    temp    = data.split(" ");
                                    output.from = temp[1].trim();
                                }
                            }

                            // Date attribute captured
                            if(data.matches("Date:.*")) {
                                rawDate = data.substring(5).trim();
                                // Removing this string character is necessary for the date to be formatted
                                rawDate = rawDate.replace(" at", "");
                                // Instantiating the SimpleDateFormat class
                                // The format is different for the normal date and
                                // forwarded date presentations
                                SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm");
                                // Parsing the given String to Date object
                                try {
                                    output.date = formatter.parse(rawDate);
                                    //System.out.println("Date object value: " + date);
                                } catch (ParseException e) {
                                    System.out.println("An Error Occurred.");
                                    e.printStackTrace();
                                }
                            }

                            // Subject attribute captured
                            if(data.matches("Subject:.*")) {
                                output.subject  = data.substring(8).trim();
                            }

                            // To attribute captured
                            if (getToSubMethod(myReader, sentTos, data)) output.to   = sentTos;
                        }

                    }
                    break;
                }
            }

            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An Error Occurred.");
            e.printStackTrace();
        }
        return output;
    }

    private static boolean getToSubMethod(Scanner myReader, ArrayList<String> sentTos, String data) {
        String[] temp;
        if (data.matches("To:.*")) {
            if (data.contains(",")) {

                boolean loop = true;
                while (myReader.hasNextLine() && loop) {
                    if (data.contains(",")) {
                        parseMail(data, sentTos);
                    }
                    else if (data.contains("<")) {
                        temp = data.split("<");
                        sentTos.add(temp[1].replace(">", "").trim());
                    }
                    else {
                        if (data.contains(":")) {
                            loop = false;
                        } else {
                            sentTos.add(data.trim());
                        }
                    }
                    data = myReader.nextLine(); //*
                }
            }
            else if (data.contains("<")) {
                temp = data.split("<");
                sentTos.add(temp[1].replace(">", "").trim());
            }
            else {
                temp = data.split(" ");
                sentTos.add(temp[1].trim());
            }
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println("______________________________________________________");
        System.out.println( ANSI_B_YELLOW + "Program Starting...." + ANSI_RESET);
        System.out.println("______________________________________________________");

        Scanner userInput = new Scanner(System.in); // Create a Scanner object
        System.out.println(ANSI_BLUE + ANSI_BOLD + "TASK" + ANSI_RESET);
        System.out.println(ANSI_GREEN + ANSI_BOLD + "1" + ANSI_RESET +
                ": Validate if an email is a reply to another email");
        System.out.println(ANSI_GREEN + ANSI_BOLD + "2" + ANSI_RESET +
                ": Validate if an email is a forward to another email");
        System.out.println(ANSI_GREEN + ANSI_BOLD + "3" + ANSI_RESET +
                ": Graphically contextualise and authenticate email communications");
        System.out.println(ANSI_GREEN + ANSI_BOLD + "4" + ANSI_RESET +
                ": Validate if an email is a 'reply' descendent to another email and to what degree");
        System.out.println("______________________________________________________");
        System.out.print("Enter Task: ");
        String task = userInput.nextLine();

        System.out.println("______________________________________________________");
        switch (task) {
            case "1" -> {
                System.out.println("Enter the file name for the first email:");
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Enter the file name for the first email.");
                chooser.showOpenDialog(null);
                String fileName = chooser.getSelectedFile().toPath().toString();
                System.out.println("______________________________________________________");
                printAttributes(fileName);
                System.out.println("______________________________________________________");

                System.out.println("Enter the file name for the second email:");
                JFileChooser chooser2 = new JFileChooser();
                chooser2.setDialogTitle("Enter the file name for the second email.");
                chooser2.showOpenDialog(null);
                String fileName2 = chooser2.getSelectedFile().toPath().toString();
                System.out.println("______________________________________________________");
                printAttributes(fileName2);

                checkReply(fileName, fileName2);
                System.out.println(getSentMessage(fileName));
                System.out.println(getQuotedReply(fileName2));
            }
            case "2" -> {
                System.out.println("Select original email file:");
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select original email file.");
                chooser.showOpenDialog(null);
                String orgFileName = chooser.getSelectedFile().toPath().toString();
                System.out.println("______________________________________________________");
                printAttributes(orgFileName);
                System.out.println("______________________________________________________");

                System.out.println("Select forwarded email file:");
                JFileChooser chooser2 = new JFileChooser();
                chooser2.setDialogTitle("Select forwarded email file.");
                chooser2.showOpenDialog(null);
                String fwdFileName = chooser2.getSelectedFile().toPath().toString();
                System.out.println("______________________________________________________");
                printAttributes(fwdFileName);

                checkForward(orgFileName, fwdFileName);
                System.out.println(getSentMessage(orgFileName));
                System.out.println(getQuotedForward(fwdFileName));
            }
            case "3" -> {
                System.out.println("Select email files:");
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select email files.");
                chooser.setMultiSelectionEnabled(true);

                // Show the dialog; wait until dialog is closed
                chooser.showOpenDialog(null);

                System.out.println("______________________________________________________");
                // Retrieve the selected files.
                File[] files = chooser.getSelectedFiles();
                // An arraylist object that contains all the email files
                // transformed into mail objects
                ArrayList<mail> fls = new ArrayList<>();

                // Create mail objects from all the selected files for querying
                String path, n,t, f, sub, sM, qM, frm;
                ArrayList<String> sTo;
                Date d;
                for (File fn : files) {
                    path    = fn.toPath().toString();
                    n   = fn.getName();
                    d   = getDate(path);
                    t   = getInReplyTo(path);
                    f   = getMessageID(path);
                    sub = getSubject(path);
                    sM  = getSentMessage(path);
                    // If the email does not contain a quoted reply,
                    // the qM variable should store the quoted forward
                    qM  = getQuotedReply(path);
                    frm = getFromAddress(path);
                    sTo = getToAddress(path);
                    if (qM.equals("")) {
                        qM  = getQuotedForward(path);
                        mail node   = new mail(n, d, t, f, sub, sM, qM, frm , sTo);
                        node.fwd    = true;
                        node.atr    = getFwdAttributes(path);
                        fls.add(node);
                    }
                    else {
                        fls.add(new mail(n, d, t, f, sub, sM, qM, frm, sTo));
                    }

                }

                // Bubble sort mail list fls
                ArrayList<mail> srtM = bubbleSort(fls);
                boolean toValid, validDate, validSubject, validQuote;
                double value;
                for (mail mail : srtM) {
                    validQuote = false;

                    for (mail l : srtM) {
                        // count variable is used to check for matching attributes
                        float count = 0;
                        // check if mail l is a reply
                        toValid = mail.messageId.equals(l.replyToId);
                        validDate = l.date.compareTo(mail.date) > 0;
                        validSubject = checkSub(mail.subject, l.subject);
                        // For program efficiency sake
                        // All suspected replies and forwards will be added to
                        // the mail objects replies field
                        if (toValid & validDate & validSubject) {
                            value = findSimilarity(mail.sentMsg, l.qtdMsg);
                            if (value >= 0.8) validQuote = true;
                            l.similarity = value * 100;

                            if (l.fwd) {
                                if (l.atr.from.equals(mail.from)) {
                                    count++;
                                }

                                if (l.atr.date.equals(mail.date)) {
                                    count++;
                                }

                                if (l.atr.subject.equals(mail.subject)) {
                                    count++;
                                }

                                if (l.atr.to.equals(mail.to)) {
                                    count++;
                                }

                                l.atrSimilarity = (count/4) * 100;
                            }


                            if (validQuote) {
                                mail.replies.add(l);
                            }
                        }
                    }
                }
                System.out.println(ANSI_YELLOW + "└── - indicates a suspected reply" + ANSI_RESET);
                System.out.println(ANSI_PURPLE + "└── - indicates a suspected forward" + ANSI_RESET);
                System.out.println("______________________________________________________");

                for (mail s : srtM) {
                    if (s.flag) {
                        continue;
                    }

                    boolean[] flag = new boolean[s.replies.size() + 1];
                    Arrays.fill(flag, true);

                    printNTree(s, flag, 0, false);
                    s.flag = true;
                    System.out.println();
                }
            }
            case "4" -> {
                System.out.println("Enter the file name for the first email:");
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Enter the file name for the first email.");
                chooser.showOpenDialog(null);
                String fileName = chooser.getSelectedFile().toPath().toString();
                System.out.println("______________________________________________________");
                printAttributes(fileName);
                System.out.println("______________________________________________________");

                System.out.println("Enter the file name for the second email:");
                JFileChooser chooser2 = new JFileChooser();
                chooser2.setDialogTitle("Enter the file name for the second email.");
                chooser2.showOpenDialog(null);
                String fileName2 = chooser2.getSelectedFile().toPath().toString();
                System.out.println("______________________________________________________");
                printAttributes(fileName2);
                
                System.out.println("______________________________________________________");
                System.out.println("(i.e. A depth/degree/level of 1 indicates a direct reply.)");
                checkDescendant(fileName,fileName2);
            }
            case "999" -> {
                String quotedOut = getQuotedReply("M/mail8.txt");
                System.out.println(quotedOut);
            }
            default -> throw new IllegalStateException("Unexpected value: " + task);
        }
        System.out.println("______________________________________________________");
    }
}