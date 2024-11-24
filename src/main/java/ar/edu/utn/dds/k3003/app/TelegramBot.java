package ar.edu.utn.dds.k3003.app;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.nio.charset.StandardCharsets;

public class TelegramBot extends TelegramLongPollingBot {

    private String apiEndpoint = "localhost/8082";
    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) { // se invoca cuando el bot recibe un mensaje

        // Se obtiene el mensaje escrito por el usuario
        final String messageTextReceived = update.getMessage().getText();
        String rta = messageTextReceived;
        if (messageTextReceived.startsWith("/puntos")) {
            String[] mensaje = messageTextReceived.split(" ");
            String idColaborador = mensaje[1];
            rta = "calcular puntos colaborador: " + idColaborador;
            /*
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(
                    this.apiEndpoint + "/puntos/colaboradores/" + idColaborador + "/puntos");

            HttpResponse execute = httpClient.execute(httpPost);
            rta = IOUtils.toString(
                    execute.getEntity().getContent(),
                    StandardCharsets.UTF_8.name());
            //return rta;

             */
        }


        // Respuesta
        // Se obtiene el id de chat del usuario
        Long chatId = update.getMessage().getChatId();
        // Se crea un objeto mensaje
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(rta);
        try {
// Se envía el mensaje
            execute(message);
        } catch (TelegramApiException e) {
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
    public static void main(String[] args)
            throws TelegramApiException {
// Se crea un nuevo Bot API
        final TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
// Se registra el bot
            telegramBotsApi.registerBot(new TelegramBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
