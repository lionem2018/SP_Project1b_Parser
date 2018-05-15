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

	// 프로그램의 section별로 literal table을 저장하는 공간
	ArrayList<SymbolTable> literalList;
	// 프로그램의 section별로 참조하는(reference) 심볼 table 저장하는 공간
	ArrayList<SymbolTable> externalList;
	// 프로그램의 section별로 modification record를 작성하기 위한 정보 table 저장하는 공간
	ArrayList<SymbolTable> modifList;

	static int locCounter; // location counter
	static int programNumber;  // section program 번호 저장하는 변수

	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile
	 *            : instruction 명세를 작성한 파일 이름.
	 */
	public Assembler(String instFile)
	{
		// 필요한 공간 할당
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		literalList = new ArrayList<SymbolTable>();
		externalList = new ArrayList<SymbolTable>();
		modifList = new ArrayList<SymbolTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/**
	 * 어셈블러의 메인 루틴
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
			// 인자로 들어온 이름의 파일을 열어
			// 생성한 오브젝트 코드들을 쓰기 위해 BufferedWriter를 생성
			File file = new File(fileName);
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
			if (file.isFile() && file.canWrite())
			{
				// 코드 리스트에 들어가있는 코드 수 만큼 파일 출력
				for (int i = 0; i < codeList.size(); i++)
				{
					bufferedWriter.write(codeList.get(i));
					bufferedWriter.newLine();
					
					// 만약 end record로써 첫번째 글자가 'E'라면
					// 새 프로그램 시작으로 개행
					if(codeList.get(i).charAt(0) == 'E')
					{
						bufferedWriter.newLine();
					}
				}
			}
			bufferedWriter.close();
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
			// 인자로 들어온 이름의 파일을 열어
			// 심볼 테이블을 쓰기 위해 BufferedWriter를 생성
			File file = new File(fileName);
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
			// output: 출력할 심볼 정보를 담은 문자열
			String output;

			if (file.isFile() && file.canWrite())
			{
				for (int i = 0; i < symtabList.size(); i++)
				{
					for (int j = 0; j < symtabList.get(i).getSize(); j++)
					{
						// output = <Symbol>	<location> 형태로 저장
						// location 값은 16진수 형의 대문자로 출력
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
		// tokenIndex: 섹션별 토큰 테이블 상의 최근 생성된 토큰의 인덱스
		// line: 해당 토큰 파싱할 소스 코드 라인
		// literal: 오퍼랜드에 들어있는 리터럴
		// currentToken: 최근 생성한 토큰
		int tokenIndex = 0;
		String line, literal;
		Token currentToken;

		// input파일을 통해 읽어들인 소스코드의 라인 수만큼 반복
		for (int i = 0; i < lineList.size(); i++)
		{
			// lineList로 부터 토큰 파싱할 라인을 가져옴
			line = lineList.get(i);

			// 라인에 "START" 문자열이 포함된 경우
			if (line.contains("START"))
			{
				// 현재 소스코드의 주소값을 저장하는 locCounter 초기화
				// 섹션별 토큰 테이블 상의 최근 생성된 토큰의 인덱스 초기화
				// 필요한 테이블 객체들을 생성하여 각 List에 넣어줌
				locCounter = 0;
				tokenIndex = 0;
				symtabList.add(new SymbolTable());
				literalList.add(new SymbolTable());
				externalList.add(new SymbolTable());
				modifList.add(new SymbolTable());
				TokenList.add(new TokenTable(symtabList.get(programNumber), literalList.get(programNumber), externalList.get(programNumber), instTable));
			}
			else if (lineList.get(i).contains("CSECT"))
			{
				// 섹션 구별을 위한 programNumber 값 증가
				// 현재 소스코드의 주소값을 저장하는 locCounter 초기화
				// 섹션별 토큰 테이블 상의 최근 생성된 토큰의 인덱스 초기화
				// 필요한 테이블 객체들을 생성하여 각 List에 넣어줌
				programNumber++;
				locCounter = 0;
				tokenIndex = 0;
				symtabList.add(new SymbolTable());
				literalList.add(new SymbolTable());
				externalList.add(new SymbolTable());
				modifList.add(new SymbolTable());
				TokenList.add(new TokenTable(symtabList.get(programNumber), literalList.get(programNumber), externalList.get(programNumber), instTable));
			}
			
			// 해당 소스 코드 라인을 token으로 추가
			TokenList.get(programNumber).putToken(line);
			
			// 위에서 생성한 토큰을 가져와 저장
			currentToken = TokenList.get(programNumber).getToken(tokenIndex);

			// 레이블이 존재하는데, '.'이 아닌 경우 해당 레이블을 심볼테이블에 넣어줌
			if (!currentToken.label.equals("") && !currentToken.label.equals("."))
			{
				// 연산자가 EQU 지시어인 경우
				// 레이블을 심볼로, 피연산자를 계산하여 심볼 주소로 넣어줌
				if (currentToken.operator.equals("EQU"))
				{
					symtabList.get(programNumber).putSymbol(currentToken.label, operateAddress(currentToken.operand[0]));
				}
				// 이외의 경우
				// 레이블을 심볼로, locCounter 값을 심볼의 주소로 넣어줌
				else
				{
					symtabList.get(programNumber).putSymbol(currentToken.label, locCounter);
				}

				// 피연산자에 "=" 표시가 있는 경우 (리터럴인 경우)
				// 해당 피연산자를 리터럴로, 리터럴 주소로 0을 넣어줌
				if (currentToken.operand != null && currentToken.operand[0].contains("="))
				{
					literalList.get(programNumber).putSymbol(currentToken.operand[0], 0);
				}
			}
			
			// 연산자가 존재하는 경우
			if (currentToken.operator != null)
			{
				// 해당 연산자가 "LTORG"나 "END" 지시어인 경우
				if (currentToken.operator.equals("LTORG") || currentToken.operator.equals("END"))
				{
					// 해당 프로그램에서 생성되었던 리터럴의 주소값을 locCouner 값으로 수정함
					for (int j = 0; j < literalList.get(programNumber).getSize(); j++)
					{
						literal = literalList.get(programNumber).getSymbol(j);
						literalList.get(programNumber).modifySymbol(literal, locCounter);

						// 리터럴에 따라 locCounter 값을 증가시킴
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
				// 연산자가 "EXTREF" 지시어인 경우
				else if (currentToken.operator.equals("EXTREF"))
				{
					// 피연산자로 들어온 심볼 개수 만큼 external 테이블에 넣어줌
					for (int j = 0; j < currentToken.operand.length; j++)
						externalList.get(programNumber).putSymbol(currentToken.operand[j], 0);
				}
				// 그 외의 경우 피연산자가 존재한다면
				else if(currentToken.operand != null)
				{
					// external 테이블에 들어있는 개수 만큼 modification 정보 테이블 작성
					for(int j = 0; j < externalList.get(programNumber).getSize(); j++)
					{
						// 피연산자에 심볼이 extref 참조 선언한 심볼이 있는 경우
						if(currentToken.operand[0].contains(externalList.get(programNumber).getSymbol(j)))
						{
							// 기본으로 수정 사이즈를 6으로 지정
							int modifSize = 6;
							
							// 4형식에서 피연산자로 사용한 경우 수정 사이즈는 5
							if(currentToken.operator.contains("+"))
							{
								modifSize = 5;
							}
							
							// 오퍼랜드에 (-)연산이 포함된 경우
							// 심볼 연산에 따라 차례로 modif 테이블에 추가
							// 심볼, 수정할 주소값, 수정할 사이즈
							// +심볼1 / -심볼2
							if(currentToken.operand[0].contains("-"))
							{
								String opSymbols[] = currentToken.operand[0].split("-");
								modifList.get(programNumber).putModifSymbol("+"+opSymbols[0], locCounter + (6-modifSize), modifSize);
								modifList.get(programNumber).putModifSymbol("-"+opSymbols[1], locCounter + (6-modifSize), modifSize);
							}
							// 이외의 경우 +심볼 형태로만 추가
							else
								modifList.get(programNumber).putModifSymbol("+"+currentToken.operand[0], locCounter + (6-modifSize), modifSize);
							break;
						}
					}
				}
			}
			// 현재 토큰의 크기만큼 locCounter 값 증가
			locCounter += currentToken.byteSize;
			//토큰 인덱스 값 증가
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
		// currentToken: 오브젝트 코드를 생성할 토큰
		// codeLine: 오브젝트 프로그램으로 출력할 한 코드 라인
		// tokenIndex: 한 코드라인에 출력할 오브젝트 코드(토큰) 개수
		// lineSize: 한 라인에 쓰여질 코드의 바이트 수
		Token currentToken;
		String codeLine = "";
		int tokenIndex = 0, lineSize = 0;

		// 섹션 별로 토큰들의 오브젝트 코드 생성 후, 오브젝트 프로그램 코드 라인 작성
		for (int i = 0; i < TokenList.size(); i++)
		{
			// 해당 섹션의 토큰들의 오브젝트 코드 생성
			for (int j = 0; j < TokenList.get(i).getSize(); j++)
			{
				TokenList.get(i).makeObjectCode(j);
			}

			// 해당 섹션의 토큰 개수 만큼 실행
			for (int j = 0; j < TokenList.get(i).getSize(); j++)
			{
				// 토큰을 하나 가져옴
				currentToken = TokenList.get(i).getToken(j);

				// 해당 토큰의 레이블이 "."인 경우,
				// 실제 소스 코드가 아닌 주석이므로 생략
				if (currentToken.label.equals("."))
				{
					continue;
				}
				// 토큰의 연산자가 "START"지시어나 "CSECT"지시어인 경우
				// Header record 작성
				else if (currentToken.operator.equals("START") || currentToken.operator.equals("CSECT"))
				{
					// 새로운 섹션 프로그램의 시작이므로 토큰 인덱스 0으로 초기화
					tokenIndex = 0;

					// 시작 주소로 해당 섹션 프로그램의 첫번째 토큰에 들어있는 주소값을 가져옴
					int startAddress = TokenList.get(i).getToken(0).location;
					// 해당 섹션 프로그램의 토큰들의 바이트 사이즈와 리터럴들의 크기를 모두 더해
					// 섹션 프로그램의 크기를 구함
					int programSize = 0;
					for (int k = 0; k < TokenList.get(i).getSize(); k++)
						programSize += TokenList.get(i).getToken(k).byteSize;

					for (int k = 0; k < literalList.get(i).getSize(); k++)
						programSize += literalList.get(i).getLiteralSize(k);

					// 섹션 프로그램 이름, 시작 주소, 프로그램 크기를 포함한 Header record 작성
					codeLine = "H" + currentToken.label + " "
							+ String.format("%06X%06X", startAddress, programSize - startAddress);
				}
				// 토큰의 연산자가 "EXTDEF" 지시어인 경우
				// Define record 작성
				else if (currentToken.operator.equals("EXTDEF"))
				{
					// 해당 토큰의 피연산자로 들어있는 정의한 심볼들을 가지고 Define record 작성
					codeLine = "D";
					for (int k = 0; k < currentToken.operand.length; k++)
						codeLine += currentToken.operand[k]
								+ String.format("%06X", symtabList.get(i).search(currentToken.operand[k]));
				}
				// 토큰의 연산자가 "EXTREF" 지시어인 경우
				// Refer record 작성
				else if (currentToken.operator.equals("EXTREF"))
				{
					// 해당 토큰의 피연산자로 들어있는 참조 심볼들을 가지고 Refer record 작성
					codeLine = "R";
					for (int k = 0; k < currentToken.operand.length; k++)
						codeLine += currentToken.operand[k];
				}
				// 토큰의 연산자가 명령어인 경우
				// Text record 작성
				else if (instTable.isInstruction(currentToken.operator))
				{
					lineSize = 0;
					tokenIndex = j;
					// 한 코드 라인으로 몇 개의 토큰을 작성할 것인지 셈
					while (tokenIndex < TokenList.get(i).getSize())
					{
						if (TokenList.get(i).getToken(tokenIndex).byteSize == 0
								|| TokenList.get(i).getToken(tokenIndex).operator.equals("RESW")
								|| TokenList.get(i).getToken(tokenIndex).operator.equals("RESB")
								|| (lineSize + TokenList.get(i).getToken(tokenIndex).byteSize) > 30)
							break;

						lineSize += TokenList.get(i).getToken(tokenIndex).byteSize;
						tokenIndex++;
					}

					// 현재 토큰 주소값, 한 라인에 쓰일 토큰 바이트 크기의 합, 토큰의 오브젝트 코드들로
					// Text record 작성
					codeLine = "T" + String.format("%06X%02X", currentToken.location, lineSize);

					for (int k = j; k < tokenIndex; k++, j++)
					{
						codeLine += TokenList.get(i).getToken(k).objectCode;
					}

					j--;
				}
				// 토큰의 연산자가 "BYTE" 지시어나 "WORD" 지시어인 경우
				// 별 다른 동작 없이 넘어감
				else if (currentToken.operator.equals("BYTE") | currentToken.operator.equals("WORD"))
				{
					lineSize = 0;
				}
				// 토큰의 연산자가 "LTORG" 지시어나 "END" 지시어인 경우
				// 해당 섹션 프로그램의 리터럴 값들을 오브젝트 코드로 생성
				else if (currentToken.operator.equals("LTORG") || currentToken.operator.equals("END"))
				{
					lineSize = 0;
					// 출력할 리터럴들의 바이트 크기 합을 구함
					for (int k = 0; k < literalList.get(i).getSize(); k++)
					{
						lineSize += literalList.get(i).getLiteralSize(k);
					}

					// Text record 작성
					codeLine = "T" + String.format("%06X%02X", currentToken.location, lineSize);

					for (int k = 0; k < literalList.get(i).getSize(); k++)
					{
						// 각 리터럴의 데이터 부분만을 가져와 오브젝트 코드 생성함
						String literalData = literalList.get(i).getSymbol(k);
						// "X"의 형태로 표기된 데이터의 경우 그 데이터부분을 그대로 오브젝트 코드로 사용
						if (literalData.contains("X"))
						{
							literalData = literalData.replaceAll("X|\'", "");
						}
						// "C"의 형태로 표기된 데이터의 경우 데이터 부분의 각 자리를 아스키코드값으로 변환하여
						// 오브젝트 코드로 사용
						else if (literalData.contains("C"))
						{
							String temp = "";
							literalData = literalData.replaceAll("C|\'", "");

							for (int l = 0; l < literalList.get(i).getLiteralSize(k); l++)
								temp += String.format("%02X", (int) literalData.charAt(l));
							literalData = temp;
						}
						codeLine += literalData;
					}
				}
				else  // 이외의 경우는 생략함
					continue;

				// 위에서 생성한 코드 라인을 code list에 추가
				codeList.add(codeLine);
			}
			
			// 한 프로그램에 대한 오브젝트 프로그램 작성이 끝나기 전에
			// Modification record 작성
			// modif table에 저장해둔 수정 정보들을 모두 출력
			for(int j = 0; j < modifList.get(i).getSize(); j++)
				codeList.add("M" + String.format("%06X%02X", modifList.get(i).getLocation(j), modifList.get(i).getModifSize(j)) + modifList.get(i).getSymbol(j));

			// 첫번째 프로그램이 끝난 경우에는 End record와 함께 시작주소 표시
			// 이외의 프로그램이 끝난 경우에는 End record만 표시
			if (i == 0)
				codeList.add("E" + String.format("%06X", TokenList.get(i).getToken(0).location));
			else
				codeList.add("E");
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
			// 인자로 들어온 이름의 파일을 열어, input 소스코드를 읽어오기 위해 BufferedReader를 생성
			File file = new File(inputFile);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(fileReader);
			// line: input 파일에서 읽어들인 한 라인
			String line = "";

			// 읽어들인 라인들은 line list에 저장함
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

	/**
	 * 피연산자로 연산이 들어온 경우 주소값을 계산한다
	 * 
	 * @param inputOperand: 연산할 피연산자
	 * @return: 계산된 주소값
	 */
	private int operateAddress(String inputOperand)
	{
		// result: 리턴할 계산된 주소값
		// operands: 피연산자 내에서 연산자를 제외한 계산할 심볼들
		int result = 0;
		String operands[];

		// 피연산자가 "*" 인 경우 현재 locCounter를 주소값으로 반환 
		if (inputOperand.equals("*"))
		{
			result = locCounter;
		}
		// 이외의 경우(연산이 필요한 경우)
		else
		{
			// 포함된 연산이 (-) 인 경우
			if (inputOperand.contains("-"))
			{
				// 연산자를 기준으로 계산할 심볼들을 구하여
				// 해당 심볼들의 주소값을 가지고 (-) 연산 수행
				operands = inputOperand.split("-");
				result = symtabList.get(programNumber).search(operands[0])
						- symtabList.get(programNumber).search(operands[1]);
			}
		}
		
		// 주소값 반환
		return result;
	}
}
