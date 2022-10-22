/**
 * Java application which scrapes the webpage https://www.comparetv.com.au/streaming-search-library/?ctvcp=1770
 * and prints the total number of Netflix shows and movies, and the URL and name of each movie/show in Australia.
 *
 * Author: Ibrahim Anees
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {

        // Base API URL
        String url = "https://www.comparetv.com.au/wp-admin/admin-ajax.php";

        String response = getResponseString(url);

        int totalNumber = parseNumberOfItems(response);

        System.out.println("Total Netflix shows: " + totalNumber);
        System.out.println("CSV output:");

        int[] testingSizes = printData(response);

        // Checking all values of testing sizes are equal
        boolean allEqual = (Arrays.stream(testingSizes).distinct().count() == 1) && (testingSizes[0] == totalNumber);
        //System.out.println(allEqual);

    }

    /**
     * Method that sends a request to the base API URL with appropriate headers to return unparsed string of all movies/shows
     * @param urlString: String of base URL
     * @return Unparsed string of all movies/shows
     * @throws IOException
     */
    private static String getResponseString(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");

        httpConn.setRequestProperty("authority", "www.comparetv.com.au");
        httpConn.setRequestProperty("accept", "*/*");
        httpConn.setRequestProperty("accept-language", "en-US,en;q=0.9");
        httpConn.setRequestProperty("cache-control", "no-cache");
        httpConn.setRequestProperty("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpConn.setRequestProperty("cookie", "_gcl_au=1.1.1685724349.1666407857; _gid=GA1.3.309647089.1666407858; _ga_YHH25785JF=GS1.1.1666421940.3.1.1666425713.59.0.0; _uetsid=3798f57051b611edad4423cd274a1ff5; _uetvid=37992ef051b611ed85b7f98e47fdec91; _ga=GA1.3.1541971590.1666407858");
        httpConn.setRequestProperty("origin", "https://www.comparetv.com.au");
        httpConn.setRequestProperty("pragma", "no-cache");
        httpConn.setRequestProperty("referer", "https://www.comparetv.com.au/streaming-search-library/?ctvcp=1770");
        httpConn.setRequestProperty("sec-ch-ua", "\"Chromium\";v=\"106\", \"Google Chrome\";v=\"106\", \"Not;A=Brand\";v=\"99\"");
        httpConn.setRequestProperty("sec-ch-ua-mobile", "?1");
        httpConn.setRequestProperty("sec-ch-ua-platform", "\"Android\"");
        httpConn.setRequestProperty("sec-fetch-dest", "empty");
        httpConn.setRequestProperty("sec-fetch-mode", "cors");
        httpConn.setRequestProperty("sec-fetch-site", "same-origin");
        httpConn.setRequestProperty("user-agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Mobile Safari/537.36");
        httpConn.setRequestProperty("x-requested-with", "XMLHttpRequest");

        httpConn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
        writer.write("action=provider_content&provider=1770&genre=0&type=0&limit=1200&sort=latest&page=1&context=search");
        writer.flush();
        writer.close();
        httpConn.getOutputStream().close();

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";
        return response;
    }

    /**
     * Gets the number of shows/movies from the unparsed string
     * @param response Unparsed string of shows/movies
     * @return integer representing number of shows/movies
     */
    private static int parseNumberOfItems(String response) {
        int startingIndex = 0;
        int endingIndex = 0;
        boolean endingFound = false;
        for (int i = response.length() - 1; i >= 0; i--) {
            char c = response.charAt(i);
            if (endingFound == false) {
                if (Character.isDigit(c)) {
                    endingIndex = i;
                    endingFound = true;
                }
            } else {
                if (!Character.isDigit(c)) {
                    startingIndex = i;
                    break;
                }
            }
        }
        return Integer.valueOf(response.substring(startingIndex+1, endingIndex+1));
    }


    /**
     * Parses and prints the titles and URLs of all shows/movies in the required format
     * @param response Unparsed string of shows/movies
     * @return integer array of sizes of number of indexes for String manipulation. This is used for testing purposes.
     */
    private static int[] printData(String response) {

        String urlStart = "<div class=\\\"search-content-item\\\"><a href=\\\"";
        ArrayList<Integer> urlStartIndexes = returnIndexes(response, urlStart);

        String urlEnd = "\\\"><img src=\\\"";
        ArrayList<Integer> urlEndIndexes = returnIndexes(response, urlEnd);

        String nameStart = "\\\"\\/><p>";
        ArrayList<Integer> nameStartIndexes = returnIndexes(response, nameStart);

        String nameEnd = "<br><span>";
        ArrayList<Integer> nameEndIndexes = returnIndexes(response, nameEnd);

        for (int i = 0; i < urlStartIndexes.size(); i++) {
            System.out.println(
                    "\"" + response.substring(nameStartIndexes.get(i)+8, nameEndIndexes.get(i)) + "\"" + "," +
                            "\"" + response.substring(urlStartIndexes.get(i)+45, urlEndIndexes.get(i)).replace("\\/", "/") + "\"");
        }

        return new int[] {urlStartIndexes.size(), urlEndIndexes.size(), nameStartIndexes.size(), nameEndIndexes.size()};
    }

    /**
     * Helper method for printData method
     * @param response Unparsed string of shows/movies
     * @param substring
     * @return ArrayList of Integers representing the starting indexes of all occurences of the substring
     */
    private static ArrayList<Integer> returnIndexes(String response, String substring) {
        ArrayList<Integer> indexes = new ArrayList<>();
        int index = response.indexOf(substring);
        while (index >= 0) {
            indexes.add(index);
            index = response.indexOf(substring, index + 1);
        }
        return indexes;
    }
}