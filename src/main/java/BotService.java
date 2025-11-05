import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
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

public class BotService {
	
	private static TelegramClient telegramClient = new OkHttpTelegramClient(Configuracion.BOT_TOKEN);
	
	
	public static List<Event> buscaEventos(String cadenaBuscar) {
		List<Event> lista= NinjaService.buscaEventos(cadenaBuscar);
		return lista;
	}
	
    public static void sendMessage(Long chatId, String text) {
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
    
    public static void desconocido(Update update, Long chatId) {
    	sendMessage(chatId,"comando desconocido");
    	BotConfiguracion.estados.put(chatId, Estados.INICIAL);
		    	
    }
	
	public static void activarAlertas(Update update, Long chatId) {
		
		User user = new User();
		user.setChatId(chatId);
		user.setName(update.getMessage().getChat().getUserName());

		UsersUtils.saveUserIfNotExists(user);
		BotConfiguracion.entradas.put(chatId, new ArrayList<Odd>());
		BotConfiguracion.entradasTemp.put(chatId, new Odd());

		sendMessage(chatId, "Alertas 2UP activadas para el usuario");

		 BotConfiguracion.estados.put(chatId, Estados.INICIAL);
		
	}
	
	public static void desactivarAlertas(Update update, Long chatId) {
		UsersUtils.deleteUserByChatId(chatId.toString());

		sendMessage(chatId, "Alertas 2UP desactivadas para el usuario");

		BotConfiguracion.estados.put(chatId, Estados.INICIAL);
		
	}
	
	public static void configurarAlertas(Update update, Long chatId) {

		ConfAlerta conf=BotConfiguracion.confAlertas.get(chatId);
		if(conf==null) {
			conf= new ConfAlerta();
			conf.setChatId(chatId);
			conf.setRatioNivel1(Double.valueOf(95));
			conf.setRatioNivel2(Double.valueOf(92));
			
			BotConfiguracion.confAlertas.put(chatId, conf);
			
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

		BotConfiguracion.estados.put(chatId, Estados.CONFALERTA1);
		
	}
	
	public static void dameAlertas(Update update, Long chatId) {
		StringBuilder mens= new StringBuilder();
		mens.append("Introduce palabra o palabras de búsqueda que servirán para buscar el evento o eventos \n");
							
		String enviar=mens.toString();
		
		sendMessage(chatId, enviar);
		
		BotConfiguracion.estados.put(chatId, Estados.BUSCA_ALERTA1);
		
	}
	
	public static void confAlertas1(Update update, Long chatId, String text) {
		
		try {
			Double confRatio1=Double.valueOf(text);
			if(confRatio1==0) {
				confRatio1=95.00;
			} else if(confRatio1<92) {
				confRatio1=92.00;
			}
			ConfAlerta conf=BotConfiguracion.confAlertas.get(chatId);
			
			if(conf==null) {
				conf= new ConfAlerta();
				conf.setChatId(chatId);
				conf.setRatioNivel1(confRatio1);
				conf.setRatioNivel2(Double.valueOf(92));
				conf.setCuotaMinima(Double.valueOf(2.5));
				
				BotConfiguracion.confAlertas.put(chatId, conf);
				
			} else {
				conf.setRatioNivel1(confRatio1);
				BotConfiguracion.confAlertas.put(chatId, conf);
			}
			
			StringBuilder mens1= new StringBuilder();
			mens1.append("Nivel 1 seteado a -> <b> "+ conf.getRatioNivel1() +"%</b>\n\n");
			mens1.append("Escribe nuevo ratio para el <b>nivel 2</b>\n");
			mens1.append("escribe 0 para volver a valor defecto\n");
			mens1.append("(decimales con punto y sin el signo de %)");
			
			sendMessage(chatId, mens1.toString());
			
			BotConfiguracion.estados.put(chatId, Estados.CONFALERTA2);
			
			ConfAlertasCSVUtils.escribirConfAlertasEnCsv(BotConfiguracion.confAlertas);
			
		} catch (Exception e) {
			sendMessage(chatId,"formato de ratio incorrecto");
			BotConfiguracion.estados.put(chatId, Estados.INICIAL);
		}
		
	}
	
	public static void confAlertas2(Update update, Long chatId, String text) {
		
		try {
			Double confRatio2=Double.valueOf(text);
			
			if(confRatio2==0) {
				confRatio2=92.00;
			} else if(confRatio2<92) {
				confRatio2=92.00;
			}
			ConfAlerta conf=BotConfiguracion.confAlertas.get(chatId);
			if(conf==null) {
				conf= new ConfAlerta();
				conf.setChatId(chatId);
				conf.setRatioNivel1(Double.valueOf(95));
				conf.setRatioNivel2(confRatio2);
				conf.setCuotaMinima(Double.valueOf(2.5));
				
				BotConfiguracion.confAlertas.put(chatId, conf);
				
			} else {
				conf.setRatioNivel2(confRatio2);
				BotConfiguracion.confAlertas.put(chatId, conf);
			}
			
			StringBuilder mens1= new StringBuilder();
			mens1.append("Nivel 1 seteado a -><b> "+ conf.getRatioNivel1() +"%</b>\n");
			mens1.append("Nivel 2 seteado a -><b> "+ conf.getRatioNivel2() +"%</b>\n\n");
			
			mens1.append("Escribe <b>cuota mínima</b> para recibir la alerta\n");
			mens1.append("escribe 0 para volver a valor defecto\n");
			mens1.append("(decimales con punto y sin el signo de %)");
			
			String enviar=mens1.toString();
			sendMessage(chatId, enviar);
								
			BotConfiguracion.estados.put(chatId, Estados.CONFALERTA3);
			
			ConfAlertasCSVUtils.escribirConfAlertasEnCsv(BotConfiguracion.confAlertas);
			
		} catch (Exception e) {
			sendMessage(chatId,"formato de ratio incorrecto");
			BotConfiguracion.estados.put(chatId, Estados.INICIAL);
		}
		
	}
	
	public static void confAlertas3(Update update, Long chatId, String text) {
		
		try {
			Double confCuotaMinima=Double.valueOf(text);
			
			if(confCuotaMinima==0) {
				confCuotaMinima=2.5;
			} else if(confCuotaMinima<1.85) {
				confCuotaMinima=1.85;
			}
			ConfAlerta conf=BotConfiguracion.confAlertas.get(chatId);
			if(conf==null) {
				conf= new ConfAlerta();
				conf.setChatId(chatId);
				conf.setRatioNivel1(Double.valueOf(95));
				conf.setRatioNivel2(Double.valueOf(92));
				conf.setCuotaMinima(confCuotaMinima);
				
				BotConfiguracion.confAlertas.put(chatId, conf);
				
			} else {
				conf.setCuotaMinima(confCuotaMinima);
				BotConfiguracion.confAlertas.put(chatId, conf);
			}
			
			StringBuilder mens1= new StringBuilder();
			mens1.append("Nivel 1 seteado a -><b> "+ conf.getRatioNivel1() +"%</b>\n");
			mens1.append("Nivel 2 seteado a -><b> "+ conf.getRatioNivel2() +"%</b>\n");
			mens1.append("Cuota Mínima seteada a -><b> "+ conf.getCuotaMinima() +"</b>\n\n");
			mens1.append("Fin de la configuración");
			
			String enviar=mens1.toString();
			sendMessage(chatId, enviar);
								
			BotConfiguracion.estados.put(chatId, Estados.INICIAL);
			
			ConfAlertasCSVUtils.escribirConfAlertasEnCsv(BotConfiguracion.confAlertas);
			
		} catch (Exception e) {
			sendMessage(chatId,"formato de ratio incorrecto");
			BotConfiguracion.estados.put(chatId, Estados.INICIAL);
		}
	}
	
	public static void BuscaAlerta1(Update update, Long chatId, String text) {
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
		
						
		BotConfiguracion.estados.put(chatId, Estados.BUSCA_ALERTA2);
		
	}
	

	
	
	// CALBACKS
	public static void excluirEvento(Update update, Long chatId, String text, String[] parts) {
		
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
             BotConfiguracion.estados.put(chatId, Estados.INICIAL);
         } else {
        	 sendMessage(chatId, "Evento no encontrado en la lista interna");
        	 BotConfiguracion.estados.put(chatId, Estados.INICIAL);
         }
		
		
	}
	
	
	public static void enviar2Way(Update update, Long chatId, String text, String[] parts) {
		
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
	
	public static void buscarEventos(Update update, Long chatId, String text, String[] parts) {
		
     	String evento=parts[1];  
    	
    	List<Event> listaEventos=BotService.buscaEventos(evento);
    	            	
    	
        
    	Odd odd=new Odd();
        odd.setEvent(evento);
       // odd.setMarket_id(market_id);
       
        
        try {
        	
        	User user=UsersUtils.getUser(chatId.toString());
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
	
	
	public static void entrar0(Update update, Long chatId, String text, String[] parts, boolean cuotasAjustadas) {
		
		String idOdd = "";
      	 
    	Odd odd=new Odd();
		
		if(!cuotasAjustadas) {
			idOdd = parts[1];
    		odd=OddsCSVUtils.recuperarOdd(idOdd);	
    	} else {
    		odd=BotConfiguracion.entradasTemp.get(chatId);
    	}
		    	
    	if(odd!=null) {
    		BotConfiguracion.entradasTemp.put(chatId, odd);            		            		
    	
    		String mensaje="<b>" + AlertasFactory.getNombreBookie(odd.getBookie()) + "</b>\n";
    		mensaje+="<b>"+odd.getSelection()+"</b> Rat:<b>"+odd.getRating()+"%</b>\n";
    		mensaje+="Back:"+ odd.getBackOdd() +" | lay:" + odd.getLayOdd() + "\n";
    		mensaje+="¿Las cuotas en la alerta son correctas?";
    		MenuOpcion si=new MenuOpcion("Sí", "ent1|si");
    		MenuOpcion no=new MenuOpcion("No", "ent1|no");
    		ArrayList<MenuOpcion> sino=new ArrayList<MenuOpcion>();
    		sino.add(no);
    		sino.add(si);
    		
    		TelegramSender.sendTelegramMessageConMenuOpciones(mensaje, chatId.toString(), sino);
    	
    	}
	}
	
	
	public static void entrar1(Update update, Long chatId, String text, String[] parts) {
		
		String respuesta = parts[1];
    	
    	if("si".equals(respuesta)) {
    		StringBuilder mens= new StringBuilder();
    		mens.append("¿Con que Stake entrarás en la Bookie? \n");
    		mens.append("(pon un 0 para cancelar el proceso) \n");
    		    		
    		String enviar=mens.toString();
    		
    		sendMessage(chatId, enviar);
    		   		
    		BotConfiguracion.estados.put(chatId, Estados.ENTRAR_2);
    		
    	} else if ("no".equals(respuesta)) {
    		Odd odd=BotConfiguracion.entradasTemp.get(chatId);
    		
    		StringBuilder mens= new StringBuilder();
    		mens.append("<b>" + AlertasFactory.getNombreBookie(odd.getBookie()) + ":</b>\n");
    		mens.append("¿Que cuota Back hay en la Bookie? \n");
    		mens.append("(pon un 0 para cancelar el proceso) \n");
    		    		
    		String enviar=mens.toString();
    		
    		sendMessage(chatId, enviar);
    		   		
    		BotConfiguracion.estados.put(chatId, Estados.ENTRAR_11);
    		
    		
    	} 
		
	}
	
	public static void entrar11(Update update, Long chatId, String text) {
		
		try {
			Double backOdd=Double.valueOf(text);
			if(backOdd==0) {
				sendMessage(chatId, "proceso cancelado");
				BotConfiguracion.estados.put(chatId, Estados.INICIAL);
				return;
			}
			
			BigDecimal back = new BigDecimal(backOdd);
			back = back.setScale(2, RoundingMode.HALF_UP); // Redondea a 2 decimales
			Double backOddRed = back.doubleValue();
			
			Odd odd=BotConfiguracion.entradasTemp.get(chatId);
			odd.setBackOdd(String.valueOf(backOddRed));
			
			BotConfiguracion.entradasTemp.put(chatId,odd);
				
			
			StringBuilder mens= new StringBuilder();
    		mens.append("<b>" + "Exchange" + ":</b>\n");
    		mens.append("¿Que cuota Lay hay en Exchange? \n");
    		mens.append("(pon un 0 para cancelar el proceso) \n");
    		    		
    		String enviar=mens.toString();
    		
    		sendMessage(chatId, enviar);
    		   		
    		BotConfiguracion.estados.put(chatId, Estados.ENTRAR_12);
    		
    		
			
		} catch (Exception e) {
			sendMessage(chatId, "ha fallado el proceso");
			BotConfiguracion.estados.put(chatId, Estados.INICIAL);
		}
	
		
	}
	
	public static void entrar12(Update update, Long chatId, String text) {
		
		try {
			Double layOdd=Double.valueOf(text);
			if(layOdd==0) {
				sendMessage(chatId, "proceso cancelado");
				BotConfiguracion.estados.put(chatId, Estados.INICIAL);
				return;
			}
			
			BigDecimal lay = new BigDecimal(layOdd);
			lay = lay.setScale(2, RoundingMode.HALF_UP); // Redondea a 2 decimales
			Double layOddOddRed = lay.doubleValue();
			
			Odd odd=BotConfiguracion.entradasTemp.get(chatId);
			odd.setLayOdd(String.valueOf(layOddOddRed));
			
			
			Double layStake=100*Double.valueOf(odd.getBackOdd())/(layOddOddRed-0.02);
			Double profit=layStake*(1-0.02)-100;
			Double nuevoRating=((100+profit)/100)*100;
			Double nuevoRatingRedondeado = Math.round(nuevoRating * 100.0) / 100.0;
			
			odd.setRatingOriginal(String.valueOf(nuevoRatingRedondeado));
			odd.setRating(String.valueOf(nuevoRatingRedondeado));
			
			BotConfiguracion.entradasTemp.put(chatId,odd);
			
			entrar0(update, chatId, text, null, true);
			    		    		
			
		} catch (Exception e) {
			sendMessage(chatId, "ha fallado el proceso");
			BotConfiguracion.estados.put(chatId, Estados.INICIAL);
		}
	
		
	}
	
	public static void entrar2(Update update, Long chatId, String text) {
		
		try {
			Double backStake=Double.valueOf(text);
			if(backStake==0) {
				sendMessage(chatId, "proceso cancelado");
				BotConfiguracion.estados.put(chatId, Estados.INICIAL);
				return;
			}
			
			BigDecimal bdBack = new BigDecimal(backStake);
			bdBack = bdBack.setScale(2, RoundingMode.HALF_UP); // Redondea a 2 decimales
			Double backStakeRedondeado = bdBack.doubleValue();
			
			
			Odd entrada=BotConfiguracion.entradasTemp.get(chatId);
			entrada.setStakeEntradaBookie(backStake);
			
			Double backOdd=Double.valueOf(entrada.getBackOdd());
			Double layOdd=Double.valueOf(entrada.getLayOdd());
			
			Double layStake=(backStake*backOdd)/(layOdd - 0.02);
			
			BigDecimal bd = new BigDecimal(layStake);
			bd = bd.setScale(2, RoundingMode.HALF_UP); // Redondea a 2 decimales

			Double layStakeRedondeado = bd.doubleValue();
			
			entrada.setStakeEntradaExchange(layStakeRedondeado);
			
			Double perdidaCalificante=((backOdd-1)*backStakeRedondeado)-(layStakeRedondeado*(layOdd-1));
			BigDecimal pc = new BigDecimal(perdidaCalificante);
			pc = pc.setScale(2, RoundingMode.HALF_UP); // Redondea a 2 decimales
			Double perdidaCalificanteRedondeada=pc.doubleValue();
			
			entrada.setBookieWins(perdidaCalificanteRedondeada);
			entrada.setExchangeWins(perdidaCalificanteRedondeada);
			
			
			StringBuilder mens= new StringBuilder();
    		mens.append( entrada.getEvent() + " \n");
    		mens.append("<b>"+ AlertasFactory.getNombreBookie(entrada.getBookie()) + ":</b> \n");
    		mens.append("Ap:<b>").append(entrada.getSelection()).append("</b> Rat:<b>").append(entrada.getRating()).append("%</b>\n");
    		mens.append("<b>"+ backStakeRedondeado + "€</b> BACK a cuota <b>" +  entrada.getBackOdd()  +"</b> \n");
    		mens.append("<b>"+ "Exchange" + ":</b> \n\n");
    		mens.append("👉Apuesta->").append("<b>"+ layStakeRedondeado + "€</b> LAY a cuota <b>" +  entrada.getLayOdd()  +"</b> \n");
    		mens.append("\n");
    		mens.append("🔴Pérdida calificante <b>"+ perdidaCalificanteRedondeada + "€</b> \n");
    		mens.append("\n");
    		mens.append("¿confirmas entrada?");
    		    		
    		String enviar=mens.toString();
    		
    		 		
    		MenuOpcion si=new MenuOpcion("Sí", "ent2|si");
    		MenuOpcion no=new MenuOpcion("No", "ent2|no");
    		MenuOpcion rat=new MenuOpcion("Modificar stake", "ent2|mod");
    		ArrayList<MenuOpcion> sino=new ArrayList<MenuOpcion>();
    		sino.add(no);
    		sino.add(si);
    		sino.add(rat);
    		
    		TelegramSender.sendTelegramMessageConMenuOpciones(enviar, chatId.toString(), sino);
    		
    		
			
		} catch (Exception e) {
			sendMessage(chatId, "ha fallado el proceso");
			BotConfiguracion.estados.put(chatId, Estados.INICIAL);
		}
		
		
	}
	
	public static void entrar3(Update update, Long chatId, String text, String[] parts) {
		
		String respuesta = parts[1];
    	
    	if("si".equals(respuesta)) {
    		
    		try {
    			BotConfiguracion.entradas.get(chatId).add(BotConfiguracion.entradasTemp.get(chatId));
        		OddsCSVUtils.escribirCSV(Configuracion.CSV_FILE_ENTRADAS+chatId+"_entradas.csv", BotConfiguracion.entradas.get(chatId));
        		
        		StringBuilder mens= new StringBuilder();
        		mens.append("entrada añadida a \"Mis entradas\" \n");
        		    		    		
        		String enviar=mens.toString();
        		sendMessage(chatId, enviar);
        		   		
        		BotConfiguracion.estados.put(chatId, Estados.INICIAL);
			} catch (Exception e) {
				BotConfiguracion.estados.put(chatId, Estados.INICIAL);
			}
    		
    		
    	} else if ("no".equals(respuesta)) {
    		
    		
    		StringBuilder mens= new StringBuilder();
    		mens.append("proceso cancelado \n");
    		    		    		
    		String enviar=mens.toString();
    		sendMessage(chatId, enviar);
    		   		
    		BotConfiguracion.estados.put(chatId, Estados.INICIAL);
    		
    		
    	} else if ("mod".equals(respuesta)) {
    		
    		String[] part = { "ent1", "si" };
    		entrar1(update, chatId, text, part);
    				
    		
    	}
		
	}
	
	public static boolean buscarEntrada(Update update, Long chatId, String text) {
		
		ArrayList<Odd> entradasUsuario=recargarEntradasUsuario(update, chatId);
		
		for (Odd odd : entradasUsuario) {
			if(odd.getEvent().toLowerCase().contains(text.toLowerCase())) {
				enviarFichaEntrada(odd, chatId);
				return true;
			}
		}
		
		return false;
	}
	
	
	public static ArrayList<Odd> recargarEntradasUsuario(Update update, Long chatId) {
		
		try {
			
			ArrayList<Odd> entradasUsuario=OddsCSVUtils.leerCSV(Configuracion.CSV_FILE_ENTRADAS + chatId + "_entradas.csv");
			ArrayList<Odd> cierresUsuario=OddsCSVUtils.leerCSV(Configuracion.CSV_FILE_ENTRADAS + chatId + "_cierres.csv");
			
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
				
			}
								
			//Borrar eventos ya finalizados
			
			ArrayList<Odd> entradasUsuarioFiltradas=new ArrayList<Odd>();
			
			for (Odd odd : entradasUsuario) {
				
				String fechaTexto=odd.getsFechaPartido();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
				
				// Convertimos el String a LocalDateTime
		        LocalDateTime fechaObjetivo = LocalDateTime.parse(fechaTexto, formatter);
		        LocalDateTime ahora = LocalDateTime.now();

		        // Calculamos la diferencia
		        Duration diferencia = Duration.between(fechaObjetivo, ahora);
		        long horasDiferencia = diferencia.toHours();

		        if (fechaObjetivo.isBefore(ahora) && horasDiferencia >= 3) {
		            //eliminar partido, no agregarlo al array Filtrado
		        	System.out.println("La fecha/hora es anterior por 3 horas o más.");
		        } else {
		        	//conservar partido
		        	entradasUsuarioFiltradas.add(odd);
		            System.out.println("La fecha/hora no es anterior por 3 horas o más.");
		        }	
			}
				
			BotConfiguracion.entradas.put(chatId, entradasUsuarioFiltradas);
			grabarDatosUsuario(chatId);
			
			return entradasUsuarioFiltradas;
			
			
		} catch (Exception e) {
			sendMessage(chatId, "ha fallado el proceso");
			BotConfiguracion.estados.put(chatId, Estados.INICIAL);
			return new ArrayList<Odd>();
		}
		
	}
	
	public static void verMisEntradas(Update update, Long chatId) {
		
		try {
			
			ArrayList<Odd> entradasUsuario=recargarEntradasUsuario(update, chatId);
				
			for (Odd odd : entradasUsuario) {
				
				enviarFichaEntrada(odd, chatId);
								
			}
			
		} catch (Exception e) {
			sendMessage(chatId, "ha fallado el proceso");
			BotConfiguracion.estados.put(chatId, Estados.INICIAL);
		}
		
		
	
		
		
		
	}
	
	public static void enviarFichaEntrada(Odd odd, Long chatId) {
		
		StringBuilder mensaje=AlertasFactory.createFichaEntrada(odd);
		
		odd=recalcularCierresParciales(odd);
						
		MenuOpcion men1=new MenuOpcion("2UP --> cerrar/cubrir", "early1|" + odd.getIdOdd());
		ArrayList<MenuOpcion> menu=new ArrayList<MenuOpcion>();
		menu.add(men1);
		    		
		TelegramSender.sendTelegramMessageConMenuOpciones(mensaje.toString(), chatId.toString(), menu);		
		
		
	}
	
	public static Odd recalcularCierresParciales(Odd o) {
		
		
		//Calculos
		Double layStake=o.getStakeEntradaExchange();
		Double layOdd=Double.valueOf(o.getLayOdd());
		Double backOdd=Double.valueOf(o.getBackOdd());
		Double liabilityOriginal=layStake*(layOdd-1);
				
		for (Odd c : o.getCierres()) {
			Double layCierreParcial=(c.getStakeEarly()*c.getBackOddEarly())/layOdd;
			layStake-=layCierreParcial;
		}
				
		Double liability=layStake*(layOdd-1);
		Double suma= layStake + liability;
		
				
		//equipo 2UP gana
		Double sumaEntradasBookie=0.0;
		Double sumaEntradasExchange=0.0;
		Double resultado=0.0;
		sumaEntradasBookie+=o.getStakeEntradaBookie()*(backOdd-1);
		
		for (Odd c : o.getCierres()) {
			sumaEntradasBookie+=c.getStakeEarly()*(c.getBackOddEarly()-1);
		}
		sumaEntradasExchange+=liabilityOriginal*-1;
		resultado=sumaEntradasBookie + sumaEntradasExchange;
		o.setBookieWins(Math.round(resultado * 100.0) / 100.0);
		
		//equipo 2UP NO gana
		sumaEntradasBookie=0.0;
		sumaEntradasExchange=0.0;
		resultado=0.0;
		
		if(o.getCierres().size()>0) {
			sumaEntradasBookie+=o.getStakeEntradaBookie()*(backOdd-1);
		} else {
			sumaEntradasBookie+=o.getStakeEntradaBookie()*-1;
		}
				
		for (Odd c : o.getCierres()) {
			sumaEntradasBookie+=c.getStakeEarly()*-1;
		}
		sumaEntradasBookie=sumaEntradasBookie;
		if(o.getCierres().size()>0) {
			sumaEntradasExchange+=o.getStakeEntradaExchange();	
		} else {
			sumaEntradasExchange+=o.getStakeEntradaExchange()*0.98;
		}
		
				
		resultado=sumaEntradasBookie + sumaEntradasExchange;
		o.setExchangeWins(Math.round(resultado * 100.0) / 100.0);
			
		
		return o;
	}
	
	public static void early1(Update update, Long chatId, String text, String[] parts) {
		
		String respuesta = parts[1];
		Long idOdd=Long.valueOf(respuesta);
		
		ArrayList<Odd> entradasUsuario=BotConfiguracion.entradas.get(chatId);
		
		boolean encontrado=false;
		for (Odd odd : entradasUsuario) {
			if(odd.getIdOdd().longValue()==idOdd.longValue()) {
				encontrado=true;
				BotConfiguracion.entradasTemp.put(chatId, odd);
			}
		}
		
		if(!encontrado) {
			StringBuilder mens= new StringBuilder();
    		mens.append("entrada no encontrada \n");
    		    		    		
    		String enviar=mens.toString();
    		sendMessage(chatId, enviar);
    		   		
    		BotConfiguracion.estados.put(chatId, Estados.INICIAL);
		} else {
			StringBuilder mens= new StringBuilder();
    		mens.append("introduce la nueva cuota back\n");
    		mens.append("(pon un 0 para cancelar el proceso) \n");
    		    		
    		String enviar=mens.toString();
    		
    		sendMessage(chatId, enviar);
    		   		
    		BotConfiguracion.estados.put(chatId, Estados.EARLY2);
			
		}	
    	
    
		
	}
	
	public static void early2(Update update, Long chatId, String text) {
		
		try {
			Double nuevaCuotaBack=Double.valueOf(text);
			if(nuevaCuotaBack==0) {
				sendMessage(chatId, "proceso cancelado");
				BotConfiguracion.estados.put(chatId, Estados.INICIAL);
				return;
			}
			
			Odd oddCierre=BotConfiguracion.entradasTemp.get(chatId);
			oddCierre.setBackOddEarly(nuevaCuotaBack);
			BotConfiguracion.entradasTemp.put(chatId, oddCierre);
					
			
			StringBuilder mens= new StringBuilder();
    		mens.append("introduce % que quieres cerrar\n");
    		mens.append("(pon un 0 para cancelar el proceso) \n");
    		    		
    		String enviar=mens.toString();
    		
    		sendMessage(chatId, enviar);
    		   		
    		BotConfiguracion.estados.put(chatId, Estados.EARLY3);
		   		
			
		} catch (Exception e) {
			sendMessage(chatId, "ha fallado el proceso");
			BotConfiguracion.estados.put(chatId, Estados.INICIAL);
		}
		
		
		
		
	}
	
public static void early3(Update update, Long chatId, String text) {
		
		try {
			Double PorcentajeCierre=Double.valueOf(text);
			if(PorcentajeCierre==0) {
				sendMessage(chatId, "proceso cancelado");
				BotConfiguracion.estados.put(chatId, Estados.INICIAL);
				return;
			}
			
			Odd o=BotConfiguracion.entradasTemp.get(chatId);
			o.setPorcEarly(PorcentajeCierre);
			
			
			//Calculos
			Double layStake=o.getStakeEntradaExchange();
			Double layOdd=Double.valueOf(o.getLayOdd());
			Double backOdd=Double.valueOf(o.getBackOdd());
			Double liabilityOriginal=layStake*(layOdd-1);
			
			for (Odd c : o.getCierres()) {
				Double layCierreParcial=(c.getStakeEarly()*c.getBackOddEarly())/layOdd;
				layStake-=layCierreParcial;
			}
			
			Double liability=layStake*(layOdd-1);
			Double suma= layStake + liability;
			
						
			Double nuevoStakeEarly=Math.round((suma/o.getBackOddEarly()*(PorcentajeCierre/100)) * 100.0) / 100.0;
			o.setStakeEarly(nuevoStakeEarly);
			
						
			//equipo 2UP gana
			Double sumaEntradasBookie=0.0;
			Double sumaEntradasExchange=0.0;
			Double resultado=0.0;
			sumaEntradasBookie+=o.getStakeEntradaBookie()*(backOdd-1);
			sumaEntradasBookie+=nuevoStakeEarly*(o.getBackOddEarly()-1);
			
			for (Odd c : o.getCierres()) {
				sumaEntradasBookie+=c.getStakeEarly()*(c.getBackOddEarly()-1);
			}
			
			sumaEntradasExchange+=liabilityOriginal*-1;
			resultado=sumaEntradasBookie + sumaEntradasExchange;
			
			o.setBookieWins(Math.round(resultado * 100.0) / 100.0);
			
			//equipo 2UP NO gana
			sumaEntradasBookie=0.0;
			sumaEntradasExchange=0.0;
			resultado=0.0;
			
			
			sumaEntradasBookie+=o.getStakeEntradaBookie()*(backOdd-1);
			
			
			sumaEntradasBookie+=nuevoStakeEarly*-1;
			
			for (Odd c : o.getCierres()) {
				sumaEntradasBookie+=c.getStakeEarly()*-1;
			}
						
			sumaEntradasExchange+=o.getStakeEntradaExchange();
			
			resultado=sumaEntradasBookie + sumaEntradasExchange;
			o.setExchangeWins(Math.round(resultado * 100.0) / 100.0);
						
			
			BotConfiguracion.entradasTemp.put(chatId, o);

			StringBuilder mensaje=AlertasFactory.createFichaCierreParcial(o);
			
			MenuOpcion men1=new MenuOpcion("¿confirmas cierre parcial?", "early4|" + o.getIdOdd() + "|si");
			MenuOpcion men2=new MenuOpcion("cambiar porcentaje cierre", "early4|" + o.getIdOdd() + "|porc");
			MenuOpcion men3=new MenuOpcion("cancelar", "early4|" + o.getIdOdd() + "|cancelar");
    		ArrayList<MenuOpcion> menu=new ArrayList<MenuOpcion>();
    		menu.add(men1);
    		menu.add(men2);
    		menu.add(men3);
    		    		
    		TelegramSender.sendTelegramMessageConMenuOpciones(mensaje.toString(), chatId.toString(), menu);			
						
			
		} catch (Exception e) {
			sendMessage(chatId, "ha fallado el proceso");
			BotConfiguracion.estados.put(chatId, Estados.INICIAL);
		}
		
		
		
		
	}

	public static void early4(Update update, Long chatId, String text, String[] parts) {

		try {
			
			String idOdd = parts[1];
			String respuesta = parts[2];

			if ("si".equals(respuesta)) {

				try {
					ArrayList<Odd> entradasUsuario=BotConfiguracion.entradas.get(chatId);
					Odd cierreTemp=BotConfiguracion.entradasTemp.get(chatId);
					
					for (Odd o : entradasUsuario) {
						if(o.getIdOdd().equals(cierreTemp.getIdOdd())) {
							o.getCierres().add(cierreTemp);
														
							StringBuilder mens = new StringBuilder();
							mens.append("cierre añadido");

							String enviar = mens.toString();
							sendMessage(chatId, enviar);
							
							enviarFichaEntrada(o, chatId);
														
						}
					}
					
					grabarDatosUsuario(chatId);		
					
					BotConfiguracion.estados.put(chatId, Estados.INICIAL);
				} catch (Exception e) {
					BotConfiguracion.estados.put(chatId, Estados.INICIAL);
				}

			} else if ("porc".equals(respuesta)) {

				Odd o=BotConfiguracion.entradasTemp.get(chatId);
				text=String.valueOf(o.getBackOddEarly());
				
				early2(update, chatId, text);
				
				
			} else if ("cancelar".equals(respuesta)) {

				sendMessage(chatId, "proceso cancelado");
				BotConfiguracion.estados.put(chatId, Estados.INICIAL);
				
				
			}
		} catch (Exception e) {
			sendMessage(chatId, "ha fallado el proceso");
			BotConfiguracion.estados.put(chatId, Estados.INICIAL);
		}
		
		

	}
	
	public static void grabarDatosUsuario(Long chatId) throws Exception {
		
		
		ArrayList<Odd> entradasUsuario=BotConfiguracion.entradas.get(chatId);
		
		ArrayList<Odd> cierresUsuario= new ArrayList<Odd>();
		
		for (Odd entrada : entradasUsuario) {
			if(entrada.getCierres()!=null && !entrada.getCierres().isEmpty()) {
				for (Odd cierre : entrada.getCierres()) {
					cierresUsuario.add(cierre);
				}
			}
		}
		
		OddsCSVUtils.escribirCSV(Configuracion.CSV_FILE_ENTRADAS + chatId + "_entradas.csv", entradasUsuario);
		OddsCSVUtils.escribirCSV(Configuracion.CSV_FILE_ENTRADAS + chatId + "_cierres.csv", cierresUsuario);
		
		
	}
	
}
