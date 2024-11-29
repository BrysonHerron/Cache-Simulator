public class Block {
    boolean isDirty;
    boolean isEmpty;
    int tag;
    int recent;

    public Block() {
        isDirty = false;
        isEmpty = true;
    }
}
