package org.gobuki.utils;


import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * @author Steffen Kremsler
 * @since 2016
 *
 */
public class HexUtil {

    private static final int BYTES_PER_LINE = 16;
    private static final OutputFormat DEFAULT_OUTPUT_FORMAT = OutputFormat.LINUX_HEXDUMP;

    /**
     * SHORT: decimal position, capital hex letters, no space after the first 8 bytes, no | around the ascii representation
     * LINUX_HEXDUMP: hexadecimal position, lower case letters, space after first 8 bytes, | around ascii representation
     *                handy for use in diffs against hexdump -C
     *
     */
    public enum OutputFormat {
        LINUX_HEXDUMP, SHORT;
    }

    public static void printHex(byte[] data) {
        printHex(data, DEFAULT_OUTPUT_FORMAT, System.out);
    }

    public static void printHex(byte[] data, int indentSpaces) {
        printHex(data, indentSpaces, DEFAULT_OUTPUT_FORMAT, System.out);
    }


    public static void printHex(byte[] data, int indentSpaces, PrintStream out) {
        printHex(data, indentSpaces, DEFAULT_OUTPUT_FORMAT, out);
    }

    public static void printHex(byte[] data, PrintStream out) {
        printHex(data, DEFAULT_OUTPUT_FORMAT, out);
    }

    public static void printHex(byte[] data, OutputFormat outputFormat) {
        printHex(data, outputFormat, System.out);
    }

    public static void printHex(byte[] data, int indentSpaces, OutputFormat outputFormat) {
        printHex(data, indentSpaces, outputFormat, System.out);
    }
    public static void printHex(FileInputStream fileInputStream) {
        try {
            printHex(fileInputStream, 0, fileInputStream.getChannel().size(), OutputFormat.LINUX_HEXDUMP, System.out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void printHex(FileInputStream fileInputStream, OutputFormat outputFormat, PrintStream out) {
        try {
            printHex(fileInputStream, 0, fileInputStream.getChannel().size(), outputFormat, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void printHex(FileInputStream fileInputStream, int indentSpaces, OutputFormat outputFormat, PrintStream out) {
        try {
            printHex(fileInputStream, indentSpaces, fileInputStream.getChannel().size(), outputFormat, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void printHex(byte[] data, OutputFormat outputFormat, PrintStream out) {
        printHex(new ByteArrayInputStream(data), 0, data.length, outputFormat, out);
    }

    public static void printHex(byte[] data, int indentSpaces, OutputFormat outputFormat, PrintStream out) {
        printHex(new ByteArrayInputStream(data), indentSpaces, data.length, outputFormat, out);
    }

    public static void printHex(InputStream inputStream, int indentSpaces, long streamLength, OutputFormat outputFormat, PrintStream out) {
        String byteFormat = "%02x ";
        int lineBytesAccumulated = 0;
        int address = 0;
        int totalBytesProcessed = 0;
        int totalLines = 0;
        StringBuilder hexRepresentation = new StringBuilder();
        StringBuilder asciiRepresentation = new StringBuilder();

        String indent;
        if (indentSpaces > 0) {
            indent = String.format("%" + indentSpaces + "s", "");
        } else {
            indent = "";
        }

        byte[] readBuffer = new byte[32768];
        int bytesRead;

        try {
            while ((bytesRead = inputStream.read(readBuffer)) != -1) {
                for (byte b : readBuffer) {
                    // hex representation
                    hexRepresentation.append(String.format(byteFormat, b));
                    // output space after each 8 bytes
                    if (outputFormat == OutputFormat.LINUX_HEXDUMP && hexRepresentation.length() == 8 * 3) {
                        hexRepresentation.append(" ");
                    }
                    // ascii representation if printable
                    if (b > 31 && b < 127) {
                        asciiRepresentation.append((char) b);
                    } else {
                        asciiRepresentation.append(".");
                    }
                    lineBytesAccumulated++;
                    totalBytesProcessed++;

                    // print the line if we accumulated enough characters or we are at the last byte
                    // reset line context variables
                    if ((lineBytesAccumulated == BYTES_PER_LINE) || totalBytesProcessed == streamLength) {
                        if (outputFormat == OutputFormat.SHORT) {
                            printf(out,"%s%6s %-48s%s%n", indent, address, hexRepresentation, asciiRepresentation);
                        } else {
                            printf(out,"%s%08x  %-49s |%s|%n", indent, address, hexRepresentation, asciiRepresentation);
                        }
                        hexRepresentation.delete(0, hexRepresentation.length());
                        asciiRepresentation.delete(0, asciiRepresentation.length());

                        address += lineBytesAccumulated;
                        lineBytesAccumulated = 0;
                        totalLines++;
                    }
                    if (totalBytesProcessed == bytesRead) {
                        break;
                    }
                }
            }
            // required to dump last line when the stream length is unknown, when used with stdin
            if (hexRepresentation.length() > 0) {
                if (outputFormat == OutputFormat.SHORT) {
                    printf(out,"%s%6s %-48s%s%n", indent, address, hexRepresentation, asciiRepresentation);
                } else {
                    printf(out,"%s%08x  %-49s |%s|%n", indent, address, hexRepresentation, asciiRepresentation);
                }
                address += lineBytesAccumulated;
            }

            if (outputFormat != OutputFormat.LINUX_HEXDUMP) {
                printf(out, "dumped %s bytes in %s lines", totalBytesProcessed, totalLines);
            } else {
                printf(out, "%s%08x%n", indent, address);
            }
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }


    //
    //  Formatting methods
    //

    public static String formatMacAddress(byte[] data) {
        return String.format("%02X:%02X:%02X:%02X:%02X:%02X", data[0], data[1], data[2], data[3], data[4], data[5]);
    }

    /**
     * useful to format fingerprints
     *
     * @param data
     * @return
     */
    public static String formatAsByteTuples(byte[] data) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < data.length ; i++) {
            if (i == data.length - 1) {
                s.append(String.format("%02X", data[i]));
            } else {
                s.append(String.format("%02X:", data[i]));
            }
        }
        return s.toString();
    }


    // slf4j
    //Logger logger = LoggerFactory.getLogger(HexUtil.class);
    // juli (java.util.logger interface)
    //Logger logger = Logger.getLogger(HexUtil.class.getName());
    private static void printf(PrintStream out, String line, Object... formatArgs) {
        if (out != null) {
            out.printf(line, formatArgs);
        } else {
            // If you want to use a logger, just
            // use the methods without PrintStream and
            // replace the following System.out.printf statement
            // with a call to your preferred logger
            System.out.printf(line, formatArgs);

            // sl4j
            //logger.debug(String.format(line, formatArgs))
            // juli
            //logger.finest(String.format(line, formatArgs));
        }
    }


    public static void main(String[] args) throws IOException {

        // read data from stdin when piped
        if (System.in.available() > 0) {
            HexUtil.printHex(System.in, 0, Long.MAX_VALUE, OutputFormat.LINUX_HEXDUMP, System.out);
        } else if (args.length > 0 && args[0] != null) {
            try {
                HexUtil.printHex(Files.readAllBytes(Paths.get(args[0])));
            } catch (Exception e) {
                System.err.printf("Failed to read file: {}%n", args[0]);
            }
        } else {
            System.out.printf("usage: hexdump.jar <file>%n");
        }
    }
}