package at.reisisoft.hardcodedBackup;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        List<IOException> exceptions = new LinkedList<>();
        int roundRobinSize = Integer.parseInt(args[0]);
        String[] folders = new String[args.length - 2];
        for (int i = 2; i < args.length; i++)
            folders[i - 2] = args[i];
        String backup = args[1];
        File config = new File(args[1], "round.robin");
        int currentRobin = 0;
        if (config.exists())
            try {
                currentRobin = new Scanner(config).nextInt();
            } catch (FileNotFoundException e) {
                exceptions.add(e);
                System.err.println("Config file not readqable, oerwriting 0");
            }
        File target = new File(backup, Integer.toString(currentRobin));
        System.out.format("Read config file!%nRoundRobin size:\t%s%nBackup location:\t%s%nConfig file location:\t%s%n", roundRobinSize, target, config);
        File[] source = new File[folders.length];
        for (int i = 0; i < source.length; i++)
            source[i] = new File(folders[i]);
        System.out.println("Starting backup");
        int allowedExceptionsForSuccess = exceptions.size();
        System.out.println("Checking if target is empty!");
        if (target.exists()) {
            try {
                System.out.format("Deleting '%s', please be patient!", target);
                FileUtils.deleteDirectory(target);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < source.length; i++) {
            System.out.format("%s of %s Backuping folder %s. This can take a lot of time!%n", i + 1, source.length, folders[i]);
            try {
                FileUtils.copyDirectory(source[i], new File(target, folders[i].replace(":", "")), true);
            } catch (IOException e) {
                exceptions.add(e);
                System.err.println("Error copying " + folders[i]);
            }

        }
        if (allowedExceptionsForSuccess == exceptions.size()) {
            try {
                if (config.exists() || config.createNewFile()) {
                    PrintStream ps = new PrintStream(config);
                    ps.println((currentRobin + 1) % roundRobinSize);
                    ps.close();
                }
            } catch (IOException e) {
                exceptions.add(e);
                System.err.println("Switching to next folder for the next backup did not succeed!");
            }
        } else {
            System.err.println("The following exceptions occured:");
            exceptions.forEach(Exception::printStackTrace);
        }
        System.out.println("Backup findished in folder " + backup + "\nPress ENTER to exit");
        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException("Error while closing", e);
        }
    }
}
