/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webservertavano;
import java.io.* ;
import java.net.* ;
import java.util.* ;

/**
 * @author Tavano
 */

public final class WebServer {
    //Inicia o programa para receber os sockets multi threads
    public static void main (String arvg[]) throws Exception{
        //Porta do meu servidor
        int porta = 6789;
        //Cria a variável de sochet
        ServerSocket socketServ = new ServerSocket(porta);
        Socket socketCli;
        System.out.println( "-----Servidor Iniciado-----" );
        while (true) {
            System.out.println( "-----Nova conexão-----" );
            //Apenas um mensagem TCP para indicar que o servidor encontra-se ativo
            socketCli = socketServ.accept();
            HttpRequest request = new HttpRequest(socketCli);
            //Cria Thread com base na requisição
            Thread thread = new Thread (request);
            //Inicia Thread
            thread.start();
        }
    }
}


final class HttpRequest implements Runnable {
    // Criação do CRLF e do socket
    final static String CRLF = "\r\n";
    Socket socket;

    //Construtor
    public HttpRequest (Socket socket) throws Exception{
        this.socket = socket;
    }

    // roda processrequest e trata as exceções
    public void run(){
        try{
            processRequest();
        } catch (Exception e) {
            System.out.println ("Entrou em excessao em run");
            System.out.println (e);
        }
    }

    private void processRequest () throws Exception{
        //Input Stram do novo objeto, e OutPut Stream
        InputStreamReader is = new  InputStreamReader(socket.getInputStream());
        DataOutputStream os = new  DataOutputStream(socket.getOutputStream());

        //Cria buffer para a entrada para trabalhar com metodos, à trente
        BufferedReader br = new BufferedReader(is);
        //Obter a linha de requisição da mensagem de requisição HTTP do Buffer
        String requestLine = br.readLine();
        //Exibir a linha de requisição no Console
        System.out.println(); // pula uma linha
        System.out.println("Conteúdo da nova conexão:"); // pula uma linha/
        System.out.println(requestLine);
        
        /**Preparando pra receber Header**/
        System.out.println("    Headers:"); // pula uma linha/
        String headerLine = null;
        //Percorre todas linhas da mensagem
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);
        }
        // Close streams and socket.
        //os.close();
        //br.close();
        //socket.close();
        //Fim da Parte 1
        
        //Preparação para Receber requisiçoes GET
        // Extract the filename from the request line.
        StringTokenizer tokens = new StringTokenizer(requestLine);
        String requestMethod = tokens.nextToken();  // skip over the method, which should be "GET"
        System.out.println("Requisição do tipo: " + requestMethod);
        String outputFile = tokens.nextToken();
        // Prepend a "." so that file request is within the current directory.
        outputFile = "." + outputFile;
        System.out.println(outputFile);

        // Open the requested file.
        FileInputStream fis = null;
        boolean fileExists = true;
        try {
            fis = new FileInputStream(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println("Excessao em fileStream: "+e);
            fileExists = false;
        }

        //Criando OutPut
        String msgHtml = null;

        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        if(fileExists){
            statusLine = "HTTP/1.0 200 OK" + CRLF;
            contentTypeLine = "Content-type: " + contentType(outputFile)+ CRLF;
        } 
        else {
            statusLine = "HTTP/1.0 404 Not found" + CRLF;
            contentTypeLine = "Content-type: " +  contentType(outputFile)+  CRLF;
            msgHtml = "<HTML><HEAD><TITLE> OPS!!! Arquivo nao encontrado!" + "</TITLE></HEAD>" + "<BODY> Arquivo Nao Encontrado </BODY></HTML>";
        }

        os.writeBytes(statusLine);
        os.writeBytes(contentTypeLine);
        os.writeBytes(CRLF);
        // Send the entity body.
        if (fileExists)	{
            sendBytes(fis, os);
            fis.close();
        } else {
            msgHtml = "<HTML><HEAD><TITLE> Error " +
            "</TITLE></HEAD>" + 
            "<BODY> OPS... </BR>" + 
            "404: Página nao encontrada." + 
            "</BODY></HTML>" +
            CRLF;
            os.writeBytes(msgHtml);
        }
        
    }
    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
        // Construct a 1K buffer to hold bytes on their way to the socket.
        byte[] buffer = new byte[1024];
        int bytes = 0;

        // Copy requested file into the socket's output stream.
        while((bytes = fis.read(buffer)) != -1 ) {
            os.write(buffer, 0, bytes);
        }
    }
    
    private static String contentType(String arquivo){
        if(arquivo.endsWith(".htm")||
            arquivo.endsWith(".html")||
            arquivo.endsWith(".txt")) 
                return "text/html";

            //Em ultimo caso, arquivos desconhecidos:
            return "text/html";
            //return "application/octet-stream";
        }
    }

