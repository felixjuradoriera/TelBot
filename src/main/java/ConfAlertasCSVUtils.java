import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfAlertasCSVUtils {
	
	private static final String CSV_CONFIGURE_ALERTS = "C:"+ File.separator +"BOT" + File.separator +"CONF"+File.separator+ "configuracionAlertas.csv";

	/**
	 * Añade un objeto AlertaExclusion al CSV si no existe ya con mismo chatId y
	 * market_id.
	 */
	public static void addIfNotExists(ConfAlerta alerta) throws IOException {
		Path path = Paths.get(CSV_CONFIGURE_ALERTS);

		// Creamos el fichero si no existe
		if (Files.notExists(path)) {
			Files.createFile(path);
		}

		// Leemos todas las líneas
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

		boolean exists = false;
		for (String line : lines) {
			String[] parts = line.split(",", -1);
			if (parts.length >= 4) {
				Long chatId = Long.parseLong(parts[0]);
				String marketId = parts[1];
				if (chatId.equals(alerta.getChatId())) {
					exists = true;
					break;
				}
			}
		}

		if (!exists) {
			
			String newLine = alerta.getChatId() + "," + alerta.getRatioNivel1() + "," + alerta.getRatioNivel2()+ "," + alerta.getCuotaMinima();
			// Añadimos al final
			Files.write(path, (newLine + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
					StandardOpenOption.APPEND);
		}
	}

	/**
	 * Lee el CSV y devuelve una lista de ConfAlertas.
	 */
	public static HashMap<Long, ConfAlerta> loadFromCSV() throws IOException {
		Path path = Paths.get(CSV_CONFIGURE_ALERTS);
		HashMap<Long, ConfAlerta> lista = new HashMap<>();

		if (Files.notExists(path)) {
			return lista; // vacío
		}

		try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;
				String[] parts = line.split(",", -1);
				if (parts.length >= 4) {
					ConfAlerta alerta = new ConfAlerta();
					alerta.setChatId(Long.parseLong(parts[0]));
					alerta.setRatioNivel1(Double.parseDouble(parts[1]));
					alerta.setRatioNivel2(Double.parseDouble(parts[2]));
					alerta.setCuotaMinima(Double.parseDouble(parts[3]));
					lista.put(alerta.getChatId(), alerta);
				}
			}
		}

		return lista;
	}
	
	
	public static void escribirConfAlertasEnCsv(HashMap<Long, ConfAlerta> alertas) {
	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_CONFIGURE_ALERTS, false))) {
	        // Cabecera opcional:
	        // writer.write("chatId,market_id,sFechaPartido,evento");
	        // writer.newLine();

	       
	    	
	    	for (Map.Entry<Long, ConfAlerta> entry : alertas.entrySet()) {
	            Long clave = entry.getKey();
	            ConfAlerta valor = entry.getValue();
	            writer.write(
	                    valor.getChatId() + "," +
	                    valor.getRatioNivel1() + "," +
	                    valor.getRatioNivel2() + "," +
	                    valor.getCuotaMinima());
	            writer.newLine();
	        }

	        System.out.println("✅ CSV sobrescrito correctamente en: " + CSV_CONFIGURE_ALERTS);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
}
