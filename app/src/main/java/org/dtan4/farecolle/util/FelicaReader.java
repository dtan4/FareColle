package org.dtan4.farecolle.util;

import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class FelicaReader {
    private static final String TAG = "farecolle.util.FelicaReader";
    private NfcF nfc;
    private byte[] felicaIDm;

    public FelicaReader(Tag tag) {
        nfc = NfcF.get(tag);
        felicaIDm = tag.getId();
    }

    public String felicaIDStr() {
        return toHex(felicaIDm);
    }

    public ArrayList<History> getHistory() {
        ArrayList<History> historyList = null;

        try {
            nfc.connect();

            try {
                byte[] request = readWithoutEncryption(felicaIDm, 10);
                byte[] response = nfc.transceive(request);

                historyList = History.getHistoryList(response);
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

        bout.write(0);           // データ長バイトのダミー
        bout.write(0x06);        // Felicaコマンド「Read Without Encryption」
        bout.write(idm);         // カードID 8byte
        bout.write(1);           // サービスコードリストの長さ(以下２バイトがこの数分繰り返す)
        bout.write(0x0f);        // 履歴のサービスコード下位バイト
        bout.write(0x09);        // 履歴のサービスコード上位バイト
        bout.write(size);        // ブロック数

        for (int i = 0; i < size; i++) {
            bout.write(0x80);    // ブロックエレメント上位バイト 「Felicaユーザマニュアル抜粋」の4.3項参照
            bout.write(i);       // ブロック番号
        }

        byte[] msg = bout.toByteArray();
        msg[0] = (byte) msg.length; // 先頭１バイトはデータ長

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
