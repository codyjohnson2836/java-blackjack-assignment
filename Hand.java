import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/* 
 * Hand class to represent the player's and dealer's set of drawn cards
 * Has methods for helping with blackjack such as:
 *   hasBlackjack  - returns true if the hand is a blackjack, otherwise false
 *   hasBust       - returns true if the hand is a bust, otherwise false
 *   getSumOfCards - returns the int sum of the values of all Cards in the hand
 *   displayCards  - displays the Cards one after the other, indented
 */
public class Hand implements Serializable
{
    // Class variable
    // Holds List of Card objects
    private ArrayList<Card> cards;
    // default serialVersion ID
    private static final long serialVersionUID = 1L;


    // Constructor for empty hand
    public Hand()
    {
        cards = new ArrayList<Card>();
    }


    // Cards setter and getter
    public ArrayList<Card> getCards() {return cards;}
    public void setCards(ArrayList<Card> obj) {cards = obj;}


    // Returns the sum closest to 21
    // Will return a value greater than 21 if there are no possible values less than 21
    // Multiple values can occur if the Hand has at least one ace
    // Can optionally ignore cards where facedown = true and not include them in summation
    public int getSumOfCards(boolean ignoreFacedownCards)
    {
        // Create arraylist so that elements can be added if necessary
        ArrayList<Integer> possibleValues = new ArrayList<Integer>();
        possibleValues.add(0);

        // Loop through each card and add its value to each value in the list
        for (Card c : cards)
        {
            if (ignoreFacedownCards == false || c.getFacedown() == false)
            {   // aces will result in two separate values; therefore they need special treatment
                if (c.isAce() == true)
                    addAceValues(possibleValues);
                // add singular card's value to each value in list
                else
                    addCardValue(possibleValues, c.getValue());
                // sorts list and removes all values greater than 21, unless there is only 1 value remaining
                removeLargeValues(possibleValues);
            }
        }
        // return last element in list since the list is already sorted from removeLargeValues method
        return possibleValues.get(possibleValues.size() - 1);
    }


    // duplicates existing list of values, adding 1 to the first list and 11 to the second
    //   since aces have a value of either 1 or 11
    private void addAceValues(ArrayList<Integer> myList)
    {
        // make sure there is at least another card available to perform operation
        if (myList.size() > 0)
        {
            // extend list with its own duplicate
            myList.addAll(myList);

            // loop through all elements, add 1 if in first half of list and 11 if in second half
            for (int i = 0; i < myList.size(); i++)
            {
                if (i < myList.size() / 2)
                    myList.set(i, myList.get(i) + 1);
                else
                    myList.set(i, myList.get(i) + 11);
            }
        }
        // otherwise simply add both values of ace (1 and 11) as possible values
        else
        {
            myList.add(1);
            myList.add(11);
        }
    }


    // add value taken from a card to each element in list
    private void addCardValue(ArrayList<Integer> myList, int value)
    {
        for (int i = 0; i < myList.size(); i++)
            myList.set(i, myList.get(i) + value);
    }


    // remove all values in list greater than 21, unless doing so would leave the list empty
    private void removeLargeValues(ArrayList<Integer> myList)
    {
        // sort list to make removing values easy
        Collections.sort(myList);

        while (myList.size() > 1)
        {
            if (myList.get(myList.size() - 1) > 21)
                myList.remove(myList.size() - 1);
            else
                break;
        }
    }


    // add card to list
    public void addCard(Card c)
    {
        cards.add(c);
    }


    // remove and return first card from list
    public Card removeFirstCard()
    {
        return cards.remove(0);
    }


    // return the card at the given index
    public Card getCardAtIndex(int i)
    {
        return cards.get(i);
    }


    // display cards neatly indented
    // if ignoreFacedownCards = true, hide cards with true facedown attributes (represent them with ???)
    public void displayCards(boolean ignoreFacedownCards)
    {
        for (Card c : cards)
        {
            // normal cards
            if (ignoreFacedownCards == false || c.getFacedown() == false)
                System.out.format("    %s%n", c.toString());
            // facedown = true
            else
                System.out.format("    ???%n");
        }
        // print sum of cards after listing them
        System.out.format("    Total: %d%n%n", getSumOfCards(ignoreFacedownCards));
    }


    // do the cards sum up to a blackjack? (21)
    public boolean hasBlackjack()
    {
        return getSumOfCards(false) == 21;
    }


    // do the cards sum up to a bust? (> 21)
    public boolean hasBust()
    {
        return getSumOfCards(false) > 21;
    }
}
