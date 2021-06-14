import java.util.Scanner;
/*
 * Game class containing all methods for playing a game of blackjack
 * playGame begins the game
 * constructor can be provided with custom values to change the game experience
 */
public class Game
{
    // class variables
    private int minBet, maxBet, numChips;
	private boolean allowBetting;
	private Deck drawDeck, discardDeck;
    private Scanner kbReader;


    // constructor
    // can be sent custom values for different game experience
    public Game(int totalNumOfDecks, boolean allowBetting, int startingAmount, int minBet, int maxBet)
    {
        this.minBet = minBet;
        this.maxBet = maxBet;
        numChips = startingAmount;

        this.allowBetting = allowBetting;

        drawDeck = new Deck(totalNumOfDecks);
        discardDeck = new Deck();

        kbReader = new Scanner(System.in);
    }


    // run this method to start playing blackjack
	public void playGame()
    {
        int round = 1;
        drawDeck.shuffle();
        // run infinitely if betting is disabled, else until player runs out of chips
        while (numChips >= minBet || allowBetting == false)
        {
            playRound(round);
            round++;

            // player is out of chips; exit program
            if (allowBetting == true && numChips < minBet)
            {
                System.out.println("Unfortunately, you've run out of chips. Please come again!");
                System.out.print("Press enter to exit the program. ");
            }
            // prompt user for input before moving on to next round to give them time to read
            else
                System.out.print("Press enter to continue to the next round. ");
            // newline characters are cleared by the first nextLine()
            // input is waited for by the second newLine()
            kbReader.nextLine();
            kbReader.nextLine();
        }
    }


    // plays a single round of blackjack
    // master method
    // could use some tuning up
	private void playRound(int round)
    {
        System.out.format("==========================================%n" + "Round %d%n%n", round);
        // get bet for current round if betting is enabled
        int bettingAmount = 0;
        if (allowBetting == true)
        {
            bettingAmount = getBettingAmount(minBet, Math.min(maxBet, numChips));
            numChips -= bettingAmount;
            System.out.format("You bet: %d%n" + "Chips remaining: %d%n%n", bettingAmount, numChips);
        }

        // create hands for player and dealer
        Hand playerHand = new Hand();
        Hand dealerHand = new Hand();
        // deal them each two cards
        // second of dealer's cards is hidden until the player finishes their turn
        hit(playerHand, true, false);
        hit(dealerHand, false, false);
        hit(playerHand, true, false);
        hit(dealerHand, false, true);

        //display number of cards in deck, reset deck if it is half empty
        System.out.format("%nDeck: %d cards%n", drawDeck.getCards().size());
        // returns true if current deck size < 0.5 * its initial size
        if (drawDeck.isLowOnCards() == true)
        {
            drawDeck.resetDeck(discardDeck);
            System.out.format("Half of deck used; shuffling...%n%n");
        }
        else
            System.out.println();

        // display the player's hand
        displayHand(playerHand, true);
        // if the player has a natural blackjack, announce it
        if (playerHand.hasBlackjack() == true)
        {
            System.out.format("Blackjack!%n%n");

            // reveal dealer's second card
            displayHand(dealerHand, false);
            revealFacedownCard(dealerHand);

            // if the dealer also has a blackjack, announce it
            if (dealerHand.hasBlackjack() == true)
                System.out.format("%nBlackjack!%n%n");

            // player has blackjack, dealer doesn't; pay out 3:2
            else
                if (allowBetting == true)
                    numChips += (int)Math.ceil(bettingAmount * 2.5);
            // print winner message
            int bias = calculateWinnerBias(playerHand, dealerHand);
            printWinner(bias);

            // start new round
            return;
        }

        // display dealer's hand
        displayHand(dealerHand, false);

        // should player input continue to be handed?
        boolean allowInput = true;
        // should the dealer's turn be skipped after the player's?
        boolean skipDealerTurn = false;
        // is the player allowed to take insurance currently?
        boolean canTakeInsurance;
        // disable if betting is not allowed
        if (allowBetting == true)
            canTakeInsurance = true;
        else
            canTakeInsurance = false;

        // get player input
        // dynamically change options that player can select according to the status of their hand
        while (allowInput == true)
        {
            // stores the number that the user will press to select a given option
            int keyCount = 3;
            
            // double downs
            int doubleDownKey = getKey(keyCount, numChips >= bettingAmount || allowBetting == false);
            if (doubleDownKey != -1)
                keyCount++;

            // taking insurance
            int insuranceKey = getKey(keyCount, canTakeInsurance == true && dealerHand.getCardAtIndex(0).isAce() == true);
            if (insuranceKey != -1)
                keyCount++;

            // surrendering
            int surrenderKey = getKey(keyCount, insuranceKey == -1);
            if (surrenderKey != -1)
                keyCount++;

            // print options for player to choose from
            displayOptions(doubleDownKey, insuranceKey, surrenderKey);
            // since surrenderKey was last one updated, it has the greatest value
            int playerInput = getPlayerInputAsInt(0, surrenderKey);

            System.out.format("============================%n");

            // if player bets correctly on dealer getting blackjack,
            //   skip rest of commands and print special insurance info
            // otherwise, continue as normal
            boolean continueRoundAfterInsurance = true;

            // perform action based on which input player chose
            switch (playerInput)
            {
                // hit
                case 1:
                    hit(playerHand, true, false);
                    System.out.println();
                    break;
                // stand
                case 2:
                    allowInput = false;
                    break;
                // exit game
                case 0:
                    exitGame();
                    break;
                // non-constant values; compare with if-else
                default:
                    // double down
                    if (playerInput == doubleDownKey)
                    {
                        if (allowBetting == true)
                            bettingAmount = doubleDown(playerHand, bettingAmount);
                        else
                            hit(playerHand, true, false);
                        allowInput = false;
                        System.out.println();
                    }
                    // take insurance
                    else if (playerInput == insuranceKey)
                        continueRoundAfterInsurance = takeInsurance(bettingAmount, dealerHand);
                    // surrender
                    else if (playerInput == surrenderKey)
                    {
                        System.out.format("Hand surrendered, partial bet returned%n%n");
                        if (allowBetting == true)
                            numChips += (int)Math.ceil(bettingAmount / 2.0);
                        return;
                    }
            }
            // end early since player guessed right on insurance
            if (continueRoundAfterInsurance == false)
            {
                displayHand(dealerHand, false);
                System.out.format("Blackjack!%n%n");
                System.out.format("Result: Bet lost, insurance paid out%n%n");
                return;
            }

            // show player hand
            displayHand(playerHand, true);
            // disable insurance since player has made their first input by now
            canTakeInsurance = false;

            // if player has bust or blackjack, announce it
            if (playerHand.hasBust())
            {
                allowInput = false;
                skipDealerTurn = true;
                System.out.format("Bust!%n%n");
            }
            else if (playerHand.hasBlackjack())
                System.out.format("Blackjack!%n%n");
            // display dealer's hand
            displayHand(dealerHand, false);
        }
        // exited player's input loop

        // skip dealer's turn if true (used if player busts)
        // otherwise, play dealer's turn out like normal
        if (skipDealerTurn == false)
        {
            dealerTurn(dealerHand);

            if (dealerHand.hasBlackjack())
                System.out.format("Blackjack!%n%n");
            else if (dealerHand.hasBust())
                System.out.format("Bust!%n%n");
        }

        // display round result message and start new round
        int bias = calculateWinnerBias(playerHand, dealerHand);
        winChips(bias, bettingAmount);
        printWinner(bias);
    }



