import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
	
		public static String botToken = "7380837153:AAHMQFIyGwO-FSwq9DvpQjnH4JroSy9tOSs"; // PRO
		//public static String botToken = "7029538813:AAH2I40DoMKEWLpVph3qrWUJ3vilGTEQABg"; // PRE
	
	   public static void main(String[] args) {
	       try {
	                    
	           TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
	           botsApplication.registerBot(botToken, new BotConfiguracion());
	         
	       } catch (Exception e) {
	           e.printStackTrace();
	       }
	   }
	}