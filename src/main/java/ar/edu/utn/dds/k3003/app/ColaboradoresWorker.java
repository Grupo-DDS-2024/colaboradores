package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.model.Clases.NotificacionesHeladeras;
import ar.edu.utn.dds.k3003.model.Enums.TipoNotificacionEnum;
import ar.edu.utn.dds.k3003.repositories.NotificacionRepository;
import com.rabbitmq.client.*;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ColaboradoresWorker extends DefaultConsumer {

    private String queueName;
    private EntityManagerFactory entityManagerFactory;

    protected ColaboradoresWorker(Channel channel, String queueName, EntityManagerFactory entityManagerFactory) {
        super(channel);
        this.queueName = queueName;
        this.entityManagerFactory = entityManagerFactory;
    }

    public static void main(String[] args) throws Exception {
    // Establecer la conexión con CloudAMQP
        Map<String, String> envMQ = System.getenv();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(envMQ.get("NOTIFICACIONES_HOST"));
        factory.setUsername(envMQ.get("NOTIFICACIONES_USERNAME"));
        factory.setPassword(envMQ.get("NOTIFICACIONES_PASSWORD"));
        // En el plan más barato, el VHOST == USER
        factory.setVirtualHost(envMQ.get("NOTIFICACIONES_USERNAME"));
        String queueName = envMQ.get("NOTIFICACIONES_NAME");
        Connection connection = factory.newConnection();
        com.rabbitmq.client.Channel channel = connection.createChannel();

        EntityManagerFactory entityManagerFactory2 = WebApp.startEntityManagerFactory();

        ColaboradoresWorker worker = new ColaboradoresWorker(channel, queueName, entityManagerFactory2);
        worker.init();
    }


    public void init() throws IOException {
        // Declarar la cola desde la cual consumir mensajes
        this.getChannel().queueDeclare(this.queueName, false, false, false, null);
        // Consumir mensajes de la cola
        this.getChannel().basicConsume(this.queueName, false, this);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        // Confirmar la recepción del mensaje a la mensajeria
        this.getChannel().basicAck(envelope.getDeliveryTag(), false);
        String mensaje = new String(body, "UTF-8");
        System.out.println(mensaje);
        mensaje = mensaje.substring(1,mensaje.length() - 1);
        Map<String,String> valores = new HashMap<>();
        String[] partes = mensaje.split(", ");
        for (String parte:partes){
            String[] claveValor = parte.split("=");
            if(claveValor.length == 2){
                valores.put(claveValor[0],claveValor[1]);
            }
        }
        Long colaboradorId = Long.parseLong(valores.get("colaborador_id"));
        TipoNotificacionEnum tipo = TipoNotificacionEnum.buscarEnum(Integer.parseInt(valores.get("tipo")));
        int heladeraId = Integer.parseInt(valores.get("heladera_id"));

        NotificacionRepository repo = new NotificacionRepository(entityManagerFactory);
        NotificacionesHeladeras notificacionesHeladeras = new NotificacionesHeladeras(colaboradorId,heladeraId,tipo);
        repo.save(notificacionesHeladeras);



    }
}
