package regressiontest.util;

import com.crankuptheamps.client.HAClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Created by Deepak on 09/05/2017.
 */
public class ResponseStatsWriter {


    private HAClient ampsClient;
    private ObjectMapper jsonMapper;
    private String topic;

    public ResponseStatsWriter(HAClient ampsClient, ObjectMapper jsonMapper, String topic) {
        this.ampsClient = ampsClient;
        this.jsonMapper = jsonMapper;
        this.topic = topic;
    }

    public void writeStats(String callID, long timeTaken) throws Exception {
        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("ID", LocalDateTime.now().toString());
        messageMap.put("FunctionCALLID", callID);
        messageMap.put("TimeTakenInMillis", timeTaken);
        messageMap.put("Type", "Stats");
        final String message = jsonMapper.writeValueAsString(messageMap);
        ampsClient.publish(topic, message);
    }
}
