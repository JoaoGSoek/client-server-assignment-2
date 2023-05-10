import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {

    private static File messageFile;
    private static File newMessageFile;

    private static class MessageReader extends Thread{

        public void run() {

            while(true){
            
                try {

                    Scanner newMessageFileScanner = new Scanner(newMessageFile);
                    boolean hasNewMessage = false;

                    if(newMessageFileScanner.hasNextLine()){ // Verificando a existencia de novas mensagens
                        
                        hasNewMessage = newMessageFileScanner.nextLine().equals("TRUE");
                        
                        PrintWriter newMessageWriter = new PrintWriter(newMessageFile);
                        newMessageWriter.println("FALSE");
                        newMessageWriter.close();

                    }

                    if(hasNewMessage){

                        String messages = Files.readString(Paths.get("messages.txt"), StandardCharsets.UTF_8);
                        System.out.println("=====================\n");
                        System.out.println(messages);
                        System.out.println("Digite sua mensagem:");

                    }

                    newMessageFileScanner.close();
                    Thread.sleep(1000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            
            }

        }

    }
    public static void main(String[] args) {
        
        try {

            messageFile = new File("messages.txt");
            messageFile.createNewFile();

            newMessageFile = new File("new-message-control.txt");
            newMessageFile.createNewFile();

            MessageReader readerThread = new MessageReader();
            readerThread.start();
                        
            PrintWriter newMessageWriter = new PrintWriter(newMessageFile);
            newMessageWriter.println("TRUE");
            newMessageWriter.close();

            while(true){

                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));                
                String message = reader.readLine().toString();
                
                PrintWriter messageWriter = new PrintWriter(new FileWriter(messageFile, true));
                messageWriter.append(message + "\n");
                messageWriter.close();

                System.out.println("Aguarde");

            }

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

}
