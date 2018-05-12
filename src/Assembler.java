import java.io.*;
import java.util.ArrayList;

/**
 * Assembler : 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다. 프로그램의 수행 작업은 다음과
 * 같다. <br>
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. <br>
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. <br>
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) <br>
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) <br>
 * 
 * <br>
 * <br>
 * 작성중의 유의사항 : <br>
 * 1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은
 * 안된다.<br>
 * 2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 * 3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 * 4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br>
 * <br>
 * + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수
 * 있습니다.
 */
public class Assembler
{
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간 */
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간 */
	ArrayList<TokenTable> TokenList;
	/**
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간. <br>
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;

	ArrayList<SymbolTable> literalList;
	ArrayList<SymbolTable> externalList;

	static int locCounter;
	static int programNumber;

	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile
	 *            : instruction 명세를 작성한 파일 이름.
	 */
	public Assembler(String instFile)
	{
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		literalList = new ArrayList<SymbolTable>();
		externalList = new ArrayList<SymbolTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/**
	 * 어셐블러의 메인 루틴
	 */
	public static void main(String[] args)
	{
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");

		assembler.pass1();
		assembler.printSymbolTable("symtab_20160286");

		assembler.pass2();
		assembler.printObjectCode("output_20160286");

	}

	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.<br>
	 * 
	 * @param fileName
	 *            : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName)
	{
		// TODO Auto-generated method stub
		try
		{
			File file = new File(fileName);
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
			String output;
			if (file.isFile() && file.canWrite())
			{
				for (int i = 0; i < TokenList.size(); i++)
				{
					for (int j = 0; j < TokenList.get(i).getSize(); j++)
					{
						System.out.println(TokenList.get(i).getObjectCode(j));
					}
				}
				
				for(int i = 0; i < codeList.size(); i++)
				{
					System.out.println(codeList.get(i));
				}
			}
		}
		catch (IOException e)
		{

		}
	}

	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.<br>
	 * 
	 * @param fileName
	 *            : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName)
	{
		// TODO Auto-generated method stub
		try
		{
			File file = new File(fileName);
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
			String output;

			if (file.isFile() && file.canWrite())
			{
				for (int i = 0; i < symtabList.size(); i++)
				{
					for (int j = 0; j < symtabList.get(i).getSize(); j++)
					{
						output = symtabList.get(i).getSymbol(j) + "\t"
								+ Integer.toHexString(symtabList.get(i).getLocation(j)).toUpperCase();

						bufferedWriter.write(output);
						bufferedWriter.newLine();
					}

					bufferedWriter.newLine();
				}
			}

			bufferedWriter.close();
		}
		catch (IOException e)
		{
			System.err.println(e);
		}

	}

	/**
	 * pass1 과정을 수행한다.<br>
	 * 1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성<br>
	 * 2) label을 symbolTable에 정리<br>
	 * <br>
	 * <br>
	 * 주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1()
	{
		// TODO Auto-generated method stub
		int tokenIndex = 0;
		String line, literal;
		Token currentToken;

		for (int i = 0; i < lineList.size(); i++)
		{
			line = lineList.get(i);

			if (line.contains("START"))
			{
				locCounter = 0;
				symtabList.add(new SymbolTable());
				literalList.add(new SymbolTable());
				externalList.add(new SymbolTable());
				TokenList.add(new TokenTable(symtabList.get(programNumber), literalList.get(programNumber), instTable));
			}
			else if (lineList.get(i).contains("CSECT"))
			{
				programNumber++;
				locCounter = 0;
				tokenIndex = 0;
				symtabList.add(new SymbolTable());
				literalList.add(new SymbolTable());
				externalList.add(new SymbolTable());
				TokenList.add(new TokenTable(symtabList.get(programNumber), literalList.get(programNumber), instTable));
			}

			TokenList.get(programNumber).putToken(line);

			currentToken = TokenList.get(programNumber).getToken(tokenIndex);

			if (!currentToken.label.equals("") && !currentToken.label.equals("."))
			{
				if (currentToken.operator.equals("EQU"))
				{
					symtabList.get(programNumber).putSymbol(currentToken.label,
							operateAddress(currentToken.operand[0]));
				}
				else
				{
					symtabList.get(programNumber).putSymbol(currentToken.label, locCounter);
				}

				if (currentToken.operand != null && currentToken.operand[0].contains("="))
				{
					literalList.get(programNumber).putSymbol(currentToken.operand[0], 0);
				}
			}

			if (currentToken.operator != null)
			{
				if (currentToken.operator.equals("LTORG") || currentToken.operator.equals("END"))
				{
					for (int j = 0; j < literalList.get(programNumber).getSize(); j++)
					{
						literal = literalList.get(programNumber).getSymbol(j);
						literalList.get(programNumber).modifySymbol(literal, locCounter);

						if (literal.contains("X"))
						{
							locCounter++;
						}
						else if (literal.contains("C"))
						{
							literal = literal.replaceAll("C|\'", "");
							locCounter += literal.length();
						}
					}
				}
				else if(currentToken.operator.equals("EXTREF"))
				{
					for(int j = 0; j < currentToken.operand.length; j++)
					externalList.get(programNumber).putSymbol(currentToken.operand[j], 0);
				}
			}
			locCounter += currentToken.byteSize;
			tokenIndex++;
		}
	}

	/**
	 * pass2 과정을 수행한다.<br>
	 * 1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2()
	{
		// TODO Auto-generated method stub
		Token currentToken;
		String codeLine = "";
		
		for (int i = 0; i < TokenList.size(); i++)
		{
			for (int j = 0; j < TokenList.get(i).getSize(); j++)
			{
				TokenList.get(i).makeObjectCode(j);
			}
			
			for(int j = 0; j < TokenList.get(i).getSize(); j++)
			{
				currentToken = TokenList.get(i).getToken(j);
				
				if(currentToken.label.equals("."))
				{
					continue;
				}
				else if(currentToken.operator.equals("START") || currentToken.operator.equals("CSECT"))
				{
					int startAddress = TokenList.get(i).getToken(0).location;
					int programSize = 0;
					for(int  k = 0; k < TokenList.get(i).getSize(); k++)
						programSize += TokenList.get(i).getToken(k).byteSize;
					
					for(int k = 0; k < literalList.get(i).getSize(); k++)
						programSize += literalList.get(i).getLiteralSize(k);
					
					codeLine = "H" + currentToken.label + String.format("%06X%06X", startAddress, programSize-startAddress);   /////3번째 프로그램 크기가 문제 (리터럴도 추가해줘야함)
				}
				else if(currentToken.operator.equals("EXTDEF"))
				{
					codeLine = "D";
					for(int k = 0; k < currentToken.operand.length; k++)
						codeLine += currentToken.operand[k];
				}
				else if(currentToken.operator.equals("EXTREF"))
				{
					codeLine = "R";
					for(int k = 0; k < currentToken.operand.length; k++)
						codeLine += currentToken.operand[k];
				}
				else
				{
					continue;
				}
				
				codeList.add(codeLine);
			}
		}
	}

	/**
	 * inputFile을 읽어들여서 lineList에 저장한다.<br>
	 * 
	 * @param inputFile
	 *            : input 파일 이름.
	 */
	private void loadInputFile(String inputFile)
	{
		// TODO Auto-generated method stub
		try
		{
			File file = new File(inputFile);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(fileReader);
			String line = "";

			while ((line = bufReader.readLine()) != null)
			{
				lineList.add(line);
			}
			bufReader.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println("파일을 열 수 없습니다.");
		}
		catch (IOException e)
		{
			System.out.println(e);
		}
	}

	private int operateAddress(String inputOperand)
	{
		int result = 0;
		String operands[];

		if (inputOperand.equals("*"))
		{
			result = locCounter;
		}
		else
		{
			if (inputOperand.contains("-"))
			{
				operands = inputOperand.split("-");
				result = symtabList.get(programNumber).search(operands[0])
						- symtabList.get(programNumber).search(operands[1]);
			}
		}
		return result;
	}
}
