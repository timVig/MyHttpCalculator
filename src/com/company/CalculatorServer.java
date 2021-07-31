package com.company;
import com.sun.net.httpserver.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;

public class CalculatorServer {
    private final HttpServer server;
    private HashMap< Trio, String > memoize;

    public CalculatorServer(Executor exec) throws IOException {
        server = HttpServer.create();
        server.setExecutor(exec);
        server.createContext( "/Calculator/operations", new CommandHandler( this ) );
        memoize = new HashMap<>();
    }

    public void start() throws IOException {
        server.bind( new InetSocketAddress("localhost", 8001), 0 );
        server.start();
    }

    public void stop() { server.stop(0); }
    public void stop( int delay ){ server.stop( delay ); }

    private static class CommandHandler implements HttpHandler {
        private CalculatorServer server;
        private Calculator calculator;
        private EncryptionHandler encrypter;

        CommandHandler( CalculatorServer s1 ) {
            this.server = s1;
            this.calculator = new Calculator();
            this.encrypter = new EncryptionHandler();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try( exchange ){ //no need to check headers, all are GET requests
                var cmd2 = exchange.getRequestURI().toString();
                var body = exchange.getRequestBody();
                InputStreamReader inRead = new InputStreamReader( body, UTF_8);
                BufferedReader bufferRead = new BufferedReader(inRead);

                int b;
                StringBuilder sb = new StringBuilder(512);
                while( (b = bufferRead.read()) != -1 ){
                    sb.append( (char) b );
                }

                bufferRead.close();
                inRead.close();
                cmd2 = cmd2 + encrypter.decrypt( sb.toString(), 1); //decrypt using caesar
                List<String> commands = new LinkedList<>();
                String str = "";

                for( int i = 0; i < cmd2.length(); i++ ){
                    if( cmd2.charAt(i) == '/' && !str.isEmpty() ) {
                        commands.add(str); str = "";
                    } else if (cmd2.charAt(i) == '/' ){ }
                    else { str = str + cmd2.charAt(i); }
                }
                commands.add(str);

                Double op1 = Double.parseDouble(commands.get(3) );
                Double op2 = Double.parseDouble( commands.get(4) );
                Double answer = 0.0;

                String responseStr;
                Trio trio = new Trio( op1, op2, commands.get(2) );

                if( this.server.memoize.containsKey(trio) ){
                    responseStr = this.server.memoize.get(trio);
                } else {
                    if( commands.get(2).equals("add") ){ answer = calculator.add( op1, op2 ); }
                    if( commands.get(2).equals("sub") ){ answer = calculator.sub( op1, op2 ); }
                    if( commands.get(2).equals("mul") ){ answer = calculator.multiply( op1, op2 ); }
                    if( commands.get(2).equals("div") ){ answer = calculator.divide( op1, op2 ); }
                    responseStr = String.valueOf(answer); //encrypt data before sending it back
                    this.server.memoize.put(trio, responseStr);
                }

                responseStr = encrypter.encrypt(responseStr, 2); //encrypt response using letter substitution
                var response = responseStr.getBytes(UTF_8);
                exchange.sendResponseHeaders(HTTP_OK, response.length);
                exchange.getResponseBody().write(response);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        var server = new CalculatorServer(Executors.newSingleThreadScheduledExecutor());
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}

class Trio{
    double term1; double term2; String operation;
    public Trio( double x, double y, String s ){
        this.term1 = x;
        this.term2 = y;
        this.operation = s;
    }

    @Override
    public int hashCode(){ return (int) ( 31.0 * term1 + term2 ) + operation.charAt(0); }

    @Override
    public boolean equals( Object o ){
        Trio trio = (Trio) o;
        return trio.term1 == this.term1 && trio.term2 == this.term2 && this.operation.equals(trio.operation);
    }
}
