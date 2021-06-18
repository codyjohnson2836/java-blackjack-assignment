import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Scanner;

/*
 * Game class containing all methods for playing a game of blackjack
 * runGame begins the game
 * constructor can be provided with custom values to change the game experience
 */
public class Game implements Serializable
{
    // class variables
    private int minBet, maxBet, numChips, state, roundNum;
	private boolean allowBetting;
	private Deck drawDeck, discardDeck;
    private GameHelper gameHelper;
    // default serialVersion ID
    private static final long serialVersionUID = 1L;


    // constructor
    // can be sent custom values for different game experience
    public Game(int numOfDecks, boolean allowBetting, int startingAmount, int minBet, int maxBet)
    {
        drawDeck = new Deck(numOfDecks);
        drawDeck.shuffle();
        discardDeck = new Deck();
        this.allowBetting = allowBetting;
        numChips = startingAmount;
        this.minBet = minBet;
        this.maxBet = maxBet;
        roundNum = 1;
    }


    // run this method to start playing blackjack
	public void runGame(Scanner kbReader)
    {
        // run until game is manually exited by player or until the player runs out of chips
        boolean loopRounds = true;
        while (loopRounds == true && (numChips >= Math.max(minBet, 1) || allowBetting == false || state != 0))
        {
            // play the round out
            loopRounds = playRound();

            // perform after-round management
            roundNum++;
            state = 0;
            // move cards out of hands and into discard deck
            discardDeck.transferCards(gameHelper.getPlayerHand());
            discardDeck.transferCards(gameHelper.getDealerHand());

            // player is out of chips; print goodbye message
            if (allowBetting == true && numChips < minBet && loopRounds == true)
            {
                System.out.println("Unfortunately, you've run out of chips. Please come again!");
                System.out.print("Press enter to return to the menu. ");
                kbReader.nextLine();
                System.out.println();
            }
            // player has chips and game is still continuing
            // display options, parse player input, and select the corresponding player option
            else if (loopRounds == true)
            {
                boolean loopRoundOptions = true;
                while (loopRoundOptions)
                {
                    displayEndRoundOptions();

                    int playerInput = Menu.getPlayerInputAsInt(0, 2);

                    if (playerInput == 0)
                        loopRounds = false;

                    loopRoundOptions = performEndRoundAction(playerInput);
                }
            }
        }
    }


    // display options for after the round ends
    private void displayEndRoundOptions()
    {
        System.out.println("Select an option by entering its number:");
        System.out.println("    [1]  Continue");
        System.out.println("    [2]  Save game");
        System.out.println("    [0]  Exit to menu");
    }


    // perform an action based on the player's chosen option
    // returns whether to keep looping the menu or not
    private boolean performEndRoundAction(int playerChoice)
    {
        switch (playerChoice)
        {
            // if player is continuing, stop looping the after-round options
            case 1:
                return false;
            case 2:
                return saveGame(this);
            default:
                return exitToMenu();
        }
    }


