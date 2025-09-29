import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class AlertaExclusionCSVUtils {

	/**
	 * Añade un objeto AlertaExclusion al CSV si no existe ya con mismo chatId y
	 * market_id.
	 */
	public static void addIfNotExists(AlertaExclusion alerta, String csvPath) throws IOException {
		Path path = Paths.get(csvPath);

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
				if (chatId.equals(alerta.getChatId()) && marketId.equals(alerta.getMarket_id())) {
					exists = true;
					break;
				}
			}
		}

		if (!exists) {
			// Formato CSV: chatId,market_id,sFechaPartido
			String newLine = alerta.getChatId() + "," + alerta.getMarket_id() + "," + alerta.getsFechaPartido()+ "," + alerta.getEvento();
			// Añadimos al final
			Files.write(path, (newLine + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
					StandardOpenOption.APPEND);
		}
	}

	/**
	 * Lee el CSV y devuelve una lista de AlertaExclusion.
	 */
	public static List<AlertaExclusion> loadFromCSV(String csvPath) throws IOException {
		Path path = Paths.get(csvPath);
		List<AlertaExclusion> lista = new ArrayList<>();

		if (Files.notExists(path)) {
			return lista; // vacío
		}

		try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;
				String[] parts = line.split(",", -1);
				if (parts.length >= 3) {
					AlertaExclusion alerta = new AlertaExclusion();
					alerta.setChatId(Long.parseLong(parts[0]));
					alerta.setMarket_id(parts[1]);
					alerta.setsFechaPartido(parts[2]);
					alerta.setEvento(parts[3]);
					lista.add(alerta);
				}
			}
		}

		return lista;
	}
}
