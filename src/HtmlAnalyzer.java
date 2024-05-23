import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Stack;

public class HtmlAnalizer {

    public static void main(String[] args) {
        try {
            String uri = null;
            String finalContent = null;
            if (args.length > 0) {
                uri = args[0];
            }
            if (uri.startsWith("http://") || uri.startsWith("https://")) {
                finalContent = parseHtml(uri);
                System.out.println(findContentOfDeepestTag(finalContent).trim());
            } else {
                uri = "https://" + args[0];
                if (isValidURI(uri)) {
                    finalContent = parseHtml(uri);
                    System.out.println(findContentOfDeepestTag(finalContent).trim());
                } else {
                    uri = "http://" + args[0];
                    if (isValidURI(uri)) {
                        finalContent = parseHtml(uri);
                        System.out.println(findContentOfDeepestTag(finalContent).trim());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("URL connection error");
        }

    }

    public static String parseHtml(String uri) throws IOException {

        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder content = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }
        reader.close();
        connection.disconnect();
        return content.toString();
    }

    public static String findContentOfDeepestTag(String content) {
        int largestDepth = -1;
        int currentDepth = -1;
        String deepestContent = null;

        Stack<String> tagsStack = new Stack<>();

        int position = 0;
        while (position < content.length()) {
            char c = content.charAt(position);
            if (c != '<') {
                position++;
                continue;
            }
            int closure = content.indexOf(">", position + 1);
            if (closure == -1) {
                break;
            }

            String tag = content.substring(position + 1, closure);

            if (tag.startsWith("!")) {
                position++;
                continue;
            }

            if (!tag.startsWith("/")) {
                currentDepth++;
                tagsStack.push(tag);
            } else {
                currentDepth--;
                var lastTag = tagsStack.pop();
                if (!lastTag.equals(tag.substring(1))) {
                    return "malformed HTML";
                }
            }

            if (currentDepth > largestDepth) {
                largestDepth = currentDepth;
                int tagStartIndex = closure + 1;
                int tagEndIndex = content.indexOf("</" + tag + ">", tagStartIndex);
                if (tagEndIndex != -1) {
                    deepestContent = content.substring(tagStartIndex, tagEndIndex);
                }
            }
            position = closure + 1;
        }
        return deepestContent;
    }

    public static boolean isValidURI(String url) {
        try {
            new java.net.URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}

