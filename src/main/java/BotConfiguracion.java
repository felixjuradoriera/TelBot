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

public class BotConfiguracion implements LongPollingSingleThreadUpdateConsumer  {

	
	private TelegramClient telegramClient = new OkHttpTelegramClient(Configuracion.BOT_TOKEN); 
	
	private static final String CSV_USERS = "C:"+ File.separator +"BOT" + File.separator +"CONF"+File.separator+ "users.csv";
	private static final String CSV_EXCLUDE_ALERTS = "C:"+ File.separator +"BOT" + File.separator +"CONF"+File.separator+ "alertasExclusiones.csv";
	
	
	 
	private static HashMap<Long, ConfAlerta> confAlertas;
	private static HashMap<Long, String> estados;
	
	public BotConfiguracion() throws IOException {
	
		confAlertas= new HashMap<>();
		
		confAlertas=ConfAlertasCSVUtils.loadFromCSV();
		
		estados= new HashMap<>();
		
		System.out.println("CARGA INICIAL COMPLETADA");
		
		
	}


	public class Estados {
	    public static final String INICIAL = "INICIAL";
	    public static final String CONFALERTA1 = "CONFALERTA1";
	    public static final String CONFALERTA2 = "CONFALERTA2";
	    public static final String CONFALERTA3 = "CONFALERTA3";
	    public static final String BUSCA_ALERTA1 = "BUSCALERTAS1";
	    public static final String BUSCA_ALERTA2 = "BUSCALERTAS2";
	    
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

			            
			if (estadoUsuario.equals(Estados.INICIAL)) {
				if ("/activar_alertas".equals(text)) {

					User user = new User();
					user.setChatId(chatId);
					user.setName(update.getMessage().getChat().getUserName());

					saveUserIfNotExists(user);

					sendMessage(chatId, "Alertas 2UP activadas para el usuario");

					estados.put(chatId, Estados.INICIAL);
				} else if ("/desactivar_alertas".equals(text)) {

					deleteUserByChatId(chatId.toString());

					sendMessage(chatId, "Alertas 2UP desactivadas para el usuario");

					estados.put(chatId, Estados.INICIAL);
				} else if ("/conf_alertas".equals(text)) {

					ConfAlerta conf=confAlertas.get(chatId);
					if(conf==null) {
						conf= new ConfAlerta();
						conf.setChatId(chatId);
						conf.setRatioNivel1(Double.valueOf(95));
						conf.setRatioNivel2(Double.valueOf(92));
						
						confAlertas.put(chatId, conf);
						
					}
					
					StringBuilder mens= new StringBuilder();
					mens.append("configuraciones para el usuario \n");
					mens.append("<b>Nivel 1</b> (cuotas hasta 5)-><b>" + conf.getRatioNivel1() + "% </b>\n");
					mens.append("<b>Nivel 2</b> (cuotas a partir de 5)-><b>" + conf.getRatioNivel2() + "%</b>\n");
					mens.append("<b>Cuota Mínima</b>-><b>" + conf.getCuotaMinima() + "%</b>\n");
					
					String enviar=mens.toString();
					
					sendMessage(chatId, enviar);
					
					StringBuilder mens1= new StringBuilder();
					mens1.append("Escribe nuevo ratio para el <b>nivel 1</b>\n");
					mens1.append("escribe 0 para volver a valor defecto\n");
					mens1.append("(decimales con punto y sin el signo de %)");
					
					
					sendMessage(chatId, mens1.toString());

					estados.put(chatId, Estados.CONFALERTA1);
				} else if("/dame_alertas".equals(text)) { 
				
					StringBuilder mens= new StringBuilder();
					mens.append("Introduce palabra o palabras de búsqueda que servirán para buscar el evento o eventos \n");
										
					String enviar=mens.toString();
					
					sendMessage(chatId, enviar);
					
					estados.put(chatId, Estados.BUSCA_ALERTA1);
				} else {
					estados.put(chatId, Estados.INICIAL);
				}
			} else if (estadoUsuario.equals(Estados.CONFALERTA1)) {
				
				try {
					Double confRatio1=Double.valueOf(text);
					if(confRatio1==0) {
						confRatio1=95.00;
					} else if(confRatio1<92) {
						confRatio1=92.00;
					}
					ConfAlerta conf=confAlertas.get(chatId);
					if(conf==null) {
						conf= new ConfAlerta();
						conf.setChatId(chatId);
						conf.setRatioNivel1(confRatio1);
						conf.setRatioNivel2(Double.valueOf(92));
						conf.setCuotaMinima(Double.valueOf(2.5));
						
						confAlertas.put(chatId, conf);
						
					} else {
						conf.setRatioNivel1(confRatio1);
						confAlertas.put(chatId, conf);
					}
					
					StringBuilder mens1= new StringBuilder();
					mens1.append("Nivel 1 seteado a -> <b> "+ conf.getRatioNivel1() +"%</b>\n\n");
					mens1.append("Escribe nuevo ratio para el <b>nivel 2</b>\n");
					mens1.append("escribe 0 para volver a valor defecto\n");
					mens1.append("(decimales con punto y sin el signo de %)");
					
					sendMessage(chatId, mens1.toString());
					
					estados.put(chatId, Estados.CONFALERTA2);
					
					ConfAlertasCSVUtils.escribirConfAlertasEnCsv(confAlertas);
					
				} catch (Exception e) {
					sendMessage(chatId,"formato de ratio incorrecto");
					estados.put(chatId, Estados.INICIAL);
				}
				
				
			} else if (estadoUsuario.equals(Estados.CONFALERTA2)) {
				
				try {
					Double confRatio2=Double.valueOf(text);
					
					if(confRatio2==0) {
						confRatio2=92.00;
					} else if(confRatio2<92) {
						confRatio2=92.00;
					}
					ConfAlerta conf=confAlertas.get(chatId);
					if(conf==null) {
						conf= new ConfAlerta();
						conf.setChatId(chatId);
						conf.setRatioNivel1(Double.valueOf(95));
						conf.setRatioNivel2(confRatio2);
						conf.setCuotaMinima(Double.valueOf(2.5));
						
						confAlertas.put(chatId, conf);
						
					} else {
						conf.setRatioNivel2(confRatio2);
						confAlertas.put(chatId, conf);
					}
					
					StringBuilder mens1= new StringBuilder();
					mens1.append("Nivel 1 seteado a -><b> "+ conf.getRatioNivel1() +"%</b>\n");
					mens1.append("Nivel 2 seteado a -><b> "+ conf.getRatioNivel2() +"%</b>\n\n");
					
					mens1.append("Escribe <b>cuota mínima</b> para recibir la alerta\n");
					mens1.append("escribe 0 para volver a valor defecto\n");
					mens1.append("(decimales con punto y sin el signo de %)");
					
					String enviar=mens1.toString();
					sendMessage(chatId, enviar);
										
					estados.put(chatId, Estados.CONFALERTA3);
					
					ConfAlertasCSVUtils.escribirConfAlertasEnCsv(confAlertas);
					
				} catch (Exception e) {
					sendMessage(chatId,"formato de ratio incorrecto");
					estados.put(chatId, Estados.INICIAL);
				}
				
				
			} else if (estadoUsuario.equals(Estados.CONFALERTA3)) {
				
				try {
					Double confCuotaMinima=Double.valueOf(text);
					
					if(confCuotaMinima==0) {
						confCuotaMinima=2.5;
					} else if(confCuotaMinima<1.85) {
						confCuotaMinima=1.85;
					}
					ConfAlerta conf=confAlertas.get(chatId);
					if(conf==null) {
						conf= new ConfAlerta();
						conf.setChatId(chatId);
						conf.setRatioNivel1(Double.valueOf(95));
						conf.setRatioNivel2(Double.valueOf(92));
						conf.setCuotaMinima(confCuotaMinima);
						
						confAlertas.put(chatId, conf);
						
					} else {
						conf.setCuotaMinima(confCuotaMinima);
						confAlertas.put(chatId, conf);
					}
					
					StringBuilder mens1= new StringBuilder();
					mens1.append("Nivel 1 seteado a -><b> "+ conf.getRatioNivel1() +"%</b>\n");
					mens1.append("Nivel 2 seteado a -><b> "+ conf.getRatioNivel2() +"%</b>\n");
					mens1.append("Cuota Mínima seteada a -><b> "+ conf.getCuotaMinima() +"</b>\n\n");
					mens1.append("Fin de la configuración");
					
					String enviar=mens1.toString();
					sendMessage(chatId, enviar);
										
					estados.put(chatId, Estados.INICIAL);
					
					ConfAlertasCSVUtils.escribirConfAlertasEnCsv(confAlertas);
					
				} catch (Exception e) {
					sendMessage(chatId,"formato de ratio incorrecto");
					estados.put(chatId, Estados.INICIAL);
				}
				
				
			} else if (estadoUsuario.equals(Estados.BUSCA_ALERTA1)) { 
				
				String cadenaBuscar=text;
				
				List<Event> eventos=BotService.buscaEventos(cadenaBuscar);
				ArrayList<MenuOpcion> opciones= new ArrayList<MenuOpcion>();
				
				for (Event e : eventos) {
					MenuOpcion opcion= new MenuOpcion(e.getValue(), "bus|" + e.getValue());
					opciones.add(opcion);
				}
				
				StringBuilder mens1= new StringBuilder();
				mens1.append("Escoge el evento si esta entre los encontrados:\n");
								
				TelegramSender.sendTelegramMessageConMenuOpciones(mens1.toString(), String.valueOf(chatId), opciones);
				
								
				estados.put(chatId, Estados.BUSCA_ALERTA2);
			
			}
			else {
				sendMessage(chatId,"comando desconocido");
				estados.put(chatId, Estados.INICIAL);
			}

		}
        
        

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            System.out.println(callbackData);
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String[] parts = callbackData.split("\\|");
            
