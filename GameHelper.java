import java.io.Serializable;

/*
 * GameHelper object used by Game to easily store many variables used in a round
 */
public class GameHelper implements Serializable
{
    // class variables
    private Hand playerHand, dealerHand;
    private int bettingAmount;
    private boolean endGame;
    // default serialVersion ID
    private static final long serialVersionUID = 1L;


    // constructor
    public GameHelper()
    {
        playerHand = new Hand();
        dealerHand = new Hand();
        bettingAmount = 0;
        endGame = false;
    }


    // setters and getters
    public Hand getPlayerHand() {return playerHand;}
    public void setPlayerHand(Hand newPlayerHand) {playerHand = newPlayerHand;}
    public Hand getDealerHand() {return dealerHand;}
    public void setDealerHand(Hand newDealerHand) {dealerHand = newDealerHand;}
    public int getBettingAmount() {return bettingAmount;}
    public void setBettingAmount(int newBettingAmount) {bettingAmount = newBettingAmount;}
    public boolean getEndGame() {return endGame;}
    public void setEndGame(boolean newEndGame) {endGame = newEndGame;}
}
