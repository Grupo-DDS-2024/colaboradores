package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.model.Request.UpdateFormasColaborarRequest;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborar;
import ar.edu.utn.dds.k3003.model.formaDeColaborar.FormaDeColaborarActualizadoEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
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
                 fachada.agregarDesdeBot(chatId);
                 sendMessage(chatId,"Hola! Tu ID ha sido registrado."); // preg formas de colaborar, nombre, etc
             }
             mostrarMenuPrincipal(chatId);


         }else if(update.hasMessage() && update.getMessage().hasText()){
            String chatId = update.getMessage().getChatId().toString();
            switch(userState.get(chatId)){
                case "id_heladera":
                    Integer id_heladera = Integer.parseInt(update.getMessage().getText());
                    reportarHeladera(chatId,id_heladera);
                    break;
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


                     // Ver las incidencias en una heladera.
                     // Des/suscribirse a los eventos de una heladera.
                     // Recibir información de dichos eventos.
                     // Cerrar una incidencia (volver a activar la heladera).
                     // Recibir mensaje que un traslado fue asignado al usuario.
                     execute(message);
                     break;
                 case "logistica":
                     message.setText("Seleccionaste logistica");
                     //Retirar una vianda
                     //Ver los retiros del día de una heladera
                     //Dar de alta una ruta
                     //Iniciar y finalizar traslado de vianda
                     break;
                 case "heladeras":
                     message.setText("Seleccionaste heladeras");
                     //ver las heladeras en una zona
                     // Ver la ocupación de las viandas en una heladera

                     break;
                 case "viandas":
                     message.setText("Seleccionaste viandas");
                     // Crear y depositar una vianda
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
                     break;

                 case "reportarHeladera":
                     userState.put(chatId,"id_heladera");
                     sendMessage(chatId,"Ingrese el id de la Heladera a reportar.");
                     break;
                 default:
                     message.setText("Opcion no reconocida");
                     break;
             }

         }



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

        // Agregar botones: Ir al inicio, Salir capaz?

        markup.setKeyboard(filas);

        message.setReplyMarkup(markup);


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
    private void sendMessage(String chatId, String text) {
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
