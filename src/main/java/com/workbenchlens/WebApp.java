package com.workbenchlens;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Browser interface for local Workbench Lens sessions. */
public final class WebApp {
    private static final int MAX_IMAGE_BYTES = 8 * 1024 * 1024;
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        if (System.getenv("WORKBENCH_DB_PASSWORD") == null || System.getenv("WORKBENCH_DB_PASSWORD").isBlank()) {
            System.err.println("Set WORKBENCH_DB_PASSWORD to the Oracle WORKBENCH password before starting Workbench Lens.");
            return;
        }
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", WebApp::servePage);
        server.createContext("/api/connect", WebApp::connect);
        server.createContext("/api/ask", WebApp::ask);
        server.createContext("/api/register", WebApp::register);
        server.createContext("/api/login", WebApp::login);
        server.createContext("/api/logout", WebApp::logout);
        server.createContext("/api/admin/users", WebApp::adminUsers);
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("Workbench Lens is available at http://localhost:" + port);
    }

    private static void servePage(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod()) || !"/".equals(exchange.getRequestURI().getPath())) {
            send(exchange, 404, "text/plain; charset=utf-8", "Not found"); return;
        }
        byte[] page = WebPage.HTML.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Security-Policy", "default-src 'self'; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self'; base-uri 'none'; frame-ancestors 'none'");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Referrer-Policy", "no-referrer");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        String token = sessionToken(exchange);
        if (token == null) token = UUID.randomUUID().toString();
        exchange.getResponseHeaders().set("Set-Cookie", "WLSESSION=" + token + "; HttpOnly; SameSite=Strict; Path=/");
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, page.length);
        try (var out = exchange.getResponseBody()) { out.write(page); }
    }

    private static void connect(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) { send(exchange, 405, "application/json", "{\"error\":\"POST required\"}"); return; }
        String token = sessionToken(exchange);
        if (token == null) { send(exchange, 403, "application/json", "{\"error\":\"Refresh the page and try again.\"}"); return; }
        try {
            JsonNode body = JSON.readTree(readLimited(exchange, 16 * 1024));
            if (body == null || !body.isObject()) throw new IOException("Invalid JSON body");
            String password = body.path("password").asText("");
            if (password.isBlank() || password.length() > 512) { send(exchange, 400, "application/json", "{\"error\":\"Enter your Oracle password.\"}"); return; }
            List<Tool> tools = new ToolRepository().loadTools(password);
            Session old = SESSIONS.get(token);
            SESSIONS.put(token, new Session(old == null ? null : old.user, tools));
            sendJson(exchange, 200, Map.of("connected", true, "toolCount", tools.size(), "tools", toolSummaries(tools)));
        } catch (SQLException e) {
            send(exchange, 400, "application/json", "{\"error\":\"Could not connect to Oracle. Check the password and local database, then try again.\"}");
        } catch (Exception e) {
            send(exchange, 400, "application/json", "{\"error\":\"Invalid connection request.\"}");
        }
    }

    private static void register(HttpExchange x) throws IOException {
        if (!"POST".equals(x.getRequestMethod())) { send(x,405,"application/json","{\"error\":\"POST required\"}"); return; }
        String token=sessionToken(x); if(token==null){send(x,403,"application/json","{\"error\":\"Refresh the page and try again.\"}");return;}
        char[] password=null;
        try {
            JsonNode b=JSON.readTree(readLimited(x,16*1024)); String username=b.path("username").asText("").trim();
            String email=b.path("email").asText("").trim(); password=b.path("password").asText("").toCharArray();
            if(!username.matches("[A-Za-z0-9_.-]{3,50}") || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || password.length<10){send(x,400,"application/json","{\"error\":\"Use a 3-50 character username, valid email, and password of at least 10 characters.\"}");return;}
            UserRepository.User u=new UserRepository().register(username,email,password); password=null;
            String authenticatedToken=UUID.randomUUID().toString(); SESSIONS.remove(token); SESSIONS.put(authenticatedToken,new Session(u,null)); setSessionCookie(x,authenticatedToken);
            sendJson(x,201,Map.of("username",u.username(),"role",u.role()));
        } catch(SQLException e){send(x,400,"application/json",JSON.writeValueAsString(Map.of("error",e.getMessage())));}
          catch(Exception e){send(x,400,"application/json","{\"error\":\"Registration failed. Check database setup and input.\"}");}
        finally {if(password!=null) java.util.Arrays.fill(password,'\0');}
    }

    private static void login(HttpExchange x) throws IOException {
        if (!"POST".equals(x.getRequestMethod())) { send(x,405,"application/json","{\"error\":\"POST required\"}"); return; }
        String token=sessionToken(x); if(token==null){send(x,403,"application/json","{\"error\":\"Refresh the page and try again.\"}");return;}
        char[] password=null;
        try {JsonNode b=JSON.readTree(readLimited(x,16*1024));password=b.path("password").asText("").toCharArray();
            UserRepository.User u=new UserRepository().authenticate(b.path("username").asText(""),password);password=null;
            String authenticatedToken=UUID.randomUUID().toString(); SESSIONS.remove(token); SESSIONS.put(authenticatedToken,new Session(u,null));setSessionCookie(x,authenticatedToken);
            sendJson(x,200,Map.of("username",u.username(),"role",u.role()));
        }catch(SQLException e){send(x,401,"application/json",JSON.writeValueAsString(Map.of("error",e.getMessage())));}
         catch(Exception e){send(x,400,"application/json","{\"error\":\"Login failed. Check the request and database connection.\"}");}
        finally {if(password!=null) java.util.Arrays.fill(password,'\0');}
    }

    private static void logout(HttpExchange x) throws IOException {String t=sessionToken(x);if(t!=null)SESSIONS.remove(t);String fresh=UUID.randomUUID().toString();setSessionCookie(x,fresh);sendJson(x,200,Map.of("loggedOut",true));}

    private static void setSessionCookie(HttpExchange x,String token) { x.getResponseHeaders().set("Set-Cookie","WLSESSION="+token+"; HttpOnly; SameSite=Strict; Path=/"); }

    private static void adminUsers(HttpExchange x) throws IOException {
        String t=sessionToken(x); Session s=t==null?null:SESSIONS.get(t);
        if(s==null||s.user==null||!"ADMIN".equals(s.user.role())){send(x,403,"application/json","{\"error\":\"Administrator access required.\"}");return;}
        try{sendJson(x,200,Map.of("users",new UserRepository().listUsers()));}
        catch(SQLException e){send(x,500,"application/json","{\"error\":\"Could not load users.\"}");}
    }

    private static void ask(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) { send(exchange, 405, "application/json", "{\"error\":\"POST required\"}"); return; }
        String token = sessionToken(exchange);
        Session session = token == null ? null : SESSIONS.get(token);
        if (session == null || session.user == null) { send(exchange, 401, "application/json", "{\"error\":\"Log in first.\"}"); return; }
        if (session.tools == null) { send(exchange, 401, "application/json", "{\"error\":\"Connect to Oracle catalogue first.\"}"); return; }
        JsonNode body;
        try {
            body = JSON.readTree(readLimited(exchange, MAX_IMAGE_BYTES + 512 * 1024));
            if (body == null || !body.isObject()) throw new IOException("Invalid JSON body");
        } catch (IOException e) {
            send(exchange, 400, "application/json", "{\"error\":\"Request was invalid or too large. Upload an image up to 8 MB.\"}");
            return;
        }
        try {
            String question = body.path("question").asText("").trim();
            String encoded = body.path("image").asText("");
            if (question.isBlank() || question.length() > 4000) { send(exchange, 400, "application/json", "{\"error\":\"Enter a question (up to 4,000 characters).\"}"); return; }
            if (encoded.isBlank() || encoded.length() > (MAX_IMAGE_BYTES * 4 / 3) + 16) { send(exchange, 400, "application/json", "{\"error\":\"Choose an image up to 8 MB.\"}"); return; }
            byte[] image;
            try { image = Base64.getDecoder().decode(encoded); }
            catch (IllegalArgumentException e) { send(exchange, 400, "application/json", "{\"error\":\"The uploaded image could not be read.\"}"); return; }
            if (image.length == 0 || image.length > MAX_IMAGE_BYTES || !isImage(image)) { send(exchange, 400, "application/json", "{\"error\":\"Upload a valid PNG, JPEG, GIF, or WebP image up to 8 MB.\"}"); return; }
            OllamaClient ollama = new OllamaClient();
            String description = ollama.ask("Identify the likely tool in this image. Describe visible features and likely purpose. If uncertain, say so. Do not guess the brand or exact model.", image);
            String answerPrompt = """
                    You are Workbench Lens. Answer the user's question using the Oracle catalogue and image estimate below.
                    Oracle entries are stored facts, but any status other than VERIFIED is unreviewed. The image estimate is AI-generated and unverified.
                    Do not treat an unverified claim as confirmed. If facts are absent, say so. Do not invent brand, model, or specifications. Give cautious safety guidance and refer to the manufacturer's manual for model-specific instructions.

                    Image estimate (UNVERIFIED AI-GENERATED):
                    %s

                    Oracle catalogue:
                    %s

                    User question:
                    %s
                    """.formatted(description, catalogueContext(session.tools), question);
            String answer = ollama.ask(answerPrompt, null);
            sendJson(exchange, 200, Map.of("description", description, "answer", answer, "imageStatus", "UNVERIFIED", "catalogue", toolSummaries(session.tools)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); send(exchange, 503, "application/json", "{\"error\":\"The local AI request was interrupted.\"}");
        } catch (IOException e) {
            send(exchange, 502, "application/json", "{\"error\":\"Could not reach local Ollama. Check that Ollama is running with minicpm-v available.\"}");
        }
    }

    private static List<Map<String, String>> toolSummaries(List<Tool> tools) {
        List<Map<String, String>> result = new ArrayList<>();
        for (Tool tool : tools) result.add(Map.of("name", safe(tool.getName()), "category", safe(tool.getCategory()), "group", safe(tool.getCatalogueGroup()), "purpose", safe(tool.getPurpose()), "safetyNotes", safe(tool.getSafetyNotes()), "status", safe(tool.getVerificationStatus())));
        return result;
    }

    private static String catalogueContext(List<Tool> tools) {
        StringBuilder result = new StringBuilder();
        for (Tool tool : tools) result.append("Tool: ").append(safe(tool.getName())).append("\nCategory: ").append(safe(tool.getCategory())).append("\nCatalogue group: ").append(safe(tool.getCatalogueGroup())).append("\nPurpose: ").append(safe(tool.getPurpose())).append("\nSafety notes: ").append(safe(tool.getSafetyNotes())).append("\nVerification status: ").append(safe(tool.getVerificationStatus())).append("\n\n");
        return result.toString();
    }

    private static String safe(String value) { return value == null ? "Not provided" : value; }

    private static String sessionToken(HttpExchange exchange) {
        String cookie = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookie == null) return null;
        for (String part : cookie.split(";")) { String item = part.trim(); if (item.startsWith("WLSESSION=")) return item.substring(10); }
        return null;
    }

    private static byte[] readLimited(HttpExchange exchange, int max) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192]; int read;
        try (var input = exchange.getRequestBody()) {
            while ((read = input.read(buffer)) != -1) { if (out.size() + read > max) throw new IOException("Request too large"); out.write(buffer, 0, read); }
        }
        return out.toByteArray();
    }

    private static boolean isImage(byte[] b) {
        return b.length >= 12 && ((b[0] & 255) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G')
                || b.length >= 3 && (b[0] & 255) == 0xff && (b[1] & 255) == 0xd8 && (b[2] & 255) == 0xff
                || b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F'
                || b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
    }

    private static void sendJson(HttpExchange exchange, int status, Object value) throws IOException { send(exchange, status, "application/json; charset=utf-8", JSON.writeValueAsString(value)); }
    private static void send(HttpExchange exchange, int status, String type, String value) throws IOException {
        byte[] data = value.getBytes(StandardCharsets.UTF_8); exchange.getResponseHeaders().set("Content-Type", type); exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, data.length); try (var out = exchange.getResponseBody()) { out.write(data); }
    }

    private record Session(UserRepository.User user, List<Tool> tools) { }
}
