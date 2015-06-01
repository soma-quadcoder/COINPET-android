package com.quadcoder.coinpet.bluetooth;

import java.util.Calendar;

/**
 * Created by Phangji on 6/1/15.
 */
public class BluetoothUtil {
    private static BluetoothUtil ourInstance = new BluetoothUtil();

    public static BluetoothUtil getInstance() {
        return ourInstance;
    }

    private BluetoothUtil() {
    }

    public static final char S = 'S';
    public static final char E = 'E';
    public static final char DUMM = 'r';
    public static final char UNLOCK = 'u';
    public static final char SUCCESS = 's';
    public static final char FAIL = 'f';

    interface Opcode {
        byte PN_REGISTER = 0x01;
        byte PN_RESPONSE = 0x02;
        byte ACK = 0x03;
        byte UTC_SET = 0x04;
        byte GOAL_SET = 0x05;
        byte READ_MONEY_SYNC = 0x06;
        byte WRITE_UNLOCK = 0x07;
        byte READ_MONEY = 0x08;
        byte WRITE_SYNC = 0x09;

        byte BOARD_CON_REQ = 0x10;
        byte BOARD_CON_RES = 0x11;
        byte QUIZ_USER_INPUT = 0x12;
        byte QUIZ_DISCONN = 0x13;
        byte QUIZ_TIMEOVER = 0x14;
        byte QUIZ_GAME_END = 0x15;

    }

    byte[] ack(boolean isSuccess) {
        byte[] buffer = new byte[5];
        buffer[0] = S;
        buffer[1] = Opcode.ACK;
        buffer[2] = 1;
        buffer[buffer.length-1] = E;
        if(isSuccess) {
            buffer[3] = SUCCESS;
        } else {
            buffer[3] = FAIL;
        }

        return buffer;
    }

    byte[] sendUTC() {
        byte[] buffer = new byte[9];
        buffer[0] = S;
        buffer[1] = Opcode.UTC_SET;
        buffer[2] = 5;
        buffer[buffer.length-1] = E;

        Calendar now = Calendar.getInstance();
        int[] data = new int[5];
        data[0] = now.get(Calendar.YEAR);
        data[1] = now.get(Calendar.MONTH);
        data[2] = now.get(Calendar.DAY_OF_MONTH);
        data[3] = now.get(Calendar.HOUR_OF_DAY);
        data[4] = now.get(Calendar.MINUTE);

        for(int i=3; i<buffer.length-1; i++) {
            buffer[i] = (byte)data[i-3];
        }

        return buffer;
    }

    byte[] requestSync() {
        final byte[] buffer = new byte[5];
        buffer[0] = S;
        buffer[1] = Opcode.WRITE_SYNC;
        buffer[2] = 1;
        buffer[buffer.length-1] = E;

        buffer[3] = DUMM;

        return buffer;
    }

    byte[] unlock() {
        final byte[] buffer = new byte[5];
        buffer[0] = S;
        buffer[1] = Opcode.WRITE_UNLOCK;
        buffer[2] = 1;
        buffer[buffer.length-1] = E;

        buffer[3] = UNLOCK;

        return buffer;
    }

    byte[] requestBoardConn() {
        final byte[] buffer = new byte[5];
        buffer[0] = S;
        buffer[1] = Opcode.BOARD_CON_REQ;
        buffer[2] = 1;
        buffer[buffer.length-1] = E;

        buffer[3] = DUMM;

        return buffer;
    }

    byte[] quizTimeOver() {
        final byte[] buffer = new byte[5];
        buffer[0] = S;
        buffer[1] = Opcode.QUIZ_TIMEOVER;
        buffer[2] = 1;
        buffer[buffer.length-1] = E;

        buffer[3] = 'o';

        return buffer;
    }

    byte[] quizIsEnded() {
        final byte[] buffer = new byte[5];
        buffer[0] = S;
        buffer[1] = Opcode.QUIZ_TIMEOVER;
        buffer[2] = 1;
        buffer[buffer.length-1] = E;

        buffer[3] = 'e';

        return buffer;
    }


}