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

    private int setSize;
    private int numSets;

    private Block[][] cacheArray;


    public CacheSimulator(String fileName){
        this.file = new File(fileName);
        this.output = new StringBuilder();
    }

    public void initCacheArray() {
        Block[][] cacheArray = new Block[numSets][setSize];
        for (int i = 0; i < numSets; i++) {
            for (int j = 0; j < setSize; j++) {
                cacheArray[i][j] = new Block();
            }
        }
    }


    public void write(int index, int tag) {
        int blockIndex = hitCheck(index, tag);
        Block block;
        if (blockIndex != -1) {
            hits++;
            
        } else {
            misses++;
            blockIndex = 
        }
    }

    public void read(int index, int tag) {
        int blockIndex = hitCheck(index, tag);
        if (blockIndex != -1) {
            hits++;
        } else {
            misses++;

        }
    }

    
    public int hitCheck(int index, int tag) {
        for (int i = 0; i < setSize; i++) {
            if (cacheArray[index][i].tag == tag)
            {
                return i;
            }
        }
        return -1;
    }

    public int findEmptyBlock(int index, int tag) {
        
    }


    public void go() throws FileNotFoundException{
        this.instructionList = getInstructions();
        initOutput();
        numSets = Integer.parseInt(this.instructionList.get(0).split(": "
            )[1].trim());
        setSize = Integer.parseInt(this.instructionList.get(1).split(": "
            )[1].trim());
        //Check numSets
        if (numSets <= 8192 && setSize <= 8) {
            int lineSize = Integer.parseInt(this.instructionList.get(2).split(": "
                )[1].trim());
            //Check lineSize
            if (lineSize >= 4 && lineSize % 2 == 0){
                //continue code here
                initCacheArray();
                for (int i = 3; i < instructionList.size(); i++){
                    Object[] typeIndOffTag = parseInstruction(this.instructionList.get(i), 
                        lineSize, numSets, setSize);
                    String type = (String) typeIndOffTag[0];
                    int index = (int) typeIndOffTag[1];
                    int offset = (int) typeIndOffTag[2];
                    int tag = (int) typeIndOffTag[3];

                    if (type.equals("read")){
                        read(index, tag);
                    }
                    else{
                        write(index, tag);
                    }
                }
            }
            else{
                System.out.println("Illegal LineSize, Please correct the error and try again.");
                System.exit(0);
            }
        }
        else{
            System.out.println("Illegal numSets/setSize, please correct" + 
                "the error and try again.");
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

    public Object[] parseInstruction(String instruction, int lineSize, int numSets, int setSize) {
        String[] instructPieces = instruction.split(":");
        Object[] typeIndOffTag = new Object[4];
        String type = instructPieces[0];
    
        // Parse the hex address from the instruction
        String hexAddress = instructPieces[2];
        int address = Integer.parseInt(hexAddress, 16);
        String binaryAddress = Integer.toBinaryString(address);
    
        // Calculate the total number of bits required for the binary address
        int numIndexBits = (int) Math.ceil(Math.log(numSets) / Math.log(2));  // number of bits for index
        int numOffsetBits = (int) Math.ceil(Math.log(lineSize) / Math.log(2)); // number of bits for offset
        int totalBits = numIndexBits + numOffsetBits; // Total bits = index + offset
    
        // Pad the binary address to match the required total size
        binaryAddress = String.format("%" + totalBits + "s", binaryAddress).replace(' ', '0');
    
        // Calculate the start positions
        int offsetStart = binaryAddress.length() - numOffsetBits;
        int indexStart = offsetStart - numIndexBits;

        // Validate the binary address length
        if (indexStart < 0) {
            throw new IllegalArgumentException("Binary address too short. Ensure it has sufficient bits for tag, index, and offset.");
        }

        // Extract the tag, index, and offset
        String tagBits = binaryAddress.substring(0, indexStart);
        int tag = tagBits.isEmpty() ? 0 : Integer.parseInt(tagBits, 2);

        int index = Integer.parseInt(binaryAddress.substring(indexStart, offsetStart), 2);
        int offset = Integer.parseInt(binaryAddress.substring(offsetStart), 2);

        // Debug Output
        System.out.println("Tag: " + tag + ", Index: " + index + ", Offset: " + offset);

    
        // Populate result array with the extracted values
        typeIndOffTag[0] = type;
        typeIndOffTag[1] = index;
        typeIndOffTag[2] = offset;
        typeIndOffTag[3] = tag;
    
        return typeIndOffTag;
    }
    
    
    
    
    

    
}


