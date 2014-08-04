package translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;


public class Parser {
	private Scanner scanner;
	private String command;
	public String getCommand() {
		return command;
	}
	private String[] commandArgs;
	public Parser(File file) throws FileNotFoundException {
		this.scanner = new Scanner(file);
		commandArgs=null;
	}
	public boolean hasMoreCommands(){
		return scanner.hasNextLine();
	}
	public void advance(){
		command = scanner.nextLine();
		
		
		if (command.matches("(//.*|\\s*)")) {
			advance();
			return;
		}

		if (command.matches(".*//.*")) {
			command = command.substring(0, command.indexOf("//")).trim();
		}
		commandArgs = command.split(" ");
		System.out.println(Arrays.toString(commandArgs));
	}
	public Command commandType(){
		System.out.println(command);
		switch(commandArgs[0]){
		case "push":
			return Command.PUSH;
		case "pop":
			return Command.POP;
		case "add":
		case "sub":
		case "neg":
		case "eq":
		case "gt":
		case "lt":
		case "and":
		case "or":
		case "not":
			return Command.ARITHMETIC;
		case "label":
			return Command.LABEL;
		case "goto":
			return Command.GOTO;
		case "if-goto":
			return Command.IF;
		case "function":
			return Command.FUNCTION;
		case "call":
			return Command.CALL;
		case "return":
			return Command.RETURN;
		}
		
		return null;							//Alternatively throw an exception 
	}
	public String arg1(){
		return commandArgs[1];
	}
	public String arg2(){
		return commandArgs[2];
	}
}
