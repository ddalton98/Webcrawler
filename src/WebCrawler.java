import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Scanner;
import java.util.ArrayList;

public class WebCrawler {
    /*
        The parameters for the webcrawler
     */
    private final static boolean READ_URL_FROM_CMD_LINE = true;
    private final static int NUMBER_OF_URLS_TO_TRAVERSE = 100;
    private final static String URL_PREFIX = "http:";
    private final static String URL_INPUT_FILEPATH = "URLs_TO_CRAWL";
    private final static String CRAWLED_URLS_FILEPATH = "URLs_CRAWLED";
    private final static String ERROR_LOG_FILEPATH = "ERROR_LOG_CRAWLER";
    private static ArrayList<String> errorLog = new ArrayList<>();
    private static ArrayList<String> successfullyCrawledUrls = new ArrayList<>();


    public static void main(String[] args) {
        ArrayList<String> urlsToCrawl = initialiseCrawler(READ_URL_FROM_CMD_LINE);

        for(String currentCrawlUrl : urlsToCrawl){
            crawler(currentCrawlUrl); // Traverse the Web from the a starting url
        }
        dumpArraylistsToFile();
        System.out.println("Crawling Complete.");
    }

    private static ArrayList<String> initialiseCrawler(boolean readUrlsFromCmdline){
        FileIO.fileCheck(URL_INPUT_FILEPATH);
        FileIO.fileCheck(CRAWLED_URLS_FILEPATH);
        FileIO.fileCheck(ERROR_LOG_FILEPATH);
        ArrayList<String> urlsToCrawl = new ArrayList<>();
        if(readUrlsFromCmdline){
            Scanner input = new Scanner(System.in);
            System.out.print("Enter a URL: ");
            String url = input.nextLine();
            urlsToCrawl.add(url);
        }else {
            urlsToCrawl = FileIO.readFromUrlFile(URL_INPUT_FILEPATH);
        }
        return urlsToCrawl;
    }

    private static void dumpArraylistsToFile(){
        FileIO.writeUrlsToFile(CRAWLED_URLS_FILEPATH, successfullyCrawledUrls);
        FileIO.writeUrlsToFile(ERROR_LOG_FILEPATH, errorLog);
    }

    public static void crawler(String startingURL) {
        System.out.println("Crawling...");
        ArrayList<String> listOfPendingURLs = new ArrayList<>();
        ArrayList<String> listOfTraversedURLs = new ArrayList<>();


        listOfPendingURLs.add(startingURL);
        while (!listOfPendingURLs.isEmpty() &&
                listOfTraversedURLs.size() <= NUMBER_OF_URLS_TO_TRAVERSE) {
            String urlString = listOfPendingURLs.remove(0);
            listOfTraversedURLs.add(urlString);
            successfullyCrawledUrls.add(urlString + "\r\n");

            for (String s: getSubURLs(urlString)) {
                if (!listOfTraversedURLs.contains(s) &&
                        !listOfPendingURLs.contains(s))
                    listOfPendingURLs.add(s);
            }
        }
    }

    public static ArrayList<String> getSubURLs(String urlString) {
        ArrayList<String> list = new ArrayList<>();

        try {
            java.net.URL url = new java.net.URL(urlString);
            Scanner input = new Scanner(url.openStream());
            int current = 0;
            while (input.hasNext()) {
                String line = input.nextLine();
                current = line.indexOf(URL_PREFIX, current);
                while (current > 0) {
                    int endIndex = line.indexOf("\"", current);
                    if (endIndex > 0) { // Ensure that a correct URL is found
                        list.add(line.substring(current, endIndex));
                        current = line.indexOf(URL_PREFIX, endIndex);
                    }
                    else
                        current = -1;
                }
            }
        }
        catch (Exception ex) {
            Instant errorTime = Instant.now();
            DateTimeFormatter formatter = DateTimeFormatter
                            .ofLocalizedTime( FormatStyle.LONG )
                            .withLocale( Locale.UK)
                            .withZone( ZoneId.systemDefault() );

            String outputTime = formatter.format(errorTime);
            System.out.println(outputTime + "; Error: " + ex.toString());
            errorLog.add(outputTime + ": " + ex.toString() + "\r\n");
        }
        return list;
    }
}