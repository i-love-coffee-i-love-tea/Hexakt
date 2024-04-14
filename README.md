# Hex dump utility written in java

Creates output exactly formatted as util-linux's `hexdump -C`

It has helped me during development of filesystem tools, a network packet logger and binary format
parsers.


## Usage 


### In java code

Copy the class, use one of the methods to dump the input data in hex. It has no dependencies.

Input can either come from

	- byte[]
	- FileInputStream
	- InputStream 
 
Output can go to

	- Custom PrintStream
	- System.out
	- A logger that you like. You just need to insert
		- the logger import(s)
		- a line for getting a logger
		- the logger usage in `HexUtil.printf`

### at the console (not its main purpose, but also possible)

With a file argument

	`java -jar hexakt.jar filename`


With piped stdin

	`cat file | java -jar hexakt.jar`




## Example output

	00000000  4a 61 76 61 3a 20 57 72  69 74 65 20 6f 6e 63 65  |Java: Write once|
	00000010  2c 20 72 75 6e 20 61 77  61 79 2e 0a 0a           |, run away...|
	0000001d

