import javax.swing.*;
import java.io.File;    // Import File class
import java.io.FileNotFoundException;   // Import this class to handle errors
import java.util.*;
import java.text.SimpleDateFormat; // Import this class to format string dates
import java.text.ParseException; // Import class for parse exception

class mail {
    public mail(String n, Date d, String t, String f,
                String sub, String sM, String qM) {
        name = n;
        date = d;
        replyToId = t;
        messageId = f;
        replies = new ArrayList<>();
        flag = false;
        subject = sub;
        sentMsg = sM;
        qtdMsg = qM;
    }
    public String name;
    public Date date;
    public String replyToId;
    public String messageId;
    public ArrayList<mail> replies;
    public String subject;
    public boolean flag;
    public String sentMsg;
    public String qtdMsg;
    public double similarity;
}

public class Main {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";
    //public static final String ANSI_PURPLE = "\u001b[35m";
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
                    SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");
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
        String[] temp;
        ArrayList<String> sentTos = new ArrayList<>();

        try {
            File myObj = new File(fName);
            Scanner myReader = new Scanner(myObj);

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

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
                    break;
                }
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

        System.out.printf(format, ANSI_BOLD + "The From Address: \t" + ANSI_RESET, fromAddress);
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

        System.out.printf(format, ANSI_BOLD + "The To Address: \t" + ANSI_RESET, output);
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
                               && !data.contains("wrote:")) {
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

        String str = String.format("%1$tB %1$td, %1$tY",x.date);
        String str2 = String.format("%.2f", x.similarity);

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
            System.out.println(x.name + ", " + str); //replaced x.date
        }
        else if (isLast) {
            //System.out.print("├──--- " +  x.name + ", " + x.date + '\n');
            System.out.print("└── " +  x.name + ", " + ANSI_BLUE + str  + ANSI_RESET + ", Quoted Similarity: "
                    + ANSI_GREEN + str2 + "%" + ANSI_RESET + '\n');
            x.flag = true;
            if (depth < flag.length) {
                flag[depth] = false;
            }
        }
        else {
            //System.out.print("├──--- " +  x.name + ", " + x.date + '\n');
            System.out.print("└── " +  x.name + ", " + ANSI_BLUE + str  + ANSI_RESET  + ", Quoted Similarity: "
                    + ANSI_GREEN + str2 + "%" + ANSI_RESET + '\n');
            x.flag = true;
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
                ": Graphically contextualise email communications");
        System.out.println(ANSI_GREEN + ANSI_BOLD + "4" + ANSI_RESET +
                ": Validate if an email is a descendent to another email and to what degree");
        System.out.println("______________________________________________________");
        System.out.print("Enter Task: ");
        String task = userInput.nextLine();

        System.out.println("______________________________________________________");
        switch (task) {
            case "1" -> {
                System.out.println("Enter the file name for the first email:");
                String fileName = userInput.nextLine(); // Read user input
                System.out.println("______________________________________________________");
                printAttributes(fileName);
                System.out.println("______________________________________________________");
                System.out.println("Enter the file name for the second email:");
                String fileName2 = userInput.nextLine(); // Read user input
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
                ArrayList<mail> fls = new ArrayList<>();

                // Create mail objects from all the selected files for querying
                String path, n,t, f, sub, sM, qM;
                Date d;
                for (File fn : files) {
                    path = fn.toPath().toString();
                    n = fn.getName();
                    d = getDate(path);
                    t = getInReplyTo(path);
                    f = getMessageID(path);
                    sub = getSubject(path);
                    sM = getSentMessage(path);
                    qM = getQuotedReply(path);

                    fls.add(new mail(n, d, t, f, sub, sM, qM));
                }

                // Bubble sort mail list fls
                ArrayList<mail> srtM = bubbleSort(fls);
                boolean toValid, validDate, validSubject, validQuote;
                double value;
                for (mail mail : srtM) {
                    validQuote = false;

                    for (mail l : srtM) {
                        // check if mail l is a reply
                        toValid = mail.messageId.equals(l.replyToId);
                        validDate = l.date.compareTo(mail.date) > 0;
                        validSubject = mail.subject.contains(l.subject) || l.subject.contains(mail.subject);
                        // For program efficiency sake
                        if (toValid & validDate & validSubject) {
                            value = findSimilarity(mail.sentMsg, l.qtdMsg);
                            if (value >= 0.8) validQuote = true;
                            l.similarity = value * 100;

                            if (validQuote) {
                                mail.replies.add(l);
                            }
                        }
                    }
                }
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
                System.out.println("File name for the first email:");
                String fileN = userInput.nextLine(); // Read user input
                printAttributes(fileN);
                System.out.println("______________________________________________________");
                System.out.println("File name for the second email:");
                String fileN2 = userInput.nextLine(); // Read user input
                System.out.println("______________________________________________________");
                printAttributes(fileN2);
                System.out.println("______________________________________________________");
                System.out.println("(i.e. A depth/degree/level of 1 indicates a direct reply.)");
                checkDescendant(fileN,fileN2);
            }
            case "999" -> {
                String quotedOut = getQuotedReply("M/mail8.txt");
                System.out.println(quotedOut);
            }
        }
        System.out.println("______________________________________________________");
    }
}