import javax.swing.*;
import java.io.File;    // Import File class
import java.io.FileNotFoundException;   // Import this class to handle errors
import java.util.*;
import java.text.SimpleDateFormat; // Import this class to format string dates
import java.text.ParseException; // Import class for parse exception

class mail {
    public mail(String n, Date d, ArrayList<String> t, String f) {
        name = n;
        date = d;
        to = t;
        from = f;
        replies = new ArrayList<>();
    }
    public String name;
    public Date date;
    public ArrayList<String> to;
    public String from;
    public ArrayList<mail> replies;
}

public class Main {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";

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

        // Printing out output
        /*
         * The second string begins after 40 characters. The dash means that the
         * first string is left-justified.
         */
        String format = "%-20s%s%n";

        System.out.printf(format, "The From Address: ", fromAddress);
        System.out.printf(format, "The Date: ", date);

        StringBuilder output = new StringBuilder();


        for (int k = 0; k < S.size(); k++) {
            //System.out.print(S.get(k));
            output.append(S.get(k));
            if (k != S.size() - 1) {
                //System.out.print(", ");
                output.append(", ");
            }
        }

        System.out.printf(format, "The To Address: ", output);
        System.out.println();
    }

    private static void checkReply(String f1, String f2) {
        String fromAddress = getFromAddress(f1);
        Date date = getDate(f1);
        ArrayList<String> S = getToAddress(f1);

        String fromAddress2 = getFromAddress(f2);
        Date date2 = getDate(f2);
        ArrayList<String> S2 = getToAddress(f2);

        System.out.println("=================================================");
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

        if (date2.compareTo(date) > 0) {
            System.out.println(ANSI_GREEN + "✓ The date from the corresponding email occurs after the first email"
                     + ANSI_RESET);
        } else {
            System.out.println(ANSI_RED + "x The date from the corresponding email does not occur " +
                    "after the first email" + ANSI_RESET);
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


        System.out.println("=================================================");
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
                        if (!data.contains("--")) {
                            //data += " ";
                            data = data.replace(">","");
                            reply.append(data);
                        }
                        else if (data.contains("--")) {
                            output = reply.toString().trim();
                            output = output.replace("  ", " ");
                            return output;
                        }
                    }
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

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("Program Starting....");
        System.out.println("=================================================");

        Scanner userInput = new Scanner(System.in); // Create a Scanner object
        System.out.println(ANSI_BLUE + "TASK" + ANSI_RESET);
        System.out.println(ANSI_GREEN + "1" + ANSI_RESET + ": Validate if an email is a reply to another email");
        System.out.println(ANSI_GREEN + "2" + ANSI_RESET + ": Graphically contextualise email communications");
        System.out.println();
        System.out.print("Enter Task: ");
        String task = userInput.nextLine();

        if (task.equals("1")) {
            System.out.println("=================================================");
            System.out.println("Enter the file name for the first email:");
            String fileName = userInput.nextLine(); // Read user input

            printAttributes(fileName);

            System.out.println("=================================================");
            System.out.println("Enter the file name for the second email:");
            String fileName2 = userInput.nextLine(); // Read user input
            System.out.println("=================================================");

            printAttributes(fileName2);

            checkReply(fileName, fileName2);

            System.out.println(getSentMessage(fileName));
            System.out.println(getQuotedReply(fileName2));
        }
        else if(task.equals("2")) {
            System.out.println("=================================================");
            System.out.println("Select email files:");

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select email files.");
            chooser.setMultiSelectionEnabled(true);

            // Show the dialog; wait until dialog is closed
            chooser.showOpenDialog(null);

            // Retrieve the selected files.
            File[] files = chooser.getSelectedFiles();
            ArrayList<mail> fls = new ArrayList<>();

            // Create mail objects from all the selected files for querying
            ArrayList<String> t;
            String path, n, f;
            Date d;
            for (File fn : files) {
                path = fn.toPath().toString();
                n = fn.getName();
                d = getDate(path);
                t = getToAddress(path);
                f = getFromAddress(path);
                fls.add(new mail(n,d,t,f));
            }

            // Bubble sort mail list fls
            ArrayList<mail> srtM = bubbleSort(fls);

            for (mail m : srtM) {
                System.out.println(m.name + ", " + m.date);
                for (mail l : srtM) {
                    for (String toAds : l.to) {
                        // check if mail l addresses mail m, and mail l occurs afterwards mail m
                        if (m.from.equals(toAds) && l.date.compareTo(m.date) > 0) {
                            m.replies.add(l);
                            System.out.println("\t └── " + l.name + ", " + l.date);
                        }
                    }
                }
                System.out.println();
            }

        }

    }
}