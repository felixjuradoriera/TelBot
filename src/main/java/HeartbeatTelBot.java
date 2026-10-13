import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import conf.Configuracion;
import utils.HealthCheck;

/**
 * Heartbeat de readiness para TelBot. Cada 5 minutos comprueba que el bot
 * realmente PODRÍA atender una petición (no solo que el proceso esté vivo):
 *   - que la API de Telegram responde a getMe con nuestro token (token válido + red + Telegram OK)
 *   - que el directorio de configuración/persistencia (BASE_DIR) es accesible
 * Si todo va bien -> ping OK a HealthChecks; si algo falla -> ping /fail.
 * Si el proceso muere o el PC se apaga, deja de llegar el ping y HealthChecks avisa.
 */
public class HeartbeatTelBot {

	private static final long INTERVALO_MS = 5 * 60 * 1000L; // 5 min

	public static void iniciar() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						comprobar();
					} catch (Throwable e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(INTERVALO_MS);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}, "heartbeat-telbot");
		t.setDaemon(true);
		t.start();
		System.out.println("Heartbeat TelBot iniciado (cada 5 min)");
	}

	private static void comprobar() {
		boolean ok = true;
		StringBuilder motivo = new StringBuilder();

		// 1) readiness real: ¿responde Telegram a getMe con nuestro token?
		if (!telegramGetMeOk()) {
			ok = false;
			motivo.append("getMe Telegram KO; ");
		}

		// 2) ¿accesible el directorio de configuración/persistencia?
		File base = new File(Configuracion.BASE_DIR);
		if (!base.isDirectory() || !base.canRead()) {
			ok = false;
			motivo.append("BASE_DIR inaccesible (").append(Configuracion.BASE_DIR).append("); ");
		}

		if (ok) {
			HealthCheck.pingTelbotOk();
		} else {
			System.out.println("TelBot readiness KO: " + motivo);
			HealthCheck.pingTelbotFail();
		}
	}

	private static boolean telegramGetMeOk() {
		if (Configuracion.BOT_TOKEN == null || Configuracion.BOT_TOKEN.isEmpty()) {
			return false;
		}
		try {
			URL url = new URL("https://api.telegram.org/bot" + Configuracion.BOT_TOKEN + "/getMe");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			int code = conn.getResponseCode();
			if (code != 200) {
				try { if (conn.getErrorStream() != null) conn.getErrorStream().close(); } catch (Exception ig) { }
				return false;
			}
			StringBuilder sb = new StringBuilder();
			try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
				String line;
				while ((line = in.readLine()) != null) {
					sb.append(line);
				}
			}
			return sb.indexOf("\"ok\":true") >= 0;
		} catch (Exception e) {
			return false;
		}
	}
}
