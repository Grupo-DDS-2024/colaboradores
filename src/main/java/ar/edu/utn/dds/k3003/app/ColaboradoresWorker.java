package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.model.NotificacionesHeladeras;
import ar.edu.utn.dds.k3003.model.TipoNotificacionEnum;
import ar.edu.utn.dds.k3003.repositories.NotificacionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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


        /*
        String temp = analisisId.substring(analisisId.indexOf("TemperaturaDTO(") + "TemperaturaDTO(".length(), analisisId.length() - 2);
        String[] campos = temp.split(", ");
        int tempe = 0;
        int heladeriaId = 400;
        for (String campo : campos) {
            if (campo.startsWith("temperatura=")) {
                tempe = Integer.parseInt(campo.split("=")[1]);
            }
            if (campo.startsWith("heladeraId=")) {
                heladeriaId = Integer.parseInt(campo.split("=")[1]);
            }
            if (campo.startsWith("fechaMedicion=")) {
                fecha = LocalDateTime.parse(campo.split("=")[1]);
            }
        }


        System.out.println("id");
        System.out.println(heladeriaId);
        Temperatura temperatura = new Temperatura(tempe, heladeriaId, fecha);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        HeladeraJPARepository repositorio = new HeladeraJPARepository(entityManager);
        Heladera heladera = repositorio.findById(heladeriaId);
        heladera.setTemperatura(temperatura);
        repositorio.save(heladera); //dsp cambiarlo a update
        entityManager.getTransaction().commit();
        entityManager.close();
        */
    }
}

/*
                Map<String, Object> response = new HashMap<>();
                response.put("colaborador_id:",s.getColaborador_id());
                response.put("heladera_id:",heladera_id);
                response.put("tipo:", 0);

                Map<String, Object> response = new HashMap<>();
                response.put("colaborador_id:",s.getColaborador_id());
                response.put("heladera_id:",heladera_id);
                response.put("tipo:",1);
 */