    // plays a single round of blackjack
    // master method
    // returns whether to continue the game or not
	private boolean playRound()
    {
        switch (state)
        {
            // state set to 0 when player finishes a round
            // if player then saves and loads, start next round
            case 0:
                // create GameHelper object to store useful variables
                gameHelper = new GameHelper();
                // set up the round by getting the bet and drawing cards
                setUpRound();


            // state set to 1 at beginning of this section
            // if player then saves and loads, their betting amount has been saved
            //   and already removed from their chips, and cards have already been dealt
            case 1:
                state = 1;

                // display the player's hand
                displayHand(gameHelper.getPlayerHand(), true);

                // check if the player has a natural blackjack, and if so,
                //   "return" to a new round after finishing up the current round
                boolean hasNatural = checkForNatural();
                if (hasNatural == true)
                    return true;

                // display dealer's hand
                displayHand(gameHelper.getDealerHand(), false);


                // section for taking and denying insurance
                if (allowBetting == true && gameHelper.getDealerHand().getCardAtIndex(0).isAce() == true && numChips >= gameHelper.getBettingAmount() / 2)
                {
                    // display insurance options, prompt user for input, and perform insurance action based on user input
                    // loop until the player chooses an option that is not saving the game
                    int playerInput = 3;
                    boolean continueRound = true;
                    while (playerInput == 3)
                    {
                        // display options for player to choose from
                        displayInsuranceOptions();

                        // get player input
                        playerInput = Menu.getPlayerInputAsInt(0, 3);

                        // perform the action chosen by the player
                        continueRound = performInsuranceAction(playerInput);

                        // if player chose to save game, display hands again and loop
                        if (playerInput == 3)
                        {
                            displayHand(gameHelper.getPlayerHand(), true);
                            displayHand(gameHelper.getDealerHand(), false);
                        }
                    }

                    // if exit to menu was chosen, end the game
                    if (gameHelper.getEndGame() == true)
                        return false;

                    // if the dealer had a blackjack after all, move on to the next round
                    if (continueRound == false)
                        return true;
                }


            // state set to 2 at beginning of this section
            // if player then saves and loads, their betting amount has been saved
            //   and already removed from their chips, cards have already been dealt,
            //   and insurance has already been dealt with
            case 2:
                // display hands if the player has just loaded in
                if (state == 2)
                {
                    displayHand(gameHelper.getPlayerHand(), true);
                    displayHand(gameHelper.getDealerHand(), false);
                }

                // loop the player's turn until they either end it, bust, or blackjack
                state = 2;
                int turnCount = 0;
                boolean continueTurn = true;
                boolean playerBust = false;
                while (continueTurn == true)
                {
                    // stores the number that the user will press to select a given option
                    int keyCount = 3;
                    
                    // double downs
                    int doubleDownKey = getKey(keyCount, allowBetting == true && numChips >= gameHelper.getBettingAmount() && turnCount == 0);
                    if (doubleDownKey != -1)
                        keyCount++;

                    // surrendering
                    int surrenderKey = getKey(keyCount, gameHelper.getBettingAmount() > 1);
                    if (surrenderKey != -1)
                        keyCount++;

                    // print options for player to choose from
                    // keyCount will be equal to the key immediately after surrenderKey,
                    //   which will be used to set the key for saving the game
                    displayNormalOptions(doubleDownKey, surrenderKey, keyCount);

                    // get player input
                    int playerInput = Menu.getPlayerInputAsInt(0, keyCount);

                    // perform action based on player input
                    continueTurn = performNormalAction(playerInput, doubleDownKey, surrenderKey, keyCount);

                    // if player surrendered, end round
                    if (playerInput == surrenderKey)
                        return true;

                    // if player exited to menu, end game
                    if (gameHelper.getEndGame() == true)
                        return false;

                    // show player hand
                    displayHand(gameHelper.getPlayerHand(), true);

                    // if player has blackjack, display it 
                    if (gameHelper.getPlayerHand().hasBlackjack() == true)
                        System.out.format("Blackjack!%n%n");
                    // if player has bust, display it and end player turn
                    else if (gameHelper.getPlayerHand().hasBust() == true)
                    {
                        System.out.format("Bust!%n%n");
                        playerBust = true;
                        continueTurn = false;
                    }

                    // show dealer hand
                    displayHand(gameHelper.getDealerHand(), false);

                    // disable insurance only if player has chosen something other than save game
                    if (playerInput != keyCount)
                        turnCount++;
                }


                // player's turn is over
                // allow dealer to take their turn if player did not bust
                // dealer draws until the total of their cards is greater than or equal to 17
                if (playerBust == false)
                {
                    dealerTurn(gameHelper.getDealerHand());

                    // if dealer has blackjack or bust, announce it
                    if (gameHelper.getDealerHand().hasBlackjack() == true)
                        System.out.format("Blackjack!%n%n");
                    else if (gameHelper.getDealerHand().hasBust() == true)
                        System.out.format("Bust!%n%n");
                }

                // calculate int defining who won (player = 1, dealer = -1, push = 0)
                int bias = calculateWinnerBias(gameHelper.getPlayerHand(), gameHelper.getDealerHand());
                // if betting is allowed, give the player chips if they won
                if (allowBetting == true)
                    winChips(bias, gameHelper.getBettingAmount());
                // display the winner
                displayWinner(bias);
        }

        // start new round
        return true;
    }


