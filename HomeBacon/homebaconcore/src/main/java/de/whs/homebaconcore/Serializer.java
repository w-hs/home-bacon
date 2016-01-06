package de.whs.homebaconcore;

/**
 * Created by Dennis on 15.12.2015.
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream b = null;
        ObjectOutputStream o = null;
                try {
                    b= new ByteArrayOutputStream();
                    o = new ObjectOutputStream(b);
                    o.writeObject(obj);
                    return b.toByteArray();
                }
                finally {
                    if ( o != null) o.close();
                    if (b != null) b.close();
                }
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = null;
        ObjectInputStream o = null;
        try {
            b = new ByteArrayInputStream(bytes);
            o = new ObjectInputStream(b);
            return o.readObject();
        }
        finally {
            if (o != null) o.close();
            if (b != null) b.close();
        }
    }
}
