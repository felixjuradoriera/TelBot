import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import conf.Configuracion;

public class Main {
	
			
	   public static void main(String[] args) {
	       LogTimestamp.instalar();
	       try {
	                    
	           TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
	           botsApplication.registerBot(Configuracion.BOT_TOKEN, new BotConfiguracion());

	           // Heartbeat de readiness a HealthChecks (cada 5 min)
	           HeartbeatTelBot.iniciar();
	         
	       } catch (Exception e) {
	           e.printStackTrace();
	       }
	   }
	}