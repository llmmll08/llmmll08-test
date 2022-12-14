package cc.bitky.clustermanage.tcp.util;

import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cc.bitky.clustermanage.server.message.base.BaseMessage;
import cc.bitky.clustermanage.server.message.web.WebMsgDeployEmployeeCardNumber;
import cc.bitky.clustermanage.server.message.web.WebMsgDeployEmployeeDepartment;
import cc.bitky.clustermanage.server.message.web.WebMsgDeployEmployeeDeviceId;
import cc.bitky.clustermanage.server.message.web.WebMsgDeployEmployeeName;
import cc.bitky.clustermanage.server.message.web.WebMsgDeployFreeCardNumber;
import cc.bitky.clustermanage.server.message.web.WebMsgDeployLedSetting;
import cc.bitky.clustermanage.server.message.web.WebMsgDeployRemainChargeTimes;
import cc.bitky.clustermanage.server.message.web.WebMsgInitClearDeviceStatus;
import cc.bitky.clustermanage.server.message.web.WebMsgInitMarchConfirmCardResponse;
import cc.bitky.clustermanage.server.message.web.WebMsgObtainDeviceStatus;
import cc.bitky.clustermanage.server.message.web.WebMsgOperateBoxUnlock;

@Component
public class TcpMsgBuilder {
    private Charset charset_GB2312 = Charset.forName("GB2312");

    public static String byteArrayToString(byte[] cards) {
        StringBuilder builder = new StringBuilder();
        for (byte b : cards) {
            String s = Integer.toHexString(b & 0xFF).toUpperCase();
            if (s.length() == 1) {
                builder.append('0').append(s);
            } else builder.append(s);
        }
        return builder.toString();
    }

    public static byte[] stringCardToByteArray(String cards) {
        if (cards.length() > 16) cards = cards.substring(0, 16);
        if (cards.length() < 16) {
            int count = 16 - cards.length();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < count; i++) {
                builder.append("0");
            }
            builder.append(cards);
            cards = builder.toString();
        }

