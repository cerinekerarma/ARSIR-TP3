import java.io.*;
import java.net.*;
import java.nio.file.*;

public class ServeurHTTP {
    private static final int PORT = 8080;
    private static final String BASE_DIR = "C:\\Users\\thiba\\IdeaProjects\\ARSIR-TP3\\TP3\\src";

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
            String contextLine = in.readLine();
            if (requestLine == null) return;

            System.out.println("Requête reçue : " + requestLine);

            // Récupérer la méthode et le fichier demandé
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                envoyerReponse(out, "400 Bad Request", "Requête invalide.");
                return;
            }

            String[] partsContext = contextLine.split(":");

            if (partsContext.length < 2) {
                envoyerReponse(out, "400 Bad Request", "Requête invalide.");
                return;
            }

            String key = partsContext[0];
            String value = partsContext[0];

            String method = parts[0];
            String chemin = parts[1].equals("/") ? "index.html" : parts[1].substring(1);

            // Vérifier la méthode HTTP
            if (!method.equals("GET")) {
                envoyerReponse(out, "405 Method Not Allowed", "Méthode non autorisée.");
                return;
            }


            System.out.println(key);
            if (!key.equals("Host")){
                envoyerReponse(out, "400 Bad Request", "Requête invalide.");
                return;
            }

            // Traiter la requête GET
            try {
                Path fichier = Paths.get(BASE_DIR, chemin);
                if (Files.exists(fichier) && !Files.isDirectory(fichier)) {
                    byte[] contenu = Files.readAllBytes(fichier);
                    envoyerReponse(out, "200 OK", new String(contenu), "text/html");
                } else {
                    envoyerReponse(out, "404 Not Found", "Page non trouvée.");
                }
            } catch (IOException e) {
                // Gestion de l'erreur interne du serveur (500)
                envoyerReponse(out, "500 Internal Server Error", "Erreur interne du serveur.");
                e.printStackTrace();  // Afficher l'erreur dans la console du serveur
            }

        } catch (IOException e) {
            // Gestion d'une exception non prévue dans la lecture de la requête
            try {
                OutputStream out = client.getOutputStream();
                envoyerReponse(out, "500 Internal Server Error", "Erreur interne du serveur.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
