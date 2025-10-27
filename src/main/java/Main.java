import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import conf.Configuracion;

public class Main {
	
			
	   public static void main(String[] args) {
	       try {
	                    
	           TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
	           botsApplication.registerBot(Configuracion.BOT_TOKEN, new BotConfiguracion());
	         
	       } catch (Exception e) {
	           e.printStackTrace();
	       }
	   }
	}