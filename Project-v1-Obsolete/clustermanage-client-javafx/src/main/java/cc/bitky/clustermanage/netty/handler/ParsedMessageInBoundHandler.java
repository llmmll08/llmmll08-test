package cc.bitky.clustermanage.netty.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.bitky.clustermanage.netty.message.MsgType;
import cc.bitky.clustermanage.netty.message.base.IMessage;
import cc.bitky.clustermanage.netty.message.tcp.TcpMsgResponseEmployeeCardnumber;
import cc.bitky.clustermanage.netty.message.tcp.TcpMsgResponseEmployeeDepartment;
import cc.bitky.clustermanage.netty.message.tcp.TcpMsgResponseEmployeeDepartment2;
import cc.bitky.clustermanage.netty.message.tcp.TcpMsgResponseEmployeeName;
import cc.bitky.clustermanage.netty.message.tcp.TcpMsgResponseFreeCardNumber;
import cc.bitky.clustermanage.netty.message.tcp.TcpMsgResponseLed;
import cc.bitky.clustermanage.netty.message.tcp.TcpMsgResponseOperateBoxUnlock;
import cc.bitky.clustermanage.netty.message.tcp.TcpMsgResponseRemainChargeTimes;
import cc.bitky.clustermanage.netty.message.web.WebMsgDeployEmployeeCardNumber;
import cc.bitky.clustermanage.netty.message.web.WebMsgDeployEmployeeDepartment;
import cc.bitky.clustermanage.netty.message.web.WebMsgDeployEmployeeDepartment2;
import cc.bitky.clustermanage.netty.message.web.WebMsgDeployEmployeeDeviceId;
import cc.bitky.clustermanage.netty.message.web.WebMsgDeployEmployeeName;
import cc.bitky.clustermanage.netty.message.web.WebMsgDeployFreeCardSpecial;
import cc.bitky.clustermanage.netty.message.web.WebMsgDeployLedSetting;
import cc.bitky.clustermanage.netty.message.web.WebMsgDeployRemainChargeTimes;
import cc.bitky.clustermanage.netty.message.web.WebMsgInitMarchConfirmCardResponse;
import cc.bitky.clustermanage.view.Container;
import cc.bitky.clustermanage.view.MainView;
import cc.bitky.clustermanage.view.bean.Device;
import cc.bitky.clustermanage.view.bean.DeviceKey;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ParsedMessageInBoundHandler extends SimpleChannelInboundHandler<IMessage> {

    private int sum = 0;
    private int errorSum = 0;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMessage msg) {
        logger.debug("------??????CAN??????msgId=" + msg.getMsgId() + ", groupId=" + msg.getGroupId() + ", boxId=" + msg.getBoxId() + "???------");
        MainView.getInstance().updateGroupCount(msg.getGroupId());
        logger.warn("#%#%?????????????????????" + ++sum);
        MainView.getInstance().remoteUpdateDevice(sum);
        if (errorSum != 0)
            logger.warn("#%#%?????????????????????????????????" + errorSum);
        Device device;

        if (msg.getMsgId() >= 0x30 && msg.getMsgId() <= 0x3F) {
            TcpMsgResponseLed responseLed = new TcpMsgResponseLed(msg.getGroupId(), msg.getBoxId(), 1);
            responseLed.setMsgId(msg.getMsgId() + 0x30);
            switch (msg.getMsgId()) {
                case MsgType.SERVER_LED_SETTING:
                    logger.debug("??????LED?????????");
                    break;
                case MsgType.SERVER_LED_STOP:
                    logger.debug("??????LED?????????");
                    break;
                default:
                    WebMsgDeployLedSetting deployLedSetting = (WebMsgDeployLedSetting) msg;
                    logger.debug("??????LED?????????" + deployLedSetting.getText());
            }
            ctx.channel().writeAndFlush(responseLed);
            return;
        }

        switch (msg.getMsgId()) {
            case MsgType.SERVER_REQUSET_STATUS:
                logger.debug("???????????????????????????");
                break;

            case MsgType.SERVER_SET_REMAIN_CHARGE_TIMES:
                WebMsgDeployRemainChargeTimes remainChargeTimes = (WebMsgDeployRemainChargeTimes) msg;
                logger.debug("??????????????????????????????: " + remainChargeTimes.getTimes());
                device = getDevice(msg);
                addHistory(device, "????????????:" + remainChargeTimes.getTimes());
                MainView.getInstance().remoteUpdateDevice(sum, device);
                ctx.channel().writeAndFlush(new TcpMsgResponseRemainChargeTimes(device.getGroupId(), device.getDeviceId(), device.getStatus() > 4 ? 0 : 1));
                break;

            case MsgType.SERVER_SET_DEVICE_ID:
                WebMsgDeployEmployeeDeviceId deployEmployeeDeviceId = (WebMsgDeployEmployeeDeviceId) msg;
                logger.debug("?????????????????? Id ??????: " + deployEmployeeDeviceId.getUpdatedDeviceId());
                DeviceKey deviceKeyOld = new DeviceKey(msg.getGroupId(), msg.getBoxId());
                DeviceKey deviceKeyNew = new DeviceKey(msg.getGroupId(), deployEmployeeDeviceId.getUpdatedDeviceId());
                Device deviceOld = Container.deviceHashMap.remove(deviceKeyNew);
                device = Container.deviceHashMap.remove(deviceKeyOld);
                if (device == null) {
                    device = new Device(msg.getGroupId(), deployEmployeeDeviceId.getUpdatedDeviceId());
                }
                device.setDeviceId(deployEmployeeDeviceId.getUpdatedDeviceId());
                addHistory(device, "Id:" + deployEmployeeDeviceId.getUpdatedDeviceId());
                Container.deviceHashMap.put(deviceKeyNew, device);
                MainView.getInstance().remoteUpdateDevice(sum, deviceOld, device);
                break;

            case MsgType.SERVER_SET_EMPLOYEE_NAME:
                WebMsgDeployEmployeeName webMsgDeployEmployeeName = (WebMsgDeployEmployeeName) msg;
                logger.debug("??????????????????????????????: " + webMsgDeployEmployeeName.getValue());
                device = getDevice(msg);
                device.setName(webMsgDeployEmployeeName.getValue());
                addHistory(device, "??????:" + webMsgDeployEmployeeName.getValue());
                MainView.getInstance().remoteUpdateDevice(sum, device);
                ctx.channel().writeAndFlush(new TcpMsgResponseEmployeeName(device.getGroupId(), device.getDeviceId(), device.getStatus() > 4 ? 0 : 1));
                break;

            case MsgType.SERVER_SET_EMPLOYEE_DEPARTMENT_1:
                WebMsgDeployEmployeeDepartment deployEmployeeDepartment1 = (WebMsgDeployEmployeeDepartment) msg;
                logger.debug("???????????????????????????1?????????: " + deployEmployeeDepartment1.getValue());
                device = getDevice(msg);
                device.setDepartment(deployEmployeeDepartment1.getValue());
                addHistory(device, "??????1:" + device.getDepartment());
                MainView.getInstance().remoteUpdateDevice(sum, device);
                ctx.channel().writeAndFlush(new TcpMsgResponseEmployeeDepartment(device.getGroupId(), device.getDeviceId(), device.getStatus() > 4 ? 0 : 1));
                break;

            case MsgType.SERVER_SET_EMPLOYEE_DEPARTMENT_2:
                WebMsgDeployEmployeeDepartment2 deployEmployeeDepartment2 = (WebMsgDeployEmployeeDepartment2) msg;
                logger.debug("???????????????????????????2?????????: " + deployEmployeeDepartment2.getValue());
                device = getDevice(msg);
                device.setDepartment(device.getDepartment() + deployEmployeeDepartment2.getValue());
                addHistory(device, "??????2:" + deployEmployeeDepartment2.getValue());
                MainView.getInstance().remoteUpdateDevice(sum, device);
                ctx.channel().writeAndFlush(new TcpMsgResponseEmployeeDepartment2(device.getGroupId(), device.getDeviceId(), device.getStatus() > 4 ? 0 : 1));
                break;

            case MsgType.SERVER_SET_EMPLOYEE_CARD_NUMBER:
                WebMsgDeployEmployeeCardNumber deployEmployeeCardNumber = (WebMsgDeployEmployeeCardNumber) msg;
                logger.debug("??????????????????????????????: " + deployEmployeeCardNumber.getCardNumber());
                device = getDevice(msg);
                device.setCardNumber(deployEmployeeCardNumber.getCardNumber());
                addHistory(device, "??????:" + deployEmployeeCardNumber.getCardNumber());
                MainView.getInstance().remoteUpdateDevice(sum, device);
                ctx.channel().writeAndFlush(new TcpMsgResponseEmployeeCardnumber(device.getGroupId(), device.getDeviceId(), device.getStatus() > 4 ? 0 : 1));
                break;

            case MsgType.SERVER_REMOTE_UNLOCK:
                logger.debug("???????????????????????????");
                device = getDevice(msg);
                addHistory(device, "??????");
                MainView.getInstance().remoteUpdateDevice(sum, device);
                ctx.channel().writeAndFlush(new TcpMsgResponseOperateBoxUnlock(device.getGroupId(), device.getDeviceId(), device.getStatus() > 4 ? 0 : 1));
                break;

            case MsgType.SERVER_SET_FREE_CARD_NUMBER:
                WebMsgDeployFreeCardSpecial deployFreeCardSpecial = (WebMsgDeployFreeCardSpecial) msg;
                logger.debug("??????????????????????????????" + deployFreeCardSpecial.getItemId() + "???" + deployFreeCardSpecial.getCardNumber() + "???");
                device = getDevice(msg);
                addHistory(device, "??????:" + deployFreeCardSpecial.getItemId());
                MainView.getInstance().remoteUpdateDevice(sum, device);
                ctx.channel().writeAndFlush(new TcpMsgResponseFreeCardNumber(device.getGroupId(), device.getDeviceId(), device.getStatus() > 4 ? 0 : 1, deployFreeCardSpecial.getItemId()));
                break;

            case MsgType.INITIALIZE_SERVER_MARCH_CONFIRM_CARD_RESPONSE:
                WebMsgInitMarchConfirmCardResponse marchConfirmCardResponse = (WebMsgInitMarchConfirmCardResponse) msg;
                logger.debug("??????????????? ????????????????????????: " + marchConfirmCardResponse.isSuccessful());
                device = getDevice(msg);
                addHistory(device, "??????:" + marchConfirmCardResponse.isSuccessful());
                MainView.getInstance().remoteUpdateDevice(sum, device);
                break;

            case MsgType.INITIALIZE_SERVER_CLEAR_INITIALIZE_MESSAGE:
                logger.debug("??????????????? ??????????????????????????????");
                device = getDevice(msg);
                device.setName("");
                device.setDepartment("");
                device.setCardNumber("0");
                device.setStatus(0);
                addHistory(device, "????????????");
                MainView.getInstance().remoteUpdateDevice(sum, device);
                break;

            default:
                logger.warn("????????????????????? Message");
                errorSum++;
                break;
        }
    }

    /**
     * ?????? HashMap ?????????????????? bean ?????????????????????
     *
     * @param device   ??? HashMap ?????????????????? bean
     * @param addValue ??????????????????
     */
    private void addHistory(Device device, String addValue) {
        String finalStr = (device.getHistoryList().size() + 1) + ". " + addValue;
        device.addHistoryList(finalStr);
    }

    /**
     * ??????????????? HashMap ?????????????????? bean
     *
     * @param msg ????????? Message
     * @return ?????????????????? bean
     */
    private Device getDevice(IMessage msg) {
        DeviceKey deviceKey = new DeviceKey(msg.getGroupId(), msg.getBoxId());
        Device device = Container.deviceHashMap.get(deviceKey);
        if (device == null) {
            device = new Device(msg.getGroupId(), msg.getBoxId());
            Container.deviceHashMap.put(deviceKey, device);
        }
        return device;
    }

    /**
     * ????????????????????????
     *
     * @param rec ??????????????????????????????
     * @param err ??????????????????????????????
     */
    void clearRecCount(boolean rec, boolean err) {
        if (rec) sum = 0;
        if (err) errorSum = 0;
    }
}

