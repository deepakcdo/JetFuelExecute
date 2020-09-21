package regressiontest.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class WebQuery {

    public String doWebQuery(String urlToGet) throws Exception{
        URL url = new URL(urlToGet);

        URLConnection con = url.openConnection();
        InputStream is =con.getInputStream();

        StringBuilder textBuilder = new StringBuilder();


        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;

        // read each line and write to System.out
        while ((line = br.readLine()) != null) {
            textBuilder.append(line);
        }
        return textBuilder.toString();
    }
}
