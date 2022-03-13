import java.io.File;    // Import File class
import java.io.FileNotFoundException;   // Import this class to handle errors
import java.util.Scanner;   // Import the Scanner class to read text files
import java.util.ArrayList; // Import the ArrayList class
import java.util.Date;  // Import the Data class
import java.text.SimpleDateFormat; // Import this class to format string dates
import java.text.ParseException; // Import class for parse exception


public class Main {
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
                        parseMail(data, sentTos);

                        boolean loop = true;
                        while (myReader.hasNextLine() && loop) {
                            String secondaryData = myReader.nextLine();
                            if (secondaryData.contains(",")) {
                                parseMail(data, sentTos);
                            }
                            else if (secondaryData.contains("<")) {
                                temp = secondaryData.split("<");
                                sentTos.add(temp[1].replace(">", "").trim());
                            }
                            else {
                                if (secondaryData.contains(":")) {
                                    loop = false;
                                } else {
                                    sentTos.add(secondaryData.trim());
                                }
                            }
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

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("Program Starting....");
        System.out.println("=================================================");

        Scanner userInput = new Scanner(System.in); // Create a Scanner object
        System.out.println("Enter email text file name, eg. 'email1.txt':");
        String fileName = userInput.nextLine(); // Read user input
        System.out.println("=================================================");

        String fromAddress = getFromAddress(fileName);
        Date date = getDate(fileName);
        ArrayList<String> S = getToAddress(fileName);

        // Printing out output
        System.out.println("The From Address: " + fromAddress);
        System.out.println("The Date: " + date);
        System.out.print("The To Address: ");
        for (int k = 0; k < S.size(); k++) {
            System.out.print(S.get(k));
            if (k != S.size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.println();
    }
}