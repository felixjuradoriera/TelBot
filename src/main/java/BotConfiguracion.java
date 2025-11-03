import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import conf.Configuracion;
import dto.AlertaExclusion;
import dto.ConfAlerta;
import dto.Event;
import dto.MenuOpcion;
import dto.Odd;
import dto.User;
import service.NinjaService;
import telegram.TelegramSender;
import utils.AlertaExclusionCSVUtils;
import utils.AlertasFactory;
import utils.ConfAlertasCSVUtils;
import utils.OddUtils;
import utils.OddsCSVUtils;
import utils.UsersUtils;

public class BotConfiguracion implements LongPollingSingleThreadUpdateConsumer  {

	
	 
	
	private static final String CSV_USERS = "C:"+ File.separator +"BOT" + File.separator +"CONF"+File.separator+ "users.csv";
	private static final String CSV_EXCLUDE_ALERTS = "C:"+ File.separator +"BOT" + File.separator +"CONF"+File.separator+ "alertasExclusiones.csv";
	
	
	 
	protected static HashMap<Long, ConfAlerta> confAlertas;
	protected static HashMap<Long, Odd> entradasTemp;
	protected static HashMap<Long, ArrayList<Odd>> entradas;
	protected static HashMap<Long, String> estados;
	
	public BotConfiguracion() throws IOException {
	
		confAlertas= new HashMap<>();
		
		confAlertas=ConfAlertasCSVUtils.loadFromCSV();
		
		estados= new HashMap<>();
		
		entradasTemp= new HashMap<>();
		entradas= new HashMap<>();
		
		List<User> users=UsersUtils.readUsers();
		
		for (User user : users) {
			ArrayList<Odd> entradasUsuario=OddsCSVUtils.leerCSV(Configuracion.CSV_FILE_ENTRADAS + user.getChatId() + "_entradas.csv");
			ArrayList<Odd> cierresUsuario=OddsCSVUtils.leerCSV(Configuracion.CSV_FILE_ENTRADAS + user.getChatId() + "_cierres.csv");
			
			ArrayList<Odd> cierresApuesta= new ArrayList<Odd>();
			
			for (Odd odd : entradasUsuario) {
				for (Odd odd1 : cierresUsuario) {
					if(odd.getIdOdd().longValue()==odd1.getIdOdd()) {
						cierresApuesta.add(odd1);
					}
				}
				
				odd.setCierres(cierresApuesta);
				cierresApuesta= new ArrayList<Odd>();
				
				
				odd=BotService.recalcularCierresParciales(odd);
				System.out.println("ODD RECALCULADO");
			}
								
			
			entradas.put(user.getChatId(), entradasUsuario);
		}
		
		
		System.out.println("CARGA INICIAL COMPLETADA");
		
		
	}


	@Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
          
			String estadoUsuario = estados.get(chatId);
			if (estadoUsuario == null) {
				estadoUsuario = Estados.INICIAL;
				estados.put(chatId, estadoUsuario);
			}

			switch (estadoUsuario) {
			case Estados.INICIAL:
				switch (text) {
				case "/activar_alertas":
					BotService.activarAlertas(update, chatId);
					break;
				case "/desactivar_alertas":
					BotService.desactivarAlertas(update, chatId);
					break;
				case "/conf_alertas":
					BotService.configurarAlertas(update, chatId);
					break;
				case "/dame_alertas":
					BotService.dameAlertas(update, chatId);
					break;
				case "/mis_entradas":
					BotService.verMisEntradas(update, chatId);
					break;
				default:
					if(BotService.buscarEntrada(update, chatId, text)) {
						estados.put(chatId, Estados.INICIAL);
					} else {
						BotService.sendMessage(chatId, "Comando desconocido o entrada no encontrada");
						estados.put(chatId, Estados.INICIAL);
					}
					
					
					break;
				}				
				break;
			case Estados.CONFALERTA1:
				BotService.confAlertas1(update, chatId, text);
				break;
			case Estados.CONFALERTA2:
				BotService.confAlertas2(update, chatId, text);
				break;
			case Estados.BUSCA_ALERTA1:
				BotService.BuscaAlerta1(update, chatId, text);
				break;
			case Estados.ENTRAR_2:
				BotService.entrar2(update, chatId, text);
				break;
			case Estados.ENTRAR_11:
				BotService.entrar11(update, chatId, text);
				break;
			case Estados.ENTRAR_12:
				BotService.entrar12(update, chatId, text);
				break;
			case Estados.EARLY2:
				BotService.early2(update, chatId, text);
				break;
			case Estados.EARLY3:
				BotService.early3(update, chatId, text);
				break;
			default:
				BotService.desconocido(update, chatId);
				break;
			}
	
		}
        
        

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            System.out.println(callbackData);
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String[] parts = callbackData.split("\\|");
            String opcion=parts[0];
          
            switch (opcion) {
			case "excluir":
				BotService.excluirEvento(update, chatId, opcion, parts);
				break;
			case "way":
				BotService.enviar2Way(update, chatId, opcion, parts);
				break;
			case "bus":
				BotService.buscarEventos(update, chatId, opcion, parts);
				break;
			case "entrar":
				BotService.entrar0(update, chatId, opcion, parts,false);
				break;
			case "ent1":
				BotService.entrar1(update, chatId, opcion, parts);
				break;
			case "ent2":
				BotService.entrar3(update, chatId, opcion, parts);
				break;
			case "early1":
				BotService.early1(update, chatId, opcion, parts);
				break;
			case "early4":
				BotService.early4(update, chatId, opcion, parts);
				break;

			default:
				break;
			}
         
        
        }
    }
	
	@Override
    public void consume(List<Update> updates) {
        // Reutilizamos el otro método
        for (Update update : updates) {
            consume(update);
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
	
	
	

	
}