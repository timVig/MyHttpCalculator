package com.company;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class CalculatorClient {
    static JFrame frame = new JFrame( "Calculator" );
    static String operand1 = "";
    static String operand2 = "";
    static String operation = "";
    static String convertResult = "000000000";
    static int mode = 0;
    static boolean reloaded = false;
    static HttpClient client = HttpClient.newHttpClient();
    static HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
    static String local = "http://localhost:8001";
    static String httplink = "/Calculator/operations/";
    static URI uri;
    static EncryptionHandler encrypter = new EncryptionHandler();

    public static void main(String[] args) {
        startUI();
    }

    public static void startUI(){
        frame.setSize( 550, 700 );
        frame.getContentPane().add(new MyCanvas());
        frame.setVisible( true );
        frame.repaint();
        frame.addMouseListener( new myMouseListener() );
    }

    public static void readClick( char c ){
        if( mode == 0 ){ //dealing with first operand
            if( c == '+' || c == '-' || c == '*' || c == '/' ) {
                operation += c; mode = 1;
                convertResult += " " + operation + " ";
            } else {
                if( !isFull(operand1) && !reloaded ){ operand1 += c; }
                convertResult = operand1;
            }

        } else if ( mode == 1 ){ //dealing with second operand
            if( c == '=' ){
                double term1 = Double.parseDouble( operand1 );
                double term2 = Double.parseDouble( operand2 );
                double result = 0;

                if( operation.equals("+") ) result = Double.parseDouble(sendRequest("add", term1, term2));
                if( operation.equals("-") ) result = Double.parseDouble(sendRequest("sub", term1, term2));
                if( operation.equals("*") ) result = Double.parseDouble(sendRequest("mul", term1, term2));
                if( operation.equals("/") ) result = Double.parseDouble(sendRequest("div", term1, term2));

                convertResult = String.valueOf( result );
                reload();
            } else if( !( c == '+' || c == '-' || c == '*' || c == '/'  ) ) {
                if( !isFull(operand2) ){
                    operand2 += c; convertResult += c;
                }
            }
        }
    }

    public static String sendRequest( String op, double term1, double term2 ){
        String request = local + httplink;
        uri = URI.create( request );
        String encrypted = encrypter.encrypt( op + "/" + term1 +  "/" + term2, 1 ); //encrypt using caesarCipher

        HttpRequest.BodyPublisher b1 = HttpRequest.BodyPublishers.ofString( encrypted );

        HttpRequest.Builder build = HttpRequest.newBuilder(uri).
                version(HttpClient.Version.HTTP_1_1).POST(b1).setHeader("Accept", "application/json");

        HttpResponse<String> reply = null;

        try { reply = client.send( build.build(), handler ); }
        catch (IOException | InterruptedException e) { e.printStackTrace(); }
        String response = encrypter.decrypt( reply.body(), 2 ); //decrypt using letter substitution cipher
        return response;
    }

    public static boolean isFull( String s ){ return s.length() > 8; }

    public static void reset(){
        mode = 0;
        operation = "";
        operand1 = "";
        operand2 = "";
        convertResult = "000000000";
        reloaded = false;
    }

    public static void reload(){
        operand1 = convertResult;
        operand2 = "";
        operation = "";
        mode = 0;
        reloaded = true;
    }

    static class myMouseListener implements MouseListener{

        @Override
        public void mouseClicked(MouseEvent e) {
            if( e.getX() >= 100 && e.getX() <= 150 ){ //col 0
                if( e.getY() >= 200 && e.getY() <= 300 ){ readClick('1'); }
                else if ( e.getY() >= 300 && e.getY() <= 400 ){ readClick('4'); }
                else if ( e.getY() >= 400 && e.getY() <= 500 ){ readClick('7'); }
                else if( e.getY() >= 500 && e.getY() <= 600 ){ reset(); }
            } else if ( e.getX() >= 180 && e.getX() <= 230 ) { //col 1
                getYClick( '2', '5', '8', '0', e );
            } else if( e.getX() >= 270 && e.getX() <= 320 ) { //col 2
                getYClick( '3', '6', '9', '=', e );
            } else if( e.getX() >= 350 && e.getX() <= 400 ) { //col 3
                getYClick( '+', '-', '*', '/', e );
            } frame.repaint();
        }

        public void getYClick( char c1, char c2, char c3, char c4, MouseEvent e ){
            if( e.getY() >= 200 && e.getY() <= 300 ){ readClick(c1); }
            else if ( e.getY() >= 300 && e.getY() <= 400 ){ readClick(c2); }
            else if ( e.getY() >= 400 && e.getY() <= 500 ){ readClick(c3); }
            else if( e.getY() >= 500 && e.getY() <= 600 ){ readClick(c4); }
        }

        public void mousePressed(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
    }

    static class MyCanvas extends JComponent {
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor( new Color(0x000000) ); //whole calculator
            g2.drawRect( 50, 50, 400, 550 );
            g2.fillRect( 50, 50, 400, 550 );

            g2.setColor( new Color(0xFFFFFF) ); //display screen
            g2.drawRect( 100, 100, 300, 50 );
            g2.fillRect( 100, 100, 300, 50 );

            g2.setColor( new Color(0x5E5B5E) ); //draw buttons
            fillCol( 200, g2 );
            fillCol(300, g2 );
            fillCol( 400, g2 );
            fillCol(500, g2 );

            g2.setColor( new Color(0x000000)); //display font
            g2.setFont( new Font( "Times New Roman", Font.BOLD, 20 ) );
            g2.drawString(convertResult, 105, 140 );

            g2.setFont( new Font( "Times New Roman", Font.BOLD, 30 ) );
            g2.setColor( new Color(0xFFFFFF)); //button font
            drawCol( 115, g2, "1", "4", "7", "c" );
            drawCol( 195, g2, "2", "5", "8", "0" );
            drawCol( 285, g2, "3", "6", "9", "=" );
            drawCol( 365, g2, "+", "-", "*", "/" );
        }

        public void fillCol( int y, Graphics g2 ){
            g2.drawRect( 100, y, 50, 50 );
            g2.drawRect( 180, y, 50, 50 );
            g2.drawRect( 270, y, 50, 50 );
            g2.drawRect( 350, y, 50, 50 );
            g2.fillRect( 100, y, 50, 50 );
            g2.fillRect( 180, y, 50, 50 );
            g2.fillRect( 270, y, 50, 50 );
            g2.fillRect( 350, y, 50, 50 );
        }

        public void drawCol( int x, Graphics g2, String s1, String s2, String s3, String s4 ){
            g2.drawString(s1, x, 240 );
            g2.drawString(s2, x, 340 );
            g2.drawString(s3, x, 440 );
            g2.drawString(s4, x, 540 );
        }
    }
}
