import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Redirige System.out y System.err para que cada línea impresa lleve delante
 * un timestamp [dd/MM/yyyy HH:mm:ss]. Se instala una sola vez al arrancar.
 * Así todas las trazas (también las de las clases de Comunes) salen fechadas,
 * sin tener que tocar cada println.
 */
public class LogTimestamp {

	private static boolean instalado = false;

	public static synchronized void instalar() {
		if (instalado) return;
		System.setOut(new PrintStream(new PrefijoFechaStream(System.out), true));
		System.setErr(new PrintStream(new PrefijoFechaStream(System.err), true));
		instalado = true;
	}

	/** Inserta el timestamp al principio de cada línea (a nivel de bytes). */
	private static class PrefijoFechaStream extends FilterOutputStream {
		private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		private boolean inicioLinea = true;

		PrefijoFechaStream(OutputStream out) {
			super(out);
		}

		private void escribirPrefijo() throws IOException {
			out.write(("[" + LocalDateTime.now().format(FMT) + "] ").getBytes());
		}

		@Override
		public synchronized void write(int b) throws IOException {
			if (inicioLinea && b != '\n') {
				escribirPrefijo();
				inicioLinea = false;
			}
			out.write(b);
			if (b == '\n') {
				inicioLinea = true;
			}
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) throws IOException {
			for (int i = 0; i < len; i++) {
				write(b[off + i]);
			}
		}
	}
}
