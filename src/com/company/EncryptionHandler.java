package com.company;

import java.util.HashMap;

public class EncryptionHandler { //handles 2 letter cipher methods for encrypting/decrypting strings
    int caesarCipherKey;
    final char[] letterCKey = "abcdefghijklmnopqrstuvwxyz1234567890./".toCharArray(); //init ECRPT letter sub pairings
    final char[] letterDKey = "4ynwz0fqju9sie58ogbv6231rltd/ckxa7.mhp".toCharArray(); //init DCRPT letter sub pairings
    HashMap<Character, Character> letterCipherKey;
    HashMap<Character, Character> letterDecipherKey;

    public EncryptionHandler(){
        letterCipherKey = new HashMap<>();
        letterDecipherKey = new HashMap<>();
        for( int i = 0; i < letterCKey.length; i++ ){
            letterCipherKey.put( letterCKey[i], letterDKey[i] );
            letterDecipherKey.put( letterDKey[i], letterCKey[i] );
        }
        setKey(13);
    }

    public EncryptionHandler( int caesarKey ){
        setKey(caesarKey);
    }

    public String encrypt( String s, int mode ){
        if( mode == 1 ) return encryptCaesar(s);
        if( mode == 2 ) return encryptLetterCipher(s);
        else return s;
    }

    public String decrypt( String s, int mode ){
        if( mode == 1 ) return decryptCaesar(s);
        if( mode == 2 ) return decryptLetterCipher(s);
        else return s;
    }

    public String encryptLetterCipher( String s ){
        StringBuilder encrypted = new StringBuilder("");
        for( char c: s.toCharArray() ){
            encrypted.append( (char) ( this.letterCipherKey.get(c) ) );
        }
        return encrypted.toString();
    }

    public String decryptLetterCipher( String s ){
        StringBuilder decrypted = new StringBuilder("");
        for( char c: s.toCharArray() ){
            decrypted.append( (char) ( this.letterDecipherKey.get(c) ) );
        }
        return decrypted.toString();
    }

    public String encryptCaesar( String s ){
        StringBuilder encrypted = new StringBuilder("");
        for( char c: s.toCharArray() ){
            encrypted.append( (char) (c - caesarCipherKey) );
        } return encrypted.toString();
    }

    public String decryptCaesar( String s ){
        StringBuilder decrypted = new StringBuilder("");
        for( char c: s.toCharArray() ){
            decrypted.append( (char) (c + caesarCipherKey) );
        } return decrypted.toString();
    }

    private int getKey(){
        return caesarCipherKey;
    }

    public void setKey( int caesarKey ){
        this.caesarCipherKey = caesarKey;
    }
}