    // get the player's betting amount and draw cards for both
    private void setUpRound()
    {
        // display header
        System.out.format("==========================================%n" + "Round %d%n%n", roundNum);

        // get bet for current round if betting is enabled
        if (allowBetting == true)
        {
            // get betting amount
            // minimum will be whatever the minimum bet was set to when the Game object was created
            // maximum will be the minimum value between the maximum bet set on Game creation and the number of chips the player has
            gameHelper.setBettingAmount(getBettingAmount(minBet, Math.min(maxBet, numChips)));
            numChips -= gameHelper.getBettingAmount();
            System.out.format("You bet: %d%n" + "Chips remaining: %d%n%n", gameHelper.getBettingAmount(), numChips);
        }

        // deal player and dealer each two cards
        // second of dealer's cards is hidden until the player finishes their turn
        hit(gameHelper.getPlayerHand(), true, false);
        hit(gameHelper.getDealerHand(), false, false);
        hit(gameHelper.getPlayerHand(), true, false);
        hit(gameHelper.getDealerHand(), false, true);

        // display number of cards in deck, reset deck if it is half empty
        System.out.format("%nDeck: %d cards%n", drawDeck.getSize());
        // returns true if current deck size < 0.5 * its initial size
        if (drawDeck.isLowOnCards() == true)
        {
            System.out.format("Over half of deck used. Cards shuffled.%n%n");
            drawDeck.resetDeck(discardDeck);
        }
        else
            System.out.println();
    }


    // check if the player has a natural blackjack and finish the round if so
    // returns whether to end the round or not
    private boolean checkForNatural()
    {
        // if the player has a natural blackjack, announce it
        if (gameHelper.getPlayerHand().hasBlackjack() == true)
        {
            System.out.format("Blackjack!%n%n");

            // reveal dealer's second card
            displayHand(gameHelper.getDealerHand(), false);
            revealFacedownCard(gameHelper.getDealerHand());

            // if the dealer also has a blackjack, announce it
            if (gameHelper.getDealerHand().hasBlackjack() == true)
                System.out.format("%nBlackjack!%n%n");

            // player has blackjack, dealer doesn't; pay out 3:2
            else
                if (allowBetting == true)
                    numChips += (int)Math.ceil(gameHelper.getBettingAmount() * 2.5);
                    
            // print winner message
            int bias = calculateWinnerBias(gameHelper.getPlayerHand(), gameHelper.getDealerHand());
            displayWinner(bias);

            return true;
        }
        // the player does not have a natural blackjack
        else
            return false;
    }


    // display options for when player must choose insurance options
    private void displayInsuranceOptions()
    {
        System.out.println("What will you do?");
        System.out.println("    [1]  Take insurance");
        System.out.println("    [2]  Deny insurance");
        System.out.println("    [3]  Save game");
        System.out.println("    [0]  Exit to menu");
    }


    // performs an insurance option based on the input the player provided
    private boolean performInsuranceAction(int playerChoice)
    {
        switch (playerChoice)
        {
            case 1:
                return takeInsurance();
            case 2:
                return denyInsurance();
            case 3:
                return saveGame(this);
            default:
                return exitToMenu();
        }
    }


    // prompt user for int betting amount
    // must provide minimum and maximum values
    private int getBettingAmount(int min, int max)
    {
        System.out.format("How much will you bet? (Chips: %d)%n", numChips);

        int bettingAmount = Menu.getPlayerInputAsInt(min, max);

        return bettingAmount;
    }


    // print card info as dealer draws them, including the new sum of the hands with the cards' values added
    private void logCard(Card c, int sum, boolean isForPlayer)
    {
        if (isForPlayer == true)
            System.out.print("The dealer draws a card for you");
        else
            System.out.print("The dealer draws a card for themself");
        if (c.getFacedown() == true)
            System.out.format(" and places it facedown.%n");
        else
            System.out.format(": %s (sum: %d)%n", c.toString(), sum);
    }


    // display the player's or dealer's hand with short header
    private void displayHand(Hand h, boolean isForPlayer)
    {
        if (isForPlayer == true)
        {
            System.out.format("Your hand:%n");
            h.displayCards(false);
        }
        else
        {
            System.out.format("Dealer's hand:%n");
            h.displayCards(true);
        }
    }


