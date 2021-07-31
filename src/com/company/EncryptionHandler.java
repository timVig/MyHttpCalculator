package com.company;

public class EncryptionHandler {
    int letterCipherKey;
    public EncryptionHandler(){
        setKey(13);
    }

    public EncryptionHandler( int caesarKey ){
        setKey(caesarKey);
    }

    public String encrypt( String s ){
        StringBuilder encrypted = new StringBuilder("");
        for( char c: s.toCharArray() ){
            encrypted.append( (char) (c - letterCipherKey) );
        } return encrypted.toString();
    }

    public String decrypt( String s ){
        StringBuilder decrypted = new StringBuilder("");
        for( char c: s.toCharArray() ){
            decrypted.append( (char) (c + letterCipherKey) );
        } return decrypted.toString();
    }

    private int getKey(){
        return letterCipherKey;
    }

    public void setKey( int caesarKey ){
        this.letterCipherKey = caesarKey;
    }
}
