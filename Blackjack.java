public class Blackjack
{
    public static void main(String[] args)
    {
        /* 
         * Main blackjack game object initialized with default values
         *   total number of decks used: 6
         *   allow betting: true
         *   starting amount of chips: 100
         *   minimum bet: 2
         *   maximum bet: 500
         */
        Game blackjack = new Game(6, true, 100, 2, 500);
        // Method for starting and playing the game
        blackjack.playGame();
    }
}