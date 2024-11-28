package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.dtos.RutaDTO;
import ar.edu.utn.dds.k3003.model.Request.ArreglarHeladeraRequest;
import ar.edu.utn.dds.k3003.model.Request.SuscripcionADesperfectoRequest;
import ar.edu.utn.dds.k3003.model.Request.SuscripcionCantViandasRequest;
import ar.edu.utn.dds.k3003.model.Request.UpdateFormasColaborarRequest;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborar;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborarActualizadoEnum;
import ar.edu.utn.dds.k3003.repositories.SuscripcionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.h2.util.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.json.*;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TelegramBot extends TelegramLongPollingBot {

    private String apiColaboradores = System.getenv().getOrDefault("API_COLABORADORES", "http://localhost:8082");
    private String apiHeladeras = System.getenv().getOrDefault("API_HELADERAS", "http://localhost:8080");
    private String apiLogistica = System.getenv().getOrDefault("API_LOGISTICA", "http://localhost:8083");
    private String apiViandas = System.getenv().getOrDefault("API_VIANDAS", "http://localhost:8081");
    private Fachada fachada;
    private Map<String, Set<FormaDeColaborarActualizadoEnum>> formasPorUsuario;
    private Map<String, String> userState;

    private String emjTrasportador = "\uD83D\uDE9A";
    private String emjTecnico = "\uD83E\uDDD1\u200D\uD83D\uDD27";
    private String emjDonadorViandas = "\uD83C\uDF72";
    private String emjDonadorDinero = "\uD83D\uDCB0";
    private String emjConfirmar = "✅";

    public TelegramBot(Fachada fachada) throws TelegramApiException {

        //super();
        this.fachada = fachada;
        this.formasPorUsuario =  new HashMap<>();
        this.userState = new HashMap<>();

        final TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
// Se registra el bot
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) { // se invoca cuando el bot recibe un mensaje

        if(update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().equals("/start")){
             String chatId = update.getMessage().getChatId().toString();
             if(fachada.existeChat(chatId)){
                 String nombre = fachada.buscarXId(Long.parseLong(chatId)).getNombre();
                 sendMessage(chatId,"¡Bienvenido de nuevo, " + nombre + "!");
                 userState.put(chatId, "default");
                 mostrarMenuPrincipal(chatId);
             }else{
                 sendMessage(chatId,"¡Bienvenido! Por favor escriba su nombre:");
                 userState.put(chatId,"nombreInput");

             }


         }else if(update.hasMessage() && update.getMessage().hasText()){
            String chatId = update.getMessage().getChatId().toString();
            switch(userState.get(chatId)){
                case "id_heladera":
                    Integer id_heladera = Integer.parseInt(update.getMessage().getText());
                    reportarHeladera(chatId,id_heladera);
                    mostrarMenuColaboradores(chatId);
                    break;
                case "codigoQR":
                    String codigoQR = update.getMessage().getText();
                    agregarVianda(chatId, codigoQR);
                    continuarAgregarVianda(chatId);
                    break;
                case "suscQuedan":
                    String[] partes = update.getMessage().getText().split(";",2);
                    Integer heladeraId = Integer.parseInt(partes[0].trim());
                    Integer valor = Integer.parseInt(partes[1].trim());
                    suscQuedan(chatId,heladeraId,valor);
                    mostrarMenuColaboradores(chatId);
                    break;
                case "suscFaltan":
                    String[] partesFaltan = update.getMessage().getText().split(";",2);
                    Integer heladeraIdFaltan = Integer.parseInt(partesFaltan[0].trim());
                    Integer valorFaltan = Integer.parseInt(partesFaltan[1].trim());
                    suscFaltan(chatId,heladeraIdFaltan,valorFaltan);
                    mostrarMenuColaboradores(chatId);
                    break;
                case "suscDesperfecto":
                    Integer id_heladeraSusc = Integer.parseInt(update.getMessage().getText());
                    suscDesperfectos(chatId,id_heladeraSusc);
                    mostrarMenuColaboradores(chatId);
                    break;
                case "ocupacionViandas":
                    Integer id_heladeraOcupacion = Integer.parseInt(update.getMessage().getText());
                    obtenerOcupacionHeladeras(chatId, id_heladeraOcupacion);
                    continuarOcupacionHeladeras(chatId);
                    break;
                case "incidencia_id":
                    Integer id_incidente = Integer.parseInt(update.getMessage().getText());
                    cerrarIncidencia(chatId,id_incidente);
                    mostrarMenuColaboradores(chatId);
                    break;
                case "depositarVianda":
                    String[] partesDepositar = update.getMessage().getText().split(";",2);
                    String codigoQRDepositar = partesDepositar[0].trim();
                    Integer heladeraIdDepositar = Integer.parseInt(partesDepositar[1].trim());
                    depositarVianda(chatId,codigoQRDepositar,heladeraIdDepositar);
                    mostrarMenuLogistica(chatId);
                    break;
                case "retirarVianda":
                    String[] partesRetirar = update.getMessage().getText().split(";",2);
                    String codigoQRRetirar = partesRetirar[0].trim();
                    Integer heladeraIdRetirar = Integer.parseInt(partesRetirar[1].trim());
                    retirarVianda(chatId,codigoQRRetirar,heladeraIdRetirar);
                    mostrarMenuLogistica(chatId);
                    break;
                case "crearRuta":
                    String[] partesRuta = update.getMessage().getText().split(";",2);
                    Integer heladeraOrigen = Integer.parseInt(partesRuta[0].trim());
                    Integer heladeraDestino = Integer.parseInt(partesRuta[1].trim());
                    crearRuta(chatId,heladeraOrigen,heladeraDestino);
                    mostrarMenuLogistica(chatId);
                    break;

                case "depositarTraslado":
                    Long idTrasladoDepositar = Long.parseLong(update.getMessage().getText());
                    depositarTraslado(chatId, idTrasladoDepositar);
                    mostrarMenuLogistica(chatId);
                    break;

                case "retirarTraslado":
                    Long idTrasladoRetirar = Long.parseLong(update.getMessage().getText());
                    retirarTraslado(chatId, idTrasladoRetirar);
                    mostrarMenuLogistica(chatId);
                    break;
                case "desuscribirse":
                    Integer idSuscripcion = Integer.parseInt(update.getMessage().getText());
                    desuscribirse(chatId,idSuscripcion);
                    mostrarMenuColaboradores(chatId);
                    break;
                case "nombreInput":
                    String nombre = update.getMessage().getText().trim();
                    fachada.agregarDesdeBot(chatId,nombre);
                    sendMessage(chatId,"\uD83D\uDE80 Gracias, "+nombre+". Ahora seleccione cómo desea colaborar:");
                    var message = new SendMessage();
                    message.setChatId(chatId);
                    agregarFormaColaborar(message);
                    execute(message);
                    break;

                default:
                    sendMessage(chatId,"Para iniciar el bot escriba /start");
            }
        } else if(update.hasCallbackQuery()){
             String callback = update.getCallbackQuery().getData();
             String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

             SendMessage message = new SendMessage();
             message.setChatId(chatId);
             //callback.

             switch (callback){
                 case "colaboradores":
                     message.setText("Selecciona ");
                     this.botonesColaboradores(message);
                     execute(message);
                     break;
                 case "logistica":
                     message.setText("Seleccionaste logistica");
                     this.botonesLogistica(message);
                     execute(message);
                     break;
                 case "heladeras":
                     message.setText("Seleccionaste heladeras");
                     //ver las heladeras en una zona TODO
                     this.botonesHeladeras(message);
                     execute(message);
                     break;
                 case "viandas":
                     message.setText("Seleccionaste viandas");
                     this.botonesViandas(message);
                     execute(message);
                     break;
                 case "datosColaborador":
                     this.consultarPuntos(chatId);
                     mostrarMenuColaboradores(chatId);
                     break;

                 case "formaColaborar":
                     this.agregarFormaColaborar(message);
                     execute(message);

                     break;
                 case "donador_viandas":
                     formasPorUsuario.putIfAbsent(chatId, new HashSet<>());
                     Set<FormaDeColaborarActualizadoEnum> seleccionVianda = formasPorUsuario.get(chatId);
                     seleccionVianda.add(FormaDeColaborarActualizadoEnum.DONADOR_VIANDAS);
                     formasPorUsuario.put(chatId,seleccionVianda);
                    break;
                 case "donador_dinero":
                     formasPorUsuario.putIfAbsent(chatId, new HashSet<>());
                     Set<FormaDeColaborarActualizadoEnum> seleccionDinero = formasPorUsuario.get(chatId);
                     seleccionDinero.add(FormaDeColaborarActualizadoEnum.DONADOR_DINERO);
                     formasPorUsuario.put(chatId,seleccionDinero);
                     break;
                 case "tecnico":
                     formasPorUsuario.putIfAbsent(chatId, new HashSet<>());
                     Set<FormaDeColaborarActualizadoEnum> seleccionTecnico = formasPorUsuario.get(chatId);
                     seleccionTecnico.add(FormaDeColaborarActualizadoEnum.TECNICO);
                     formasPorUsuario.put(chatId,seleccionTecnico);
                     break;
                 case "transportador":
                     formasPorUsuario.putIfAbsent(chatId, new HashSet<>());
                     Set<FormaDeColaborarActualizadoEnum> seleccionTransportador = formasPorUsuario.get(chatId);
                     seleccionTransportador.add(FormaDeColaborarActualizadoEnum.TRANSPORTADOR);
                     formasPorUsuario.put(chatId,seleccionTransportador);
                     break;
                 case "confirmar_forma_colaborar":
                     if (!formasPorUsuario.containsKey(chatId)) break; // si tocas confirmar sin seleccionar ninguna forma
                     modificarFormas(formasPorUsuario.get(chatId),chatId);
                     //if (!userState.containsKey(chatId)) break;
                     if(userState.get(chatId).equalsIgnoreCase("nombreInput")){
                         mostrarMenuPrincipal(chatId);
                     }else{
                         mostrarMenuColaboradores(chatId);
                     }
                     break;

                 case "reportarHeladera":
                     userState.put(chatId,"id_heladera");
                     sendMessage(chatId,"Ingrese el id de la Heladera a reportar.");
                     break;
                 case "verIncidentes":
                     verIncidentes(chatId);
                     mostrarMenuColaboradores(chatId);
                     break;
                 case "agregarVianda":
                     userState.put(chatId,"codigoQR");
                     sendMessage(chatId,"Ingrese el código QR de la vianda a agregar.");
                     break;
                 case "depositarVianda":
                     userState.put(chatId,"depositarVianda");
                     sendMessage(chatId,"Ingrese el código QR de la vianda y el ID de la heladera, separados por ';'\nPor ejemplo: qr50;2");
                     break;
                 case "retirarVianda":
                     userState.put(chatId,"retirarVianda");
                     sendMessage(chatId,"Ingrese el código QR de la vianda y el ID de la heladera, separados por ';'\nPor ejemplo: qr50;2");
                     break;
                 case "suscribirseAHeladera":
                     suscripcionAHeladera(message);
                     execute(message);
                     break;
                 case "suscQuedan":
                     userState.put(chatId,"suscQuedan");
                     sendMessage(chatId,"Ingrese el id de la heladera a la que suscribirse y el valor de la notificacion separados por un ;");
                     break;
                 case "suscFaltan":
                     userState.put(chatId,"suscFaltan");
                     sendMessage(chatId,"Ingrese el id de la heladera a la que suscribirse y el valor de la notificacion separados por un ;");
                     break;
                 case "suscDesperfecto":
                     userState.put(chatId,"suscDesperfecto");
                     sendMessage(chatId,"Ingrese el id de la heladera a la que suscribirse");
                     break;
                 case "ocupacionViandas":
                     userState.put(chatId,"ocupacionViandas");
                     sendMessage(chatId,"Ingrese el id de la heladera");
                     break;
                 case "cerrarIncidencia":
                     userState.put(chatId,"incidencia_id");
                     sendMessage(chatId,"Ingrese el id de la incidencia a cerrar");
                     break;
                 case "crearRuta":
                     userState.put(chatId,"crearRuta");
                     sendMessage(chatId,"Ingrese el id de la heladera origen y la heladera destino separadas por un ;");
                     break;
                 case "depositarTraslado":
                     userState.put(chatId,"depositarTraslado");
                     sendMessage(chatId,"Ingrese el id del traslado a depositar");
                     break;
                 case "retirarTraslado":
                     userState.put(chatId,"retirarTraslado");
                     sendMessage(chatId,"Ingrese el id del traslado a retirar");
                     break;
                 case "trasladosAsignados":
                     trasladosAsignados(chatId);
                     mostrarMenuColaboradores(chatId);
                     break;

                 case "verSuscripciones":
                     verSuscripciones(chatId);
                     mostrarMenuColaboradores(chatId);
                     break;
                 case "desuscribirse":
                     userState.put(chatId,"desuscribirse");
                     sendMessage(chatId,"Ingrese el ID de la suscripción a desuscribirse.");
                     break;
                 case "retirosDelDia":
                     retirosDelDia(chatId);
                     mostrarMenuLogistica(chatId);
                     break;

                 case "menuPrincipal":
                     mostrarMenuPrincipal(chatId);
                     break;
                 default:
                     message.setText("Opcion no reconocida");
                     execute(message);
                     break;
             }


         }



    }

    @SneakyThrows
    private void verIncidentes(String chatId){
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(apiColaboradores+"/incidentes");
        HttpResponse execute = httpClient.execute(httpGet);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        JsonReader jsonReader = Json.createReader(new StringReader(rta));
        JsonArray root = jsonReader.readArray();
        jsonReader.close();
        sendMessage(chatId,"Los incidentes actuales son:");
        for(int i = 0; i<root.size();i++){
            JsonObject elemento = root.getJsonObject(i);
            int id = elemento.getInt("id");
            int heladeraId = elemento.getInt("heladeraId");
            String fechaIncidente = elemento.getString("fechaIncidente");
            String tipoIncidente = elemento.getString("tipoIncidente");
            String estado = elemento.getString("estado");

            // Construir el mensaje con la información extraída
            String mensaje = String.format("\uD83D\uDC40 Incidente:\n\uD83D\uDC49 ID: %d\n❄\uFE0F Heladera ID: %d\n\uD83D\uDCC5 Fecha: %s\n⚠\uFE0F Tipo: %s\n\uD83D\uDCDC Estado: %s",
                    id, heladeraId, fechaIncidente, tipoIncidente, estado);
            sendMessage(chatId,mensaje);
        }

    }
    private void continuarAgregarVianda(String chatId) throws TelegramApiException {
        InlineKeyboardMarkup markup= new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> fila1 = new ArrayList<>();
        fila1.add(createButton("\uD83C\uDF71 Agregar otra vianda","agregarVianda"));
        filas.add(fila1);
        List<InlineKeyboardButton> fila2 = new ArrayList<>();
        fila2.add(createButton("\uD83C\uDFE0 Volver al menú","menuPrincipal"));
        filas.add(fila2);

        // Agregar botones: Ir al inicio, Salir capaz?

        markup.setKeyboard(filas);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Selecciona ");
        message.setReplyMarkup(markup);
        execute(message);

    }

    private void continuarOcupacionHeladeras(String chatId) throws TelegramApiException {
        InlineKeyboardMarkup markup= new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> fila1 = new ArrayList<>();
        fila1.add(createButton("\uD83E\uDDCA \uD83C\uDF71 Ver ocupación de otra heladera","ocupacionViandas"));
        filas.add(fila1);
        List<InlineKeyboardButton> fila2 = new ArrayList<>();
        fila2.add(createButton("\uD83C\uDFE0 Volver al menú","menuPrincipal"));
        filas.add(fila2);

        // Agregar botones: Ir al inicio, Salir capaz?

        markup.setKeyboard(filas);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Selecciona ");
        message.setReplyMarkup(markup);
        execute(message);

    }

    private void agregarVianda(String chatId, String codigoQR) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiViandas + "/viandas");


        String jsonBody = """
            {
                "codigoQR": "%s",
                "colaboradorId": "%s"
            }
        """.formatted(codigoQR, chatId);
        System.out.println(jsonBody);
        StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");

        HttpResponse execute = httpClient.execute(httpPost);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        if (execute.getStatusLine().getStatusCode() == 400){
            sendMessage(chatId, rta);
            return;
        }
        JsonReader jsonReader = Json.createReader(new StringReader(rta));
        JsonObject root = jsonReader.readObject();
        jsonReader.close();

        int idVianda = root.getInt("id");
        String fechaElaboracion = root.getString("fechaElaboracion");
        String estadoVianda = root.getString("estado");
        // {"id":1,"codigoQR":"qr55","fechaElaboracion":"2024-11-27T18:49:46Z","estado":"PREPARADA","colaboradorId":7617688664,"heladeraId":null}

        sendMessage(chatId, "✅ Vianda agregada correctamente\n\uD83D\uDC49 ID Vianda: "+idVianda+"\n\uD83D\uDCC5 Fecha de elaboración: "+fechaElaboracion+"\n\uD83D\uDCA1 Estado: " + estadoVianda);
    }

    private void consultarPuntos(String chatId) throws IOException {
        Long idColaborador = fachada.obtenerIdColaborador(chatId);
        HttpClient httpClient = HttpClients.createDefault();
        System.out.println("URL: " + this.apiColaboradores + "/colaboradores/" + idColaborador);
        HttpGet httpGet = new HttpGet(
                this.apiColaboradores + "/colaboradores/" + idColaborador);

        HttpResponse execute = httpClient.execute(httpGet);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);


        JsonReader jsonReader = Json.createReader(new StringReader(rta));
        JsonObject root = jsonReader.readObject();
        jsonReader.close();

        // Extraer valores
        JsonArray jsonArray = root.getJsonArray("formas");

        Set<String> formas = new HashSet<>();
        for (int i =  0; i < jsonArray.size(); i++) {
            formas.add(jsonArray.get(i).toString().trim().replace("\"", ""));
        }



        String nombreColaborador = root.getString("nombre");
        sendMessage(chatId, "\uD83D\uDC49 ID Colaborador: " + chatId);
        sendMessage(chatId, "\uD83E\uDDD1 Nombre Colaborador: " + nombreColaborador);
        System.out.println(formas);
        // ["TRANSPORTADOR", "TECNICO"]
        mostrarFormasDeColaborar(chatId, formas);

        Double puntos = (double) root.getInt("puntos");
        sendMessage(chatId, "\uD83D\uDCAF Puntos de colaborador: " + puntos);
        // {"id":7617688664,"nombre":"Federico","formas":["TECNICO","DONADOR_DINERO"]}
    }

    private void agregarFormaColaborar(SendMessage message){

        InlineKeyboardMarkup markup= new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> fila1 = new ArrayList<>();
        List<InlineKeyboardButton> fila2 = new ArrayList<>();
        List<InlineKeyboardButton> fila3 = new ArrayList<>();
        List<InlineKeyboardButton> fila4 = new ArrayList<>();
        List<InlineKeyboardButton> fila5 = new ArrayList<>();
        fila1.add(createButton(emjDonadorViandas + "Donador de viandas","donador_viandas"));
        fila2.add(createButton(emjDonadorDinero + "Donador de dinero","donador_dinero"));
        fila3.add(createButton(emjTecnico + "Técnico","tecnico"));
        fila4.add(createButton(emjTrasportador + "Transportador","transportador"));
        fila5.add(createButton(emjConfirmar + "Confirmar", "confirmar_forma_colaborar"));
        filas.add(fila1);
        filas.add(fila2);
        filas.add(fila3);
        filas.add(fila4);
        filas.add(fila5);
        markup.setKeyboard(filas);
        message.setReplyMarkup(markup);
        message.setText("Agregando forma de colaborar");


    }

    private void suscripcionAHeladera(SendMessage message){
        message.setText("Seleccione la suscripcion que desea:");
        InlineKeyboardMarkup markup= new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> fila1 = new ArrayList<>();
        List<InlineKeyboardButton> fila2 = new ArrayList<>();
        List<InlineKeyboardButton> fila3 = new ArrayList<>();
        fila1.add(createButton("\uD83D\uDC49 Suscripción a quedan viandas","suscQuedan"));
        fila2.add(createButton("\uD83D\uDC49 Suscripción a faltan viandas","suscFaltan"));
        fila3.add(createButton("\uD83D\uDC49 Suscripción a desperfecto","suscDesperfecto"));
        filas.add(fila1);
        filas.add(fila2);
        filas.add(fila3);
        markup.setKeyboard(filas);
        message.setReplyMarkup(markup);
    }

    private void botonesColaboradores(SendMessage message){
        InlineKeyboardMarkup markup= new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> fila1 = new ArrayList<>();
        fila1.add(createButton("Obtener datos \uD83D\uDCCA","datosColaborador"));
        filas.add(fila1);
        List<InlineKeyboardButton> fila2 = new ArrayList<>();
        fila2.add(createButton("Cambiar formas de colaborar \uD83D\uDD04","formaColaborar"));
        filas.add(fila2);
        List<InlineKeyboardButton> fila3 = new ArrayList<>();
        fila3.add(createButton("Reportar heladera \uD83E\uDDCA ⚠\uFE0F","reportarHeladera")); //🗄️
        filas.add(fila3);
        List<InlineKeyboardButton> fila4 = new ArrayList<>();
        fila4.add(createButton("Ver Incidentes \uD83D\uDEA8","verIncidentes"));
        filas.add(fila4);
        List<InlineKeyboardButton> fila5 = new ArrayList<>();
        fila5.add(createButton("Suscribirse a eventos de Heladera \uD83E\uDDCA\uD83D\uDD14","suscribirseAHeladera"));
        filas.add(fila5);
        List<InlineKeyboardButton> fila6 = new ArrayList<>();
        fila6.add(createButton("Cerrar una incidencia \uD83D\uDEA8 \uD83D\uDD12","cerrarIncidencia"));
        filas.add(fila6);
        List<InlineKeyboardButton> fila7 = new ArrayList<>();
        fila7.add(createButton("Ver mis traslados asignados \uD83D\uDE9A\uD83D\uDCCB","trasladosAsignados"));
        filas.add(fila7);

        List<InlineKeyboardButton> fila8 = new ArrayList<>();
        fila8.add(createButton("Ver suscripciones \uD83D\uDD0D \uD83D\uDD14","verSuscripciones"));
        filas.add(fila8);
        List<InlineKeyboardButton> fila9 = new ArrayList<>();
        fila9.add(createButton("Desuscribirse ❌\uD83D\uDD14","desuscribirse"));
        filas.add(fila9);

        List<InlineKeyboardButton> fila10 = new ArrayList<>();
        fila10.add(createButton("\uD83C\uDFE0 Volver al menú","menuPrincipal"));
        filas.add(fila10);

        // Agregar botones: Ir al inicio, Salir capaz?

        markup.setKeyboard(filas);

        message.setReplyMarkup(markup);


    }

    private void botonesViandas(SendMessage message){
        InlineKeyboardMarkup markup= new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> fila1 = new ArrayList<>();
        fila1.add(createButton("Agregar vianda \uD83C\uDF71","agregarVianda"));
        filas.add(fila1);

        List<InlineKeyboardButton> fila2 = new ArrayList<>();
        fila2.add(createButton("\uD83C\uDFE0 Volver al menú","menuPrincipal"));
        filas.add(fila2);

        // Agregar botones: Ir al inicio, Salir capaz?

        markup.setKeyboard(filas);

        message.setReplyMarkup(markup);


    }

    private void botonesHeladeras(SendMessage message){
        InlineKeyboardMarkup markup= new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> fila1 = new ArrayList<>();
        fila1.add(createButton("Ver ocupación de viandas \uD83E\uDDCA \uD83C\uDF71","ocupacionViandas"));
        filas.add(fila1);

        List<InlineKeyboardButton> fila2 = new ArrayList<>();
        fila2.add(createButton("\uD83C\uDFE0 Volver al menú","menuPrincipal"));
        filas.add(fila2);

        // Agregar botones: Ir al inicio, Salir capaz?

        markup.setKeyboard(filas);

        message.setReplyMarkup(markup);


    }

    private void botonesLogistica(SendMessage message){
        InlineKeyboardMarkup markup= new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> fila1 = new ArrayList<>();
        fila1.add(createButton("Depositar vianda \uD83C\uDF71 ➕","depositarVianda"));
        filas.add(fila1);

        List<InlineKeyboardButton> fila2 = new ArrayList<>();
        fila2.add(createButton("Retirar vianda \uD83C\uDF71 ➖","retirarVianda"));
        filas.add(fila2);

        List<InlineKeyboardButton> fila3 = new ArrayList<>();
        fila3.add(createButton("Crear una ruta \uD83D\uDEE3\uFE0F","crearRuta"));
        filas.add(fila3);

        List<InlineKeyboardButton> fila4 = new ArrayList<>();
        fila4.add(createButton("Depositar traslado  \uD83D\uDE9A ➕","depositarTraslado"));
        filas.add(fila4);

        List<InlineKeyboardButton> fila5 = new ArrayList<>();
        fila5.add(createButton("Retirar traslado \uD83D\uDE9A ➖","retirarTraslado"));
        filas.add(fila5);

        List<InlineKeyboardButton> fila6 = new ArrayList<>();
        fila6.add(createButton("Retiros del dia \uD83D\uDCC5","retirosDelDia"));
        filas.add(fila6);

        List<InlineKeyboardButton> fila7 = new ArrayList<>();
        fila7.add(createButton("\uD83C\uDFE0 Volver al menú","menuPrincipal"));
        filas.add(fila7);

        // Agregar botones: Ir al inicio, Salir capaz?

        markup.setKeyboard(filas);

        message.setReplyMarkup(markup);


    }

    private void depositarVianda(String chatId, String codigoQRDepositar, int heladeraIdDepositar) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiLogistica + "/depositar");


        String jsonBody = """
            {
                "codigoQR": "%s",
                "colaboradorId": "%s",
                "heladeraId": "%s"
            }
        """.formatted(codigoQRDepositar, chatId, heladeraIdDepositar);
        System.out.println(jsonBody);
        StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");

        HttpResponse execute = httpClient.execute(httpPost);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        if (execute.getStatusLine().getStatusCode() == 400){
            sendMessage(chatId, "❌ " + rta);
            return;
        }

        sendMessage(chatId, "✅ Vianda '"+codigoQRDepositar+"' depositada correctamente en heladera " +heladeraIdDepositar);
    }

    private void retirarVianda(String chatId, String codigoQRRetirar, int heladeraIdRetirar) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiLogistica + "/retirar");


        String jsonBody = """
            {
                "qrVianda": "%s",
                "heladeraId": "%s"
            }
        """.formatted(codigoQRRetirar, heladeraIdRetirar);
        System.out.println(jsonBody);
        StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");

        HttpResponse execute = httpClient.execute(httpPost);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        sendMessage(chatId,rta);
    }

    private void depositarTraslado(String chatId, Long idTrasladoDepositar) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(this.apiLogistica + "/depositar/" + idTrasladoDepositar);

        HttpResponse execute = httpClient.execute(httpPost);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        sendMessage(chatId,rta);
    }

    private void retirarTraslado(String chatId, Long idTrasladoRetirar) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(this.apiLogistica + "/retirar/" + idTrasladoRetirar);

        HttpResponse execute = httpClient.execute(httpPost);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        sendMessage(chatId,rta);
    }

    private void verSuscripciones(String chatId) {
        List<String> suscripciones = fachada.verSuscripciones(Long.parseLong(chatId));
        if(suscripciones.isEmpty()){
            sendMessage(chatId,"No tenes suscripciones registradas.");
            return;
        }
        for(String suscripcion:suscripciones){
            sendMessage(chatId,suscripcion);
        }

    }
    private void obtenerOcupacionHeladeras(String chatId, int heladeraId) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        System.out.println("URL: " + this.apiHeladeras + "/heladeras/" + heladeraId);
        HttpGet httpGet = new HttpGet(this.apiHeladeras + "/heladeras/" + heladeraId);

        HttpResponse execute = httpClient.execute(httpGet);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        System.out.println(rta);
        if (execute.getStatusLine().getStatusCode() == 404) {
            sendMessage(chatId,"❌ " + rta);
            return;
        }
        JsonReader jsonReader = Json.createReader(new StringReader(rta));
        JsonObject root = jsonReader.readObject();
        jsonReader.close();

        // Extraer valores
        int cantidadDeViandas = root.getJsonObject("HeladeraDTO").getInt("cantidadDeViandas");
        int heladeraViandasActuales = root.getInt("Heladera viandas actuales");

        rta = "\uD83D\uDD0B Capacidad de la heladera: " + cantidadDeViandas + "\n⚡ Viandas actuales: " + heladeraViandasActuales +
                "\n\uD83D\uDC49 Todavía podés depositar "+(cantidadDeViandas-heladeraViandasActuales)+" viandas en la heladera " + heladeraId;
        System.out.println(rta);
        sendMessage(chatId,rta);
    }

    @SneakyThrows
    private void modificarFormas(Set<FormaDeColaborarActualizadoEnum> formas, String chatId) throws JsonProcessingException {
        Long idColaborador = fachada.obtenerIdColaborador(chatId);
        HttpClient httpClient = HttpClients.createDefault();
        System.out.println("URL: " + this.apiColaboradores + "/colaboradores/" + idColaborador);
        HttpPatch httpPatch = new HttpPatch(this.apiColaboradores + "/colaboradores/" + idColaborador);
        ObjectMapper objectMapper = new ObjectMapper();
        Set<String> formasLista = formas.stream().map(FormaDeColaborarActualizadoEnum::toString).collect(Collectors.toSet());
        String json= objectMapper.writeValueAsString(new UpdateFormasColaborarRequest(formas.stream().toList()));

        StringEntity entity = new StringEntity(json);
        httpPatch.setEntity(entity);
        httpPatch.setHeader("Content-Type","application/json");
        HttpResponse execute = httpClient.execute(httpPatch);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);

        mostrarFormasDeColaborar(chatId, formasLista);
        formasPorUsuario.remove(chatId);

    }

    private void mostrarFormasDeColaborar(String chatId, Set<String> formasLista) {
        sendMessage(chatId, "Tus formas de colaborar son:");
        for (String forma : formasLista) {
            switch (forma) {
                case "TRANSPORTADOR":
                    sendMessage(chatId, emjTrasportador + "Transportador");
                    break;
                case "TECNICO":
                    sendMessage(chatId, emjTecnico + "Técnico");
                    break;
                case "DONADOR_DINERO":
                    sendMessage(chatId, emjDonadorDinero + "Donador de dinero");
                    break;
                case "DONADOR_VIANDAS":
                    sendMessage(chatId, emjDonadorViandas + "Donador de viandas");
                    break;
            }
        }
    }
    public void reportarHeladera(String chatId,Integer heladeraId) throws IOException {
        // reportar_heladera/1
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(this.apiColaboradores + "/reportar_heladera/" + heladeraId);

        HttpResponse execute = httpClient.execute(httpPost);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);

        if (execute.getStatusLine().getStatusCode() == 400) { // la heladera reportada no existe
            sendMessage(chatId, "❌ No existe la heladera " + heladeraId);
            return;
        }
        JsonReader jsonReader = Json.createReader(new StringReader(rta));
        JsonObject root = jsonReader.readObject();
        jsonReader.close();

        String formateado = root.getString("Mensaje");

        sendMessage(chatId, "⚠\uFE0F\uD83D\uDC4D " + formateado);
    }

    public void mostrarMenuPrincipal(String chatId){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Selecciona una sección:");
        InlineKeyboardMarkup markup= new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> fila1 = new ArrayList<>();
        List<InlineKeyboardButton> fila2 = new ArrayList<>();
        List<InlineKeyboardButton> fila3 = new ArrayList<>();
        List<InlineKeyboardButton> fila4 = new ArrayList<>();
        fila1.add(createButton("Sección colaboradores \uD83D\uDC65","colaboradores"));
        fila2.add(createButton("Sección Heladeras \uD83E\uDDCA","heladeras"));
        fila3.add(createButton("Sección Logistica \uD83D\uDE9A","logistica"));
        fila4.add(createButton("Sección Viandas \uD83C\uDF71","viandas"));
        filas.add(fila1);
        filas.add(fila2);
        filas.add(fila3);
        filas.add(fila4);
        markup.setKeyboard(filas);

        message.setReplyMarkup(markup);

        try{
            execute(message);
        }catch(TelegramApiException e){
            e.printStackTrace();
        }
    }

    public void mostrarMenuLogistica(String chatId) throws TelegramApiException {
        SendMessage msg = new SendMessage();
        msg.setText("Selecciona ");
        msg.setChatId(chatId);
        this.botonesLogistica(msg);
        execute(msg);
    }

    public void mostrarMenuColaboradores(String chatId) throws TelegramApiException {
        SendMessage msg = new SendMessage();
        msg.setText("Selecciona ");
        msg.setChatId(chatId);
        this.botonesColaboradores(msg);
        execute(msg);
    }

    @SneakyThrows
    private void suscQuedan(String chatId, Integer heladeraId, Integer valorNotificaion){
        Long idColaborador = fachada.obtenerIdColaborador(chatId);
        HttpClient httpClient = HttpClients.createDefault();
        System.out.println("URL: " + this.apiColaboradores + "/colaboradores/" + idColaborador);
        HttpPut httpPut = new HttpPut(this.apiColaboradores + "/colaboradores/" + idColaborador+"/suscripcionAPocasViandas");
        ObjectMapper objectMapper = new ObjectMapper();
        String json= objectMapper.writeValueAsString(new SuscripcionCantViandasRequest(heladeraId,valorNotificaion));

        StringEntity entity = new StringEntity(json);
        httpPut.setEntity(entity);
        httpPut.setHeader("Content-Type","application/json");
        HttpResponse execute = httpClient.execute(httpPut);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);

        //asd
        JsonReader jsonReader = Json.createReader(new StringReader(rta));
        JsonObject root = jsonReader.readObject();
        jsonReader.close();

        String mensaje = root.getString("Mensaje");
        int idSuscripcion = root.getInt("Suscripcion ID");

        sendMessage(chatId, "✔️ " + "Suscripción a quedan " + valorNotificaion + " viandas registrada correctamente.");
        sendMessage(chatId, "\uD83D\uDC49 ID de la suscripción: " + idSuscripcion);
    }

    @SneakyThrows
    private void suscFaltan(String chatId, Integer heladeraId, Integer valorNotificaion){
        Long idColaborador = fachada.obtenerIdColaborador(chatId);
        HttpClient httpClient = HttpClients.createDefault();
        System.out.println("URL: " + this.apiColaboradores + "/colaboradores/" + idColaborador);
        HttpPut httpPut = new HttpPut(this.apiColaboradores + "/colaboradores/" + idColaborador+"/suscripcionAFaltanViandas");
        ObjectMapper objectMapper = new ObjectMapper();
        String json= objectMapper.writeValueAsString(new SuscripcionCantViandasRequest(heladeraId,valorNotificaion));

        StringEntity entity = new StringEntity(json);
        httpPut.setEntity(entity);
        httpPut.setHeader("Content-Type","application/json");
        HttpResponse execute = httpClient.execute(httpPut);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        JsonReader jsonReader = Json.createReader(new StringReader(rta));
        JsonObject root = jsonReader.readObject();
        jsonReader.close();

        String mensaje = root.getString("Mensaje");
        int idSuscripcion = root.getInt("Suscripcion ID");

        sendMessage(chatId, "✔️ " + "Suscripción a faltan " + valorNotificaion + " viandas registrada correctamente.");
        sendMessage(chatId, "\uD83D\uDC49 ID de la suscripción: " + idSuscripcion);
    }

    @SneakyThrows
    private void suscDesperfectos(String chatId, Integer heladeraId) {
        Long idColaborador = fachada.obtenerIdColaborador(chatId);
        HttpClient httpClient = HttpClients.createDefault();
        System.out.println("URL: " + this.apiColaboradores + "/colaboradores/" + idColaborador);
        HttpPut httpPut = new HttpPut(this.apiColaboradores + "/colaboradores/" + idColaborador+"/suscripcionADesperfecto");
        ObjectMapper objectMapper = new ObjectMapper();
        String json= objectMapper.writeValueAsString(new SuscripcionADesperfectoRequest(heladeraId));

        StringEntity entity = new StringEntity(json);
        httpPut.setEntity(entity);
        httpPut.setHeader("Content-Type","application/json");
        HttpResponse execute = httpClient.execute(httpPut);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        JsonReader jsonReader = Json.createReader(new StringReader(rta));
        JsonObject root = jsonReader.readObject();
        jsonReader.close();

        String mensaje = root.getString("Mensaje");
        int idSuscripcion = root.getInt("Suscripcion ID");

        sendMessage(chatId, "✔️ " + "Suscripción a desperfectos registrada correctamente.");
        sendMessage(chatId, "\uD83D\uDC49 ID de la suscripción: " + idSuscripcion);
    }

    @SneakyThrows
    private void cerrarIncidencia(String chatId, Integer incidenteId){
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiColaboradores+"/arreglar_incidente/"+incidenteId.toString());
        ObjectMapper objectMapper = new ObjectMapper();
        String json= objectMapper.writeValueAsString(new ArreglarHeladeraRequest(Long.parseLong(chatId)));
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type","application/json");
        HttpResponse execute = httpClient.execute(httpPost);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        JsonReader jsonReader = Json.createReader(new StringReader(rta));
        JsonObject root = jsonReader.readObject();
        jsonReader.close();
        String formateado = root.getString("Mensaje");
        sendMessage(chatId, formateado);
    }

    @SneakyThrows
    private void crearRuta(String chatId, Integer heladeraOrigen, Integer heladeraDestino){
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiLogistica+"/rutas");
        ObjectMapper objectMapper = new ObjectMapper();
        String json= objectMapper.writeValueAsString(new RutaDTO(Long.parseLong(chatId),heladeraOrigen,heladeraDestino));
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type","application/json");
        HttpResponse execute = httpClient.execute(httpPost);

        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        if(execute.getStatusLine().getStatusCode()==400){
            sendMessage(chatId, "No se pudo agregar la ruta ❌");
            return;
        }
        if(execute.getStatusLine().getStatusCode()==200){

            JsonReader jsonReader = Json.createReader(new StringReader(rta));
            JsonObject root = jsonReader.readObject();
            jsonReader.close();
            String formateado = "Se creo la ruta entre la heladera: "+ root.getInt("heladeraIdOrigen")+ " y la heladera: "+root.getInt("heladeraIdDestino")+
                    " exitosamente ✔\uFE0F";
            sendMessage(chatId,formateado);
        }


    }

    @SneakyThrows
    private void trasladosAsignados(String chatId){
        HttpClient httpClient = HttpClients.createDefault();
        System.out.println("URL: " + this.apiLogistica + "/traslados/search/"+chatId+"?mes="+ LocalDateTime.now().getMonthValue()+"&anio="+LocalDateTime.now().getYear());
        HttpGet httpGet = new HttpGet(
                this.apiLogistica + "/traslados/search/"+chatId+"?mes="+ LocalDateTime.now().getMonthValue()+"&anio="+LocalDateTime.now().getYear());

        //String a = "" + LocalDateTime.now().getMonthValue();
        // https://viandas-u5sx.onrender.com/viandas/search/findByColaboradorIdAndAnioAndMes?colaboradorId=1&anio=2024&mes=10
        HttpResponse execute = httpClient.execute(httpGet);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        JsonReader jsonReader = Json.createReader(new StringReader(rta));
        JsonObject root = jsonReader.readObject();
        jsonReader.close();
        JsonValue mensaje = root.get("Mensaje");
        if(mensaje.getValueType()== JsonValue.ValueType.ARRAY){
            JsonArray array = root.getJsonArray("Mensaje");

            for(JsonObject elem:array.getValuesAs(JsonObject.class)){
                String id = elem.getJsonNumber("id").toString();
                String qrVianda = elem.getString("qrVianda");
                String status = elem.getString("status");
                String fechaTraslado = elem.getString("fechaTraslado");
                String heladeraOrigen = elem.getJsonNumber("heladeraOrigen").toString();
                String heladeraDestino = elem.getJsonNumber("heladeraDestino").toString();

                sendMessage(chatId,"Traslado asigando:"+id+"\n"+"Estado del traslado:"+status+"\n"+"Fecha del traslado:"+fechaTraslado+"\n" +
                        "QR de la vianda:"+qrVianda+"\n"+"Heladera origen:"+heladeraOrigen+"\n"+"Heladera destino:"+heladeraDestino+".");
            }
        }else if(mensaje.getValueType()== JsonValue.ValueType.STRING){
            String mensajeError = root.getString("Mensaje");
            sendMessage(chatId,mensajeError);
        }else{
            sendMessage(chatId, "Error: Error al procesar la respuesta de servidor.");
        }


    }

    @SneakyThrows
    private void desuscribirse(String chatId, Integer suscripcionId){
        HttpClient httpClient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete(apiColaboradores+"/suscripcion/"+suscripcionId.toString());
        HttpResponse execute = httpClient.execute(httpDelete);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);

        sendMessage(chatId, "✅ Desuscripción registrada correctamente.");
    }

    @SneakyThrows
    private void retirosDelDia(String chatId){
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(apiLogistica+"/retirosDelDia");
        HttpResponse execute = httpClient.execute(httpGet);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        sendMessage(chatId,rta);
    }












    @Override
    public String getBotUsername() {
// Se devuelve el nombre que dimos al bot al crearlo con el BotFather
        return System.getenv("BOT_USERNAME"); // cambiarlo a variable de entorno
    }
    @Override
    public String getBotToken() {
// Se devuelve el token que nos generó el BotFather de nuestro bot
        return System.getenv("BOT_TOKEN");
    }
    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
