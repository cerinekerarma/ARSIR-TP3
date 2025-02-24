import java.io.*;
import java.net.*;
import java.nio.file.*;

public class ServeurHTTP {
    private static final int PORT = 8080;
    private static final String BASE_DIR = "src";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur HTTP démarré sur le port " + PORT);
            System.out.println("IP: " + InetAddress.getLocalHost().getHostAddress());

            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> gererClient(client)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void gererClient(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             OutputStream out = client.getOutputStream()) {

            String requestLine = in.readLine();
            if (requestLine == null) return;

            System.out.println("Requête reçue : " + requestLine);

            // Récupérer le fichier demandé
            String[] parts = requestLine.split(" ");
            if (parts.length < 2 || !parts[0].equals("GET")) {
                envoyerReponse(out, "400 Bad Request", "Requête invalide.");
                return;
            }

            String chemin = parts[1].equals("/") ? "index.html" : parts[1].substring(1);
            Path fichier = Paths.get(BASE_DIR, chemin);

            if (Files.exists(fichier) && !Files.isDirectory(fichier)) {
                byte[] contenu = Files.readAllBytes(fichier);
                envoyerReponse(out, "200 OK", new String(contenu), "text/html");
            } else {
                envoyerReponse(out, "404 Not Found", "Page non trouvée.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void envoyerReponse(OutputStream out, String status, String contenu) throws IOException {
        envoyerReponse(out, status, contenu, "text/plain");
    }

    private static void envoyerReponse(OutputStream out, String status, String contenu, String type) throws IOException {
        PrintWriter writer = new PrintWriter(out, true);
        writer.println("HTTP/1.1 " + status);
        writer.println("Content-Type: " + type);
        writer.println("Content-Length: " + contenu.length());
        writer.println();
        writer.println(contenu);
    }
}
