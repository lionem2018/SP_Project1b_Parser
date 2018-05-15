import java.util.ArrayList;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로
 * 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable
{
	public static final int MAX_OPERAND = 3;

	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab; // symbol table
	SymbolTable litTab;  // literal table
	SymbolTable extTab;  // external (reference) table
	InstTable instTab;  // instruction table

	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;
	
	// Program Counter 레지스터값을 저장하는 변수
	int programCounter;

	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * 
	 * @param symTab
	 *            : 해당 section과 연결되어있는 symbol table
	 * @param instTab
	 *            : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab, SymbolTable litTab, SymbolTable extTab, InstTable instTab)
	{
		//  ArrayList 객체 할당, 필요한 Table을 링크, 및 PC 값을 0으로 초기화
		tokenList = new ArrayList<>();
		this.symTab = symTab;
		this.litTab = litTab;
		this.extTab = extTab;
		this.instTab = instTab;
		programCounter = 0;
	}

	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * 
	 * @param line
	 *            : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line)
	{
		tokenList.add(new Token(line, instTab));
	}

	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * 
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index)
	{
		return tokenList.get(index);
	}

	/**
	 * Pass2 과정에서 사용한다. instruction table, symbol table 등을 참조하여 object code를 생성하고, 이를
	 * 저장한다.
	 * 
	 * @param index
	 */
	public void makeObjectCode(int index)
	{
		// 해당 index의 프로그램 소스 크기를 더해 PC 레지스터 값 구함
		programCounter += tokenList.get(index).byteSize;
		
		// currentToken: 해당 index의 token 저장
		// operator: 해당 index의 token operator 저장
		// targetAddress: 해당 소스코드의 target address
		// operandData: 해당 소스코드의 operand 저장
		// addressData: object code에 담을 주소값을 String으로 저장
		Token currentToken = tokenList.get(index);
		String operator = currentToken.operator;
		int targetAddress = 0;
		String operandData, addressData;

		// operator가 없다면 object code를 구할 필요가 없으므로 메소드 종료
		if (operator == null)
			return;

		// operator에 '+'가 표시되어 있다면 지움
		// instTable 상에서 검색을 편하게 하기 위함
		if (operator.contains("+"))
			operator = operator.replaceAll("[+]", "");

		// operator가 명령어인 경우
		if (instTab.isInstruction(operator))
		{
			// opcode: object code 상의 상위 첫번째 바이트 표현
			// 명령어 기계어 코드를 구함
			int opcode = instTab.getOpcode(operator);

			// 3또는 4형식 명령어인 경우
			if (instTab.getFormat(operator) == 3)
			{
				// 현재 token의 nFlag와 iFlag 정보를 opcode에 표시
				opcode += currentToken.getFlag(nFlag) / iFlag;
				opcode += currentToken.getFlag(iFlag) / iFlag;
				
				// xbpe: object code 상의 상위 두번째 바이트 표현
				// 현재 token의 xFlag, bFlag, pFlag, eFlag 정보를 xbpe에 표시
				int xbpe = 0;
				xbpe += currentToken.getFlag(xFlag);
				xbpe += currentToken.getFlag(bFlag);
				xbpe += currentToken.getFlag(pFlag);
				xbpe += currentToken.getFlag(eFlag);
				
				// 명령어의 피연산자 개수가 1개 이상인 경우
				if (instTab.getNumberOfOperand(operator) >= 1)
				{
					// 현재 token이 indirect addressing이나 simple addressing인 경우
					// 두 경우 모두 nFlag가 설정되어 있는 상태, operand에 들어온 심볼로 접근
					if (currentToken.getFlag(nFlag) == nFlag)
					{
						// 현재 token의 operand를 구함
						operandData = currentToken.operand[0];

						// operand에 '@'가 표시되어 있다면 제거
						if (operandData.contains("@"))
							operandData = operandData.replaceAll("@", "");

						// operand에 '=' 표시가 있다면 제거
						// '=' 표시가 있다면 리터럴이라는 뜻이므로
						// literal table에서 리터럴 주소를 찾아 타겟주소로 저장
						if (operandData.contains("="))
						{
							operandData = operandData.replaceAll("=", "");
							targetAddress = litTab.search(operandData);
						}
						// '='표시가 없다면 심볼이라는 뜻이므로
						// symbol table에서 심볼 주소를 찾아 타겟주소로 저장
						else  
							targetAddress = symTab.search(operandData);
					}
					// 현재 token이 immediate addressing인 경우
					// iFlag가 설정되어있는 상태, operand에 들어온 값으로 접근
					else if (currentToken.getFlag(iFlag) == iFlag)
					{
						// 현재 token의 operand를 구함
						operandData = currentToken.operand[0];

						// operand에 '#'이 표시되어 있으므로 제거
						if (operandData.contains("#"))
							operandData = operandData.replaceAll("#", "");

						// operand에 '#'이 제거된 순수 상수부분을 타겟주소로 저장
						targetAddress = Integer.parseInt(operandData);
					}
					
					// 현재 token이 PC relative인 경우
					// pFlag가 설정되어있는 상태
					if (currentToken.getFlag(pFlag) == pFlag)
					{
						// 타겟 주소에서 PC 값을 뺌
						targetAddress -= programCounter;
					}
					// 현재 token이 4형식인 경우
					// eFlag가 설정되어있는 상태
					else if (currentToken.getFlag(eFlag) == eFlag)
					{
						// 타겟 주소를 0으로 지정
						targetAddress = 0;
					}
				}
				// 이외의 경우는 타겟 주소를 0으로 지정
				else
					targetAddress = 0;
				
				// 타겟 주소를 해당 token의 바이트 사이즈에 따라 주소를 String으로 변환함
				addressData = addressToString(targetAddress, currentToken.byteSize);
				
				// 이제까지 구한 opcode, xbpe, address 정보를 조합하여 현재 token의 object code로 저장
				currentToken.objectCode = String.format("%02X%01X", opcode, xbpe) + addressData;
			}
			// 2형식 명령어인 경우
			else if (instTab.getFormat(operator) == 2)
			{
				// register1, register2: 레지스터 번호를 저장
				int register1 = 0, register2 = 0;

				// operand의 개수가 1개인 경우 
				if (instTab.getNumberOfOperand(operator) == 1)
				{
					// operand의 레지스터 정보에 따라 레지스터 번호 저장
					if (currentToken.operand[0].equals("A"))
						register1 = 0;
					else if (currentToken.operand[0].equals("X"))
						register1 = 1;
					else if (currentToken.operand[0].equals("L"))
						register1 = 2;
					else if (currentToken.operand[0].equals("B"))
						register1 = 3;
					else if (currentToken.operand[0].equals("S"))
						register1 = 4;
					else if (currentToken.operand[0].equals("T"))
						register1 = 5;
					
					// 두번째 레지스터 번호는 0으로 지정
					register2 = 0;
				}
				// operand의 개수가 2개인 경우
				else if (instTab.getNumberOfOperand(operator) == 2)
				{
					// 첫번째 operand의 레지스터 정보에 따라 첫번재 레지스터 번호 저장
					if (currentToken.operand[0].equals("A"))
						register1 = 0;
					else if (currentToken.operand[0].equals("X"))
						register1 = 1;
					else if (currentToken.operand[0].equals("L"))
						register1 = 2;
					else if (currentToken.operand[0].equals("B"))
						register1 = 3;
					else if (currentToken.operand[0].equals("S"))
						register1 = 4;
					else if (currentToken.operand[0].equals("T"))
						register1 = 5;

					// 두번째 operand의 레지스터 정보에 따라 두번재 레지스터 번호 저장
					if (currentToken.operand[1].equals("A"))
						register2 = 0;
					else if (currentToken.operand[1].equals("X"))
						register2 = 1;
					else if (currentToken.operand[1].equals("L"))
						register2 = 2;
					else if (currentToken.operand[1].equals("B"))
						register2 = 3;
					else if (currentToken.operand[1].equals("S"))
						register2 = 4;
					else if (currentToken.operand[1].equals("T"))
						register2 = 5;
				}
				
				// 이제까지 구한 opcode, 두 register 정보를 조합하여 현재 token의 object code로 저장 
				currentToken.objectCode = String.format("%02X%01X%01X", opcode, register1, register2);
			}
		}
		// operand가 "BYTE"나 "WORD" 지시어인 경우
		else if (operator.equals("BYTE") || operator.equals("WORD"))
		{
			// "BYTE" 지시어인 경우
			if (operator.equals("BYTE"))
			{
				// operand의 첫번째 글자가 'X'인 경우 16진수 값이라는 뜻이므로
				if (currentToken.operand[0].charAt(0) == 'X')
				{
					// 리터럴의 데이터 부분만을 가져와 저장하여 object code로 저장
					operandData = currentToken.operand[0].replaceAll("X|\'", "");
					currentToken.objectCode = operandData;
				}
			}
			// "WORD" 지시어인 경우
			else if (operator.equals("WORD"))
			{
				// 참조하는 심볼이 operand에 들어있는지 검사
				int i;
				for (i = 0; i < extTab.getSize(); i++)
				{
					if (currentToken.operand[0].contains(extTab.getSymbol(i)))
						break;
				}

				// operand에 참조하는 심볼이 쓰인 경우 object code로 0을 자리수에 맞게 저장
				if (i < extTab.getSize())
					currentToken.objectCode = String.format("%06X", 0);
			}
		}
	}

	/**
	 * index번호에 해당하는 object code를 리턴한다.
	 * 
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index)
	{
		return tokenList.get(index).objectCode;
	}

	/**
	 * token table의 크기를 리턴한다.
	 * 
	 * @return: token table의 사이즈
	 */
	public int getSize()
	{
		return tokenList.size();
	}

	/**
	 * int형으로 들어온 주소값을 필요한 자리수만큼 String형으로 변환한다.
	 * 
	 * @param address: 변환할 주소값
	 * @param size: 해당 주소값이 쓰이는 소스 코드의 byte size
	 * @return String 형으로 변환된 주소값
	 */
	private String addressToString(int address, int size)
	{
		String addressData = "";

		// 소스코드가 4형식 명령어인 경우 주소값으로 5자리 출력
		if (size == 4)
			addressData = String.format("%05X", address);
		// 3형식 명령어인 경우 주소값으로 3자리 출력
		else
			addressData = String.format("%03X", address);

		// address 값이 음수인 경우 출력할 자리수 지정이 불가능하기 때문에
		// String 형으로 변환된 주소값에서 끝에서부터 필요한 만큼 따로 가져옴
		if (address < 0)
		{
			if (size == 3)
				addressData = addressData.substring(addressData.length() - 3);
			else if (size == 4)
				addressData = addressData.substring(addressData.length() - 5);
		}
		
		// String 형으로 변환된 주소값 리턴
		return addressData;
	}
}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후 의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 의미 해석이 끝나면 pass2에서
 * object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token
{
	// 의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들
	String objectCode;
	int byteSize;

	// operator가 명령어인지, 오퍼랜드 개수가 몇 개인지
	// 확인하기 위한 명령어 테이블
	InstTable instTable;

	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다.
	 * 
	 * @param line
	 *            문장단위로 저장된 프로그램 코드
	 */
	public Token(String line, InstTable instTable)
	{
		// token parsing을 위해 instruction table을 링크
		this.instTable = instTable;
		// 인자로 들어온 line을 파싱
		parsing(line);
	}

	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * 
	 * @param line
	 *            문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line)
	{
		// 인자로 들어온 소스코드 한 라인을 탭 단위로 분리시킴
		// split 메소드는 나누는 기준이 되는 String 값 사이에 아무 값이 없을 경우 null을 반환함
		String units[] = line.split("\t");

		// 분리된 유닛의 첫번째가 "."인 경우(주석)
		if (units[0].equals("."))
		{
			// "." 을 label에 저장
			label = units[0];

			// "." 외 코멘트가 존재하는 경우 코멘트에 저장
			if (units.length > 1)
				comment = units[1];
		}
		// "."이 아닌 소스코드들인 경우
		else
		{
			// label과 operator 값을 넣어줌
			// label이 존재하지 않는 경우에는 null이 들어가게 됨
			label = units[0];
			operator = units[1];

			// 해당 line에 들어가있는 연산자가 명령어라면 피연산자 개수가 0이 아닌 경우에만 operand를 넣어줌
			// RSUB와 같이, 명령어이지만 피연산자 개수가 0개일 때
			// 코멘트가 operand로 잘못 들어가는 일을 방지하기 위함
			if (!(instTable.getNumberOfOperand(operator) == 0))
			{
				// 피연산자가 존재할 때 ","를 기준으로 나누어 operand에 저장
				if (units.length > 2)
					operand = units[2].split(",", TokenTable.MAX_OPERAND);

				// 코멘트가 존재할 경우 comment에 저장
				if (units.length > 3)
					comment = units[3];
			}
			// 피연산자가 없는 명령어를 가진 소스코드가 코멘트가 존재하면 comment에 넣어줌
			else
			{
				if (units[2] != null)
					comment = units[2];
			}

			// operator에 "+"가 표시되어있는 경우 4형식
			// 소스코드 크기로 4 지정
			// eFlag 설정
			if (operator.contains("+"))
			{
				byteSize = 4;
				setFlag(TokenTable.eFlag, 1);
			}
			// 이외의 경우 해당 명령어의 형식을 소스코드 크기로 지정
			// 명령어가 아닌 경우 0이 지정
			// 피연산자가 존재하면서도 크기가 0보다 크면(연산자가 명령어인 경우)
			// PC relative를 위해 pFalg 설정
			else
			{
				byteSize = getInstSize(operator);

				if (operand != null && byteSize > 0)
					setFlag(TokenTable.pFlag, 1);
			}
			
			// 소스 코드의 크기가 3이상인 경우,
			// 해당 소스 코드의 연산자가 3또는 4형식을 지원하는 명령어이므로
			// 상황에 따라 Flag들을 지정
			if (byteSize >= 3)
			{
				// 피연산자가 있는 경우
				if (operand != null)
				{
					// 피연산자가 1개 이상인데, 두번재 피연산자가 'X'인 경우
					// looping을 위해 indexed addressing을 사용하므로 xFlag 표시
					if (operand.length > 1 && operand[1].equals("X"))
					{
						setFlag(TokenTable.xFlag, 1);
					}

					// 피연산자에 "#"이 표시된 경우
					// immediate addressing을 사용하므로 iFlag 표시 및 pFlag 표시 삭제
					if (operand[0].contains("#"))
					{
						setFlag(TokenTable.iFlag, 1);
						setFlag(TokenTable.pFlag, 0);
					}
					// 피연산자에 "@"이 표시된 경우
					// indirect addressing을 사용하므로 nFlag 표시
					else if (operand[0].contains("@"))
					{
						setFlag(TokenTable.nFlag, 1);
					}
					// 그 이외의 경우는 simple addressing이므로 nFlag와 iFlag 표시
					else
					{
						setFlag(TokenTable.nFlag, 1);
						setFlag(TokenTable.iFlag, 1);
					}
				}
			}
			// 소스 코드 주소값으로 현재 token의 주소값을 저장하고 있던
			// Assembler class 내의 locCounter 값을 저장
			location = Assembler.locCounter;
		}
	}

	/**
	 * n,i,x,b,p,e flag를 설정한다. <br>
	 * <br>
	 * 
	 * 사용 예 : setFlag(nFlag, 1); <br>
	 * 또는 setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag
	 *            : 원하는 비트 위치
	 * @param value
	 *            : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value)
	{
		if (value == 1)
			nixbpe |= flag;
		else
			nixbpe ^= flag;
	}

	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 <br>
	 * <br>
	 * 
	 * 사용 예 : getFlag(nFlag) <br>
	 * 또는 getFlag(nFlag|iFlag)
	 * 
	 * @param flags
	 *            : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags)
	{
		return nixbpe & flags;
	}

	/**
	 * 해당 operator의 바이트 크기를 구하기 위한 메소드이다.
	 * 
	 * @param operator: 크기를 구할 operator
	 * @return: operator의 바이트 크기
	 */
	public int getInstSize(String operator)
	{
		int size = 0;

		// 명령어인 경우
		if (instTable.isInstruction(operator))
		{
			// instruction table 상의 형식을 크기값으로서 지정
			size = instTable.getFormat(operator);
		}
		// "RESB" 지시어인 경우
		else if (operator.equals("RESB"))
		{
			// operand의 지정된 크기만큼을 크기값으로 지정
			size = Integer.parseInt(operand[0]);
		}
		// "RESW" 지시어인 경우
		else if (operator.equals("RESW"))
		{
			// operand의 지정된 크기에 3을 곱한 값을 크기값으로 지정
			size = Integer.parseInt(operand[0]) * 3;
		}
		// "BYTE" 지시어인 경우
		else if (operator.equals("BYTE"))
		{
			// BYTE는 크기가 1이므로 1을 크기값으로 지정 
			size = 1;
		}
		// "WORD" 지시어인 경우
		else if (operator.equals("WORD"))
		{
			// WORD는 크기가 3이므로 3을 크기값으로 지정
			size = 3;
		}
		// 이외의 연산자는 메모리를 차지하지 않으므로 0으로 지정
		else
			size = 0;

		return size;
	}
}
