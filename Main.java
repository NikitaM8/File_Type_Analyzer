package analyzer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws IOException {
        //String algType = "--KMP";
        //String fileName = "/Users/ann/Downloads/dataset_91069.txt";
        //String filePath = "/Users/ann/Downloads/files_for_check";
        //String filePatterns = "/Users/ann/Downloads/patterns.db";

        //String algType = "--RabinKarp";
        String algType = "--KMP";
        String filePatterns = args[1];
        String filePath = args[0];

        //prepare files
        File dirFiles = new File(filePath);
        File[] files = dirFiles.listFiles();

        List<Callable<String>> callables = new ArrayList<>(); //prepare list for tasks
        ExecutorService executor = Executors.newFixedThreadPool(10); //prepare executor

        Context checker = null;

        //choose type of algorithm
        switch (algType) {
            case "--naive":
                checker = new Context(new NaiveAlgorithm());
                break;
            case "--KMP":
                checker = new Context(new kmpAlgorithm());
                break;
            case "--RabinKarp":
                checker = new Context(new RabinKarpAlgorithm());
                break;
            default:
                break;
        }

        if (checker == null) {
            throw new RuntimeException(
                    "Unknown strategy type passed. Please, write to the author of the problem.");
        }

        if (files != null) {
            for (File file : files) {
                //prepare the task for each file
                Context finalChecker = checker;

                Callable<String> c = () -> {
                    List<String> patterns = getPatterns(filePatterns); //get list of patterns from file

                    try {
                        boolean matches = false;
                        String[] parameters;
                        String fileTypePattern = null;
                        String resultingFileType;
                        String lastMatchedFileType = null;
                        String fileName = null;

                        //try to detect each pattern in file
                        for (String pattern : patterns) {
                            parameters = pattern.split(";"); //get parameters from current line with pattern
                            fileTypePattern = parameters[1].replace("\"", "");
                            resultingFileType = parameters[2].replace("\"", "");

                            if (finalChecker.checkFileType(filePath + "/" + file.getName(), fileTypePattern, resultingFileType)) {
                                fileName = file.getName();
                                lastMatchedFileType = resultingFileType;
                                matches = true;
                            }
                        }

                        if (matches) {
                            return fileName + ": " + lastMatchedFileType;
                        }

                        return file.getName() + ": " + "Unknown file type";
                    } catch (IOException e) {
                        return "no such file or directory";
                    }
                };

                //add the task to list
                callables.add(c);
            }
        }

        //wait and get the results
        try {
            List<Future<String>> future = executor.invokeAll(callables);

            for (Future<String> f : future) {
                try {
                    System.out.println(f.get());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdown();
    }

    /**
     *
     * @param filePatterns - path to file with patterns
     * @return Array list of patterns from file
     * @throws IOException in-out exception while read with buffer
     **/
    public static List<String> getPatterns(String filePatterns) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePatterns));
        List<String> patterns = new ArrayList<>();

        int firstRead;

        while ((firstRead = reader.read()) != -1) {
            String line;

            line = (char) firstRead + reader.readLine();

            patterns.add(line);
        }

        return patterns;
    }
}

class Context {
    private FindingStrategy strategy;

    public Context(FindingStrategy strategy) {
        this.strategy = strategy;
    }

    boolean checkFileType(String fileName, String fileTypePattern, String resultingFileType) throws IOException {
        return this.strategy.findSubString(fileName, fileTypePattern, resultingFileType);
    }
}

interface FindingStrategy {
    boolean findSubString(String fileName, String fileTypePattern, String resultingFileType) throws IOException;
}

class NaiveAlgorithm implements FindingStrategy {

