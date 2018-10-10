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
     * 闇�瑕佷粠shell鑴氭湰涓紶鍏ョ殑鍙傛暟
     */
    public static int closeNum = 0;
    public static int isStopNum = 0;
    public static int currentNum = 0;
    public String failMacIds = "";
    static long startTime = 0;
    static long endTime = 0;
    private static ExecutorService singleThreadExecutor;
    private static int rowNum = 50;

    public static void main(String[] args) {
        singleThreadExecutor = Executors.newSingleThreadExecutor();

        //103"d8a01d648450,d8a01d612a50","C:/Users/SL/Desktop/Honeywell_Sensor_v0.10.8.3.bin","109","d8a01d648ba4,d8a01d61294c"
        startTime = System.currentTimeMillis();
        String[] test = {"192.168.10.105", "192.168.10.109", "C:/Users/SL/Desktop/Honeywell_Sensor_v0.10.9.2.bin", "888888888888",
                "d8a01d648450,d8a01d612a50", "192.168.10.110", "C:/Users/SL/Desktop/Honeywell_Sensor_v0.10.9.2.bin", "999999999999",
                "d8a01d648ba4,d8a01d61294c"};


        ParamBean paramBean = new ParamBean();
        List<MeshBean> meshBeanList = new ArrayList<>();
        paramBean.setServerIp(test[0]);

        int num = test.length;
        for (int i = 1; i < num; i++) {
            MeshBean meshBean = new MeshBean();
            String ip = test[i];
            String path = test[i + 1];
            String meshId = test[i + 2];
            String[] updateOta = test[i + 3].split(",");
            int row = (int) Math.ceil(updateOta.length / rowNum);


            for (int j = 0; j < row; j++) {

            }
            meshBean.setMeshTargetIp(ip);
            meshBean.setFilePath(path);
            meshBean.setMeshId(meshId);
            i = i + 3;
        }


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

            updateData(mSendIp, ip, path, meshId, updateOta);
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
        devices = new LinkedList<IEspDevice>();
        for (int i = 0; i < macList.size(); i++) {
            MeshNode node = new MeshNode();
            MacInfo macInfo = macList.get(i);
            node.setMac(macInfo.getmMac());
            node.setHost(macInfo.getmIp());
            IEspDevice device = EspDeviceFactory.parseMeshNode(node); // 鏂板缓瀹炰緥
            device.setProtocolPort(80);
            device.setProtocol("http");
            device.setMeshId(meshId);
            devices.add(device);
        }
        // C:/Users/SL/Desktop/Honeywell_Sensor.bin
        File bin = new File(path); // path
        EspOTAClient otaClient = new EspOTAClient(bin, devices, new EspOTAClient.OTACallback() {
            @Override
            public void onOTAPrepare(EspOTAClient client) {
                System.out.println("鍗囩骇鍓嶇殑鍑嗗client=" + client);
            }

            @Override
            public void onOTAProgressUpdate(EspOTAClient client, List<EspOTAClient.OTAProgress> progressList) {
                for (EspOTAClient.OTAProgress otaProgress : progressList) {
                    IEspDevice device = otaProgress.getDevice();
                    String mMeshId = device.getMeshId();
                    String mCurrentMac = device.getMac();
                    int progress = otaProgress.getProgress();
                    String message = otaProgress.getMessage();
                    String sendData = mMeshId + "-" + progress;
                    singleThreadExecutor.execute(new DataRunnable("http://" + sendIp + ":3000/api/ota/alter/progress", sendData));
                    System.out.println(mCurrentMac + " -------- progress info ------- " + sendData);
                }
            }

            @Override
            public void onOTAResult(EspOTAClient client, List<IEspDevice> completeDevices) {

                isStopNum++;
                String mMeshId = meshId;
                for (IEspDevice device : completeDevices) {
                    MacInfo macInfo = new MacInfo();
                    String mac = device.getMac();
                    macInfo.setmMac(device.getMac());
                    System.out.print("Updata Success Device:" + mac);
                    successList.add(macInfo);
                }
                List<MacInfo> failList = getFailerDevice(macList, successList);
                System.out.println("-------->  Update Fault Device Size:" + failList.size());
                String failMac = mMeshId + ":";
                System.out.println("-------->  The Size Of Success:" + successList.size());
                for (int i = 0; i < failList.size(); i++) {
                    MacInfo macInfo = new MacInfo();
                    macInfo = failList.get(i);
                    failMac = failMac + "-" + macInfo.getmMac();
                    System.out.println("The Device Of Update Fault Mac:" + macInfo.getmMac());
                }
                currentNum++;

                int ceil = (int) Math.ceil(data.length / rowNum);
                if (currentNum == ceil) {
                    singleThreadExecutor.execute(new DataRunnable("http://" + sendIp + ":3000/api/ota/alter/state", failMac));
                    EspHttpParams params = new EspHttpParams();
                    params.setTryCount(3);
                    DeviceUtil.delayRequestRetry(completeDevices, "ota_reboot", params);
                    client.close();
                    endTime = System.currentTimeMillis();
                    long time = endTime - startTime;
                    System.out.println("------------------------>The Update Time Of Use:" + time / 1000 + "S!");
                    System.out.println("The Process Is Already Stop!...");
                    if (currentNum < ceil) {

                    }
                } else {

                }

            }
        });
        otaClient.start();

        System.out.println("==========> update " + meshId);
    }

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
