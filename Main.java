import java.io.File;    // Import File class
import java.io.FileNotFoundException;   // Import this class to handle errors
import java.util.Scanner;   // Import the Scanner class to read text files
import java.util.ArrayList; // Import the ArrayList class

public class Main {
    public static void main(String args[]) {
        System.out.println("=================================================");
        System.out.println("Program Starting....");
        System.out.println("=================================================");

        Scanner userInput = new Scanner(System.in); // Create a Scanner object
        System.out.println("Enter email text file name, eg. \'email1.txt\':");
        String fileName = userInput.nextLine(); // Read user input
        System.out.println("=================================================");

        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);

            String fromAddress, toAddress, rawDate;
            fromAddress = toAddress = rawDate = "";
            String[] temp, temp2;
            ArrayList<String> sentTos = new ArrayList<String>();

            // Reading and processing the input
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.matches("From:.*")) {
                    if (data.contains("<")) {
                        temp = data.split("<");
                        fromAddress = temp[1].replace(">", "").trim();
                    }
                    else {
                        temp = data.split(" ");
                        fromAddress = temp[1].trim();
                    }

                }
                temp = null;
                if(data.matches("Date:.*")) {
                    rawDate = data.substring(5,data.length).trim();
                }
                
                temp = null;
                if (data.matches("To:.*")) {
                    if (data.contains(",")) {
                        temp2 = data.split(",");
                        for (int i = 0; i < temp2.length; i++) {
                            temp = temp2[i].split("<");
                            if (1 < temp.length) {
                                sentTos.add(temp[1].replace(">", "").trim());
                            }
                        }
                        temp2 = null;
                        Boolean loop = true;
                        while (myReader.hasNextLine() && loop) {
                            String secondaryData = myReader.nextLine();
                            if (secondaryData.contains(",")) {
                                temp2 = secondaryData.split(",");
                                for (int i = 0; i < temp2.length; i++) {
                                    temp = temp2[i].split("<");
                                    if (1 < temp.length) {
                                        sentTos.add(temp[1].replace(">", "").trim());
                                    }
                                }
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

            // Printing out output
            System.out.println("The From Address: " + fromAddress);
            System.out.println("The Date: " + rawDate);
            System.out.print("The To Address: ");
            for (int k = 0; k < sentTos.size(); k++) {
                System.out.print(sentTos.get(k));
                if (k != sentTos.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
            myReader.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("An error occured.");
            e.printStackTrace();
        }

    }
}