    // return an int corresponding to the winner
    // 1  = player victory
    // 0  = push (tie)
    // -1 = dealer victory
    private int calculateWinnerBias(Hand playerHand, Hand dealerHand)
    {
        if (playerHand.hasBlackjack() == true)
        {
            if (dealerHand.hasBlackjack() == true)
                return 0;
            else
                return 1;
        }
        else if (playerHand.hasBust() == true)
            return -1;
        else
        {
            if (dealerHand.hasBust() == true || dealerHand.getSumOfCards(false) < playerHand.getSumOfCards(false))
                return 1;
            else if (dealerHand.hasBlackjack() == true || dealerHand.getSumOfCards(false) > playerHand.getSumOfCards(false))
                return -1;
            else
                return 0;
        }
    }


    // grant chips to player if they won
    // pay out 1:1 if the player wins, or return their original bet if there was a push
    private void winChips(int winBias, int bettingAmount)
    {
        switch (winBias)
        {
            // player victory
            case 1:
                numChips += bettingAmount * 2;
                break;
            // push
            case 0:
                numChips += bettingAmount;
        }
    }


    // display winner message based on winner int
    private void displayWinner(int winnerBias)
    {
        String result;

        switch (winnerBias)
        {
            // player victory
            case 1:
                result = "You win!";
                break;
            // dealer victory
            case -1:
                result = "Dealer wins!";
                break;
            // push
            default:
                result = "Push! No winner.";
        }

        System.out.format("Result: %s%n%n", result);
    }


    // display the options provided to the player as they play
    private void displayNormalOptions(int doubleDownKey, int surrenderKey, int saveGameKey)
    {
        // hit/stand always available
        System.out.format("What will you do?%n");
        System.out.println("    [1]  Hit");
        System.out.println("    [2]  Stand");

        // double down and surrender are situational
        if (doubleDownKey != -1)
            System.out.format("    [%d]  Double down%n", doubleDownKey);
        if (surrenderKey != -1)
            System.out.format("    [%d]  Surrender%n", surrenderKey);

        // save game and exit to menu are always available
        System.out.format("    [%d]  Save game%n", saveGameKey);
        System.out.println("    [0]  Exit to menu");
    }


    // perform an action for a normal player turn based on which options are available and which key the player entered
    // returns whether to continue getting input or not
    private boolean performNormalAction(int playerChoice, int doubleDownKey, int surrenderKey, int saveGameKey)
    {
        switch (playerChoice)
        {
            // player chose hit
            case 1:
                boolean contAfterHit = hit(gameHelper.getPlayerHand(), true, false);
                System.out.println();
                return contAfterHit;

            // player chose stand
            case 2:
                return stand();

            // player chose exit to menu
            case 0:
                return exitToMenu();

            // non-constant values, use if-else to choose between them
            default:
                // player chose double down
                if (playerChoice == doubleDownKey)
                {
                    boolean contAfterDoubleDown = doubleDown(gameHelper.getPlayerHand());
                    System.out.println();
                    return contAfterDoubleDown;
                }

                // player chose surrender
                else if (playerChoice == surrenderKey)
                {
                    return surrender(gameHelper.getBettingAmount());
                }

                // player chose save game
                else if (playerChoice == saveGameKey)
                {
                    return saveGame(this);
                }
        }

        // necessary for compliling program, but should never be accessible
        return true;
    }


    // used when the dealer flips their card faceup
    // prints revealed card
    // assumes position of card is 1, since that is the only possible position it can have in the current program
    private void revealFacedownCard(Hand dealerHand)
    {
        dealerHand.getCardAtIndex(1).setFacedown(false);

        System.out.format("The dealer reveals their facedown card: "
                + "%s (sum: %d)%n", dealerHand.getCardAtIndex(1).toString(), dealerHand.getSumOfCards(false));
    }


    // perform dealer's turn
    // draw until card sum >= 17
    private void dealerTurn(Hand dealerHand)
    {
        revealFacedownCard(dealerHand);

        while (dealerHand.getSumOfCards(false) < 17)
            hit(dealerHand, false, false);

        System.out.println();
    }


    // get dynamic number key on keyboard that will be pressed for a given option
    // return the key to be pressed only if the option's condition evaluates to true
    private int getKey(int keyCount, boolean condition)
    {
        if (condition == true)
            return keyCount;
        else
            return -1;
    }


