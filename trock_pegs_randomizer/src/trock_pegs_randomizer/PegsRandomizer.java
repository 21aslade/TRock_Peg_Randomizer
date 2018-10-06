package trock_pegs_randomizer;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

public class PegsRandomizer {
	public static final int version = 1;
	private String outputDirectory;
	private String inputLocation;
	private File inputFile;
	private RandomAccessFile inputAccessFile;
	private RandomAccessFile outputRom;
	private long seed;
	private Boolean outputSpoiler = false;
	private Random random;
	
	public PegsRandomizer(String sourceLocation, String outputDirectory, long seed, Boolean outputSpoiler) {
		this.inputLocation = sourceLocation;
		this.inputFile = new File(sourceLocation);
		this.outputDirectory = outputDirectory;
		this.seed = seed;
		this.outputSpoiler = outputSpoiler;
		
		this.random = new Random(seed);
		
		if (!this.inputFile.exists()) {
			System.out.println("Could not find file: " +  sourceLocation);
			System.exit(1);
		}
		
		try {
			this.inputAccessFile = new RandomAccessFile(inputFile, "r");
			Files.copy(Path.of(this.inputLocation), Path.of(this.outputDirectory + "\\TR_Pegs_Randomizer v" + PegsRandomizer.version + " - " + this.seed + " " + this.inputFile.getName()));
			this.outputRom = new RandomAccessFile(this.outputDirectory + "\\TR_Pegs_Randomizer v" + PegsRandomizer.version + " - " + this.seed + " " + this.inputFile.getName(), "rw");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void close() {
		try {
			inputAccessFile.close();
			outputRom.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void randomizePegs() {
		byte[][] pegValues = {{(byte) 0x26, (byte) 0x08}, {(byte) 0xA0, (byte) 0x05}, {(byte) 0x1A, (byte) 0x08}};
		
		ArrayUtils.shuffle(pegValues, random);
		
		writeOffset(pegValues[0], 0x267A1);
		writeOffset(pegValues[1], 0x267A3);
		writeOffset(pegValues[2], 0x267A5);
		
		if (outputSpoiler) {
			System.out.println(pegValues[0][0]);
			System.out.println(pegValues[1][0]);
			System.out.println(pegValues[2][0]);
		}
	}
	
	private void writeOffset(byte[] values, int offset) {
		try {
			outputRom.seek(offset);
			outputRom.write(values);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Args: input_file output_directory");
			return;
		}
		
		String sourceFileLocation = "";
		String outputFileDirectory = "";
		Boolean spoiler = false;
		Boolean customSeed = false;
		long seed = 0;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i] == "--spoiler") {
				spoiler = true;
			} else if (args[i] == "--seed") {
				customSeed = true;
				seed = Long.parseLong(args[++i]);
			} else {
				if (sourceFileLocation.equals("")) {
					sourceFileLocation = args[i];
				} else {
					outputFileDirectory = args[i];
				}
			}
		}
		
		if (!customSeed) {
			seed = new Random().nextLong();
		}
		
		PegsRandomizer randomizer = new PegsRandomizer(sourceFileLocation, outputFileDirectory, seed, spoiler);
		randomizer.randomizePegs();
		randomizer.close();
    }
}
