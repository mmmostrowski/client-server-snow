package techbit.snow.proxy.lang;

public final class EmptyArray {
    public final static EmptyArray NO = new EmptyArray();

    public final float[] FLOATS = new float[0];
    public final byte[] BYTES = new byte[0];
    public final int[] INTEGERS = new int[0];

    public final static class TwoDimensional {
        public final static TwoDimensional NO = new TwoDimensional();

        public final byte[][] BYTES = new byte[0][0];

        private TwoDimensional() {
        }
    }

    private EmptyArray() {
    }
}
