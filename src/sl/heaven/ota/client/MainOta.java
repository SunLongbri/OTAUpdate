package sl.heaven.ota.client;

import iot.espressif.esp32.model.device.EspDeviceFactory;
import iot.espressif.esp32.model.device.IEspDevice;
import iot.espressif.esp32.model.device.other.EspOTAClient;
import iot.espressif.esp32.model.net.MeshNode;
import iot.espressif.esp32.utils.DeviceUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import libs.espressif.net.EspHttpParams;

public class MainOta {
    /**
     * @param args
     * 需要从shell脚本中传入的参数
     */
    public static int closeNum = 0;
    public static int isStopNum = 0;
    static long startTime = 0;
    static long endTime = 0;
    private static ExecutorService singleThreadExecutor;

    public static void main(String[] args) {
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        // d8a01d648348
        // d8a01d648450
        // d8a01d648614
        // d8a01d648ba4
        //103"d8a01d648450,d8a01d612a50","C:/Users/SL/Desktop/Honeywell_Sensor_v0.10.8.3.bin","109","d8a01d648ba4,d8a01d61294c"

        startTime = System.currentTimeMillis();

        //四个设备同时升
        String[] test = {"192.168.10.105", "192.168.10.103", "C:/Users/SL/Desktop/Honeywell_Sensor_v0.10.8.4.bin", "888888888888",
                "d8a01d648450,d8a01d612a50", "192.168.10.110", "C:/Users/SL/Desktop/Honeywell_Sensor_v0.10.8.4.bin", "999999999999",
                "d8a01d648ba4,d8a01d61294c"};
        // 需要将结果发送出去的ip
        final String mSendIp = test[0];
        closeNum = 0;
        isStopNum = 0;
        int num = test.length;
        for (int i = 1; i < num; i++) {
            closeNum++;
            final String ip = test[i];
            final String path = test[i + 1];
            final String meshId = test[i + 2];
            final String[] updateOta = test[i + 3].split(",");
            i = i + 3;
//			new Thread() {
//				public void run() {
//					try {
            updateData(mSendIp, ip, path, meshId, updateOta);
//						sleep(1000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}.start();
        }
    }

