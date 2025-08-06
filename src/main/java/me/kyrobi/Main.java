package me.kyrobi;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static me.kyrobi.BufferReaderUtils.*;

public class Main {

    private static final Set<String> beatmapMd5s = new HashSet<>();

    public static void extractBeatmapMd5s(String filename) throws IOException{
        try (FileInputStream fis = new FileInputStream(filename);
             DataInputStream dis = new DataInputStream(fis)) {

            /*
            IMPORTANT

            Even though we ONLY care about the beatmap's md5, we still need to read all the
            other values as well since the database is a binary file. Because of that, we can't
            just read values off of it like keys. We have to properly ready through the entire file
            making sure that we're reading the exact amount each time, or else it will mess up.
             */

            int version = readInt(dis);
            long numBeatmaps = readUInt(dis);

            for (long i = 0; i < numBeatmaps; i++) {
                String beatmapMd5 = readString(dis);
                long numScores = readUInt(dis);

                for (long j = 0; j < numScores; j++) {
                    int gameMode = readUByte(dis);
                    int gameVersion = readInt(dis);
                    String beatmapMd5Again = readString(dis);
                    String playerName = readString(dis);
                    String replayMd5 = readString(dis);
                    int count300 = readUShort(dis);
                    int count100 = readUShort(dis);
                    int count50 = readUShort(dis);
                    int countGeki = readUShort(dis);
                    int countKatu = readUShort(dis);
                    int countMiss = readUShort(dis);
                    int totalScore = readInt(dis);
                    int maxCombo = readUShort(dis);
                    boolean perfectCombo = readBool(dis);
                    int mods = readInt(dis);
                    String lifeBarGraph = readString(dis);
                    LocalDateTime date = readDateTime(dis);
                    int replayLength = readInt(dis); // can be ignored
                    long onlineScoreId = readLong(dis);

                    beatmapMd5s.add(beatmapMd5);
                }
            }
        }
    }

    public static void deleteMaps(String songsFolderPath){
        File songsFolder = new File(songsFolderPath);
        int counter = 0;

        int foldersDeleted = 0;

        // Check if the Songs folder exists
        if(!songsFolder.exists()){
            // System.out.println("Songs folder not found: " + songsFolderPath);
            return;
        }

        // Iterate through each song folder
        File[] songFolders = songsFolder.listFiles();
        if(songFolders == null) return;

        for(File songFolderPath : songFolders){
            // Skip if it's not a directory
            if(!songFolderPath.isDirectory()){
                continue;
            }

            // Find all .osu files in the song folder
            File[] allFiles = songFolderPath.listFiles();
            if (allFiles == null){
                continue;
            }

            // Grab only the items that ends with .osu from all the files
            File[] osuFiles = java.util.Arrays.stream(allFiles)
                    .filter(file -> file.getName().endsWith(".osu"))
                    .toArray(File[]::new);

            // If no .osu files, delete the folder
            if(osuFiles.length == 0){
                System.out.println("No .osu files found in " + songFolderPath.getName() + ", deleting folder...");
                deleteDirectory(songFolderPath);
                foldersDeleted++;
                continue;
            }

            // Check MD5 hashes of all .osu files
            boolean hasMatchingHash = false;

            for(File osuFile : osuFiles){
                try{
                    // Calculate MD5 hash of the .osu file
                    byte[] fileContent = Files.readAllBytes(osuFile.toPath());
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] hashBytes = md.digest(fileContent);

                    // Since the md5 function returns raw bytes like
                    // 0x5f, 0x1C, we need to strip the first two characters
                    // so they're just 5f and 1C
                    StringBuilder sb = new StringBuilder();
                    for(byte b : hashBytes){
                        sb.append(String.format("%02x", b));
                    }
                    String md5Hash = sb.toString();

                    // Check if this hash matches any in beatmapMd5s
                    if(beatmapMd5s.contains(md5Hash)){
                        hasMatchingHash = true;
                        break;
                    }
                } catch(Exception e){
                    System.out.println("Error reading " + osuFile.getPath() + ": " + e.getMessage());
                    continue;
                }
            }

            // If no matching hash found, delete the folder
            if(!hasMatchingHash){
                System.out.println("No matching hashes found in " + songFolderPath.getName() + ", deleting folder...");
                deleteDirectory(songFolderPath);
                foldersDeleted++;
            } else {
                counter++;
                // System.out.println("Keeping folder " + songFolderPath.getName() + " (contains matching hash)");
            }
        }

        System.out.println("Kept " + counter + " folder(s).");
        System.out.println("Deleted " + foldersDeleted + " folder(s).");
    }

    // In Java, for some reason, you can't delete the entire folder
    // at once. You need to make sure the folder is empty before it
    // can be deleted or else it will error.
    private static void deleteDirectory(File directory){
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(files != null){
                for(File file : files){
                    if(file.isDirectory()){
                        deleteDirectory(file);
                    }
                    else{
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
        else{
            System.out.println("ERROR: " + directory.getName() + " is invalid!");
            System.exit(0);
        }
    }

    public static void main(String[] args){

        if(args.length != 1){
            System.out.println("Usage: java OsuMapManager <osu_folder>");
            return;
        }

        String masterPath = args[0];
        String scoresDbPath = masterPath + File.separator + "scores.db";
        String songsFolderPath = masterPath + File.separator + "Songs";

        try{
            extractBeatmapMd5s(scoresDbPath);
            deleteMaps(songsFolderPath);
        } catch(Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}