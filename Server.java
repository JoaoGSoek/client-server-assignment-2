import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static InetAddress addr = null;

    private static File messages;
    private static File newMessageControl;

    private static int socket = 3000;
    private static long lastModified;

    private static boolean exit = false;

    private static class OutputHandler extends Thread{

        public void run() {

            Socket externalServer = null;
            boolean foundSocket = false;

            while(!exit){
            
                try {

                    if(lastModified < messages.lastModified()){ // Enviando mensagens para o outro servidor

                        lastModified = messages.lastModified();

                        if(foundSocket){

                            ObjectOutputStream outputStream = new ObjectOutputStream(externalServer.getOutputStream());                            
                            outputStream.writeObject(messages);
                        
                        }else{

                            System.out.println("Não há outro servidor aberto");

                        }
                    
                        PrintWriter messageControlWriter = new PrintWriter(newMessageControl);
                        messageControlWriter.println("TRUE");
                        messageControlWriter.close();
                    
                    }

                    if(!foundSocket){ // Tentando se conectar com o outro servidor
                        
                        externalServer = new Socket(addr.getHostName(), (socket == 3000) ? 3001 : 3000);
                        System.out.println("Conectado ao servidor externo");
                        foundSocket = true;
                    
                    }

                    Thread.sleep(1000);

                } catch (Exception e) {
                    
                    // e.printStackTrace();

                }

            }

        }

    }

    private static class InputHandler extends Thread{

        private ServerSocket server;

        InputHandler(ServerSocket server){

            this.server = server;

        }

        public void run() {
            
            try {

                Socket connectedServer = server.accept();

                while(!exit){ // Recebendo mensagens do outro servidor

                    ObjectInputStream inputStream = new ObjectInputStream(connectedServer.getInputStream());

                    File inputMessages = (File) inputStream.readObject();
                    PrintWriter messageWriter = new PrintWriter(messages);
                    messageWriter.println(inputMessages);
                    messageWriter.close();
                    
                    PrintWriter messageControlWriter = new PrintWriter(newMessageControl);
                    messageControlWriter.println("TRUE");
                    messageControlWriter.close();

                }

            } catch (Exception e) {
                
                e.printStackTrace();

            }

        }

    }

    public static void main(String[] args) {
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // Acessando arquivos
        messages = new File("messages.txt");
        newMessageControl = new File("new-message-control.txt");

        // Criando arquivos se não existirem
        try {
            messages.createNewFile();
            newMessageControl.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        lastModified = messages.lastModified();
        System.out.println(lastModified);

        while(!exit){

            try{

                // Recuperando LAN IP
                // https://stackoverflow.com/questions/30419386/how-can-i-get-my-lan-ip-address-using-java
                // Available network interfaces
                // Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
                // while (nics.hasMoreElements()) {

                //     NetworkInterface nic = nics.nextElement();
                //     Enumeration<InetAddress> addrs = nic.getInetAddresses();

                //     while (addrs.hasMoreElements()) {

                //         addr = addrs.nextElement();                        
                //         if (!nic.isLoopback() && !addr.getHostName().equals("0:0:0:0:0:0:0:1")) break;
                //         addr = null;

                //     }

                //     if(addr != null) break;

                // }

                // String address = Inet4Address.getLocalHost().getHostAddress();
                // System.out.println(address);
                // String[] splitAddress = new String[4];
                // System.out.println(splitAddress.length);
                // splitAddress = address.split("[.]");
                // splitAddress[3] = "1";
                // System.out.println(splitAddress[0] + "." + splitAddress[1] + "." + splitAddress[2] + "." + splitAddress[3]);
                // address = String.join(".", splitAddress);
                // addr = InetAddress.getByName(address);
                // System.out.println(addr);

                addr = InetAddress.getByName("192.168.0.1");

                // Abrindo Socket
                ServerSocket server = new ServerSocket(socket, 1, addr);
                System.out.println("IP: " + server.getInetAddress().getHostAddress());

                // Iniciando thread de input
                InputHandler inputHandler = new InputHandler(server);
                inputHandler.start();

                // Instanciando thread de output
                OutputHandler outputHandler = new OutputHandler();
                outputHandler.start();

                while(!exit){ // loop de saída

                    System.out.println("<exit> para finalizar");

                    String command = reader.readLine().toString();
                    exit = command.equalsIgnoreCase("<exit>");

                }

                server.close();

            } catch (Exception e) {

                socket++;
                // e.printStackTrace();

            }
        
        }

    }

}
