package ar.edu.utn.dds.k3003.app;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

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
        //mensaje = mensaje.replace("=", ":").replace(", ", "\", \"").replace("{", "{\"").replace("}", "\"}");
        mensaje = mensaje.replaceAll("([a-zA-Z_]+):", "\"$1\":").replaceAll(",\\s*", ", ").replace("=", ":"); // Cambia '=' por ':';
        System.out.println("MENSAJE: " + mensaje);
        // MENSAJE: {"tipo::0", "heladera_id::1", "colaborador_id::1"}
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Parseamos el mensaje a un Map
            Map<String, Object> responseMap = objectMapper.readValue(mensaje, Map.class);

            // Accedemos a los valores individuales
            Integer tipo = (Integer) responseMap.get("tipo:");
            Integer heladeraId = (Integer) responseMap.get("heladera_id:");
            Long colaboradorId = (Long) responseMap.get("colaborador_id:");



            // Usar los valores
            System.out.println("colaborador_id: " + colaboradorId);
            System.out.println("heladera_id: " + heladeraId);
            System.out.println("tipo: " + tipo);
        } catch (Exception e) {
            e.printStackTrace();
        }

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