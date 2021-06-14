/*
 * Card class for representing playing cards
 * Stores rank, suit, and value as variables, as well as whether the card's variables
 *   are visible (facedown = false) or not (facedown = true)
 */
public class Card
{
    // Class variables
    private String rank, suit;
    private int value;
    // Indicates whether card's attributes are "visible" to player
    private boolean facedown;


    // Constructor
    public Card(String rank, String suit, int value)
    {
        this.rank = rank;
        this.suit = suit;
        this.value = value;
        // By default, cards should be visible
        // Second card in dealer's hand should not be visible
        facedown = false;
    }


    // Setters and getters
    public String getRank() {return rank;}
	public void setRank(String newRank) {rank = newRank;}
	public String getSuit() {return suit;}
	public void setSuit(String newSuit) {suit = newSuit;}
	public int getValue() {return value;}
	public void setValue(int newValue) {value = newValue;}
    public boolean getFacedown() {return facedown;}
    public void setFacedown(boolean newFacedown) {facedown = newFacedown;}


    // Returns true if the card is an ace; otherwise returns false
	public boolean isAce()
    {
        return rank.equals("Ace");
    }


    // String representation of card
    public String toString()
    {
        return String.format("%s of %s", rank, suit);
    }
}