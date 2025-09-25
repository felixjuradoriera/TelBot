import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class MyAmazingBot implements LongPollingSingleThreadUpdateConsumer  {

	
	private TelegramClient telegramClient = new OkHttpTelegramClient("7380837153:AAHMQFIyGwO-FSwq9DvpQjnH4JroSy9tOSs");

	 @Override
	    public void consume(Update update) {
	        try {
	            if (update.hasMessage() && update.getMessage().hasText()) {
	                String text = update.getMessage().getText();

	                if ("/start".equals(text)) {
	                    // Crear botones inline usando InlineKeyboardRow
	                    InlineKeyboardButton btnSi = InlineKeyboardButton.builder()
	                            .text("✅ Sí")
	                            .callbackData("CONTINUAR")
	                            .build();

	                    InlineKeyboardButton btnNo = InlineKeyboardButton.builder()
	                            .text("❌ No")
	                            .callbackData("CANCELAR")
	                            .build();

	                    InlineKeyboardRow row = new InlineKeyboardRow();
	                    row.add(btnSi);
	                    row.add(btnNo);

	                    List<InlineKeyboardRow> keyboard = new ArrayList<>();
	                    keyboard.add(row);

	                    InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
	                            .keyboard(keyboard) // 👈 Ahora sí compila
	                            .build();

	                    SendMessage message = SendMessage.builder()
	                            .chatId(update.getMessage().getChatId().toString())
	                            .text("¿Quieres continuar?")
	                            .replyMarkup(markup)
	                            .build();

	                    telegramClient.execute(message);
	                }
	            }

	            // Manejar pulsación de botones
	            if (update.hasCallbackQuery()) {
	                String data = update.getCallbackQuery().getData();
	                Long chatId = update.getCallbackQuery().getMessage().getChatId();

	                String responseText;
	                if ("CONTINUAR".equals(data)) {
	                    responseText = "Has elegido continuar 🚀";
	                } else if ("CANCELAR".equals(data)) {
	                    responseText = "Has cancelado ❌";
	                } else {
	                    responseText = "Opción no reconocida.";
	                }

	                SendMessage response = SendMessage.builder()
	                        .chatId(chatId.toString())
	                        .text(responseText)
	                        .build();

	                telegramClient.execute(response);
	            }

	        } catch (TelegramApiException e) {
	            e.printStackTrace();
	        }
	    }
	
	
    
	
	
	
	@Override
    public void consume(List<Update> updates) {
        // Reutilizamos el otro método
        for (Update update : updates) {
            consume(update);
        }
    }

	
}