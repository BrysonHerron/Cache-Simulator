import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The Cache class serves as the main entry point for running the Cache simulator
 * It initializes a Cache instance with a specified file path provided as a command-line 
 * argument and executes the simulator with the instructions from the file.
 * @author Bryson Herron
 * @author John Hollis
 * 
 * @version 1.0
 * 
 * CS260
 */

public class CacheSimulator {
    /**
     * File containing the cache instructions
     */
    private final File file;

    /**
     * String array contatining the instructions in order
     */
    private ArrayList<String> instructionList;

    /**
     * The output of our cache simulator
     */
    private final StringBuilder output;

    /**
     * The number of hits, misses, and accesses
     */
    private int hits;
    private int misses;
    private int accesses;

    /**
     * The ratios of total accesses to hits and misses.
     */
    private float hitRatio;
    private float missRatio;

    /**
     * A 2d string array that will represent our cache
     */
    private String[][] cacheArray;


    public CacheSimulator(String fileName){
        this.file = new File(fileName);
        this.output = new StringBuilder();
    }

    public void go() throws FileNotFoundException{
        this.instructionList = getInstructions();
        initOutput();
        int numSets = Integer.parseInt(this.instructionList.get(0).split(": ")[1]);
        int setSize = Integer.parseInt(this.instructionList.get(1).split(": ")[1]);
        //Check numSets
        if (numSets <= 8192 && setSize <= 8) {
            int lineSize = Integer.parseInt(this.instructionList.get(2).split(": ")[1]);
            //Check lineSize
            if (lineSize >= 4 && lineSize % 2 == 0){
                //continue code here
                this.cacheArray = new String[numSets][setSize];
                parseInstruction(this.instructionList.get(3), lineSize, numSets, setSize);
            }
            else{
                System.out.println("Illegal LineSize, Please correct the error and try again.");
                System.exit(0);
            }
        }
        else{
            System.out.println("Illegal numSets/setSize, please correct the error and try again.");
            System.exit(0);
        }
        
    }

    /**
     * The initOutput method initilizes the first few lines of output after reading
     * in the instructions.
     */
    public void initOutput(){
        this.output.append("Cache Configuration\n\n");
        String numSets = this.instructionList.get(0).split(": ")[1];
        String setSize = this.instructionList.get(1).split(": ")[1];
        this.output.append("\t").append(numSets).append(" ").append(setSize)
            .append("-way set associative entries\n");
        String lineSize = this.instructionList.get(2).split(": ")[1];
        this.output.append("\tof line size ").append(lineSize).append(" bytes\n\n\n");
        this.output.append("Results for each Reference\n\n");
        this.output.append("Access Address    Tag   Index Offset Result Memrefs\n");
        this.output.append("------ -------- ------- ----- ------ ------ -------");

    }

    /**
     * The getInstructions method reads in the file given in the command line, and returns an
     * ArrayList containing all of the instructions after parsing.
     * @return a string array contatining the parsed instructions
     * @throws FileNotFoundException
     */
    public ArrayList<String> getInstructions() throws FileNotFoundException{
        Scanner scanner1 = new Scanner(this.file);
        ArrayList<String> instructions = new ArrayList<>();
        while (scanner1.hasNextLine()) {
            String instruct = scanner1.nextLine();
            instructions.add(instruct);
        }
        scanner1.close();
        return instructions;
    }

    public void parseInstruction(String instruction, int lineSize, int numSets, int setSize){
        String[] instructPieces = instruction.split(":");
        String type = instructPieces[0];
        int size = Integer.parseInt(instructPieces[1]);
        String hexAddress = instructPieces[2];
        int address = Integer.parseInt(hexAddress, 16);
        String binaryAddress = String.format("%" + size + "s", Integer.toBinaryString(address)).replace(' ', '0');
        int numIndexBits = (int) Math.round(Math.log(lineSize/setSize) / Math.log(2) );
        int numOffsetBits = (int) Math.round(Math.log(numSets) / Math.log(2));

        //Error somewhere
        int index = Integer.parseInt(binaryAddress.substring(binaryAddress.length() - numIndexBits, binaryAddress.length()),2);
        int offset = Integer.parseInt(binaryAddress.substring(binaryAddress.length() - numIndexBits - numOffsetBits, binaryAddress.length() - numIndexBits),2);
        int tag = Integer.parseInt(binaryAddress.substring(0, binaryAddress.length() - numIndexBits - numOffsetBits),2);
        System.out.println("Type: "+type+"\nIndex: "+index+"\nOffset: "+offset+"\nTag: ");
    }

}


