import java.util.Stack;
import java.util.ArrayList;
import java.util.Collections;
/*
 * Deck class for representing a stack of cards
 * Mixes multiple decks of cards together, 6 by default (defined through Blackjack.java file)
 * Cards are pulled off the top of the stack
 */
public class Deck
{
    // Class variables
    // Stack of card objects
    private Stack<Card> cards;
    // initial size of Deck
    // used to reset the deck when it is low on cards
    private int initialSize;


    // Constructor for empty Deck
    // Used with discard pile
    public Deck()
    {
        cards = new Stack<Card>();
        initialSize = 0;
    }


    // Constructor for Deck with multiple decks of cards
    // Used with draw pile
    public Deck(int totalNumOfDecks)
    {
        cards = new Stack<Card>();

        // 13 different card ranks
        String[] cardRanks = {"Ace", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Jack", "Queen", "King"};
        // 4 different card suits
        String[] cardSuits = {"hearts", "diamonds", "clubs", "spades"};

        // Loop through all ranks, suits, and values 1-13 for the different cards in the deck
        // Repeat for each deck
        int value;
        for (int deckNum = 0; deckNum < totalNumOfDecks; deckNum++)
        {
            value = 1;

            for (String rank : cardRanks)
            {
                for (String suit : cardSuits)
                    cards.push(new Card(rank, suit, value));
                if (value != 10)
                    value++;
            }
        }

        // Set initial size for checking when the deck is low on cards
        initialSize = cards.size();
    }


    // Card setters and getters
    public Stack<Card> getCards() {return cards;}
    public void setCards(Stack<Card> obj) {cards = obj;}


    // Shuffle the Card Stack
	public void shuffle()
    {
        Collections.shuffle(cards);
    }


    // Insert cards from discard deck back into draw deck and shuffle
    public void resetDeck(Deck discardDeck)
    {
        transferCards(discardDeck);
        shuffle();
    }


    // If deck is at least half empty, shuffle it
    public boolean isLowOnCards()
    {
        return cards.size() < initialSize * 0.5;
    }


    // Take card from top of stack
	public Card drawCard()
    {
        return cards.pop();
    }


    // Add card to stack
    public void addCard(Card c)
    {
        cards.push(c);
    }


    // Take cards from the source deck, add them to current deck, and empty the source deck
	public void transferCards(Deck source)
    {
        cards.addAll(source.getCards());
        // create new empty stack
        source.setCards(new Stack<Card>());
    }


    // Take cards from the source hand, add them to current deck, and empty the source hand
    public void transferCards(Hand source)
    {
        cards.addAll(source.getCards());
        // create new empty list
        source.setCards(new ArrayList<Card>());
    }
}