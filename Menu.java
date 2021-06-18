import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Scanner;

/* 
 * Menu class for running dynamic menus (menus that change their options
 *   depending on conditions), adjusting options, and running the game
 */
public class Menu
{
    // class variables
    private static Scanner kbReader;
    private int numOfDecks, startingAmount, minBet, maxBet;
    private boolean allowBetting;


    // constructor
    public Menu()
    {
        kbReader = new Scanner(System.in);
        setDefaultOptions();
    }


    // sets all class variables (except for kbReader) to their hard-coded default values
    private void setDefaultOptions()
    {
        numOfDecks = 6;
        allowBetting = true;
        startingAmount = 100;
        minBet = 2;
        maxBet = 500;
    }


    // displays the menu, parses user input, and performs the chosen menu option
    // runs on loop unless the option "Exit program" is chosen
    public void runMenu()
    {
        boolean loopMenu = true;
        while (loopMenu)
        {
            // check if there is a saved game
            boolean fileExists = new File(System.getProperty("user.dir") + File.separator + "blackjackGameSave.dat").isFile();

            // print options for player to choose from
            displayMenu(fileExists);

            // check if save data exists; if so, allow player to select
            //   "Load saved game" and "Delete saved game" options
            int maxKey;
            if (fileExists == true)
                maxKey = 4;
            else
                maxKey = 2;

            // get player input
            int playerInput = getPlayerInputAsInt(0, maxKey);

            // perform the chosen menu action
            loopMenu = performMenuAction(playerInput, fileExists);
        }
    }
    

    // display the menu options to the user
    private void displayMenu(boolean showExtraOptions)
    {
        // print header
        System.out.println("==========================================");
        System.out.format("Main Menu%n%n");
        System.out.println("Select an option by entering its number:");
        
        // "Start new game" is always available
	    System.out.println("    [1]  Start new game");

        // "Load saved game" and "Delete saved game" only appear if there is
        //   a file in the working directory called "blackjackGameSave.dat"
        // "Options" is always available
        if (showExtraOptions == true)
        {
	        System.out.println("    [2]  Load saved game");
	        System.out.println("    [3]  Delete saved game");
            System.out.println("    [4]  Options");
        }
        else
            System.out.println("    [2]  Options");

        // "Exit program" is always available
        System.out.println("    [0]  Exit program");
    }


    // perform a menu action based on the input received from the user
    // return value specifies whether to continue looping through menus
    //   after the menu action is performed
    private boolean performMenuAction(int playerChoice, boolean showExtraOptions)
    {
        // since a save file existed in the working directory, the user
        //   was allowed to select "Load saved game" and "Delete saved game"
        if (showExtraOptions == true)
        {
            switch(playerChoice)
            {
                case 1:
                    startNewGame();
                    return true;
                case 2:
                    loadGame();
                    return true;
                case 3:
                    deleteGame();
                    return true;
                case 4:
                    runOptions();
                    return true;
                default:
                    exitProgram();
                    return false;
            }
        }
        // since a save file did not exist in the working directory, the user
        //   was not allowed to select "Load saved game" or "Delete saved game"
        else
        {
            switch(playerChoice)
            {
                case 1:
                    startNewGame();
                    return true;
                case 2:
                    runOptions();
                    return true;
                default:
                    exitProgram();
                    return false;
            }
        }
    }


    // displays options menu, parses user input, and performs the appropriate option
    private void runOptions()
    {
        // print options header
        System.out.println("==========================================");
        System.out.format("Options Menu%n%n");

        boolean loopOptions = true;
        while (loopOptions)
        {
            // display options to user
            displayOptions();

            // get player input
            int playerInput;
            if (allowBetting == true)
                playerInput = getPlayerInputAsInt(0, 6);
            else
                playerInput = getPlayerInputAsInt(0, 3);

            // perform the chosen options action
            loopOptions = performOptionsAction(playerInput);
        }
    }


    // display the options available to the user
    private void displayOptions()
    {
        // always available
        System.out.println("Select an option by entering its number:");
	    System.out.format("    [1]  %-28s%15d%n", "Number of decks to use", numOfDecks);
	    System.out.format("    [2]  %-28s%15b%n", "Allow betting", allowBetting);

        // if allowBetting is set to true, these options are displayed
        if (allowBetting == true)
        {
	        System.out.format("    [3]      %-24s%15d%n", "Starting amount", startingAmount);
            System.out.format("    [4]      %-24s%15d%n", "Minimum bet", minBet);
            System.out.format("    [5]      %-24s%15d%n", "Maximum bet", maxBet);
            System.out.println("    [6]  Restore defaults");
        }
        // otherwise, only restoring defaults is displayed
        else
            System.out.println("    [3]  Restore defaults");

        // always available
        System.out.println("    [0]  Return to menu");
    }


