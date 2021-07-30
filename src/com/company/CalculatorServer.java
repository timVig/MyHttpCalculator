package com.company;
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;

public class CalculatorServer {
    private final HttpServer server;

    public CalculatorServer(Executor exec) throws IOException {
        server = HttpServer.create();
        server.setExecutor(exec);
        server.createContext( "/Calculator/operations", new CommandHandler( this ) );
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

        CommandHandler( CalculatorServer s1 ) {
            this.server = s1;
            this.calculator = new Calculator();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try( exchange ){ //no need to check headers, all are GET requests
                var cmd2 = exchange.getRequestURI().toString();
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

                if( commands.get(2).equals("add") ){ answer = calculator.add( op1, op2 ); }
                if( commands.get(2).equals("sub") ){ answer = calculator.sub( op1, op2 ); }
                if( commands.get(2).equals("mul") ){ answer = calculator.multiply( op1, op2 ); }
                if( commands.get(2).equals("div") ){ answer = calculator.divide( op1, op2 ); }

                String responseStr = String.valueOf(answer);
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