    // function to perform hit for player or dealer as necessary
    // adds card to hand and logs it, unless it is facedown
    // returns true so player input is still accepted
    private boolean hit(Hand h, boolean isForPlayer, boolean facedown)
    {
        Card c = drawDeck.drawCard();
        c.setFacedown(facedown);
        h.addCard(c);

        logCard(c, h.getSumOfCards(false), isForPlayer);

        return true;
    }



    // cancel player input and continue on to the dealer's turn
    private boolean stand()
    {
        return false;
    }


    // double bet, hit once, and then stand
    private boolean doubleDown(Hand playerHand)
    {
        numChips -= gameHelper.getBettingAmount();
        gameHelper.setBettingAmount(gameHelper.getBettingAmount() * 2);
        System.out.format("New bet: %d" + "%nChips remaining: %d%n%n", gameHelper.getBettingAmount(), numChips);
        
        hit(playerHand, true, false);
        return stand();
    }


    // surrender, return half of player's chips rounded up
    // is treated specially in playRound method
    // returns false so player input is no longer accepted
    private boolean surrender(int betAmount)
    {
        System.out.format("Result: Surrendered. Half of bet chips returned.%n%n");
        numChips += Math.ceil(betAmount / 2.0);
        return false;
    }


    // Saves user's game to a file called "blackjackGameSave.dat" in the current working directory
    // Prints error to user if unsuccessful
    // returns true so player input is still accepted
    public boolean saveGame(Object gameObject)
    {
        try
        {
            // filePath for saving the file
            String filePath = System.getProperty("user.dir") + File.separator + "blackjackGameSave.dat";
            FileOutputStream fileOut = new FileOutputStream(filePath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(gameObject);
            objectOut.close();
            fileOut.close();
            // write success to user
            System.out.format("Game successfully saved to file: %s%n%n", filePath);
        }
        // if unsuccessful, print error to user
        catch (Exception ex)
        {
            System.out.format("Save unsuccessful. Perhaps the application does not have permission?%n%n");
            //ex.printStackTrace();
        }

        return true;
    }


    // exit to the menu by ending the game without displaying anything until arriving at the menu
    // returns false so player input is no longer accepted
    private boolean exitToMenu()
    {
        gameHelper.setEndGame(true);
        return false;
    }


    // is run if the player takes insurance
    // player immediately bets half of their chips rounded down
    // chips are lost if no blackjack, but received 2:1 if there is a blackjack
    // returns true if the dealer does not have a blackjack to continue accepting player input
    // returns false if the dealer has a blackjack to stop accepting player input
    private boolean takeInsurance()
    {
        // remove insurance from chips
        int insuranceBet = gameHelper.getBettingAmount() / 2;
        numChips -= insuranceBet;
        System.out.format("Insurance bet: %d" + "%nChips remaining: %d%n%n", insuranceBet, numChips);

        // dealer has blackjack, pay out player
        if (gameHelper.getDealerHand().hasBlackjack() == true)
        {
            revealFacedownCard(gameHelper.getDealerHand());
            numChips += 3 * insuranceBet;
            System.out.format("Blackjack!%n%n");
            System.out.format("Result: Bet lost. Insurance paid out.%n%n");
            return false;
        }
        // dealer does not have blackjack, player loses insurance permanently, round continues
        else
        {
            System.out.format("The dealer does not have a blackjack.%n%n");
            return true;
        }
    }


    // is run if the player denies insurance
    // announce if no blackjack, end round if blackjack
    // returns true if the dealer does not have a blackjack to continue accepting player input
    // returns false if the dealer has a blackjack to stop accepting player input
    private boolean denyInsurance()
    {
        // dealer has blackjack, end round
        if (gameHelper.getDealerHand().hasBlackjack() == true)
        {
            revealFacedownCard(gameHelper.getDealerHand());
            System.out.format("Blackjack!%n%n");
            System.out.format("Result: Bet lost. Insurance paid out.%n%n");
            return false;
        }
        // dealer does not have blackjack, continue round
        else
        {
            System.out.format("The dealer does not have a blackjack.%n%n");
            return true;
        }
    }
}