    public static void updateData(final String sendIp, String ip, String path, final String meshId, final String[] data) {

        String mCurrentMac = "";
        String mCurrentProgress = "";
        int dataLength = data.length;
        List<IEspDevice> devices = null;
        final List<MacInfo> macList = new ArrayList<>();
        final List<MacInfo> failList = new ArrayList<>();
        final List<MacInfo> successList = new ArrayList<>();
        for (int i = 0; i < dataLength; i++) {
            MacInfo macInfo = new MacInfo();
            String mac = (String) data[i];
            macInfo.setmIp(ip);
            macInfo.setmMac(mac);
            macList.add(macInfo);
        }
        System.out.println("初始得macList的值：" + macList.size());
        devices = new LinkedList<IEspDevice>();
        for (int i = 0; i < macList.size(); i++) {
            MeshNode node = new MeshNode();
            MacInfo macInfo = macList.get(i);
            node.setMac(macInfo.getmMac());
            node.setHost(macInfo.getmIp());
            IEspDevice device = EspDeviceFactory.parseMeshNode(node); // 新建实例
            device.setProtocolPort(80);
            device.setProtocol("http");
            device.setMeshId(meshId);
            devices.add(device);
        }
        // C:/Users/SL/Desktop/Honeywell_Sensor.bin
        File bin = new File(path); // path
        // 设置升级文件路径
        EspOTAClient otaClient = new EspOTAClient(bin, devices,
                new EspOTAClient.OTACallback() {
                    @Override
                    public void onOTAPrepare(EspOTAClient client) {

                        System.out.println("升级前的准备client=" + client);
                    }

                    @Override
                    public void onOTAProgressUpdate(EspOTAClient client, List<EspOTAClient.OTAProgress> progressList) {

                        // OTA 升级过程中回调
                        for (EspOTAClient.OTAProgress otaProgress : progressList) {
                            IEspDevice device = otaProgress.getDevice();
                            String mMeshId = device.getMeshId();
                            String mCurrentMac = device.getMac();
                            int progress = otaProgress.getProgress();
                            String message = otaProgress.getMessage();
//							System.out
//									.println("升级过程中的设备:mCurrentMac:"
//											+ mCurrentMac + "progress="
//											+ progress + "");
                            String sendData = mMeshId + "-" + progress;

                            //httpRequestUtil.sendPost("http://" + sendIp+ ":3000/api/ota/alter/progress", sendData);
                            System.out.println("00000000000000000 :" + progress);

                            singleThreadExecutor.execute(new DataRunnable("http://" + sendIp + ":3000/api/ota/alter/progress", sendData));
                            System.out.println(mCurrentMac + "------------------>progressInfo" + sendData);
                        }

                    }

                    @Override
                    public void onOTAResult(EspOTAClient client, List<IEspDevice> completeDevices) {
                        System.out.println("=========> close");
                        endTime = System.currentTimeMillis();
                        long time = endTime - startTime;
                        System.out.println("------------------------>当前升级一共使用了" + time / 1000 + "秒!");

                        // OTA 升级结束时回调
                        // completeDevices 为升级成功的设备
                        isStopNum++;
                        String mMeshId = meshId;
                        for (IEspDevice device : completeDevices) {
                            MacInfo macInfo = new MacInfo();
                            String mac = device.getMac();
                            macInfo.setmMac(device.getMac());
                            System.out.print("升级成功的设备有:" + mac);
                            successList.add(macInfo);
                        }
                        // 通过比较获取升级失败的设备
                        List<MacInfo> failList = getFailerDevice(macList,
                                successList);
                        System.out.println("升级失败的设备总量:" + failList.size());
                        String failMac = mMeshId + ":";
                        System.out.println("升级成功的设备数量:" + successList.size());
                        System.out.println("总的设备数量:" + macList.size());

                        for (int i = 0; i < failList.size(); i++) {
                            MacInfo macInfo = new MacInfo();
                            macInfo = failList.get(i);
                            failMac = failMac + "-" + macInfo.getmMac();
                            System.out.println("升级失败的设备有:" + macInfo.getmMac());
                        }

                        //	HttpRequestUtil httpRequestUtil = new HttpRequestUtil();
                        singleThreadExecutor.execute(new DataRunnable("http://" + sendIp
                                + ":3000/api/ota/alter/state", failMac));
						/*httpRequestUtil.sendPost("http://" + sendIp
								+ ":3000/api/ota/alter/state", failMac);*/
                        System.out.println("最终发送给服务端的信息为:" + failMac);
                        EspHttpParams params = new EspHttpParams();
                        params.setTryCount(3);
                        DeviceUtil.delayRequestRetry(completeDevices,
                                "ota_reboot", params);
                        if (closeNum == isStopNum) {
                            client.close();
                            System.out.println("程序执行完毕了...");
                        }
                    }
                });
        otaClient.start();
        System.out.println("==========> update "+ meshId);
    }


    /**
     * @param allList     当前所有的设备集合
     * @param successList 回调成功的设备集合
     * @return
     */
    private static List<MacInfo> getFailerDevice(List<MacInfo> allList,
                                                 List<MacInfo> successList) {
        MacInfo macAllInfo;
        MacInfo macSucInfo;
        MacInfo macFailInfo;
        List<MacInfo> macList = new ArrayList<>();
        for (int i = 0; i < allList.size(); i++) {
            macFailInfo = new MacInfo();
            macAllInfo = allList.get(i);
            String allMac = macAllInfo.getmMac();
            for (int j = 0; j < successList.size(); j++) {
                macSucInfo = successList.get(j);
                String sucMac = macSucInfo.getmMac();
                if (sucMac.equals(allMac)) {
                    break;
                }
                if (j == successList.size() - 1) {
                    macFailInfo.setmMac(allMac);
                    macList.add(macFailInfo);
                }
            }
            if (successList.size() == 0) {
                macFailInfo.setmMac(allMac);
                macList.add(macFailInfo);
            }
        }
        return macList;
    }


    public static class DataRunnable implements Runnable {
        private String url;
        private String param;

        public DataRunnable(String url, String param) {
            this.url = url;
            this.param = param;
        }

        @Override
        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;
            StringBuilder result = new StringBuilder();
            try {
                URL realUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
                conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("connection", "Keep-Alive");
                conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("contentType", "UTF-8");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                //conn.setDoInput(true);
                out = new PrintWriter(conn.getOutputStream());
                out.print(param);
                out.flush();
                System.out.println("rescode:" + conn.getResponseCode());
                if (conn.getResponseCode() == 200) {
                    InputStream inputStream = conn.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length = -1;
                    while ((length = inputStream.read(buffer)) > 0) {
                        baos.write(buffer, 0, length);
                    }
                    String resStr = new String(baos.toByteArray(), "UTF-8");
                    inputStream.close();
                    System.out.println("result : " + resStr);
                  /*  String line;
                    while ((line = in.readLine()) != null) {
                        result.append(line);
                    }*/

                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }
    }
}
