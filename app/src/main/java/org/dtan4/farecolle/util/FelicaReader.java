package org.dtan4.farecolle.util;

import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FelicaReader {
    private static final String TAG = "farecolle.util.FelicaReader";
    private NfcF nfc;
    private byte[] felicaIDm;

    public FelicaReader(Tag tag) {
        nfc = NfcF.get(tag);
        felicaIDm = tag.getId();
    }

    public String felicaId() {
        return toHex(felicaIDm);
    }

    public ArrayList<History> getHistory() {
        ArrayList<History> historyList = null;

        try {
            nfc.connect();

            try {
                byte[] request = readWithoutEncryption(felicaIDm, 10);
                byte[] response = nfc.transceive(request);

                historyList = History.getHistoryList(felicaId(), response);
            } finally {
                nfc.close();
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        return historyList;
    }

    private byte[] readWithoutEncryption(byte[] idm, int size) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);

        bout.write(0);           // dummy
        bout.write(0x06);        // Felica command "Read Without Encryption"
        bout.write(idm);         // card ID (8byte)
        bout.write(1);           // length of service code list
        bout.write(0x0f);        // lower bytes of history service code
        bout.write(0x09);        // upper bytes of history service code
        bout.write(size);        // number of blocks

        for (int i = 0; i < size; i++) {
            bout.write(0x80);    // upper bytes of block element, see User Manual 4.3
            bout.write(i);       // block number
        }

        byte[] msg = bout.toByteArray();
        msg[0] = (byte) msg.length;

        return msg;
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) {
                sb.append("0");
            }
            sb.append(hex);
        }

        return sb.toString();
    }
}
