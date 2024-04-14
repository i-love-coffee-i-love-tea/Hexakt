package org.gobuki.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class HexUtilTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    void printHex_formats_byte_array_exactly_like_util_linux_hexdump() throws IOException {

        byte[] data = Files.readAllBytes(Path.of("src/test/resources/util-linux-hexdumps/bar.txt"));
        String linuxUtilsHexdump = new String(Files.readAllBytes(Path.of("src/test/resources/util-linux-hexdumps/bar.txt.hexdump")));

        HexUtil.printHex(data);

        Assertions.assertEquals(linuxUtilsHexdump, outputStreamCaptor.toString().trim());
    }

    @Test
    void printHex_applies_indentSpaces_correctly() throws IOException {
        byte[] data = Files.readAllBytes(Path.of("src/test/resources/util-linux-hexdumps/fat.txt"));
        String linuxUtilsHexdump = new String(Files.readAllBytes(Path.of("src/test/resources/util-linux-hexdumps/fat.txt.hexdump")));

        HexUtil.printHex(data, 4);

        // For some reason outputStreamCaptor cuts off leading spaces. Only affects first line and only happens in test.
        String spacesCutOffByOutputStreamCaptor = "    ";

        Assertions.assertEquals(linuxUtilsHexdump, spacesCutOffByOutputStreamCaptor + outputStreamCaptor.toString()
                .trim());
    }

    @Test
    void printHex_applies_indentSpaces_correctly_when_used_with_output_PrintStream() throws IOException {
        byte[] data = Files.readAllBytes(Path.of("src/test/resources/util-linux-hexdumps/fat.txt"));
        String linuxUtilsHexdump = new String(Files.readAllBytes(Path.of("src/test/resources/util-linux-hexdumps/fat.txt.hexdump")));

        HexUtil.printHex(data, 4, System.out);

        // For some reason outputStreamCaptor cuts off leading spaces. Only affects first line and only happens in test.
        String spacesCutOffByOutputStreamCaptor = "    ";

        Assertions.assertEquals(linuxUtilsHexdump, spacesCutOffByOutputStreamCaptor + outputStreamCaptor.toString()
                .trim());
    }



    @Test
    void printHex_with_custom_printstream() throws IOException {
        byte[] data = Files.readAllBytes(Path.of("src/test/resources/util-linux-hexdumps/bar.txt"));
        String linuxUtilsHexdump = new String(Files.readAllBytes(Path.of("src/test/resources/util-linux-hexdumps/bar.txt.hexdump")));

        HexUtil.printHex(data, System.out);

        Assertions.assertEquals(linuxUtilsHexdump, outputStreamCaptor.toString().trim());
    }

    @Test
    void printHex_with_FileInputStream() throws IOException {
        String linuxUtilsHexdump = new String(Files.readAllBytes(Path.of("src/test/resources/util-linux-hexdumps/java.txt.hexdump")));

        HexUtil.printHex(new FileInputStream("src/test/resources/util-linux-hexdumps/java.txt"));

        Assertions.assertEquals(linuxUtilsHexdump, outputStreamCaptor.toString().trim());
    }

    @Test
    void printHex_with_ByteArrayInputStream_should_print_to_stdout() throws IOException {
        String linuxUtilsHexdump = new String(Files.readAllBytes(Path.of("src/test/resources/util-linux-hexdumps/regular.txt.hexdump")));
        byte[] data = Files.readAllBytes(Path.of("src/test/resources/util-linux-hexdumps/regular.txt"));
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

        HexUtil.printHex(inputStream, 0, data.length, HexUtil.OutputFormat.LINUX_HEXDUMP, System.out);

        Assertions.assertEquals(linuxUtilsHexdump, outputStreamCaptor.toString().trim());
    }

    @Test
    void formatMacAddress() {
        byte[] macAddress = "0123456789ab".getBytes();
        String formatted = HexUtil.formatMacAddress(macAddress);
        String expected = "30:31:32:33:34:35";
        Assertions.assertEquals(expected, formatted);
    }

    @Test
    void formatAs2ByteTuples() {
        byte[] data = "could be a certificate or ssh key fingerprint".getBytes();
        String formatted = HexUtil.formatAs2ByteTuples(data);
        String expected = "63:6F:75:6C:64:20:62:65:20:61:20:63:65:72:74:69:66:69:63:61:74:65:20:6F:72:20:73:73:68:20:6B:65:79:20:66:69:6E:67:65:72:70:72:69:6E:74";
        Assertions.assertEquals(expected, formatted);
    }

    @Test
    void main_called_with_stdin() throws IOException {
        String linuxUtilsHexdump = new String(Files.readAllBytes(Path.of("src/test/resources/util-linux-hexdumps/java.txt.hexdump")));

        provideInput(Files.readAllBytes(Path.of("src/test/resources/util-linux-hexdumps/java.txt")));
        HexUtil.main(new String[]{});

        Assertions.assertEquals(linuxUtilsHexdump, outputStreamCaptor.toString().trim());
    }

    @Test
    void main_called_with_existing_filename_as_argument_OK() throws IOException {
        String linuxUtilsHexdump = new String(Files.readAllBytes(Path.of("src/test/resources/util-linux-hexdumps/java.txt.hexdump")));

        HexUtil.main(new String[]{"src/test/resources/util-linux-hexdumps/java.txt"});

        Assertions.assertEquals(linuxUtilsHexdump, outputStreamCaptor.toString().trim());
    }

    void provideInput(byte[] data) {
        ByteArrayInputStream testIn = new ByteArrayInputStream(data);
        System.setIn(testIn);
    }

}