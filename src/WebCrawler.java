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
    private final static boolean readUrlFromCmdLine = true;
    private final static int numberOfUrlsToTraverse = 100;
    private final static String urlPrefix = "http:";
    private final static String urlInputFilepath = "URLs_TO_CRAWL";
    private final static String crawledUrlsFilepath = "URLs_CRAWLED";
    private final static String errorLogFilepath = "ERROR_LOG_CRAWLER";
    private static ArrayList<String> errorLog = new ArrayList<>();
    private static ArrayList<String> successfullyCrawledUrls = new ArrayList<>();


    public static void main(String[] args) {
        ArrayList<String> urlsToCrawl = initialiseCrawler(readUrlFromCmdLine);

        for(String currentCrawlUrl : urlsToCrawl){
            crawler(currentCrawlUrl); // Traverse the Web from the a starting url
        }
        dumpArraylistsToFile();
        System.out.println("Crawling Complete.");
    }

    private static ArrayList<String> initialiseCrawler(boolean readUrlsFromCmdline){
        FileIO.fileCheck(urlInputFilepath);
        FileIO.fileCheck(crawledUrlsFilepath);
        FileIO.fileCheck(errorLogFilepath);
        ArrayList<String> urlsToCrawl = new ArrayList<>();
        if(readUrlsFromCmdline){
            Scanner input = new Scanner(System.in);
            System.out.print("Enter a URL: ");
            String url = input.nextLine();
            urlsToCrawl.add(url);
        }else {
            urlsToCrawl = FileIO.readFromUrlFile(urlInputFilepath);
        }
        return urlsToCrawl;
    }

    private static void dumpArraylistsToFile(){
        FileIO.writeUrlsToFile(crawledUrlsFilepath, successfullyCrawledUrls);
        FileIO.writeUrlsToFile(errorLogFilepath, errorLog);
    }

    public static void crawler(String startingURL) {
        System.out.println("Crawling...");
        ArrayList<String> listOfPendingURLs = new ArrayList<>();
        ArrayList<String> listOfTraversedURLs = new ArrayList<>();


        listOfPendingURLs.add(startingURL);
        while (!listOfPendingURLs.isEmpty() &&
                listOfTraversedURLs.size() <= numberOfUrlsToTraverse) {
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
                current = line.indexOf(urlPrefix, current);
                while (current > 0) {
                    int endIndex = line.indexOf("\"", current);
                    if (endIndex > 0) { // Ensure that a correct URL is found
                        list.add(line.substring(current, endIndex));
                        current = line.indexOf(urlPrefix, endIndex);
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