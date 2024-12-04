import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**the main entry point for running the Cache simulator
 * The CacheSimulator class serves as 
 * 
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

    /**
     * A 2d string array that will represent our cache
     */

    private int setSize;
    private int numSets;

    private Block[][] cacheArray;


    /**
     * Constructor for CacheSimulator objects.
     * 
     * @param fileName name of the input file
     */
    public CacheSimulator(String fileName){
        this.file = new File(fileName);
        this.output = new StringBuilder();
        this.hits = 0;
        this.misses = 0;
    }

    /**
     * Initializes the 2D array that stores the blocks of the cache.
     * 
     * The outer array represents the indexes of the cache and the 
     * inner array represents the blocks themselves.
     */
    public void initCacheArray() {
        this.cacheArray = new Block[numSets][setSize];
        for (int i = 0; i < numSets; i++) {
            
            for (int j = 0; j < setSize; j++) {
                cacheArray[i][j] = new Block();
            }
        }
    }

    /**
     * 
     * @param index index of the block being accessed
     * @param tag tag of the block being accessed
     * @return the number of memory references(used in output)
     */
    public int write(int index, int tag) {
        int blockIndex = hitCheck(index, tag);
        System.out.println(blockIndex);
        int memRefs = 0;
        if (blockIndex != -1) {
            Block block = cacheArray[index][blockIndex];
            this.hits++;
            block.isDirty = true;
            updateRecency(blockIndex, index);
        } else {
            this.misses++;
            blockIndex = findEmptyBlock(index);
            Block block = cacheArray[index][blockIndex];
            block.tag = tag;
            block.isEmpty = false;
            memRefs++;
            if (block.isDirty){
                memRefs++;
                updateRecency(blockIndex, index);
            }
        }
        
        return memRefs;
    }

    //work in progress
    public int read(int index, int tag) {
        int blockIndex = hitCheck(index, tag);
        int memRefs = 0;
        if (blockIndex != -1) {
            this.hits++;
            updateRecency(blockIndex, index);
        } else {
            this.misses++;
            blockIndex = findEmptyBlock(index);
            Block block = cacheArray[index][blockIndex];
            block.tag = tag;
            block.isEmpty = false;
            memRefs++;
            if (block.isDirty) {
                memRefs++;
            }
            block.isDirty = false;
            updateRecency(blockIndex, index);
        }
        
        return memRefs;
    }

    /**
     * Used to determine if a cache access was a hit or miss.
     * 
     * On a hit it returns the index of a block with a given cache index.
     * On a miss it returns -1.
     * 
     * @param index The index of the cache being read from or written to.
     * @param tag The tag of the block being searched for.
     * @return Returns the index of the block in the cache array
     */
    public int hitCheck(int index, int tag) {
        for (int i = 0; i < setSize; i++) {
            if (cacheArray[index][i].tag == tag)
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the index of the block that will be used.
     * 
     * If there are empty blocks, those are used first.
     * If no empty blocks, overwrites least recently used.
     * 
     * @param index index being searched
     * @return the index in cacheArray[index] that will be used
     */
    public int findEmptyBlock(int index) {
        int leastRecentIndex = 0;
        for (int i = 0; i < cacheArray[index].length; i++) {
            // returns the index of the first empty block
            if (cacheArray[index][i].isEmpty) {
                return i;
            }
            // sets leastRecentIndex to i if the current block being checked
            // was used less recently than the block at leastRecentIndex
            // a greater recency value represents less recent
            else if (cacheArray[index][leastRecentIndex].recency < cacheArray[index][i].recency) {
                leastRecentIndex = i;
            }
        }
        return leastRecentIndex;
    }

    /**
     * Updates the recency data on all of the blocks at a given index.
     * 
     * Higher recency values represent a less recently used block.
     * Resets the block being accessed to zero.
     * 
     * @param blockIndex the specific block in a given index that was just used
     * @param index the index of the most recently used block
     */
    public void updateRecency(int blockIndex, int index) {
        for (int i = 0; i < cacheArray[index].length; i++) {
            cacheArray[index][i].recency++;
        }
        cacheArray[index][blockIndex].recency = 0;
    }


    public void go() throws FileNotFoundException{
        this.instructionList = getInstructions();
        initOutput();
        this.output.append("\n");
        int numAccesses = 0;
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
                    int currentMiss = this.misses;
                    Object[] typeIndOffTagAddr = parseInstruction(this.instructionList.get(i), 
                        lineSize, numSets, setSize);
                    String type = (String) typeIndOffTagAddr[0];
                    int index = (int) typeIndOffTagAddr[1];
                    int offset = (int) typeIndOffTagAddr[2];
                    int tag = (int) typeIndOffTagAddr[3];
                    String addr = (String) typeIndOffTagAddr[4];
                    int memRefs = 0;
                    if (type.equals("read")){
                        memRefs = read(index, tag);
                    }
                    else{
                        memRefs = write(index, tag);
                    }
                    numAccesses++;
                    String hitMiss;
                    if (currentMiss < this.misses){
                        hitMiss = "miss";
                    }
                    else{
                        hitMiss = "hit";
                    }
                    String format = " %-8s %-10s %-4d %-6d %-6d %-6s %-3d%n";
                    String formattedLine = String.format(format,
                        type,       
                        addr,       
                        tag,
                        index,
                        offset,
                        hitMiss,
                        memRefs
                    );
                    this.output.append(formattedLine); // Append the formatted line
                }
                Float hitRatio = Float.valueOf(this.hits)/numAccesses;
                Float missRatio = Float.valueOf(this.misses)/numAccesses;
                this.output.append("""
                                   \n\n\nSimulation Summary Statistics
                                   -----------------------------
                                   Total hits       :""").append(" ").append(
                    this.hits).append("\nTotal misses     : ").append(this.misses).append(
                        "\nTotal accesses   : ").append(numAccesses).append(
                            "\nHit ratio        : ").append(hitRatio).append(
                            "\nMiss ratio       : ").append(missRatio);
                System.out.println(this.output);
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
        Object[] typeIndOffTagAddr = new Object[5];
        String type = instructPieces[0];
    
        // Parse the hex address from the instruction
        String hexAddress = instructPieces[2];
        int address = Integer.parseInt(hexAddress, 16);
        String binaryAddress = Integer.toBinaryString(address);
    
        // Calculate the total number of bits required for the binary address
        // number of bits for index
        int numIndexBits = (int) Math.ceil(Math.log(numSets) / Math.log(2));
         // number of bits for offset
        int numOffsetBits = (int) Math.ceil(Math.log(lineSize) / Math.log(2));
        int totalBits = numIndexBits + numOffsetBits; // Total bits = index + offset
    
        // Pad the binary address to match the required total size
        binaryAddress = String.format("%" + totalBits + "s", binaryAddress).replace(
            ' ', '0');
    
        // Calculate the start positions
        int offsetStart = binaryAddress.length() - numOffsetBits;
        int indexStart = offsetStart - numIndexBits;

        // Validate the binary address length
        if (indexStart < 0) {
            throw new IllegalArgumentException("""
                Binary address too short. Ensure it has sufficient bits for tag, index, and offset.
                """);
        }

        // Extract the tag, index, and offset
        String tagBits = binaryAddress.substring(0, indexStart);
        int tag = tagBits.isEmpty() ? 0 : Integer.parseInt(tagBits, 2);

        int index = Integer.parseInt(binaryAddress.substring(indexStart, offsetStart), 2);
        int offset = Integer.parseInt(binaryAddress.substring(offsetStart), 2);

        // Populate result array with the extracted values
        typeIndOffTagAddr[0] = type;
        typeIndOffTagAddr[1] = index;
        typeIndOffTagAddr[2] = offset;
        typeIndOffTagAddr[3] = tag;
        typeIndOffTagAddr[4] = hexAddress;
    
        return typeIndOffTagAddr;
    }
    
    
    
    
    

    
}