        byte[] bytes = new byte[8];
        cards = cards.toUpperCase();
        char[] hexChars = cards.toCharArray();
        for (int i = 0; i < 8; i++) {
            int pos = i * 2;
            bytes[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return bytes;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * ??????????????????????????? CAN ???
     *
     * @param webMsgObtainDeviceStatus ?????????????????? bean
     * @return ????????????????????? CAN ???
     */
    public byte[] buildRequestDeviceStatus(WebMsgObtainDeviceStatus webMsgObtainDeviceStatus) {
        return buildMsgOutline(webMsgObtainDeviceStatus);
    }

    /**
     * ??????????????????????????? CAN ???
     *
     * @param webMsgDeployRemainChargeTimes ?????????????????? bean
     * @return ????????????????????? CAN ???
     */
    public byte[] buildRemainChargeTimes(WebMsgDeployRemainChargeTimes webMsgDeployRemainChargeTimes) {
        byte[] bytes = buildMsgOutline(webMsgDeployRemainChargeTimes);
        bytes[0] += 1;
        bytes[5] = (byte) webMsgDeployRemainChargeTimes.getTimes();
        return bytes;
    }

    /**
     * ???????????? ID ???CAN???
     *
     * @param webMsgDeployEmployeeDeviceId ??????ID bean
     * @return ??????ID??? CAN ???
     */
    public byte[] buildDeviceId(WebMsgDeployEmployeeDeviceId webMsgDeployEmployeeDeviceId) {
        byte[] bytes = buildMsgOutline(webMsgDeployEmployeeDeviceId);
        bytes[0] += 1;
        bytes[5] = (byte) webMsgDeployEmployeeDeviceId.getUpdatedDeviceId();
        return bytes;
    }

    /**
     * ????????????????????? CAN ???
     *
     * @param webMsgDeployEmployeeName ???????????? bean
     * @return ??????????????? CAN ???
     */
    public byte[] buildEmployeeName(WebMsgDeployEmployeeName webMsgDeployEmployeeName) {
        byte[] bytes = buildMsgOutline(webMsgDeployEmployeeName);

        byte[] bytesBody = webMsgDeployEmployeeName.getValue().getBytes(charset_GB2312);
        bytes[0] += bytesBody.length > 8 ? 8 : bytesBody.length;
        addMessageBody(bytes, bytesBody, 0);
        return bytes;
    }

    /**
     * ????????????????????? CAN ???
     *
     * @param webMsgDeployEmployeeDepartment ???????????? bean
     * @return ??????????????? CAN ???
     */
    public byte[] buildEmployeeDepartment(WebMsgDeployEmployeeDepartment webMsgDeployEmployeeDepartment) {
        byte[] bytes = buildMsgOutline(webMsgDeployEmployeeDepartment);
        byte[] bytesBody = webMsgDeployEmployeeDepartment.getValue().getBytes(charset_GB2312);

        byte[] bytes2 = bytes.clone();
        bytes2[2] += 1;
        addMessageBody(bytes, bytesBody, 0);
        addMessageBody(bytes2, bytesBody, 8);

        if (bytesBody.length > 8) {
            bytes[0] += 8;
            bytes2[0] += (bytesBody.length - 8) > 8 ? 8 : (bytesBody.length - 8);
        } else {
            bytes[0] += bytesBody.length;
        }

        byte[] bytes3 = new byte[13 * 2];
        System.arraycopy(bytes, 0, bytes3, 0, 13);
        System.arraycopy(bytes2, 0, bytes3, 13, 13);
        return bytes3;
    }

    /**
     * ????????????????????? CAN ???
     *
     * @param webMsgDeployEmployeeCardNumber ???????????? bean
     * @return ??????????????? CAN ???
     */
    public byte[] buildEmployeeCardNumber(WebMsgDeployEmployeeCardNumber webMsgDeployEmployeeCardNumber) {
        byte[] bytes = buildMsgOutline(webMsgDeployEmployeeCardNumber);
        bytes[0] += 8;
        byte[] byteCardNum = stringCardToByteArray(webMsgDeployEmployeeCardNumber.getCardNumber());
        addMessageBody(bytes, byteCardNum, 0);
        return bytes;
    }

    /**
     * ?????????????????? CAN ???
     *
     * @param webMsgOperateBoxUnlock ?????? bean
     * @return ???????????? CAN ???
     */
    public byte[] buildWebUnlock(WebMsgOperateBoxUnlock webMsgOperateBoxUnlock) {
        return buildMsgOutline(webMsgOperateBoxUnlock);
    }

    /**
     * ?????????????????? CAN ???
     *
     * @param webMsgDeployFreeCardNumber ???????????? bean
     * @return ??????????????? CAN ???
     */
    public byte[] buildFreeCardNumber(WebMsgDeployFreeCardNumber webMsgDeployFreeCardNumber) {
        String[] cards = webMsgDeployFreeCardNumber.getCardNumbers();
        int count = cards.length < 16 ? cards.length : 16;
        byte[] bytesSend = new byte[13 * count];

        for (int i = 0; i < count; i++) {
            byte[] bytes = buildMsgOutline(webMsgDeployFreeCardNumber);
            bytes[2] += i;
            addMessageBody(bytes, stringCardToByteArray(cards[i]), 0);
            System.arraycopy(bytes, 0, bytesSend, 13 * i, 13);
        }
        return bytesSend;
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param marchConfirmCardSuccessful ???????????????????????? bean
     * @return ????????????????????????????????? CAN ???
     */
    public byte[] buildInitMarchConfirmCardSuccessful(WebMsgInitMarchConfirmCardResponse marchConfirmCardSuccessful) {
        byte[] bytes = buildMsgOutline(marchConfirmCardSuccessful);
        bytes[0] += 1;
        bytes[5] = (byte) (marchConfirmCardSuccessful.isSuccessful() ? 1 : 0);
        return bytes;
    }


    /**
     * ???????????????????????????????????? CAN ???
     *
     * @param clearDeviceStatus ??????????????????????????? bean
     * @return ?????????????????????????????? CAN ???
     */
    public byte[] buildClearDeviceStatus(WebMsgInitClearDeviceStatus clearDeviceStatus) {
        return buildMsgOutline(clearDeviceStatus);
    }

    public List<byte[]> buildLedSetting(WebMsgDeployLedSetting led) {
        byte[] bytesBody = led.getText().getBytes(charset_GB2312);
        bytesBody = Arrays.copyOf(bytesBody, bytesBody.length + 2);
        bytesBody[bytesBody.length - 2] = 0x0D;
        bytesBody[bytesBody.length - 1] = 0x0A;
        int len = (int) Math.ceil(bytesBody.length / 8.0);
        byte[] byteSetting = buildMsgOutline(led);
        byteSetting[0] += 4;
        byteSetting[5] = led.getBytePoint();
        byteSetting[6] = (byte) led.getDuration();
        byteSetting[7] = (byte) len;
        byteSetting[8] = (byte) led.getBrightness();

        List<byte[]> list = new ArrayList<>();
        list.add(byteSetting);
        byte[] bytesRaw = buildMsgOutline(led);
        bytesRaw[2] += 3;
        for (int i = 0; i < len; i++) {
            byte[] bytes = Arrays.copyOf(bytesRaw, bytesRaw.length);
            int frameLen = bytesBody.length - i * 8 > 8 ? 8 : bytesBody.length - i * 8;
            bytes[0] += frameLen;
            bytes[2] += i;
            System.arraycopy(bytesBody, i * 8, bytes, 5, frameLen);
            list.add(bytes);
        }
        return list;
    }

    /**
     * ?????? CAN ?????????????????????????????????????????????
     *
     * @param message ???????????? CAN ?????? bean
     * @return ?????? CAN ???
     */
    public byte[] buildMsgOutline(BaseMessage message) {
        byte[] bytes = new byte[13];
        bytes[0] = (byte) 0x80;
        bytes[2] = (byte) message.getMsgId();
        bytes[3] = (byte) message.getDeviceId();
        bytes[4] = (byte) message.getGroupId();
        return bytes;
    }

    /**
     * ?????????CAN?????????????????????
     *
     * @param bytes     ??????CAN???
     * @param bytesBody ?????????
     * @param offset    ????????????????????????offset???????????????8?????????
     */
    private void addMessageBody(byte[] bytes, byte[] bytesBody, int offset) {
        int max = (bytesBody.length - offset) > 8 ? 8 : (bytesBody.length - offset);
        System.arraycopy(bytesBody, offset, bytes, 5, max);
    }

    private long byteArrayToLong(byte[] bytes) {
        long num = 0;
        for (int i = 0; i <= 7; i++) {
            num += (bytes[i] & 0xffL) << ((7 - i) * 8);
        }
        return num;
    }

    private byte[] longToByteArray(long num) {
        byte[] bytes = new byte[8];
        for (int i = 0; i <= 7; i++) {
            bytes[i] = (byte) ((num >> ((7 - i) * 8)) & 0xff);
        }
        return bytes;
    }


}
