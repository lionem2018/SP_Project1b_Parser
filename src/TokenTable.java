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
	SymbolTable symTab;
	SymbolTable litTab;
	InstTable instTab;

	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;
	
	int programCounter;

	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * 
	 * @param symTab
	 *            : 해당 section과 연결되어있는 symbol table
	 * @param instTab
	 *            : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab, SymbolTable litTab, InstTable instTab)
	{
		// ...
		tokenList = new ArrayList<>();
		this.symTab = symTab;
		this.litTab = litTab;
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
	 * Pass2 과정에서 사용한다. instruction table, symbol table 등을 참조하여 objectcode를 생성하고, 이를
	 * 저장한다.
	 * 
	 * @param index
	 */
	public void makeObjectCode(int index)
	{
		// ...
		programCounter += tokenList.get(index).byteSize;
		Token currentToken = tokenList.get(index);
		String operator = currentToken.operator;
		int targetAddress = 0;
		String operandData, addressData;
		
		if(operator == null)
			return;
		
		if (operator.contains("+"))
			operator = operator.replaceAll("[+]", "");

		if (instTab.isInstruction(operator))
		{
			int opcode = instTab.getOpcode(operator);

			if (instTab.getformat(operator) == 3)
			{
				opcode += currentToken.getFlag(nFlag) / iFlag;
				opcode += currentToken.getFlag(iFlag) / iFlag;

				int xbpe = 0;
				xbpe += currentToken.getFlag(xFlag);
				xbpe += currentToken.getFlag(bFlag);
				xbpe += currentToken.getFlag(pFlag);
				xbpe += currentToken.getFlag(eFlag);

				if (instTab.getNumberOfOperand(operator) >= 1)
				{

					if (currentToken.getFlag(nFlag) == nFlag)
					{
						operandData = currentToken.operand[0];

						if (operandData.contains("@"))
							operandData = operandData.replaceAll("@", "");
						
						if(operandData.contains("="))
						{
							operandData = operandData.replaceAll("=", "");
							targetAddress = litTab.search(operandData);
						}
						else
							targetAddress = symTab.search(operandData);
					}
					else if (currentToken.getFlag(iFlag) == iFlag)
					{
						operandData = currentToken.operand[0];

						if (operandData.contains("#"))
							operandData = operandData.replaceAll("#", "");

						targetAddress = Integer.parseInt(operandData);
					}

					if (currentToken.getFlag(pFlag) == pFlag)
					{
						targetAddress -= programCounter;
					}
					else if(currentToken.getFlag(eFlag) == eFlag)
					{
						targetAddress = 0;
					}
				}
				else
					targetAddress = 0;
				
				addressData = addressToString(targetAddress, currentToken.byteSize);

				currentToken.objectCode = String.format("%02X%01X", opcode, xbpe) + addressData;
				// 자바 String Format 사용할 것
			}
			else
			{
				int register1 = 0, register2 = 0;

				if (instTab.getNumberOfOperand(operator) == 1)
				{
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
				}
				else
				{
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

				currentToken.objectCode = String.format("%02X%01X%01X", opcode, register1, register2);
				// currentToken.objectCode = Integer.toHexString(opcode).toUpperCase() +
				// register1 + register2;
			}
		}
		else if (operator.equals("BYTE") || operator.equals("WORD"))
		{
			if(currentToken.operand[0].contains("X"))
			{
				operandData = currentToken.operand[0].replaceAll("X|\'", "");
				currentToken.objectCode = operandData;
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

	public int getSize()
	{
		return tokenList.size();
	}
	
	private String addressToString(int address, int size)
	{
		String addressData;
		
		if(size == 4)
			addressData = String.format("%05X", address);
		else
			addressData = String.format("%03X", address);
		
		if(address < 0)
		{
			if(size == 3)
				addressData = addressData.substring(addressData.length()-3);
			else if(size == 4)
				addressData = addressData.substring(addressData.length()-5);
		}

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

	InstTable instTable;

	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다.
	 * 
	 * @param line
	 *            문장단위로 저장된 프로그램 코드
	 */
	public Token(String line, InstTable instTable)
	{
		// initialize 추가
		this.instTable = instTable;
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
		String units[] = line.split("\t");

		if (units[0].equals("."))
		{
			label = units[0];

			if (units.length > 1)
				comment = units[1];
		}
		else
		{
			label = units[0];
			operator = units[1];

			if (!(instTable.getNumberOfOperand(operator) == 0))
			{
				if (units.length > 2)
					operand = units[2].split(",", TokenTable.MAX_OPERAND);

				if (units.length > 3)
					comment = units[3];
			}
			else
			{
				if (units[2] != null)
					comment = units[2];
			}

			if (operator.contains("+"))
			{
				byteSize = 4;
				setFlag(TokenTable.eFlag, 1);
			}
			else
			{
				byteSize = getInstSize(operator);

				if (operand != null && byteSize > 0)
					setFlag(TokenTable.pFlag, 1);
			}

			if (byteSize >= 3)
			{
				if (operand != null)
				{
					if (operand.length > 1 && operand[1].equals("X"))
					{
						setFlag(TokenTable.xFlag, 1);
					}

					if (operand[0].contains("#"))
					{
						setFlag(TokenTable.iFlag, 1);
						setFlag(TokenTable.pFlag, 0);
					}
					else if (operand[0].contains("@"))
					{
						setFlag(TokenTable.nFlag, 1);
					}
					else
					{
						setFlag(TokenTable.nFlag, 1);
						setFlag(TokenTable.iFlag, 1);
					}

				}
				else
				{
					setFlag(TokenTable.nFlag, 1);
					setFlag(TokenTable.iFlag, 1);
				}
			}
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
		// ...
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

	public int getInstSize(String operator)
	{
		int size = 0;

		if (instTable.isInstruction(operator))
		{
			size = instTable.getformat(operator);
		}
		else if (operator.equals("RESB"))
		{
			size = Integer.parseInt(operand[0]);
		}
		else if (operator.equals("RESW"))
		{
			size = Integer.parseInt(operand[0]) * 3;
		}
		else if (operator.equals("BYTE"))
		{
			size = 1;
		}
		else if (operator.equals("WORD"))
		{
			size = 3;
		}
		else 
			size = 0;

		return size;
	}
}
