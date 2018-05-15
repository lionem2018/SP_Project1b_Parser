import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;


/**
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다. <br>
 * 또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
 */
public class InstTable {
	/** 
	 * inst.data 파일을 불러와 저장하는 공간.
	 *  명령어의 이름을 집어넣으면 해당하는 Instruction의 정보들을 리턴할 수 있다.
	 */
	HashMap<String, Instruction> instMap;
	
	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * @param instFile : instruction에 대한 명세가 저장된 파일 이름
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}
	
	/**
	 * 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
	 */
	public void openFile(String fileName) {
		try {
			// 인자로 들어온 이름의 파일을 열어, 명령어 정보를 읽어오기 위해 BufferedReader를 생성
			File file = new File(fileName);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(fileReader);
			// line: inst.data 파일에서 읽어들인 한 라인
			// instName: 저장할 instruction
			// tokens: line을 분리하기 위한 StringTokenizer
			String line = "", instName;
			StringTokenizer tokens;
		
			// 파일에서 읽어들일 것이 없을 때까지 한 줄씩 읽어 들임
			while((line = bufReader.readLine()) != null){
				// 읽어 들인 라인을 공백(" ") 기준으로 한 번 분리하여
				// instruction 이름을 얻어 저장
				tokens = new StringTokenizer(line);
				instName = tokens.nextToken(" ");
				// instruction 이름을 key로 하는 해당 라인에 대한 instruction 객체를 HashMap에 저장
				instMap.put(instName, new Instruction(line));
			}
			// 입력버퍼를 닫음
			bufReader.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("파일을 열 수 없습니다.");
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}
	
	//get, set, search 등의 함수는 자유 구현
	
	/**
	 * 인자로 받은 명령어의 opcode를 구함
	 * 
	 * @param instName: opcode를 구할 명령어
	 * @return: 해당 명령어의 opcode 값
	 */
	public int getOpcode(String instName)
	{
		int opcode = 0;
		
		// HashMap에서 인자로 받은 명령어를 key값으로 가지고 있는 경우
		// 해당 명령어를 key로 instruction 객체를 구해 opcode 구함
		if(instMap.containsKey(instName))
			opcode = instMap.get(instName).opcode;
		// 해당 명령어를 key 값으로 가지고 있지 않은 경우
		// 에러의 의미로 -1 지정
		else
			opcode = -1;
		
		// opcode 리턴
		return opcode;
	}

	/**
	 * 인자로 받은 명령어의 피연산자 개수를 구한다.
	 * 
	 * @param instName: 피연산자 개수를 구할 명령어
	 * @return: 해당 명령어의 피연산자 개수
	 */
	public int getNumberOfOperand(String instName)
	{
		int numberOfOperand = 0;
		
		// HashMap에서 인자로 받은 명령어를 key값으로 가지고 있는 경우
		// 해당 명령어를 key로 instruction 객체를 구해 피연산자 개수 구함
		if(instMap.containsKey(instName))
			numberOfOperand = instMap.get(instName).numberOfOperand;
		// 해당 명령어를 key 값으로 가지고 있지 않은 경우
		// 에러의 의미로 -1 지정
		else
			numberOfOperand = -1;
		
		// 오퍼랜드 개수 리턴
		return numberOfOperand;
	}
	
	/**
	 * 인자로 받은 명령어의 형식을 구한다.
	 * 
	 * @param instName: 형식을 구할 명령어
	 * @return: 해당 명령어의 형식
	 */
	public int getformat(String instName)
	{
		int format = 0;
		
		// HashMap에서 인자로 받은 명령어를 key값으로 가지고 있는 경우
		// 해당 명령어를 key로 instruction 객체를 구해 형식 구함
		if(instMap.containsKey(instName))
			format = instMap.get(instName).format;
		// 해당 명령어를 key 값으로 가지고 있지 않은 경우
		// 형식이 없으므로 0 지정
		else
			format = 0;
		
		// 형식 리턴
		return format;
	}
	
	/**
	 * 인자로 받은 operator가 명령어인지 여부를 구한다.
	 * hash map 상에서 key로 존재하는지 구한다.
	 * 
	 * @param name: 확인할 operator 이름
	 * @return: hash map 상에 존재한다면 1, 없다면 0
	 */
	public boolean isInstruction(String name)
	{
		return instMap.containsKey(name);
	}
}
/**
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다.
 * instruction과 관련된 정보들을 저장하고 기초적인 연산을 수행한다.
 */
class Instruction {
	/* 
	 * 각자의 inst.data 파일에 맞게 저장하는 변수를 선언한다.
	 *  
	 * ex)
	 * String instruction;
	 * int opcode;
	 * int numberOfOperand;
	 * String comment;
	 */
	String instruction;
	int opcode;
	int numberOfOperand;
	
	/** instruction이 몇 바이트 명령어인지 저장. 이후 편의성을 위함 */
	int format;
	
	/**
	 * 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public void parsing(String line) {
		// TODO Auto-generated method stub
		StringTokenizer tokens = new StringTokenizer(line);
		instruction = tokens.nextToken(" ");
		format = Integer.parseInt(tokens.nextToken(" "));
		opcode = Integer.parseInt(tokens.nextToken(" "), 16);
		numberOfOperand = Integer.parseInt(tokens.nextToken(" "));
	}
	
		
	//그 외 함수 자유 구현
	
}