    // prompt user for int betting amount
    // must provide minimum and maximum values
    private int getBettingAmount(int min, int max)
    {
        System.out.format("How much will you bet? (Chips: %d)%n", numChips);

        int bettingAmount = getPlayerInputAsInt(min, max);

        return bettingAmount;
    }


    // prompt user for int input
    // loop until valid input is received
    // must provide minimum and maximum values
    private int getPlayerInputAsInt(int min, int max)
    {
        int playerInput;

        while (true)
        {
            try
            {
                playerInput = Integer.parseInt(kbReader.next());
                
                if (playerInput >= min && playerInput <= max)
                {
                    System.out.format("%n");
                    return playerInput;
                }
                else
                    System.out.format("Invalid input. Number must be an integer between %d and %d, inclusive.%n", min, max);
            }
            catch (NumberFormatException exception)
            {
                System.out.format("Invalid input. Number must be an integer between %d and %d, inclusive.%n", min, max);
            }
        }
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


    // print winner message based on winner int
    private void printWinner(int winnerBias)
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
                result = "Push, no winner";
        }

        System.out.format("Result: %s%n%n", result);
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


    // display the options provided to the player as they play
    private void displayOptions(int doubleDownKey, int insuranceKey, int surrenderKey)
    {
        // hit/stand always available
        System.out.format("What will you do?%n");
        System.out.println("    [1]  Hit");
        System.out.println("    [2]  Stand");

        // double down, insurance, surrender are situational
        if (doubleDownKey != -1)
            System.out.format("    [%d]  Double down%n", doubleDownKey);
        if (insuranceKey != -1)
            System.out.format("    [%d]  Take insurance%n", insuranceKey);
        if (surrenderKey != -1)
            System.out.format("    [%d]  Surrender%n", surrenderKey);

        // exit game is always available
        System.out.println("    [0]  Exit game");
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


    // function to perform double down for player
    // double the bet, end player's input after they hit
    // returns new amount of bet
    private int doubleDown(Hand playerHand, int bettingAmount)
    {
        numChips -= bettingAmount;
        bettingAmount *= 2;
        System.out.format("New bet: %d" + "%nChips remaining: %d%n%n", bettingAmount, numChips);
        
        hit(playerHand, true, false);

        return bettingAmount;
    }


    // function to perform insurance bet for player
    // if the player bets successfully, they lose their bet but receive their insurance 2:1
    // if player bets unsuccessfully, they keep their original bigger bet but lose their insurance bet
    // returns true to continue round if dealer doesn't have blackjack
    // returns false to end round early if dealer has a blackjack
    private boolean takeInsurance(int originalBet, Hand dealerHand)
    {
        int insuranceAmount = getBettingAmount(1, (int)Math.ceil(originalBet / 2.0));
        numChips -= insuranceAmount;
        System.out.format("You bet: %d%n" + "Chips remaining: %d%n%n", insuranceAmount, numChips);
        
        if (dealerHand.hasBlackjack() == true)
        {
            numChips += 3 * insuranceAmount;
            return false;
        }
        else
        {
            System.out.format("The dealer does not have a blackjack.%n%n");
            return true;
        }
    }


    // function to exit the game
    // is always available (except when placing bets)
	private void exitGame()
    {
        System.out.format("Thanks for playing!%n");
        System.exit(0);
    }
}