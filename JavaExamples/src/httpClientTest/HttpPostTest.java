package httpClientTest;

import java.net.URI;
import java.util.HashMap;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

public class HttpPostTest {
	// httpPost.setHeader("Accept", "application/json");
	// httpPost.setHeader("Content-type", "application/json");
	private HashMap<String, String> recordHeader = new HashMap<String, String>() {
		{
			put("Accept", "application/json");
			put("Accept", "application/json");
			put("If-Match", "*");
		}
	};

	public Integer makeHttpPost(String obj) {
		String uri = "";

		Integer i = httpRequestReq(obj, uri, recordHeader);
		return i;
	}

	private Integer httpRequestReq(String entity, String uri, HashMap<String, String> header) {
		URI test = URI.create(uri);
		HttpPost post = new HttpPost(test);

		StringEntity stringEntity = new StringEntity(entity,ContentType.APPLICATION_JSON);
		post.setEntity(stringEntity);
//		post.setHeader("Accept", "application/json");
//		post.setHeader("Content-type", "application/json");
		recordHeader.forEach(post :: setHeader);
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
		    HttpGet httpGet = new HttpGet(serviceUrl);
		    try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
		        HttpEntity entity = response.getEntity();
		        EntityUtils.consume(entity);
		    }
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
