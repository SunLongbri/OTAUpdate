package sl.heaven.ota.client;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class HttpRequestUtil {

	/**
	 * 
	 * ��ָ��URL����GET����������
	 * 
	 * ����������������Ӧ���� name1=value1&name2=value2 ����ʽ��
	 * 
	 * @param url
	 * 
	 *            ���������URL
	 * 
	 * @return URL ������Զ����Դ����Ӧ���
	 */

	public static String sendGet(String url) {
		StringBuilder result = new StringBuilder();
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			// �򿪺�URL֮�������
			URLConnection connection = realUrl.openConnection();
			// ����ͨ�õ���������
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.setRequestProperty("Accept-Charset", "UTF-8");
			connection.setRequestProperty("contentType", "UTF-8");
			// ����ʵ�ʵ�����
			connection.connect();
			// �������е���Ӧͷ�ֶ�
			// for (String key : map.keySet()) {
			// System.out.println(key + "--->" + map.get(key));
			// }
			// ���� BufferedReader����������ȡURL����Ӧ
			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result.append(line);
			}

		} catch (Exception e) {
			// System.out.println("����GET��������쳣��" + e);
			result = new StringBuilder(
					"{\"resCode\":\"1\",\"errCode\":\"1001\",\"resData\":\"\"}");
			e.printStackTrace();
			System.out.println("Զ�̷���δ����!");
		}
		// ʹ��finally�����ر�������
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result.toString();
	}

	/*********************************************************/
	OutputStream out = null;
	BufferedReader in = null;

	public void initConn(String url) {
		HttpURLConnection conn = null;
		URL realUrl;
		try {
			realUrl = new URL(url);

			conn = (HttpURLConnection) realUrl.openConnection();

			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			conn.setRequestProperty("Accept-Charset", "UTF-8");
			conn.setRequestProperty("contentType", "UTF-8");
			conn.setRequestMethod("POST");

			conn.setDoOutput(true);
			conn.setDoInput(true);

			//out = new PrintWriter(conn.getOutputStream());
			out=conn.getOutputStream();
		
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void postData(String param) {

		System.out.println("param : "+ param);
		try {
			out.write(param.getBytes());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			close();
		}
		//out.flush();
	/*	String line;
		StringBuilder result = new StringBuilder();
		try {
			while ((line = in.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			close();

		}*/

	}

	public void close() {
		System.out.println("close : ======== ");
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

	/*********************************************************/
	
	
//	/*private static HttpURLConnection conn = null;
//
//	public String sendPost(String url, String param) {
//		System.out.println("���͵�����:" + param);
//		PrintWriter out = null;
//		BufferedReader in = null;
//		StringBuilder result = new StringBuilder();
//		try {
//			URL realUrl = new URL(url);
//			// �򿪺�URL֮�������
//			if (conn == null) {
//				conn = (HttpURLConnection) realUrl.openConnection();
//			}
//			// ����ͨ�õ���������
//			conn.setRequestProperty("accept", "*/*");
//			conn.setRequestProperty("connection", "Keep-Alive");
//			conn.setRequestProperty("user-agent",
//					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//			conn.setRequestProperty("Accept-Charset", "UTF-8");
//			conn.setRequestProperty("contentType", "UTF-8");
//			conn.setRequestMethod("POST");
//			// ����POST�������������������
//			conn.setDoOutput(true);
//			conn.setDoInput(true);
//
//			// URLConnection conn = realUrl.openConnection();
//
//			// ��ȡURLConnection�����Ӧ�������
//			out = new PrintWriter(conn.getOutputStream());
//			// �����������
//			out.print(param);
//			// flush������Ļ���
//			out.flush();
//			// ����BufferedReader����������ȡURL����Ӧ
//			in = new BufferedReader(
//					new InputStreamReader(conn.getInputStream()));
//			String line;
//			while ((line = in.readLine()) != null) {
//				result.append(line);
//			}
//		} catch (Exception e) {
//			System.out.println("���� POST ��������쳣��" + e);
//			result = new StringBuilder(
//					"{\"resCode\":\"1\",\"errCode\":\"1001\",\"resData\":\"\"}");
//			e.printStackTrace();
//			System.out.println("Զ�̷���δ����!");
//		}
//		// ʹ��finally�����ر��������������
//		finally {
//			try {
//				if (out != null) {
//					out.close();
//				}
//				if (in != null) {
//					in.close();
//				}
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}
//		}
//		return result.toString();
//	}
//	*/
}
