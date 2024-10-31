package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.model.Enums.EstadoIncidenteEnum;
import ar.edu.utn.dds.k3003.model.Enums.TipoIncidenteEnum;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IncidentesWorker extends DefaultConsumer {

    private String queueName;
    private Fachada fachada;

    protected IncidentesWorker(Channel channel, String queueName, Fachada fachada) {
        super(channel);
        this.queueName = queueName;
        this.fachada=fachada;
    }

    public static void main(String[] args) throws Exception {
        // Establecer la conexión con CloudAMQP
        Map<String, String> envIncidente = System.getenv();
        ConnectionFactory factoryIncidente = new ConnectionFactory();
        factoryIncidente.setHost(envIncidente.get("NOTIFICACIONES_HOST"));
        factoryIncidente.setUsername(envIncidente.get("NOTIFICACIONES_USERNAME"));
        factoryIncidente.setPassword(envIncidente.get("NOTIFICACIONES_PASSWORD"));
        // En el plan más barato, el VHOST == USER
        factoryIncidente.setVirtualHost(envIncidente.get("NOTIFICACIONES_USERNAME"));
        String colaIncidente = envIncidente.get("INCIDENTES_NAME");
        Connection conexionIncidente = factoryIncidente.newConnection();
        com.rabbitmq.client.Channel canalIncidente = conexionIncidente.createChannel();

//        EntityManagerFactory entityManagerFactory2 = WebApp.startEntityManagerFactory();
//
//        ColaboradoresWorker worker = new ColaboradoresWorker(channel, queueName, entityManagerFactory2);
//        worker.init();
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
        String[] partes = mensaje.split(",");
        for (String parte:partes){
            String[] claveValor = parte.split("=");
            if(claveValor.length == 2){
                valores.put(claveValor[0],claveValor[1]);
            }
        }
        // REPOINCIDENTES FIND BY ID
        int heladeraId = Integer.parseInt(valores.get("heladera_id"));
        TipoIncidenteEnum tipo = TipoIncidenteEnum.buscarEnum(Integer.parseInt(valores.get("tipo")));
        this.fachada.registrarIncidente(heladeraId,tipo, EstadoIncidenteEnum.NO_REPARADO);





    }
}