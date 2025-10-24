import java.util.List;

import dto.Event;
import service.NinjaService;

public class BotService {
	
	
	
	
	public static List<Event> buscaEventos(String cadenaBuscar) {
				
		List<Event> lista= NinjaService.buscaEventos(cadenaBuscar);
			
		
		return lista;
	}

}
