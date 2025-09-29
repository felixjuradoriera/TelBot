import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class MyAmazingBot implements LongPollingSingleThreadUpdateConsumer  {

	
	private TelegramClient telegramClient = new OkHttpTelegramClient("7380837153:AAHMQFIyGwO-FSwq9DvpQjnH4JroSy9tOSs");
	private static final String CSV_USERS = "C:"+ File.separator +"BOT" + File.separator +"CONF"+File.separator+ "users.csv";
	private static final String CSV_EXCLUDE_ALERTS = "C:"+ File.separator +"BOT" + File.separator +"CONF"+File.separator+ "alertasExclusiones.csv";
	 
	
	@Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            

            if ("/activar_alertas".equals(text)) {
            	
            	User user=new User();
            	user.setChatId(chatId);
            	user.setName(update.getMessage().getChat().getUserName());
            	
            	saveUserIfNotExists(user);
            	
            	sendMessage(chatId, "Alertas 2UP activadas para el usuario");
            } else if ("/desactivar_alertas".equals(text)) {
            	
            	deleteUserByChatId(chatId.toString());
            	
            	sendMessage(chatId, "Alertas 2UP desactivadas para el usuario");
            	
            }
        }

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            System.out.println(callbackData);
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            
            String[] parts = callbackData.split("\\|");
            String marketID = parts[0];
            String sFechaPartido = parts[1];
            String evento = parts[2];
            
            AlertaExclusion alerta= new AlertaExclusion();
            alerta.setChatId(chatId);
            alerta.setMarket_id(marketID);
            alerta.setsFechaPartido(sFechaPartido);
            alerta.setEvento(evento);
            

            try {
				AlertaExclusionCSVUtils.addIfNotExists(alerta, CSV_EXCLUDE_ALERTS);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            sendMessage(chatId, "Has Excluido este evento de tus alertas");
         
        
        }
    }
	
	
	 public static void saveUserIfNotExists(User user) {
	        List<User> users = readUsers(); // leer los que ya están

	        // Verificar si ya existe ese chatId
	        boolean exists = users.stream()
	                .anyMatch(u -> u.getChatId().equals(user.getChatId()));

	        if (!exists) {
	            try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_USERS, true))) {
	                writer.write(user.getChatId() + "," + user.getName());
	                writer.newLine();
	                System.out.println("✅ Usuario agregado: " + user.getChatId());
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        } else {
	            System.out.println("ℹ️ Usuario ya existe: " + user.getChatId());
	        }
	    }

	 
	 public static void deleteUserByChatId(String chatIdToDelete) {
		    // 1. Leer todos los usuarios actuales
		    List<User> users = readUsers();

		    // 2. Filtrar fuera el usuario a borrar
		    List<User> updatedUsers = new ArrayList<>();
		    for (User u : users) {
		    	String chatId=u.getChatId().toString();
		        if (!chatId.equals(chatIdToDelete)) {
		            updatedUsers.add(u);
		        }
		    }

		    // 3. Sobrescribir el CSV con los usuarios filtrados
		    try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_USERS, false))) {
		        for (User u : updatedUsers) {
		            writer.write(u.getChatId() + "," + u.getName());
		            writer.newLine();
		        }
		        System.out.println("✅ Usuario eliminado si existía: " + chatIdToDelete);
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
	 
	 
	    public static List<User> readUsers() {
	        List<User> users = new ArrayList<>();

	        if (!Files.exists(Paths.get(CSV_USERS))) {
	            return users; // si no existe, devolvemos lista vacía
	        }

	        try (BufferedReader br = new BufferedReader(new FileReader(CSV_USERS))) {
	            String line;
	            while ((line = br.readLine()) != null) {
	                String[] parts = line.split(",");
	                if (parts.length == 2) {
	                    User user = new User();
	                    user.setChatId(Long.valueOf(parts[0].trim()));
	                    user.setName(parts[1].trim());
	                    users.add(user);
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        return users;
	    }
	
	 
	
	private void sendStartMenu(Long chatId, ArrayList<String> opciones ) {
	    SendMessage message = SendMessage.builder()
	            .chatId(chatId.toString())
	            .text("¡Bienvenido al bot, Félix! Elige una opción 👇")
	            .replyMarkup(buildKeyboard(opciones))
	            .build();

	    try {
	    	telegramClient.execute(message);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
    private void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();

        try {
        	telegramClient.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
    
    private InlineKeyboardMarkup buildKeyboard(List<String> opciones) {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        for (String opcion : opciones) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(opcion)
                    .callbackData(opcion) // aquí puedes usar la misma etiqueta como callback
                    .build();

            InlineKeyboardRow row = new InlineKeyboardRow(List.of(button));
            rows.add(row);
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }
	
	
//	    public void consumeOLD(Update update) {
//	        try {
//	            if (update.hasMessage() && update.getMessage().hasText()) {
//	                String text = update.getMessage().getText();
//
//	                if ("/start".equals(text)) {
//	                    // Crear botones inline usando InlineKeyboardRow
//	                    InlineKeyboardButton btnSi = InlineKeyboardButton.builder()
//	                            .text("✅ Sí")
//	                            .callbackData("CONTINUAR")
//	                            .build();
//
//	                    InlineKeyboardButton btnNo = InlineKeyboardButton.builder()
//	                            .text("❌ No")
//	                            .callbackData("CANCELAR")
//	                            .build();
//
//	                    InlineKeyboardRow row = new InlineKeyboardRow();
//	                    row.add(btnSi);
//	                    row.add(btnNo);
//
//	                    List<InlineKeyboardRow> keyboard = new ArrayList<>();
//	                    keyboard.add(row);
//
//	                    InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
//	                            .keyboard(keyboard) // 👈 Ahora sí compila
//	                            .build();
//
//	                    SendMessage message = SendMessage.builder()
//	                            .chatId(update.getMessage().getChatId().toString())
//	                            .text("¿Quieres continuar?")
//	                            .replyMarkup(markup)
//	                            .build();
//
//	                    telegramClient.execute(message);
//	                }
//	            }
//
//	            // Manejar pulsación de botones
//	            if (update.hasCallbackQuery()) {
//	                String data = update.getCallbackQuery().getData();
//	                Long chatId = update.getCallbackQuery().getMessage().getChatId();
//
//	                String responseText;
//	                if ("CONTINUAR".equals(data)) {
//	                    responseText = "Has elegido continuar 🚀";
//	                } else if ("CANCELAR".equals(data)) {
//	                    responseText = "Has cancelado ❌";
//	                } else {
//	                    responseText = "Opción no reconocida.";
//	                }
//
//	                SendMessage response = SendMessage.builder()
//	                        .chatId(chatId.toString())
//	                        .text(responseText)
//	                        .build();
//
//	                telegramClient.execute(response);
//	            }
//
//	        } catch (TelegramApiException e) {
//	            e.printStackTrace();
//	        }
//	    }
//	
	
    
	
	
	
	@Override
    public void consume(List<Update> updates) {
        // Reutilizamos el otro método
        for (Update update : updates) {
            consume(update);
        }
    }

	
}