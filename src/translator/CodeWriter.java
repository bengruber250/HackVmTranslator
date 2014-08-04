package translator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CodeWriter {
	private BufferedWriter out;
	private String name;
	private int comp; // Used for comparsion jumps

	public CodeWriter(PrintWriter out) {
		this.out = new BufferedWriter(out);
	}

	public void setFileName(String name) {
		this.name = name;
	}

	public void writeArithmetic(String command) throws IOException {
		switch (command) {
		/*
		 * [SP-2]=[SP-1]+[SP-2]
		 * @SP
		 * M=M-1
		 * AD=M
		 * A=A-1
		 * M=M+D
		 */
		case "add":
			out.write("@SP\nAM=M-1\nD=M\nA=A-1\nM=M+D\n");
			break;
		case "sub":
			out.write("@SP\nAM=M-1\nD=M\nA=A-1\nM=M-D\n");
			break;
		case "neg":
			out.write("@SP\nA=M-1\nM=-M\n");
			break;
		case "eq":
			out.write("@SP\nAM=M-1\nD=M\nA=A-1\nD=D-M\n@EQ\nD;JNE\n@SP\nA=M-1\nM=-1\n@END\n0;JMP\n(EQ)\n@SP\nA=M-1\nM=0\n(END)\n"
					.replace("EQ", name + "$$" + "comp" + comp++).replace(
							"END", "name" + "$$" + "END" + comp)); // sub and jeq bug --> Cannot use more than one eq
			break;
		case "gt":
			out.write("@SP\nAM=M-1\nD=M\nA=A-1\nD=D-M\n@EQ\nD;JGE\n@SP\nA=M-1\nM=-1\n@END\n0;JMP\n(EQ)\n@SP\nA=M-1\nM=0\n(END)\n"
					.replace("EQ", name + "$$" + "comp" + comp++).replace(
							"END", "name" + "$$" + "END" + comp));
			break;
		case "lt":
			out.write("@SP\nAM=M-1\nD=M\nA=A-1\nD=D-M\n@EQ\nD;JLE\n@SP\nA=M-1\nM=-1\n@END\n0;JMP\n(EQ)\n@SP\nA=M-1\nM=0\n(END)\n"
					.replace("EQ", name + "$$" + "comp" + comp++).replace(
							"END", "name" + "$$" + "END" + comp));
			break;
		case "and":
			out.write("@SP\nM=M-1\nA=M\nD=M\nA=A-1\nM=M&D\n");
			break;
		case "or":
			out.write("@SP\nM=M-1\nA=M\nD=M\nA=A-1\nM=M|D\n");
			break;
		case "not":
			out.write("@SP\nA=M-1\nM=!M\n");
			break;

		}
	}

	/*
	 * Stack starts at 256
	 * PUSH 
	 * 
	 * If static 
	 * @xxx.n D=M Push to stack
	 *
	 * @segment local 
	 * D=M Data = pointer to local
	 * @index 
	 * A=D+A Addr = index + pointer to local 
	 * D=M Data = value at local ---> Data now contains *(segment+index)
	 * @SP 
	 * M=D Push to stack 
	 * @SP
	 * M=M+1 Increment stack pointer
	 */
	public void writePushPop(Command command, String segment, int index)
			throws Exception {
		if (command == Command.PUSH) {

			switch (segment) {
			case "constant":
				out.write("@" + index + "\nD=A\n");
				break;
			case "temp":
				out.write("@" + (5 + index) + "\nD=M\n");
				break;
			case "pointer":
				out.write("@" + index + "\nD=A\n@THIS\nA=A+D\nD=M\n"); // Should not be called, but included still
				break;
			case "this":
				out.write("@" + index + "\nD=A\n@THIS\nA=M+D\nD=M\n");
				break;
			case "that":
				out.write("@" + index + "\nD=A\n@THAT\nA=M+D\nD=M\n");
				break;
			case "argument":
				if (segment.equals("argument"))
					out.write("@ARG\n");
			case "local":
				if (segment.equals("local"))
					out.write("@LCL\n");
				out.write("D=M\n@" + index + "\nA=D+A\nD=M\n");
				break;
			case "static":
				out.write("@" + name + "." + index + "\nD=M\n");
				break;
			default:
				throw new Exception("No implementation");
			}
			out.write("@SP\nA=M\nM=D\n@SP\nM=M+1\n"); // Push D to stack
			return;
		}
		/*
		 *Ignore Me
		 */
		if (command == Command.POP) {
			out.write("@SP\nAM=M-1\nD=M\n"); //
			switch (segment) {

			case "temp":
				// if (segment.equals("temp"))
				out.write("@" + (5 + index) + "\n");
				break;
			case "pointer":

				// if (segment.equals("pointer"))
				out.write("@THIS\n");
				while (index-- > 0) {
					out.write("A=A+1\n");
				}
				break;
			case "that":
				if (segment.equals("that"))
					out.write("@THAT\n");
			case "this":
				if (segment.equals("this"))
					out.write("@THIS\n");
			case "argument":
				if (segment.equals("argument"))
					out.write("@ARG\n");
			case "local":
				if (segment.equals("local"))
					out.write("@LCL\n");
				if (index == 0) {
					out.write("A=M\n");
				} else {
					if (index > 0) {
						out.write("A=M+1\n");
						index--;
					}
					while (index-- > 0) {
						out.write("A=A+1\n");
					}
				}
				break;
			case "static":
				out.write("@" + name + "." + index + "\n");
				break;
			default:
				throw new Exception("No pop implementation defined");
			}
			out.write("M=D\n");
		}
	}

	/**
	 * Flushes buffer and closes output stream
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		out.flush();
		out.close();
	}

	/**
	 * Bootstrap code<br>
	 * Initializes the stack pointer and calls Sys.init
	 * 
	 * @throws IOException
	 */
	public void writeInit() throws IOException {
		out.write("@256\nD=A\n@SP\nM=D\n"); //TODO Write the call to Sys.init
	}

	/**
	 * Creates a label that is file specific in the format [filename]$[label]
	 * 
	 * @param label
	 * @throws IOException
	 */
	public void writeLabel(String label) throws IOException {
		out.write("(" + name + "$" + label + ")\n");
	}

	/**
	 * Unconditionally jumps to label specified
	 * 
	 * @param label
	 * @throws IOException
	 */
	public void writeGoto(String label) throws IOException {
		out.write("@" + name + "$" + label + "\n0;JMP\n"); //
	}

	/**
	 * Jumps to specified label should the value on the top of the stack be nonzero and decrements the stack pointer
	 * 
	 * @param label
	 * @throws IOException
	 */
	public void writeIf(String label) throws IOException {
		out.write("@SP\nAM=M-1\nD=M\n@" + name + "$" + label + "\nD;JNE\n");
	}

}