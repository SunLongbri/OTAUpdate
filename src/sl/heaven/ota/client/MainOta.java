package sl.heaven.ota.client;

import iot.espressif.esp32.model.device.EspDeviceFactory;
import iot.espressif.esp32.model.device.IEspDevice;
import iot.espressif.esp32.model.device.other.EspOTAClient;
import iot.espressif.esp32.model.net.MeshNode;
import iot.espressif.esp32.utils.DeviceUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import libs.espressif.net.EspHttpParams;

public class MainOta {
	/**
	 * 
	 * @param args
	 *            需要从shell脚本中传入的参数
	 */
	public static int closeNum = 0;
	public static int isStopNum = 0;
	static long startTime = 0;
	static long endTime = 0;
	
	public static void main(String[] args) {

		// d8a01d648348
		// d8a01d648450
		// d8a01d648614
		// d8a01d648ba4
		// "d8a01d648450,d8a01d612a50","C:/Users/SL/Desktop/Honeywell_Sensor_v0.10.8.3.bin","999999999999","d8a01d648ba4,d8a01d61294c" 

		startTime = System.currentTimeMillis();
		
		//四个设备同时升
		 String[] test = {"192.168.10.105","192.168.10.110","C:/Users/SL/Desktop/Honeywell_Sensor_v0.10.8.4.bin","888888888888",
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
	public static HttpRequestUtil requestUtils=new HttpRequestUtil();
	public static void updateData(final String sendIp, String ip, String path,
			final String meshId, final String[] data) {

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
						requestUtils.initConn("http://" + sendIp+ ":3000/api/ota/alter/progress");
						// OTA 升级开始前回调
						System.out.println("升级前的准备client=" + client);
					}

					@Override
					public void onOTAProgressUpdate(EspOTAClient client,
							List<EspOTAClient.OTAProgress> progressList) {
						HttpRequestUtil httpRequestUtil = new HttpRequestUtil();
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
							System.out.println("00000000000000000 :"+ progress);
							if(progress!=5){
								requestUtils.postData(sendData);
							}
							
							System.out.println(mCurrentMac+"------------------>progressInfo"+ sendData);
						}

					}

					@Override
					public void onOTAResult(EspOTAClient client,List<IEspDevice> completeDevices) {
						System.out.println("=========> close" );
						endTime = System.currentTimeMillis();
						long time = endTime - startTime;
						System.out.println("------------------------>当前升级一共使用了"+time/1000+"秒!");
						requestUtils.close();
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
						requestUtils.initConn("http://" + sendIp
								+ ":3000/api/ota/alter/state");
						requestUtils.postData(failMac);
						requestUtils.close();
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
	}
	
	

	/**
	 * 
	 * @param allList
	 *            当前所有的设备集合
	 * @param successList
	 *            回调成功的设备集合
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

}
