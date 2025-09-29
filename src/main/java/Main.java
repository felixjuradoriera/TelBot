import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
	   public static void main(String[] args) {
	       try {
	           String botToken = "7380837153:AAHMQFIyGwO-FSwq9DvpQjnH4JroSy9tOSs"; // PRO
	          // String botToken = "7029538813:AAH2I40DoMKEWLpVph3qrWUJ3vilGTEQABg"; //PRE
	           
	           TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
	           botsApplication.registerBot(botToken, new MyAmazingBot());
	         
	       } catch (Exception e) {
	           e.printStackTrace();
	       }
	   }
	}