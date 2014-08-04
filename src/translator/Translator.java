package translator;

import java.io.File;
import java.io.PrintWriter;

public class Translator {
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {// Exciting code throws Exception
		File file = null;
		if (args.length != 1) {
			System.out.println("Usage: Translator <directory>|<file.vm>");
			System.exit(0);
		}
		if (args[0].matches(".*\\.vm")) {
			file = new File(args[0]);
			Parser parser = new Parser(file);
			CodeWriter writer = new CodeWriter(new PrintWriter(new File(
					args[0].substring(0, args[0].length() - 2) + "asm")));
			writer.setFileName(args[0].substring(0,args[0].length()-3));
			while (parser.hasMoreCommands()) {
				parser.advance();
				switch (parser.commandType()) {
				case PUSH:
				case POP:
					writer.writePushPop(parser.commandType(), parser.arg1(),
							Integer.parseInt(parser.arg2()));
					break;
				case ARITHMETIC:
					writer.writeArithmetic(parser.getCommand());
					break;
				default:
					break;
				}

			}
			writer.close();
		} else {
			file = new File(args[0]);
			String[] files = file.list();
			CodeWriter writer = new CodeWriter(new PrintWriter(new File(file,
					args[0] + ".asm")));
			for (String filename : files) {
				if (filename.matches(".*\\.vm")) {
					File vmFile = new File(file, filename);
					Parser parser = new Parser(vmFile);
					writer.setFileName(filename.substring(0,filename.length()-3));
					while (parser.hasMoreCommands()) {
						parser.advance();
						switch (parser.commandType()) {
						case PUSH:
						case POP:
							writer.writePushPop(parser.commandType(),
									parser.arg1(),
									Integer.parseInt(parser.arg2()));
							break;
						case ARITHMETIC:
							writer.writeArithmetic(parser.getCommand());
							break;
						case LABEL:
							writer.writeLabel(parser.arg1());
							break;
						case GOTO:
							writer.writeGoto(parser.arg1());
							break;
						case IF:
							writer.writeIf(parser.arg1());
							break;
						default:
							break;
						}
					}
				}
			}//End of for loop
			writer.close();
		}
	}
}
