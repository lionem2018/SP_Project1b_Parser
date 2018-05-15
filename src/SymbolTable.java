import java.util.ArrayList;

/**
 * symbol과 관련된 데이터와 연산을 소유한다. section 별로 하나씩 인스턴스를 할당한다.
 */
public class SymbolTable
{
	ArrayList<String> symbolList;  // 심볼을 담기 위한 리스트
	ArrayList<Integer> locationList;  // 해당 심볼의 주소값을 담기 위한 리스트
	ArrayList<Integer> modifSizeList;  // modification table에서 수정할 바이트의 크기를 저장하는 리스트
	// 기타 literal, external 선언 및 처리방법을 구현한다.

	/**
	 * 클래스 내 필드로 존재하는 리스트들 객체 생성한다.
	 */
	public SymbolTable()
	{
		// ArrayList 객체를 생성하여 초기화
		symbolList = new ArrayList<>();
		locationList = new ArrayList<>();
		modifSizeList = new ArrayList<>();
	}

	/**
	 * 새로운 Symbol을 table에 추가한다.
	 * 
	 * @param symbol
	 *            : 새로 추가되는 symbol의 label
	 * @param location
	 *            : 해당 symbol이 가지는 주소값 <br>
	 * 			<br>
	 *            주의 : 만약 중복된 symbol이 putSymbol을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다.
	 *            매칭되는 주소값의 변경은 modifySymbol()을 통해서 이루어져야 한다.
	 */
	public void putSymbol(String symbol, int location)
	{
		// 인자로 들어온 심볼을 저장
		String inputSymbol = symbol;

		// 심볼에 "="이 포함된 경우(리터럴인 경우)
		// "=" 표시를 제거함
		if (inputSymbol.contains("="))
			inputSymbol = inputSymbol.replaceAll("=", "");

		// 이전에 저장한 심볼이 아닌 경우
		if (!symbolList.contains(inputSymbol))
		{
			// 심볼과 인자로 들어온 주소값을 저장함
			symbolList.add(inputSymbol);
			locationList.add(location);
		}
	}
	
	/**
	 * modification table에 modification record에서 쓰일 심볼, 주소, 수정 바이트 크기 정보를 저장한다.
	 * 
	 * @param modifSymbol: 심볼 이름
	 * @param location: 심볼이 쓰인 소스 코드의 주소
	 * @param modifSize: 수정할 바이트 크기
	 */
	public void putModifSymbol(String modifSymbol, int location, int modifSize)
	{
		symbolList.add(modifSymbol);
		locationList.add(location);
		modifSizeList.add(modifSize);
	}

	/**
	 * 기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
	 * 
	 * @param symbol
	 *            : 변경을 원하는 symbol의 label
	 * @param newLocation
	 *            : 새로 바꾸고자 하는 주소값
	 */
	public void modifySymbol(String symbol, int newLocation)
	{
		// 인자로 들어온 심볼을 저장
		String inputSymbol = symbol;

		// 심볼에 "="이 포함된 경우(리터럴인 경우)
		// "=" 표시를 제거함
		if (inputSymbol.contains("="))
			inputSymbol = inputSymbol.replaceAll("=", "");

		// List 상에 이미 저장되어있는 경우에만 수정이 가능
		if (symbolList.contains(inputSymbol))
		{
			// 저장되어있는 심볼의 위치를 찾아 인자로 받은 새로운 주소값을 넣어줌
			for (int index = 0; index < symbolList.size(); index++)
				if (inputSymbol.equals(symbolList.get(index)))
				{
					symbolList.set(index, inputSymbol);
					locationList.set(index, newLocation);
					break;
				}
		}
	}

	/**
	 * 인자로 전달된 symbol이 어떤 주소를 지칭하는지 알려준다.
	 * 
	 * @param symbol
	 *            : 검색을 원하는 symbol의 label
	 * @return symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
	 */
	public int search(String symbol)
	{
		// 출력할 주소값 저장
		int address = 0;
		
		// 인자로 받은 심볼이 List 상에 있는 경우
		// 해당 심볼의 주소값을 찾아 address에 지정
		if (symbolList.contains(symbol))
		{
			for (int index = 0; index < symbolList.size(); index++)
				if (symbol.equals(symbolList.get(index)))
				{
					address = locationList.get(index);
					break;
				}
		}
		// 없는 경우 -1을 address에 지정
		else
			address = -1;

		// address 리턴
		return address;
	}

	/**
	 * 인자로 받은 index의 심볼을 리턴한다.
	 * 
	 * @param index: 가져올 심볼의 위치
	 * @return: 해당 위치의 심볼
	 */
	public String getSymbol(int index)
	{
		return symbolList.get(index);
	}

	/**
	 * 해당 index의 주소값을 리턴한다.
	 * 
	 * @param index: 가져올 주소값의 위치
	 * @return: 해당 위치의 주소값
	 */
	public int getLocation(int index)
	{
		return locationList.get(index);
	}

	/**
	 * symbol table의 크기를 리턴한다.
	 * 
	 * @return: symbol table의 크기
	 */
	public int getSize()
	{
		return symbolList.size();
	}
	
	/**
	 * literal table에서 사용하는 메소드
	 * 
	 * 인자로 들어온 해당 index의 리터럴 데이터 크기를 구한다
	 * 
	 * @param index: 크기를 구할 리터럴의 테이블 상의 위치
	 * @return: 리터럴 데이터 크기
	 */
	public int getLiteralSize(int index)
	{
		int size = 0;
		
		if (symbolList.get(index).contains("X"))
		{
			size = 1;
		}
		else if (symbolList.get(index).contains("C"))
		{
			String literal = symbolList.get(index).replaceAll("C|\'", "");
			size = literal.length();
		}
		
		return size;
	}
	
	/**
	 * modification table 상에 해당 인덱스의 수정할 바이트 사이즈를 리턴한다.
	 * @param index: 수정할 바이트 사이즈를 구할 심볼의 테이블 상의 위치
	 * @return: 수정할 바이트 크기
	 */
	public int getModifSize(int index)
	{
		return modifSizeList.get(index);
	}
}