    // perform the action corresponding to the player's input
    // return value specifies whether to continue displaying the options menu or not
    private boolean performOptionsAction(int playerChoice)
    {
        // different options numbers were used depending on if betting was allowed or not
        if (allowBetting == true)
        {
            switch (playerChoice)
            {
                // edit value for number of decks
                case 1:
                    System.out.format("Enter a value between 1 and 8, inclusive.%n", minBet);
                    numOfDecks = getPlayerInputAsInt(1, 8);
                    return true;

                // disable betting
                case 2:
                    allowBetting = false;
                    return true;

                // edit value for starting amount of money
                case 3:
                    System.out.format("Enter a value between %d and 100000, inclusive.%n", Math.max(minBet, 1));
                    startingAmount = getPlayerInputAsInt(Math.max(minBet, 1), 100000);
                    return true;

                // edit value for minimum bet
                case 4:
                    System.out.format("Enter a value between 0 and %d, inclusive.%n", Math.min(maxBet, startingAmount));
                    minBet = getPlayerInputAsInt(0, Math.min(maxBet, startingAmount));
                    return true;

                // edit value for maximum bet
                case 5:
                    System.out.format("Enter a value between %d and 1000000, inclusive.%n", Math.max(minBet, 1));
                    maxBet = getPlayerInputAsInt(Math.max(minBet, 1), 1000000);
                    return true;

                case 6:
                    setDefaultOptions();
                    return true;

                // exit to menu was chosen, stop looping options menu
                default:
                    return false;
            }
        }
        else
        {
            switch (playerChoice)
            {
                // edit value for number of decks
                case 1:
                    System.out.format("Enter a value between 1 and 8, inclusive.%n", minBet);
                    numOfDecks = getPlayerInputAsInt(1, 8);
                    return true;

                // enable betting
                case 2:
                    allowBetting = true;
                    return true;

                case 3:
                    setDefaultOptions();
                    return true;
                    
                // exit to menu was chosen, stop looping options menu
                default:
                    return false;
            }
        }
    }


    // prompt user for int input
    // loop until valid input is received
    // must provide minimum and maximum values
    public static int getPlayerInputAsInt(int min, int max)
    {
        int playerInput;

        while (true)
        {
            try
            {
                playerInput = Integer.parseInt(kbReader.next());
                kbReader.nextLine();

                if (playerInput >= min && playerInput <= max)
                {
                    System.out.format("%n");
                    return playerInput;
                }
                else
                    System.out.format("Invalid input. Value must be an integer between %d and %d, inclusive.%n", min, max);
            }
            catch (NumberFormatException exception)
            {
                System.out.format("Invalid input. Value must be an integer between %d and %d, inclusive.%n", min, max);
            }
        }
    }


    // print message to user; actual exiting of program is done by
    //   returning "false" in performMenuAction
    private void exitProgram()
    {
        System.out.format("Thanks for playing!%n");
    }


    // create a fresh new game object with selected options and run it
    private void startNewGame()
    {
        Game gameToRun = new Game(numOfDecks, allowBetting, startingAmount, minBet, maxBet);
        gameToRun.runGame(kbReader);
    }


    // Loads user's game from file called "blackjackGameSave.dat" in the current working directory
    // Prints error to user if unsuccessful
    private void loadGame()
    {
        try
        {
            // filePath for loading the file
            String filePath = System.getProperty("user.dir") + File.separator + "blackjackGameSave.dat";
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            Object gameObject = objectIn.readObject();
            objectIn.close();
            
            // write success to user and store loaded object in gameToRun
            System.out.format("Game successfully loaded from file: %s%n%n", filePath);
            Game gameToRun = (Game)gameObject;
            gameToRun.runGame(kbReader);
        }
        // if unsuccessful, print error to user
        catch (Exception ex)
        {
            System.out.format("Load unsuccessful. Perhaps the file was damaged or no longer exists?%n%n");
            //ex.printStackTrace();
        }
    }


    // deletes user's game save file "blackjackGameSave.dat" from current working directory
    // prints error to user if unsuccessful
    private void deleteGame()
    {
        // filePath for the file to delete
        String filePath = System.getProperty("user.dir") + File.separator + "blackjackGameSave.dat";
        File saveFile = new File(filePath);

        // delete file, print success/failure message
        if (saveFile.delete())
            System.out.format("File successfully deleted: %s%n%n", filePath);
        else
            System.out.format("Deletion unsuccessful. Perhaps the file no longer exists?%n%n");
    }
}