            String opcion=parts[0];
            
                       
            if("excluir".equals(opcion)) {
            	 String idOdd = parts[1];
                                
                 Odd odd=OddsCSVUtils.recuperarOdd(idOdd);
                 
                 if(odd!=null) {
                	 AlertaExclusion alerta= new AlertaExclusion();
                     alerta.setChatId(chatId);
                     alerta.setMarket_id(odd.getMarket_id());
                     alerta.setsFechaPartido(odd.getsFechaPartido());
                     alerta.setEvento(odd.getEvent());
                     

                     try {
         				AlertaExclusionCSVUtils.addIfNotExists(alerta);
         			} catch (IOException e) {
         				// TODO Auto-generated catch block
         				e.printStackTrace();
         			}
                     
                     sendMessage(chatId, "Has Excluido este evento de tus alertas");
                     estados.put(chatId, Estados.INICIAL);
                 } else {
                	 sendMessage(chatId, "Evento no encontrado en la lista interna");
                	 estados.put(chatId, Estados.INICIAL);
                 }
                 
                
            }
            
            if("way".equals(opcion)) {
            	 String idOdd = parts[1];
            	 
            	Odd f=OddsCSVUtils.recuperarOdd(idOdd);
                
            	Odd odd=new Odd();
                odd.setEvent(f.getEvent());
                odd.setMarket_id(f.getMarket_id());
               
                
                try {
					odd=NinjaService.rellenaCuotasTodas(odd, null, f.getMarket_id());
					Odd o=odd.getMejoresHome().get(0);
					
					odd.setCompetition(o.getCompetition());
					odd.setCountry(o.getCountry());
					odd.setsFechaPartido(o.getsFechaPartido());
					
					StringBuilder mensaje = new StringBuilder();
					mensaje = AlertasFactory.createAlerta2WAY(odd);
					System.out.println("Alerta 2WAY enviada");
					// 🔹 Enviar a Telegram
					TelegramSender.alertasEnviadas++;
					TelegramSender.sendTelegramMessageAlerta2WAY(mensaje.toString(), odd, chatId.toString());	
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
              
                
                
           }
            
            if("bus".equals(opcion)) {
            	String evento=parts[1];  
            	
            	List<Event> listaEventos=BotService.buscaEventos(evento);
            	            	
            	
                
            	Odd odd=new Odd();
                odd.setEvent(evento);
               // odd.setMarket_id(market_id);
               
                
                try {
                	
                	User user=getUser(chatId.toString());
					odd=NinjaService.rellenaCuotasTodas(odd, listaEventos.get(0).getId(),null);
					
					odd=OddUtils.pasarHijaMadre(odd, odd.getMejoresHome().get(0));
					ArrayList<Odd> fusion=new ArrayList<Odd>();
					fusion.add(odd);
					odd.setOddsFusion(fusion);
										
					StringBuilder mensaje = new StringBuilder();
					mensaje = AlertasFactory.createAlerta(odd);
					// 🔹 Enviar a Telegram
					TelegramSender.sendTelegramMessageAlerta(mensaje.toString(), odd, chatId.toString());
					
					
					odd=OddUtils.pasarHijaMadre(odd, odd.getMejoresAway().get(0));
					fusion=new ArrayList<Odd>();
					fusion.add(odd);
					odd.setOddsFusion(fusion);
					
					mensaje = new StringBuilder();
					mensaje = AlertasFactory.createAlerta(odd);
					// 🔹 Enviar a Telegram
					TelegramSender.sendTelegramMessageAlerta(mensaje.toString(), odd, chatId.toString());
					
					StringBuilder mensajeDebug = new StringBuilder();
			        mensajeDebug.append("<b>Petición manual Alerta</b>\n\n");
				    mensajeDebug.append("Evento: <b>").append(odd.getEvent()).append("</b>\n");
				    mensajeDebug.append("Usuario: <b>").append(user.getName()).append("</b>\n");
			       	TelegramSender.sendTelegramMessageDebug(mensajeDebug.toString());
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
              
                
                
           }
           
         
        
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
	
	    public static User getUser(String chatId) {
	        List<User> users = readUsers();

	       for (User user : users) {
			if (String.valueOf(user.getChatId()).equals(chatId)){
				return user;	
			}
	       }
	       return null; 
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
                .parseMode("HTML") // 👈 Muy importante
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