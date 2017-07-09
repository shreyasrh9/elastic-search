package com.shreyas;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/elastic")
public class WelcomeController {
	final static Logger logger = Logger.getLogger(WelcomeController.class);
	TransportClient client;

	public WelcomeController() throws UnknownHostException {
		Settings settings = Settings.builder().put("cluster.name", "elasticsearch_shreyasrh").build();
		client = new PreBuiltTransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

	}

	@GetMapping("/jsonIndex")
	public String addIndex() throws Exception {
		JSONParser parser = new JSONParser();
		IndexResponse indexResponse = null;
		try {

			Object obj = parser.parse(new FileReader("/Users/shreyasrh/Desktop/test.json"));
			for (int i = 0; i < ((JSONArray) obj).size(); i++) {
				JSONObject jsonObject = (JSONObject) ((JSONArray) obj).get(i);

				indexResponse = client.prepareIndex("bank", "accounts", Integer.toString(i + 1))
						.setSource(jsonObject.toJSONString()).get();
				System.out.println(jsonObject);

				System.out.println("Index name :" + indexResponse.getIndex());

				System.out.println("Type Name :" + indexResponse.getType());

				System.out.println("Document ID (generated or not) :" + indexResponse.getId());

				System.out.println("Version (if it's the first time you index this document, you will get: 1) :"
						+ indexResponse.getVersion());

				System.out.println("Status has stored current instance statement :" + indexResponse.status());

			}

		} catch (FileNotFoundException e) {
			logger.error("Error :"+e.getMessage());
		} catch (IOException e) {
			logger.error("Error :"+e.getMessage());
		} catch (Exception e) {
			logger.error("Error :"+e.getMessage());
		}
		return indexResponse.getResult().toString();
	}

	@GetMapping("/get/{id}")
	public Map<String, Object> getIndex(@PathVariable final String id) throws Exception {
		GetResponse getResponse = null;
		try {

			getResponse = client.prepareGet("bank", "accounts", id).get();
			
		}

		catch (Exception e) {
			logger.error("Error :"+e.getMessage());
		}
		return getResponse.getSource();
	}

	@GetMapping("/update/{id}")
	public String update(@PathVariable final String id) throws IOException {

		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.index("bank").type("accounts").id(id)
				.doc(jsonBuilder().startObject().field("balance", "10000").endObject());
		try {
			UpdateResponse updateResponse = client.update(updateRequest).get();
			System.out.println(updateResponse.status());
			return updateResponse.status().toString();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Error :"+e.getMessage());
		}
		return "Exception";
	}

	@GetMapping("/delete/{id}")
	public String delete(@PathVariable final String id) {

		DeleteResponse deleteResponse = client.prepareDelete("bank", "accounts", id).get();

		System.out.println(deleteResponse.getResult().toString());
		return deleteResponse.getResult().toString();
	}
}