    @Override
    public boolean findSubString(String fileName, String fileTypePattern, String resultingFileType) throws IOException {
        FileReader reader = new FileReader(fileName);

        int r = reader.read();

        while (r != -1) {
            char curChar = (char) r;
            boolean match = true;

            if (curChar == fileTypePattern.charAt(0)) {

                for (int i=1; i < fileTypePattern.length(); i++) {
                    r = reader.read();
                    curChar = (char) r;

                    if (curChar != fileTypePattern.charAt(i)) {
                        match = false;
                        break;
                    }
                }
            } else {
                r = reader.read();
                match = false;
            }

            if (match) {
                return true;
            }
        }
        reader.close();

        return false;
    }
}

class kmpAlgorithm implements FindingStrategy {

    @Override
    public boolean findSubString(String fileName, String fileTypePattern, String resultingFileType) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        String line;
        int firstRead;
        while ((firstRead = reader.read()) != -1) {
            line = (char) firstRead + reader.readLine();

            if(KMPSearch(line, fileTypePattern)) {
                return true;
            }
        }

        return false;
    }

    public static int[] prefixFunction(String str) {
        int[] prefixFunc = new int[str.length()];

        for (int i = 1; i < str.length(); i++) {
            int j = prefixFunc[i - 1];

            while (j > 0 && str.charAt(i) != str.charAt(j)) {
                j = prefixFunc[j - 1];
            }

            if (str.charAt(i) == str.charAt(j)) {
                j += 1;
            }

            prefixFunc[i] = j;
        }

        return prefixFunc;
    }

    public static boolean KMPSearch(String text, String pattern) {
        int[] prefixFunc = prefixFunction(pattern);
        ArrayList<Integer> occurrences = new ArrayList<>();
        int j = 0;

        if (pattern.length() > text.length()) {
            //occurrences.add(0);
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            while (j > 0 && text.charAt(i) != pattern.charAt(j)) {
                j = prefixFunc[j - 1];
            }

            if (text.charAt(i) == pattern.charAt(j)) {
                j += 1;
            }

            if (j == pattern.length()) {
                occurrences.add(i - j + 1);
                j = prefixFunc[j - 1];
            }
        }

        return occurrences.size() > 0;
    }
}

class RabinKarpAlgorithm implements FindingStrategy {

    @Override
    public boolean findSubString(String fileName, String fileTypePattern, String resultingFileType) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        String line;
        int firstRead;
        while ((firstRead = reader.read()) != -1) {
            line = (char) firstRead + reader.readLine();
            
            if(RabinKarp(line, fileTypePattern)) {
                return true;
            }
        }

        return false;
    }

    public static long charToLong(char ch) {
        return (long)(ch - 'A' + 1);
    }

    public static boolean RabinKarp(String text, String pattern) {

        if (pattern.length() > text.length()) {
            //occurrences.add(0);
            return false;
        }

        int a = 53;
        long m = 1_000_000_000 + 9;

        long patternHash = 0;
        long currSubstrHash = 0;
        long pow = 1;

        for (int i = 0; i < pattern.length(); i++) {
            patternHash += charToLong(pattern.charAt(i)) * pow;
            patternHash %= m;

            currSubstrHash += charToLong(text.charAt(text.length() - pattern.length() + i)) * pow;
            currSubstrHash %= m;

            if (i != pattern.length() - 1) {
                pow = pow * a % m;
            }
        }

        ArrayList<Integer> occurrences = new ArrayList<>();

        for (int i = text.length(); i >= pattern.length(); i--) {
            if (patternHash == currSubstrHash) {
                boolean patternIsFound = true;

                for (int j = 0; j < pattern.length(); j++) {
                    if (text.charAt(i - pattern.length() + j) != pattern.charAt(j)) {
                        patternIsFound = false;
                        break;
                    }
                }

                if (patternIsFound) {
                    occurrences.add(i - pattern.length());
                }
            }

            if (i > pattern.length()) {
                currSubstrHash = (currSubstrHash - charToLong(text.charAt(i - 1)) * pow % m + m) * a % m;
                currSubstrHash = (currSubstrHash + charToLong(text.charAt(i - pattern.length() - 1))) % m;
            }
        }

        Collections.reverse(occurrences);
        return occurrences.size() > 0;
    }
}