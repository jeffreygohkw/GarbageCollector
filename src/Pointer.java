public class Pointer {
    int pointer;

    public Pointer(int i) {
        this.pointer = i;
    }

    public boolean equalPointer(Pointer obj) {
        return obj.pointer == this.pointer;
    }
}
