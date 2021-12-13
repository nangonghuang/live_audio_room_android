package im.zego.liveaudioroom.util;

import org.apache.commons.lang.ArrayUtils;

import java.nio.charset.StandardCharsets;

/**
 * Special UTF-8 codec format is adopted
 */
public class ByteAndStringTransformer {
    private static ByteAndStringTransformer instance;

    private ByteAndStringTransformer() {
    }

    public static ByteAndStringTransformer getInstance() {
        if (instance == null) {
            instance = new ByteAndStringTransformer();
        }
        return instance;
    }

    public Byte[] transformToByteArray(String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        return ArrayUtils.toObject(bytes);
    }

    public String transformToString(Byte[] bytes) {
        byte[] byteArray = ArrayUtils.toPrimitive(bytes);
        String string = new String(byteArray, StandardCharsets.UTF_8);
        return string;
    }
}