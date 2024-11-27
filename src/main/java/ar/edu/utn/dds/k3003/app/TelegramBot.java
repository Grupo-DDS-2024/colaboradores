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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TelegramBot extends TelegramLongPollingBot {

    private String apiColaboradores = "http://localhost:8082";
    private String apiHeladeras = "http://localhost:8080";
    private String apiLogistica = "http://localhost:8083";
    private String apiViandas = "http://localhost:8081";
    private Fachada fachada;
    private Map<String, Set<FormaDeColaborarActualizadoEnum>> formasPorUsuario;
    private Map<String, String> userState;

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
                 sendMessage(chatId,"¡Bienvenido de nuevo!");
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
                    break;
                case "codigoQR":
                    String codigoQR = update.getMessage().getText();
                    agregarVianda(chatId, codigoQR);
                    break;
                case "suscQuedan":
                    String[] partes = update.getMessage().getText().split(";",2);
                    Integer heladeraId = Integer.parseInt(partes[0].trim());
                    Integer valor = Integer.parseInt(partes[1].trim());
                    suscQuedan(chatId,heladeraId,valor);
                    break;
                case "suscFaltan":
                    String[] partesFaltan = update.getMessage().getText().split(";",2);
                    Integer heladeraIdFaltan = Integer.parseInt(partesFaltan[0].trim());
                    Integer valorFaltan = Integer.parseInt(partesFaltan[1].trim());
                    suscFaltan(chatId,heladeraIdFaltan,valorFaltan);
                    break;
                case "suscDesperfecto":
                    Integer id_heladeraSusc = Integer.parseInt(update.getMessage().getText());
                    suscDesperfectos(chatId,id_heladeraSusc);
                    break;
                case "ocupacionViandas":
                    Integer id_heladeraOcupacion = Integer.parseInt(update.getMessage().getText());
                    obtenerOcupacionHeladeras(chatId, id_heladeraOcupacion);
                    break;
                case "incidencia_id":
                    Integer id_incidente = Integer.parseInt(update.getMessage().getText());
                    cerrarIncidencia(chatId,id_incidente);
                    break;
                case "depositarVianda":
                    String[] partesDepositar = update.getMessage().getText().split(";",2);
                    String codigoQRDepositar = partesDepositar[0].trim();
                    Integer heladeraIdDepositar = Integer.parseInt(partesDepositar[1].trim());
                    depositarVianda(chatId,codigoQRDepositar,heladeraIdDepositar);
                    break;
                case "retirarVianda":
                    String[] partesRetirar = update.getMessage().getText().split(";",2);
                    String codigoQRRetirar = partesRetirar[0].trim();
                    Integer heladeraIdRetirar = Integer.parseInt(partesRetirar[1].trim());
                    retirarVianda(chatId,codigoQRRetirar,heladeraIdRetirar);
                    break;
                case "crearRuta":
                    String[] partesRuta = update.getMessage().getText().split(";",2);
                    Integer heladeraOrigen = Integer.parseInt(partesRuta[0].trim());
                    Integer heladeraDestino = Integer.parseInt(partesRuta[1].trim());
                    crearRuta(chatId,heladeraOrigen,heladeraDestino);
                    break;

                case "depositarTraslado":
                    Long idTrasladoDepositar = Long.parseLong(update.getMessage().getText());
                    depositarTraslado(chatId, idTrasladoDepositar);
                    break;

                case "retirarTraslado":
                    Long idTrasladoRetirar = Long.parseLong(update.getMessage().getText());
                    retirarTraslado(chatId, idTrasladoRetirar);
                    break;
                case "desuscribirse":
                    Integer idSuscripcion = Integer.parseInt(update.getMessage().getText());
                    desuscribirse(chatId,idSuscripcion);
                    break;
                case "nombreInput":
                    String nombre = update.getMessage().getText().trim();
                    fachada.agregarDesdeBot(chatId,nombre);
                    sendMessage(chatId,"Gracias, "+nombre+". Ahora seleccione cómo desea colaborar:");
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
                     modificarFormas(formasPorUsuario.get(chatId),chatId);
                     if(userState.get(chatId).equalsIgnoreCase("nombreInput")){
                         mostrarMenuPrincipal(chatId);
                     }
                     break;

                 case "reportarHeladera":
                     userState.put(chatId,"id_heladera");
                     sendMessage(chatId,"Ingrese el id de la Heladera a reportar.");
                     break;
                 case "verIncidentes":
                     verIncidentes(chatId);
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
                     break;

                 case "verSuscripciones":
                     verSuscripciones(chatId);
                     break;
                 case "desuscribirse":
                     userState.put(chatId,"desuscribirse");
                     sendMessage(chatId,"Ingrese el ID de la suscripción a desuscribirse.");
                     break;
                 case "retirosDelDia":
                     retirosDelDia(chatId);
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
        sendMessage(chatId,rta);
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
        sendMessage(chatId,rta);
    }

    private void consultarPuntos(String chatId) throws IOException {
        Long idColaborador = fachada.obtenerIdColaborador(chatId);
        HttpClient httpClient = HttpClients.createDefault();
        System.out.println("URL: " + this.apiColaboradores + "/colaboradores/" + idColaborador);
        HttpGet httpGet = new HttpGet(
                this.apiColaboradores + "/colaboradores/" + idColaborador);

        HttpResponse execute = httpClient.execute(httpGet);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        sendMessage(chatId,rta);
    }

    private void agregarFormaColaborar(SendMessage message){

        InlineKeyboardMarkup markup= new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> fila1 = new ArrayList<>();
        List<InlineKeyboardButton> fila2 = new ArrayList<>();
        List<InlineKeyboardButton> fila3 = new ArrayList<>();
        List<InlineKeyboardButton> fila4 = new ArrayList<>();
        List<InlineKeyboardButton> fila5 = new ArrayList<>();
        fila1.add(createButton("Donador de viandas","donador_viandas"));
        fila2.add(createButton("Donador de dinero","donador_dinero"));
        fila3.add(createButton("Técnico","tecnico"));
        fila4.add(createButton("Transportador","transportador"));
        fila5.add(createButton("Confirmar", "confirmar_forma_colaborar"));
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
        fila1.add(createButton("Suscripción a quedan viandas","suscQuedan"));
        fila2.add(createButton("Suscripción a faltan viandas","suscFaltan"));
        fila3.add(createButton("Suscripción a desperfecto","suscDesperfecto"));
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
        fila1.add(createButton("Obtener datos","datosColaborador"));
        filas.add(fila1);
        List<InlineKeyboardButton> fila2 = new ArrayList<>();
        fila2.add(createButton("Cambiar formas de colaborar","formaColaborar"));
        filas.add(fila2);
        List<InlineKeyboardButton> fila3 = new ArrayList<>();
        fila3.add(createButton("Reportar heladera","reportarHeladera"));
        filas.add(fila3);
        List<InlineKeyboardButton> fila4 = new ArrayList<>();
        fila4.add(createButton("Ver Incidentes","verIncidentes"));
        filas.add(fila4);
        List<InlineKeyboardButton> fila5 = new ArrayList<>();
        fila5.add(createButton("Suscribirse a eventos de Heladera","suscribirseAHeladera"));
        filas.add(fila5);
        List<InlineKeyboardButton> fila6 = new ArrayList<>();
        fila6.add(createButton("Cerrar una incidencia","cerrarIncidencia"));
        filas.add(fila6);
        List<InlineKeyboardButton> fila7 = new ArrayList<>();
        fila7.add(createButton("Ver mis traslados asignados","trasladosAsignados"));
        filas.add(fila7);

        List<InlineKeyboardButton> fila8 = new ArrayList<>();
        fila8.add(createButton("Ver suscripciones","verSuscripciones"));
        filas.add(fila8);
        List<InlineKeyboardButton> fila9 = new ArrayList<>();
        fila9.add(createButton("Desuscribirse","desuscribirse"));
        filas.add(fila9);

        // Agregar botones: Ir al inicio, Salir capaz?

        markup.setKeyboard(filas);

        message.setReplyMarkup(markup);


    }

    private void botonesViandas(SendMessage message){
        InlineKeyboardMarkup markup= new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> fila1 = new ArrayList<>();
        fila1.add(createButton("Agregar vianda","agregarVianda"));
        filas.add(fila1);

        // Agregar botones: Ir al inicio, Salir capaz?

        markup.setKeyboard(filas);

        message.setReplyMarkup(markup);


    }

    private void botonesHeladeras(SendMessage message){
        InlineKeyboardMarkup markup= new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> fila1 = new ArrayList<>();
        fila1.add(createButton("Ver ocupación de viandas","ocupacionViandas"));
        filas.add(fila1);

        // Agregar botones: Ir al inicio, Salir capaz?

        markup.setKeyboard(filas);

        message.setReplyMarkup(markup);


    }

    private void botonesLogistica(SendMessage message){
        InlineKeyboardMarkup markup= new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> filas = new ArrayList<>();
        List<InlineKeyboardButton> fila1 = new ArrayList<>();
        fila1.add(createButton("Depositar vianda","depositarVianda"));
        filas.add(fila1);

        List<InlineKeyboardButton> fila2 = new ArrayList<>();
        fila2.add(createButton("Retirar vianda","retirarVianda"));
        filas.add(fila2);

        List<InlineKeyboardButton> fila3 = new ArrayList<>();
        fila3.add(createButton("Crear una ruta","crearRuta"));
        filas.add(fila3);

        List<InlineKeyboardButton> fila4 = new ArrayList<>();
        fila4.add(createButton("Depositar traslado","depositarTraslado"));
        filas.add(fila4);

        List<InlineKeyboardButton> fila5 = new ArrayList<>();
        fila5.add(createButton("Retirar traslado","retirarTraslado"));
        filas.add(fila5);

        List<InlineKeyboardButton> fila6 = new ArrayList<>();
        fila6.add(createButton("Retiros del dia","retirosDelDia"));
        filas.add(fila6);

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
        sendMessage(chatId,rta);
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
            sendMessage(chatId,rta);
            return;
        }
        JsonReader jsonReader = Json.createReader(new StringReader(rta));
        JsonObject root = jsonReader.readObject();
        jsonReader.close();

        // Extraer valores
        int cantidadDeViandas = root.getJsonObject("HeladeraDTO").getInt("cantidadDeViandas");
        int heladeraViandasActuales = root.getInt("Heladera viandas actuales");

        rta = "Capacidad de la heladera: " + cantidadDeViandas + "\nViandas actuales: " + heladeraViandasActuales;
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
        sendMessage(chatId,json);
        formasPorUsuario.remove(chatId);

    }

    public void reportarHeladera(String chatId,Integer heladeraId) throws IOException {
        // reportar_heladera/1
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(this.apiColaboradores + "/reportar_heladera/" + heladeraId);

        HttpResponse execute = httpClient.execute(httpPost);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        sendMessage(chatId,rta);
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
        fila1.add(createButton("Sección colaboradores","colaboradores"));
        fila2.add(createButton("Sección Heladeras","heladeras"));
        fila3.add(createButton("Sección Logistica","logistica"));
        fila4.add(createButton("Sección Viandas","viandas"));
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
        sendMessage(chatId,rta);
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
        sendMessage(chatId,rta);
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
        sendMessage(chatId,rta);
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
        sendMessage(chatId,rta);
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
        sendMessage(chatId,rta);
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
        sendMessage(chatId,rta);
    }

    @SneakyThrows
    private void desuscribirse(String chatId, Integer suscripcionId){
        HttpClient httpClient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete(apiColaboradores+"/suscripcion/"+suscripcionId.toString());
        HttpResponse execute = httpClient.execute(httpDelete);
        String rta = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
        sendMessage(chatId,rta);
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
